package com.soffid.iam.addons.report.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.query.JRHibernateQueryExecuter;
import net.sf.jasperreports.engine.query.JRHibernateQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.context.ApplicationContext;

import com.soffid.iam.addons.doc.api.DocumentOutputStream;
import com.soffid.iam.addons.doc.api.DocumentReference;
import com.soffid.iam.addons.doc.exception.DocumentBeanException;
import com.soffid.iam.addons.doc.service.DocumentService;
import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ParameterValue;
import com.soffid.iam.addons.report.api.Report;
import com.soffid.iam.addons.report.api.ScheduledReport;

import es.caib.seycon.ng.exception.InternalErrorException;

public class ExecutorThread extends Thread {
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass());

	private ReportService reportService;
	private ReportSchedulerService reportSchedulerService;
	private boolean end = false;

	private DocumentService documentService;

	private SessionFactory sessionFactory;

	public void end() {
		end = true;
		this.notify();
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		reportService = (ReportService) applicationContext.getBean(ReportService.SERVICE_NAME);
		reportSchedulerService = (ReportSchedulerService) applicationContext.getBean(ReportSchedulerService.SERVICE_NAME);
		documentService = (DocumentService) applicationContext.getBean(DocumentService.SERVICE_NAME);
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
	}

	public void newReportCreated() {
		this.notify ();
	}

	@Override
	public void run() {
		while ( ! end )
		{
			try
			{
				for (ExecutedReport sr : reportSchedulerService.getPendingReports())
				{
					try {
						execute (sr);
						reportSchedulerService.updateReport(sr);
					} catch (Exception e) {
						sr.setDone(false);
						sr.setError(true);
						sr.setErrorMessage(e.toString());
					}
				}
				wait (120000);
			} catch (Exception e) {
				log.warn("Error on report scheduler", e);
			}
		}
	}

	private void execute(ExecutedReport sr) throws InternalErrorException, ParseException, DocumentBeanException, IOException, JRException 
	{
		DocumentReference r = reportSchedulerService.getReportDocument(sr.getReportId());

		JRLoader loader = new JRLoader();
		
		File f = getDocument(r);
				
		JasperReport jasperReport = (JasperReport) loader.loadObject (f);
		f.delete();

		Map<String,Object> v = new HashMap<String, Object>();
		for (JRParameter jp: jasperReport.getParameters())
		{
			for (ParameterValue pv: sr.getParams())
			{
				if (pv.getName().equals (jp.getName()))
				{
					if (pv.getValue() == null)
						v.put(pv.getName(), pv.getValue());
					else if (jp.getValueClass().isAssignableFrom(pv.getValue().getClass()))
						v.put(pv.getName(), pv.getValue());
					else if (jp.getValueClass().isAssignableFrom(Integer.class))
						v.put(pv.getName(), Integer.decode(pv.getValue().toString()));
					else if (jp.getValueClass().isAssignableFrom(Long.class))
						v.put(pv.getName(), Long.decode(pv.getValue().toString()));
					else if (jp.getValueClass().isAssignableFrom(Float.class))
						v.put(pv.getName(), Float.valueOf(pv.getValue().toString()));
					else if (jp.getValueClass().isAssignableFrom(Double.class))
						v.put(pv.getName(), Double.valueOf(pv.getValue().toString()));
					else if (jp.getValueClass().isAssignableFrom(Date.class))
						v.put(pv.getName(), DateFormat.getInstance().parse(pv.getValue().toString()));
					else if (jp.getValueClass().isAssignableFrom(Boolean.class))
						v.put(pv.getName(), Boolean.parseBoolean(pv.getValue().toString()));
					else if (jp.getValueClass().isAssignableFrom(Integer.class))
						v.put(pv.getName(), Integer.decode(pv.getValue().toString()));
					else if (jp.getValueClass().isAssignableFrom(Integer.class))
						v.put(pv.getName(), Integer.decode(pv.getValue().toString()));
					else if (jp.getValueClass().isAssignableFrom(Integer.class))
						v.put(pv.getName(), Integer.decode(pv.getValue().toString()));
					else if (jp.getValueClass().isAssignableFrom(Integer.class))
						v.put(pv.getName(), Integer.decode(pv.getValue().toString()));
				}
			}
		}
		
		
		Session session = sessionFactory.openSession();
		try
		{ 
			v.put(JRHibernateQueryExecuterFactory.PARAMETER_HIBERNATE_SESSION, session);
	        // preparamos para imprimir
	        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,
	                v);
	
	        if (jasperPrint.getPages().size() > 0) {
	        	File outFile = File.createTempFile("report", "pdf");
	        	
	        	documentService.createDocument("application/pdf", sr.getName()+".pdf", "report");
	        	DocumentOutputStream out = new DocumentOutputStream(documentService);
	        	JasperExportManager.exportReportToPdfStream (jasperPrint, out);
	        	sr.setPdfDocument(documentService.getReference().toString());
	        	out.close ();
	        	
	        	documentService.createDocument("text/xml", sr.getName()+".xml", "report");
	        	out = new DocumentOutputStream(documentService);
	        	JasperExportManager.exportReportToXmlStream (jasperPrint, out);
	        	sr.setXmlDocument(documentService.getReference().toString());
	        	out.close ();

	        	documentService.createDocument("text/html", sr.getName()+".xml", "report");
	        	out = new DocumentOutputStream(documentService);
	        	JasperExportManager.exportReportToXmlStream (jasperPrint, out);
	        	sr.setHtmlDocument(documentService.getReference().toString());
	        	out.close ();
	        }
		} finally {
	        session.clear();
	        session.close();
		}
	}

	private File getDocument(DocumentReference ref) throws DocumentBeanException, InternalErrorException, IOException {
		documentService.openDocument(ref);
		File f =  File.createTempFile("report", "rpt");
		FileOutputStream out = new FileOutputStream(f);
		documentService.openDownloadTransfer();
		do
		{
			byte data [] = documentService.nextDownloadPackage(4096);
			if (data == null || data.length == 0)
				break;
			out.write(data);
		} while (true);
			
		out.close ();
		documentService.endDownloadTransfer();
		documentService.closeDocument();
		return f;
	}


}
