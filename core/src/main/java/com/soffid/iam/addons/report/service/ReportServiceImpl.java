package com.soffid.iam.addons.report.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jfree.util.Log;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import com.soffid.iam.addons.acl.api.AccessControlList;
import com.soffid.iam.addons.doc.api.DocumentReference;
import com.soffid.iam.addons.doc.exception.DocumentBeanException;
import com.soffid.iam.addons.doc.service.DocumentService;
import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ExecutedReportCriteria;
import com.soffid.iam.addons.report.api.ParameterType;
import com.soffid.iam.addons.report.api.ParameterValue;
import com.soffid.iam.addons.report.api.Report;
import com.soffid.iam.addons.report.api.ReportParameter;
import com.soffid.iam.addons.report.api.ScheduledReport;
import com.soffid.iam.addons.report.model.ExecutedReportEntity;
import com.soffid.iam.addons.report.model.ExecutedReportEntityDao;
import com.soffid.iam.addons.report.model.ExecutedReportParameterEntity;
import com.soffid.iam.addons.report.model.ExecutedReportTargetEntity;
import com.soffid.iam.addons.report.model.ReportACLEntity;
import com.soffid.iam.addons.report.model.ReportEntity;
import com.soffid.iam.addons.report.model.ReportEntityDao;
import com.soffid.iam.addons.report.model.ScheduledReportEntity;
import com.soffid.iam.addons.report.model.ScheduledReportEntityDao;

import es.caib.bpm.beans.home.DocumentHome;
import es.caib.bpm.beans.remote.Document;
import es.caib.seycon.ng.comu.Configuracio;
import es.caib.seycon.ng.comu.Usuari;
import es.caib.seycon.ng.exception.InternalErrorException;
import es.caib.seycon.ng.servei.ConfiguracioService;
import es.caib.seycon.ng.utils.Security;


public class ReportServiceImpl extends ReportServiceBase implements ApplicationContextAware, InitializingBean {

	private ApplicationContext applicationContext;
	private ExecutorThread executorThread;
	private SchedulerThread schedulerThread;

	@Override
	protected Collection<Report> handleFindReports(String name,
			boolean exactMatch) throws Exception {
		if (exactMatch){
			List<Report> list = new LinkedList<Report>();
			ReportEntity r = getReportEntityDao().findByName(name);
			if (r != null)
				list.add ( getReportEntityDao().toReport(r));
			return list;
		}
		else
		{
			return getReportEntityDao().toReportList(getReportEntityDao().findByNameFilter(name));
		}
	}

	@SuppressWarnings("unchecked")
	private ParameterType guessParameterType (JRParameter jp)
	{
    	if (jp.getValueClass().isAssignableFrom(String.class))
    		return(ParameterType.STRING_PARAM);
    	else if (jp.getValueClass().isAssignableFrom(int.class) ||
    			jp.getValueClass().isAssignableFrom(long.class) ||
    			jp.getValueClass().isAssignableFrom(Integer.class) ||
    			jp.getValueClass().isAssignableFrom(Long.class) )
    	{
    		return (ParameterType.LONG_PARAM);
    	}
    	else if (jp.getValueClass().isAssignableFrom(double.class) ||
    			jp.getValueClass().isAssignableFrom(float.class) ||
    			jp.getValueClass().isAssignableFrom(Double.class) ||
    			jp.getValueClass().isAssignableFrom(Float.class) )
    	{
    		return (ParameterType.DOUBLE_PARAM);
    	}
    	else if (jp.getValueClass().isAssignableFrom(Date.class) ||
    			jp.getValueClass().isAssignableFrom(java.sql.Date.class) ||
    			jp.getValueClass().isAssignableFrom(Date.class) ||
    			jp.getValueClass().isAssignableFrom(java.sql.Date.class) ||
    			jp.getValueClass().isAssignableFrom(Calendar.class) )
    	{
    		return(ParameterType.DATE_PARAM);
    	}
    	else if (jp.getValueClass().isAssignableFrom(boolean.class) ||
    			jp.getValueClass().isAssignableFrom(Boolean.class)  )
    	{
    		return(ParameterType.BOOLEAN_PARAM);
    	} 
    	else
    	{
    		return (ParameterType.STRING_PARAM);
    	}
	}
	
	@Override
	protected Report handleUpload(byte[] data) throws Exception {
        JasperReport jr = (JasperReport) JRLoader.loadObject(new ByteArrayInputStream(data));
        String name = jr.getName();

        Report r;
		ReportEntity re = getReportEntityDao().findByName(name);
		if (re == null)
		{
			r = new Report();
			r.setParameters(new LinkedList<ReportParameter>());
		}
		else
		{
			r = getReportEntityDao().toReport(re);
		}

		Collection<ReportParameter> oldParameters = r.getParameters();
		r.setName(jr.getName());
        List<ReportParameter> rp = new LinkedList<ReportParameter>();
        for (JRParameter jp: jr.getParameters())
        {
        	if ( jp.isForPrompting())
        	{
        		// Search existing parameter
        		boolean found = false;
        		for (ReportParameter existingParameter: oldParameters)
        		{
        			if (existingParameter.getName().equals(jp.getName()))
        			{
        				found = true;
        				existingParameter.setDescription(jp.getDescription());
        				rp.add(existingParameter);
        				ParameterType desired = guessParameterType(jp);
        				ParameterType existing = existingParameter.getType();
        				if (desired == existing ||
        					( desired == ParameterType.LONG_PARAM && 
        						( existing == ParameterType.DISPATCHER_PARAM ||
        						existing == ParameterType.GROUP_PARAM ||
        						existing == ParameterType.ROLE_PARAM ||
        						existing == ParameterType.USER_PARAM
        						
        					)))
        				{
        					// Compatibles types, nothing to do
        				}
        				else
        					existingParameter.setType(desired);
        				break;
        			}
        		}
        		if (! found )
        		{
		        	ReportParameter p = new ReportParameter();
		        	p.setName(jp.getName());
		        	p.setDescription (jp.getDescription());
		        	p.setType(guessParameterType(jp));
		        	rp.add(p);
        		}
	    	}
        }
        
        r.setParameters(rp);
        r.setAcl(new LinkedList<String>());
        r.getAcl().add(Security.getCurrentUser());

        DocumentReference ref = storeDocument (name, data);
        
        re = getReportEntityDao().reportToEntity(r);
        re.setDocId(ref.toString());
        if (r.getId() == null)
        	getReportEntityDao().create(re);
        else
        	getReportEntityDao().update(re);
        
        return r;
	}

	private DocumentReference storeDocument(String name, byte[] data) throws NamingException, RemoteException, CreateException, DocumentBeanException, InternalErrorException {
		DocumentService doc = getDocumentService();
		doc.createDocument("application/x-rpt", name+".rpt", "report");
		doc.openUploadTransfer();
		doc.nextUploadPackage(data, data.length);
		doc.endUploadTransfer();
		return doc.getReference();
	}

	@Override
	protected ExecutedReport handleLaunchReport(ScheduledReport schedule)
			throws Exception {
		ReportEntity report = getReportEntityDao().load (schedule.getId());
		if (canExecute (report))
		{
			es.caib.seycon.ng.model.UsuariEntity myself = getUsuariEntityDao ().findByCodi(Security.getCurrentUser());
					
			ExecutedReportEntity er = getExecutedReportEntityDao().newExecutedReportEntity();
			er.setReport(report);
			er.setDate(new Date());
			er.setDone(false);
			er.setError(false);
			er.setName(schedule.getName());
			ExecutedReportTargetEntity erte = getExecutedReportTargetEntityDao().newExecutedReportTargetEntity();
			erte.setReport(er);
			erte.setUser(myself);
			er.getAcl().add(erte);
			
			for (ParameterValue pv: schedule.getParams())
			{
				ExecutedReportParameterEntity erpe = getExecutedReportParameterEntityDao().parameterValueToEntity(pv);
				erpe.setReport(er);
				er.getParameters().add(erpe);
			}
			
			getExecutedReportEntityDao().create(er);
			
			if (executorThread != null)
				executorThread.newReportCreated();
			
			return getExecutedReportEntityDao().toExecutedReport(er);
		} else
			throw new InternalErrorException ("Not authorized");
	}

	private boolean canExecute(ReportEntity report) throws InternalErrorException {
		if (Security.isUserInRole("report:admin"))
			return true;
		Usuari user = getUsuariService().getCurrentUsuari();
		AccessControlList acl = new AccessControlList();
		
		for (ReportACLEntity ace: report.getAcl())
		{
			if (ace.getUser() != null)
				acl.getUsers().add(ace.getUser().getId());
			if (ace.getRole() != null)
				acl.getRoles().add(ace.getRole().getId());
			if (ace.getGroup() != null)
				acl.getGroups().add(ace.getGroup().getId());
		}
		
		return getACLService().isUserIncluded(user.getId(), acl);
	}

	private boolean canView(ExecutedReportEntity report) throws InternalErrorException {
		if (Security.isUserInRole("report:admin"))
			return true;
		Usuari user = getUsuariService().getCurrentUsuari();
		AccessControlList acl = new AccessControlList();
		
		for (ExecutedReportTargetEntity erte: report.getAcl())
		{
			if (erte.getUser().getId().equals (user.getId()))
				return true;
		}
		
		return false;
	}

	@Override
	protected ExecutedReport handleGetExecutedReportStatus(long reportId)
			throws Exception {
		ExecutedReportEntity ere = getExecutedReportEntityDao().load(reportId);
		if (ere == null)
			return null;
		if ( ! canView(ere))
			return null;
		
		return getExecutedReportEntityDao().toExecutedReport(ere);
	}

	@Override
	protected Collection<ExecutedReport> handleFindExecutedReports(
			ExecutedReportCriteria criteria) throws Exception {
		ExecutedReportEntityDao dao = getExecutedReportEntityDao();
		LinkedList<ExecutedReport> result = new LinkedList<ExecutedReport>();
		for (ExecutedReportEntity ere : dao.findByCriteria(criteria))
		{
			if (canView(ere))
				result.add (dao.toExecutedReport(ere));
		}
		
		return result;
	}

	@Override
	protected void handleRemove(Report report) throws Exception {
		ReportEntity re = getReportEntityDao().load(report.getId());
		getReportEntityDao().remove(re);
	}

	@Override
	protected void handleRemove(ScheduledReport schedule) throws Exception {
		ScheduledReportEntity re = getScheduledReportEntityDao().load(schedule.getId());
		getScheduledReportEntityDao().remove(re);
	}

	@Override
	protected ScheduledReport handleCreate(ScheduledReport schedule)
			throws Exception {
		ScheduledReportEntity sre = getScheduledReportEntityDao().scheduledReportToEntity(schedule);
		getScheduledReportEntityDao().create(sre);
		return getScheduledReportEntityDao().toScheduledReport(sre);
	}

	@Override
	protected void handleUpdate(ScheduledReport schedule) throws Exception {
		ScheduledReportEntity sre = getScheduledReportEntityDao().scheduledReportToEntity(schedule);
		getScheduledReportEntityDao().update(sre);
	}

	@Override
	protected void handleUpdate(Report report) throws Exception {
		ReportEntityDao dao = getReportEntityDao();
		ReportEntity sre = dao.load (report.getId());
		
		sre = dao.reportToEntity(report);
		dao.update(sre);
	}

	public void afterPropertiesSet() throws Exception {
		ConfiguracioService cfgSvc = getConfiguracioService();
		Configuracio cfg = cfgSvc.findParametreByCodiAndCodiXarxa("addon.report.server", null);
		String hostName = InetAddress.getLocalHost().getHostName();
		if (cfg == null)
		{
			cfg = new Configuracio ();
			cfg.setCodi("addon.report.server");
			cfg.setValor(hostName);
			cfg.setDescripcio("Console to execute reports");
			cfgSvc.create(cfg);
		} else if (! cfg.getValor().equals(hostName)) {
			Log.info("This host is not the report server ("+cfg.getValor()+")");
			return;
		}
		
		executorThread = new ExecutorThread();
		executorThread.setApplicationContext (applicationContext);

		schedulerThread = new SchedulerThread();
		schedulerThread.setApplicationContext (applicationContext);
		schedulerThread.setExecutorThread(executorThread);

		executorThread.start();
		schedulerThread.start();
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	protected void finalize ()
	{
		if (executorThread != null)
			executorThread.end();
		if (schedulerThread != null)
			schedulerThread.end();
	}

	@Override
	protected void handleWriteReportContent(ExecutedReport report,
			String format, OutputStream pdfStream) throws Exception {
		ExecutedReportEntity ere = getExecutedReportEntityDao().load(report.getId());
		if (ere == null)
			return ;
		if ( ! canView(ere))
			return ;
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		DocumentService doc = getDocumentService();
		if ("xml".equals(format))
			doc.openDocument(new DocumentReference(ere.getXmlDocument()));
		else if ("html".equals(format))
			doc.openDocument(new DocumentReference(ere.getHtmlDocument()));
		else
			doc.openDocument(new DocumentReference(ere.getPdfDocument()));
		doc.openDownloadTransfer();
		do
		{
			byte data [] = doc.nextDownloadPackage(4096);
			if (data == null || data.length == 0)
				break;
			out.write(data);
		} while (true);
			
		out.close ();
		doc.endDownloadTransfer();
		doc.closeDocument();
	}
}
