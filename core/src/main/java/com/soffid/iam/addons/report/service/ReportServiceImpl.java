package com.soffid.iam.addons.report.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.naming.InitialContext;

import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.soffid.iam.addons.acl.api.AccessControlList;
import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ExecutedReportCriteria;
import com.soffid.iam.addons.report.api.ParameterValue;
import com.soffid.iam.addons.report.api.QueryRequest;
import com.soffid.iam.addons.report.api.QueryResponse;
import com.soffid.iam.addons.report.api.Report;
import com.soffid.iam.addons.report.api.ScheduledReport;
import com.soffid.iam.addons.report.model.ExecutedReportEntity;
import com.soffid.iam.addons.report.model.ExecutedReportEntityDao;
import com.soffid.iam.addons.report.model.ExecutedReportParameterEntity;
import com.soffid.iam.addons.report.model.ExecutedReportTargetEntity;
import com.soffid.iam.addons.report.model.ReportACLEntity;
import com.soffid.iam.addons.report.model.ReportEntity;
import com.soffid.iam.addons.report.model.ReportEntityDao;
import com.soffid.iam.addons.report.model.ReportParameterEntity;
import com.soffid.iam.addons.report.model.ScheduledReportEntity;
import com.soffid.iam.addons.report.model.ScheduledReportTargetEntity;
import com.soffid.iam.addons.report.service.ejb.ReportExecutor;
import com.soffid.iam.addons.report.service.jasper.QueryExecutor;
import com.soffid.iam.api.Configuration;
import com.soffid.iam.api.User;
import com.soffid.iam.doc.api.DocumentReference;
import com.soffid.iam.doc.service.DocumentService;
import com.soffid.iam.model.UserEntity;
import com.soffid.iam.model.identity.IdentityGenerator;
import com.soffid.iam.utils.ConfigurationCache;
import com.soffid.iam.utils.Security;

import es.caib.seycon.ng.exception.InternalErrorException;


public class ReportServiceImpl extends ReportServiceBase implements ApplicationContextAware, InitializingBean {
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass());
	
	private ApplicationContext applicationContext;

	@Override
	protected Collection<Report> handleFindReports(String name,
			boolean exactMatch) throws Exception {
		Collection<ReportEntity> entities; 
		if (exactMatch)
		{
			ReportEntity r = getReportEntityDao().findByName(name);
			entities = new LinkedList<ReportEntity>();
			if (r != null)
				entities.add(r);
		}
		else if (name == null || name.trim().length() == 0)
		{
			entities = getReportEntityDao().loadAll();
		}
		else
		{
			entities = getReportEntityDao().findByNameFilter(name);
		}
		
		if ( ! Security.isUserInRole( "report:admin" ))
		{
			for (Iterator<ReportEntity> it = entities.iterator(); it.hasNext();)
			{
				ReportEntity r = it.next();
				if (! canExecute(r))
					it.remove();
			}
		}
		return getReportEntityDao().toReportList( entities );
	}

	@Override
	protected Report handleUpload(InputStream report) throws Exception {
		return new QueryExecutor().upload(getReportEntityDao(), getReportParameterEntityDao(), 
				getReportACLEntityDao(), getDocumentService(), report);
	}

	@Override
	protected ExecutedReport handleLaunchReport(ScheduledReport schedule)
			throws Exception {
		ReportEntity report = getReportEntityDao().load (schedule.getReportId());
		if (canExecute (report))
		{
			UserEntity myself = getUserEntityDao ().findByUserName(Security.getCurrentUser());
					
			ExecutedReportEntity er = getExecutedReportEntityDao().newExecutedReportEntity();
			er.setReport(report);
			er.setDate(new Date());
			er.setDone(false);
			er.setError(false);
			er.setName(schedule.getName());
			if (myself != null) {
				er.setUser(myself.getUserName());
				ExecutedReportTargetEntity erte = getExecutedReportTargetEntityDao().newExecutedReportTargetEntity();
				erte.setReport(er);
				erte.setUser(myself);
				er.getAcl().add(erte);
			}
			
			for (ParameterValue pv: schedule.getParams())
			{
				ExecutedReportParameterEntity erpe = getExecutedReportParameterEntityDao().parameterValueToEntity(pv);
				erpe.setReport(er);
				er.getParameters().add(erpe);
			}
			
			getExecutedReportEntityDao().create(er);
			
			try {
				String jndi = "java:module/ReportExecutorBean";
				ReportExecutor bean =
						(ReportExecutor) new InitialContext().lookup(jndi);
				bean.newReportCreated();
			} catch (Exception e) {
				LogFactory.getLog(getClass()).warn("Error notifying report creation", e);
			}
			
			return getExecutedReportEntityDao().toExecutedReport(er);
		} else
			throw new InternalErrorException ("Not authorized");
	}

	private boolean canExecute(ReportEntity report) throws InternalErrorException {
		if (Security.isUserInRole("report:admin"))
			return true;
		User user = getUserService().getCurrentUser();
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
		User user = getUserService().getCurrentUser();
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
		if ( !Security.isUserInRole("report:admin") && ! canView(ere))
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
			if ((ere.isDone() || ere.isError()) && canView(ere))
				result.add (dao.toExecutedReport(ere));
		}

		Collections.sort(result, new Comparator<ExecutedReport>() {

			public int compare(ExecutedReport o1, ExecutedReport o2) {
				if (o1.getDate() == null)
					return -1;
				if (o2.getDate() == null)
					return +1;
				return - (o1.getDate().compareTo(o2.getDate()));
			}
		});
		return result;
	}

	@Override
	protected void handleRemove(Report report) throws Exception {
		ReportEntity re = getReportEntityDao().load(report.getId());
		for (ScheduledReportEntity sr: re.getSchedules())
		{
			getScheduledReportEntityDao().remove(sr);
		}
		
		for (ExecutedReportEntity er: re.getExecutions())
		{
			getExecutedReportEntityDao().remove(er);
		}

		for (ReportParameterEntity rp: re.getParameters())
		{
			getReportParameterEntityDao().remove(rp);
		}
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
		if (schedule.getCronDayOfMonth() == null || schedule.getCronDayOfMonth().trim().length() == 0)
			throw new IllegalArgumentException("Missing cron day pattern");
		if (schedule.getCronDayOfWeek() == null || schedule.getCronDayOfWeek().trim().length() == 0)
			throw new IllegalArgumentException("Missing cron day of week pattern");
		if (schedule.getCronHour() == null || schedule.getCronHour().trim().length() == 0)
			throw new IllegalArgumentException("Missing cron hour pattern");
		if (schedule.getCronMinute() == null || schedule.getCronMinute().trim().length() == 0)
			throw new IllegalArgumentException("Missing cron minute pattern");
		if (schedule.getCronMonth() == null || schedule.getCronMonth().trim().length() == 0)
			throw new IllegalArgumentException("Missing cron month pattern");
		schedule.setNextExecution( null );
		schedule.setLastExecution( null );
		schedule.setCreationDate(new Date());
		ScheduledReportEntity sre = getScheduledReportEntityDao().scheduledReportToEntity(schedule);
		sre.setUser(Security.getCurrentUser());
		getScheduledReportEntityDao().create(sre);
		return getScheduledReportEntityDao().toScheduledReport(sre);
	}

	@Override
	protected void handleUpdate(ScheduledReport schedule) throws Exception {
		ScheduledReportEntity sre = getScheduledReportEntityDao().scheduledReportToEntity(schedule);
		getScheduledReportEntityDao().update(sre);
		for ( ScheduledReportTargetEntity target: sre.getAcl()) {
			if (target.getId() == null)
				getScheduledReportTargetEntityDao().create(target);
			else
				getScheduledReportTargetEntityDao().update(target);
		}
	}

	@Override
	protected void handleUpdate(Report report) throws Exception {
		ReportEntityDao dao = getReportEntityDao();
		ReportEntity sre = dao.load (report.getId());
		
		sre = dao.reportToEntity(report);
		dao.update(sre);
	}

	public void afterPropertiesSet() throws Exception {
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	protected void handleWriteReportContent(ExecutedReport report,
			String format, OutputStream pdfStream) throws Exception {
		ExecutedReportEntity ere = getExecutedReportEntityDao().load(report.getId());
		if (ere == null)
			return ;
		if ( !Security.isUserInRole("report:admin") && ! canView(ere))
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

	private boolean dump (String resource, JarOutputStream jar) throws IOException
	{
		InputStream in = getClass().getClassLoader().getResourceAsStream(resource);
		if (in != null)
		{
			jar.putNextEntry(new ZipEntry(resource));
			dumpStream(in, jar);
			jar.closeEntry();
			return true;
		}
		else
			return false;
		
	}

	private void dumpStream(InputStream in, OutputStream out)
			throws IOException {
		byte b[] = new byte[2048];
		int read;
		while ( (read = in.read(b)) > 0)
		{
			out.write (b, 0, read);
		}
		in.close ();
	}
	@Override
	protected void handleGenerateDevelopmentEnvironment(OutputStream out)
			throws Exception {
		SessionFactory sf = (SessionFactory) applicationContext.getBean("sessionFactory");
		JarOutputStream jar = new JarOutputStream(out);
		HashSet<String> storedObjects = new HashSet<String>();
		
		ByteArrayOutputStream hibernateFile  = new ByteArrayOutputStream();
		
		dumpStream(getClass().getResourceAsStream("hibernate-header"), hibernateFile);
		
		
		for (Object key : sf.getAllClassMetadata().keySet())
		{
			String className = (String) key;
			Class<?> cl = Class.forName(className);
			ClassMetadata md = (ClassMetadata) sf.getAllClassMetadata().get(className);
			
			do
			{
				dumpClass ( cl, jar, storedObjects );
				String resource = cl.getCanonicalName().replace('.', '/')+ ".hbm.xml";
				if (!storedObjects.contains(resource) && dump (resource, jar))
				{
					storedObjects.add(resource);
					hibernateFile.write ("   <mapping resource=\"".getBytes());
					hibernateFile.write (resource.getBytes());
					hibernateFile.write ("\"/>\n".getBytes());
				}
				cl = cl.getSuperclass();
			} while (cl != null);

			for (Type t : md.getPropertyTypes())
			{
				if (t instanceof CustomType)
				{
					try {
						dumpClass (getClass().getClassLoader().loadClass(t.getName()), jar, storedObjects);
					} catch ( ClassNotFoundException e) {}
				}
			}
		}
		
		dumpClass(IdentityGenerator.class, jar, storedObjects);
		try {
			dumpClass(getClass().getClassLoader().loadClass("com.soffid.iam.model.security.SecurityScopeEntity"), 
					jar, storedObjects);
		} catch ( NoClassDefFoundError e ) {
			
		} catch ( ClassNotFoundException e) {
			
		}
		
		hibernateFile.write("</session-factory>\n</hibernate-configuration>\n".getBytes());
		hibernateFile.close();
		
		jar.putNextEntry(new ZipEntry("hibernate.cfg.xml"));
		dumpStream(new ByteArrayInputStream(hibernateFile.toByteArray()), jar);
		jar.closeEntry();

		jar.close();
	}

	private void dumpClass(Class<?> cl, JarOutputStream jar, HashSet<String> storedObjects) throws IOException {
		if (! cl.isPrimitive() && cl.getPackage() != null && cl.getPackage().getName().startsWith("java") )
			return;
		
		String resourceName = cl.getCanonicalName().replace('.', '/')+ ".class";
		if (! storedObjects.contains(resourceName))
		{
			storedObjects.add(resourceName);
			if (dump (resourceName, jar))
			{
				for (Field f: cl.getDeclaredFields())
				{
					dumpClass(f.getType(), jar, storedObjects);
				}
			}
		}
	}

	@Override
	protected void handleRemove(ExecutedReport report)
			throws Exception {
		ExecutedReportEntity ere = getExecutedReportEntityDao().load(report.getId());
		if (ere != null)
		{
			User u = getUserService().getCurrentUser();
			final Collection<ExecutedReportTargetEntity> acl = new LinkedList( ere.getAcl() );
			if (u != null)
			{
				for ( Iterator<ExecutedReportTargetEntity> it = acl.iterator();
						it.hasNext();)
				{
					ExecutedReportTargetEntity target = it.next ();
					if (target.getUser() != null && target.getUser().getId().equals(u.getId()))
					{
						getExecutedReportTargetEntityDao().remove(target);
						it.remove();
					}
				}
			}
			if (acl.isEmpty())
			{
				DocumentService ds = getDocumentService();
				if (ere.getHtmlDocument() != null)
					ds.deleteDocument(new DocumentReference(ere.getHtmlDocument()));
				if (ere.getXmlDocument() != null)
					ds.deleteDocument(new DocumentReference(ere.getXmlDocument()));
				if (ere.getPdfDocument() != null)
					ds.deleteDocument(new DocumentReference(ere.getPdfDocument()));
				if (ere.getCsvDocument() != null)
					ds.deleteDocument(new DocumentReference(ere.getCsvDocument()));
				if (ere.getXlsDocument() != null)
					ds.deleteDocument(new DocumentReference(ere.getXlsDocument()));
				getExecutedReportEntityDao().remove(ere);
				
			} else {
				getExecutedReportEntityDao().update(ere);
			}
		}
	}

	@Override
	protected Collection<ScheduledReport> handleFindScheduledReports(String name) throws InternalErrorException {
		List<ScheduledReport> list;
		if (name == null || name.trim().length() == 0)
		{
			list = getScheduledReportEntityDao().toScheduledReportList(getScheduledReportEntityDao().loadAll());
		}
		else
		{
			list = getScheduledReportEntityDao().toScheduledReportList(getScheduledReportEntityDao().findByNameFilter(name));
		}
		return list;
	}

	@Override
	protected void handlePurgeExpiredReports() throws Exception {
		String daysToPreserve = ConfigurationCache.getProperty("soffid.addon.report.expire");
		if (daysToPreserve == null)
		{
			daysToPreserve = "16000";
			Configuration cfg = new Configuration();
			cfg.setCode("soffid.addon.report.expire");
			cfg.setValue(daysToPreserve);
			cfg.setDescription("Max days to keep generated reports");
			getConfigurationService().create(cfg);
		}
		if ( ! daysToPreserve.isEmpty())
		{
			int days = Integer.parseInt(daysToPreserve.trim());
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DAY_OF_YEAR, -days);
			log.info("Expiring reports since "+DateFormat.getDateInstance().format(c.getTime()));
			for (ExecutedReportEntity ere : getExecutedReportEntityDao().findExpiredReports( c.getTime()))
			{
				log.info("Expiring report #"+ere.getId()+": "+ere.getName());
				for ( Iterator<ExecutedReportTargetEntity> it = ere.getAcl().iterator();
						it.hasNext();)
				{
					ExecutedReportTargetEntity target = it.next ();
					target.setReport(null);
					it.remove();
				}
				DocumentService ds = getDocumentService();
				if (ere.getHtmlDocument() != null)
					ds.deleteDocument(new DocumentReference(ere.getHtmlDocument()));
				if (ere.getXmlDocument() != null)
					ds.deleteDocument(new DocumentReference(ere.getXmlDocument()));
				if (ere.getPdfDocument() != null)
					ds.deleteDocument(new DocumentReference(ere.getPdfDocument()));
				if (ere.getCsvDocument() != null)
					ds.deleteDocument(new DocumentReference(ere.getCsvDocument()));
				if (ere.getXlsDocument() != null)
					ds.deleteDocument(new DocumentReference(ere.getXlsDocument()));
				getExecutedReportEntityDao().remove(ere);
			}
		}
	}

	@Override
	protected QueryResponse handleQuery(QueryRequest request) throws Exception {
		SessionFactory sf = (SessionFactory) applicationContext.getBean("sessionFactory");
		return new QueryExecutor().executeQuery(request, sf);
	}

}
