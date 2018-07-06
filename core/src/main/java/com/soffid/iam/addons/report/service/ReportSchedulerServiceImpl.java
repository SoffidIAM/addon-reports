package com.soffid.iam.addons.report.service;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.jfree.util.Log;

import com.soffid.iam.doc.api.DocumentReference;
import com.soffid.iam.doc.service.DocumentService;

import es.caib.seycon.ng.comu.Configuracio;
import es.caib.seycon.ng.comu.Usuari;
import es.caib.seycon.ng.exception.InternalErrorException;
import es.caib.seycon.ng.utils.MailUtils;

import com.soffid.iam.addons.acl.api.AccessControlList;
import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ParameterValue;
import com.soffid.iam.addons.report.api.Report;
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
import com.soffid.iam.addons.report.model.ScheduledReportTargetEntity;

public class ReportSchedulerServiceImpl extends ReportSchedulerServiceBase {

	@Override
	protected List<ScheduledReport> handleGetScheduledReport() throws Exception {
		ScheduledReportEntityDao dao = getScheduledReportEntityDao();
		return dao.toScheduledReportList(dao.loadAll());
	}

	@Override
	protected List<ExecutedReport> handleGetPendingReports() throws Exception {
		ExecutedReportEntityDao dao = getExecutedReportEntityDao();
		List<ExecutedReport> list = dao.toExecutedReportList(dao.findPendingReports(false));
		
		return list;
	}


	org.apache.commons.logging.Log log = LogFactory.getLog(getClass());
	
	@Override
	protected void handleUpdateReport(ExecutedReport report) throws Exception {
		ExecutedReportEntity ere = getExecutedReportEntityDao().load(report.getId());
		ere.setError(report.isError());
		ere.setErrorMessage(report.getErrorMessage());
		ere.setDone(report.isDone());
		ere.setPdfDocument(report.getPdfDocument());
		ere.setHtmlDocument(report.getHtmlDocument());
		ere.setXmlDocument(report.getXmlDocument());
		ere.setCsvDocument(report.getCsvDocument());
		ere.setXlsDocument(report.getXlsDocument());
		getExecutedReportEntityDao().update(ere);
		if (ere.isDone() && ere.getNotify() != null && ere.getNotify().booleanValue())
		{
			log.info("Notifying report execution by mail");
			for (ExecutedReportTargetEntity target : ere.getAcl())
			{
				String userName = target.getUser().getCodi();
				log.info("Notifying report to "+userName);
				String hostName = System.getProperty("AutoSSOURL");
				if (hostName == null)
				{
					hostName = "http://"+System.getProperty("hostName")+"."+System.getProperty("domainName")+":8080";
				}
				getMailService().sendHtmlMailToActors(new String[]{userName}, 
						ere.getName(), 
						"<html><body>" //$NON-NLS-1$
						+ Messages.getString("ReportSchedulerServiceImpl.1") //$NON-NLS-1$
						+ "<br><br>" //$NON-NLS-1$
						+ Messages.getString("ReportSchedulerServiceImpl.3") //$NON-NLS-1$
						+ "<b>"+ere.getName()+"</b>" //$NON-NLS-1$ //$NON-NLS-2$
								+ Messages.getString("ReportSchedulerServiceImpl.6") //$NON-NLS-1$
								+ "<a href='"+hostName+"/index.zul?target=addon/report/report.zul?id=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
								+ report.getId()
								+"'>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
								+Messages.getString("ReportSchedulerServiceImpl.0") //$NON-NLS-1$
								+"</a>" //$NON-NLS-1$
								+"</body></html>"); //$NON-NLS-1$
			}
		}
	}

	@Override
	protected ExecutedReport handleStartReport(ScheduledReport report)
			throws Exception {
		AccessControlList acl = new AccessControlList();

		ScheduledReportEntity sre = getScheduledReportEntityDao().load(report.getId());
		for (ScheduledReportTargetEntity ace: sre.getAcl())
		{
			if (ace.getUser() != null)
				acl.getUsers().add(ace.getUser().getId());
			if (ace.getRole() != null)
				acl.getRoles().add(ace.getRole().getId());
			if (ace.getGroup() != null)
				acl.getGroups().add(ace.getGroup().getId());
		}
		
		AccessControlList targets = getACLService().expandACL(acl);
		
		ExecutedReportEntity ere = getExecutedReportEntityDao().newExecutedReportEntity();
		ere.setName(report.getName());
		ere.setDate(new Date());
		ere.setDone(false);
		ere.setError(false);
		ere.setReport(getReportEntityDao().load(report.getReportId()));
		ere.setNotify(Boolean.TRUE);
		for (Long user: targets.getUsers())
		{
			ExecutedReportTargetEntity erte = getExecutedReportTargetEntityDao().newExecutedReportTargetEntity();
			
			erte.setUser(getUsuariEntityDao().load(user));
			erte.setReport(ere);
			ere.getAcl().add(erte);
		}
		
		for (ParameterValue pm: report.getParams())
		{
			ParameterValue pv2 = new ParameterValue();
			pv2.setName(pm.getName());
			pv2.setType(pm.getType());
			pv2.setValue(pm.getValue());
			ExecutedReportParameterEntity erpe = getExecutedReportParameterEntityDao().parameterValueToEntity(pv2);
			erpe.setReport(ere);
			ere.getParameters().add(erpe);
		}
		
		getExecutedReportEntityDao().create(ere);
		
		sre.setLastExecution(new Date());
		getScheduledReportEntityDao().update(sre);
		
		return getExecutedReportEntityDao().toExecutedReport(ere);
	}

	@Override
	protected DocumentReference handleGetReportDocument(long id) throws Exception {
		ReportEntityDao dao = getReportEntityDao();
		ReportEntity re = dao.load(id);
		return new DocumentReference(re.getDocId());
	}

}
