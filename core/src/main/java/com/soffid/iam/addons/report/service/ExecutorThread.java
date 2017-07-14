package com.soffid.iam.addons.report.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRChild;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.query.JRHibernateQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;

import bsh.EvalError;
import bsh.Interpreter;

import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ParameterValue;
import com.soffid.iam.addons.report.api.Report;
import com.soffid.iam.doc.api.DocumentOutputStream;
import com.soffid.iam.doc.api.DocumentReference;
import com.soffid.iam.doc.exception.DocumentBeanException;
import com.soffid.iam.doc.service.DocumentService;

import es.caib.seycon.ng.exception.InternalErrorException;

public class ExecutorThread extends Thread {
	private static ExecutorThread executorThread = null;
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass());

	public ReportSchedulerService getReportSchedulerService() {
		return reportSchedulerService;
	}

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}

	public void setReportSchedulerService(
			ReportSchedulerService reportSchedulerService) {
		this.reportSchedulerService = reportSchedulerService;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private ReportSchedulerService reportSchedulerService;
	private ReportService reportService;
	private boolean end = false;

	public ReportService getReportService() {
		return reportService;
	}

	public void setReportService(ReportService reportService) {
		this.reportService = reportService;
	}

	private DocumentService documentService;

	private SessionFactory sessionFactory;

	public void end() {
		end = true;
		synchronized(this)
		{
			this.notify();
		}
	}


	public void newReportCreated() {
		synchronized (this)
		{
			this.notify ();
		}
	}

	@Override
	public void run() {
		setName ("ReportExecutorThread");
		while ( ! end )
		{
			try
			{
				log.info("Looking for reports to execute");
				for (ExecutedReport sr : reportSchedulerService.getPendingReports())
				{
					if (sr.getUsers() == null || sr.getUsers().isEmpty())
					{
						sr.setUsers(new LinkedList<String>());
						sr.getUsers().add("dummy");
						reportService.remove(sr);
					}
					else
					{
						try {
							log.info("Executing report "+sr.getName());
							execute (sr);
							sr.setDone(true);
							sr.setError(false);
							sr.setErrorMessage(null);
							reportSchedulerService.updateReport(sr);
						} catch (Exception e) {
							log.info("Errr executing report "+sr.getName(), e);
							sr.setDone(true);
							sr.setError(true);
							sr.setErrorMessage(e.toString());
							reportSchedulerService.updateReport(sr);
						}
					}
				}
	
				synchronized (this)
				{
					this.wait (120000);
					sleep(1000);// Wait 1 second for caller transaction to commit report execution
				}
			} catch (Throwable e) {
				e.printStackTrace();
				log.warn("Error on report executor", e);
				try {
					sleep(5000);
				} catch (InterruptedException e1) {
				}
			}
		}
		log.info ("Finished.");
	}

	private void execute(ExecutedReport sr) throws InternalErrorException, ParseException, DocumentBeanException, IOException, JRException, EvalError 
	{
		File srcdir = Files.createTempDirectory("soffid-report").toFile();
		srcdir.mkdir();
		DocumentReference r = reportSchedulerService.getReportDocument(sr.getReportId());

		File f = getDocument(srcdir, r);
				
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject (f);
		jasperReport.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
		jasperReport.setProperty("net.sf.jasperreports.subreport.runner.factory", "net.sf.jasperreports.engine.fill.JRContinuationSubreportRunnerFactory");

		List<File> children = new LinkedList<File>();
		downloadChildren(srcdir, children, jasperReport);
		
		Map<String,Object> v = new HashMap<String, Object>();
		for (JRParameter jp: jasperReport.getParameters())
		{
			for (ParameterValue pv: sr.getParams())
			{
				if (pv.getName().equals (jp.getName()))
				{
					if (pv.getValue() == null || "".equals(pv.getValue()))
						v.put(pv.getName(), null);
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
			v.put("net.sf.jasperreports.subreport.runner.factory", "net.sf.jasperreports.engine.fill.JRContinuationSubreportRunnerFactory");
			v.put("net.sf.jasperreports.awt.ignore.missing.font", "true");
	        // preparamos para imprimir
			JasperPrint jasperPrint;

			Connection conn = session.connection();
			
			jasperPrint = JasperFillManager.fillReport(jasperReport, v, conn);
			
	        if (jasperPrint.getPages().size() > 0) {
	        	File outFile = File.createTempFile("report", "pdf");
	        	
	        	documentService.createDocument("application/pdf", sr.getName()+".pdf", "report");
	        	DocumentOutputStream out = new DocumentOutputStream(documentService);
	        	JasperExportManager.exportReportToPdfStream (jasperPrint, out);
	        	out.close ();
	        	sr.setPdfDocument(documentService.getReference().toString());
	        	documentService.closeDocument();
	        	
	        	documentService.createDocument("text/xml", sr.getName()+".xml", "report");
	        	out = new DocumentOutputStream(documentService);
	        	JasperExportManager.exportReportToXmlStream (jasperPrint, out);
	        	out.close ();
	        	sr.setXmlDocument(documentService.getReference().toString());
	        	documentService.closeDocument();

	        	documentService.createDocument("applicatin/zip", sr.getName()+".zip", "report");
	        	File htmlFile = File.createTempFile("soffid"+sr.getId(), ".html");
	        	JasperExportManager.exportReportToHtmlFile(jasperPrint, htmlFile.getPath());
	        	out = new DocumentOutputStream(documentService);
	        	JarOutputStream jar = new JarOutputStream (out);
	        	dump (jar, htmlFile, sr.getName()+".html");
	        	htmlFile.delete();
	        	File dir = new File (htmlFile.getPath()+"_files");
	        	if (dir.isDirectory())
	        	{
		        	for (File subfile:dir.listFiles())
		        	{
		        		dump (jar, subfile, dir.getName()+"/"+subfile.getName());
		        		subfile.delete();
		        	}
	        	}
	        	jar.close ();
	        	sr.setHtmlDocument(documentService.getReference().toString());
	        	documentService.closeDocument();
	        	
	        	documentService.createDocument("text/csv", sr.getName()+".csv", "report");
	        	out = new DocumentOutputStream(documentService);
	        	JRCsvExporter exporterCSV = new JRCsvExporter(); 
	        	exporterCSV.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint); 
	        	exporterCSV.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, out); 
	        	exporterCSV.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, true); 
	        	exporterCSV.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, true); 
	        	exporterCSV.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, true); 
	        	exporterCSV.exportReport(); 
	        	out.close ();
	        	sr.setCsvDocument(documentService.getReference().toString());
	        	documentService.closeDocument();
	        }
		} finally {
			f.delete();
			for (File f2: children)
				f2.delete();
			srcdir.delete();
	        session.clear();
	        session.close();
        	documentService.closeDocument();
		}
	}

	private void downloadChildren(File srcdir, List<File> files, JasperReport jasperReport) throws DocumentBeanException, InternalErrorException, IOException {
		for (JRBand band: jasperReport.getAllBands())
		{
			for ( JRChild child : band.getChildren())
			{
				if (child instanceof JRSubreport)
				{
					JRSubreport sr = (JRSubreport) child;
					String expression = sr.getExpression().getText();
					int i = expression.lastIndexOf('\"');
					if ( i >= 0) expression = expression.substring(0, i);
					i = expression.lastIndexOf('\"');
					if ( i >= 0) expression = expression.substring(i+1);
					i = expression.lastIndexOf('\\');
					if ( i >= 0) expression = expression.substring(i+1);
					i = expression.lastIndexOf('/');
					if ( i >= 0) expression = expression.substring(i+1);
					i = expression.lastIndexOf('.');
					if ( i >= 0) expression = expression.substring(0, i);
					File f = new File (srcdir, expression+".jasper");
					if (! files.contains(f))
					{
						Collection<Report> reports = reportService.findReports(expression, true);
						if (reports == null || reports.isEmpty())
							throw new InternalErrorException ("Cannot find subreport "+expression);
						for (Report report: reports)
						{
							DocumentReference ref = reportSchedulerService.getReportDocument(report.getId());
							files.add (getDocument(srcdir, ref));
						}
					}
				}
			}
		}
	}

	private void dump(JarOutputStream jar, File file, String entryName) throws IOException {
		InputStream in = new FileInputStream(file);
		jar.putNextEntry(new ZipEntry(entryName));
		byte b[] = new byte[4096];
		int read = 0;
		while ( (read = in.read(b)) > 0)
			jar.write (b, 0, read);
		in.close ();
		jar.closeEntry();
	}

	private File getDocument(File dir, DocumentReference ref) throws DocumentBeanException, InternalErrorException, IOException {
		documentService.openDocument(ref);
		File f =  new File(dir, documentService.getExternalName()+".jasper");
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


	private ExecutorThread ()
	{
		
	}
	
	public static ExecutorThread getInstance ()
	{
		if (executorThread == null)
			executorThread = new ExecutorThread();
		
		return executorThread;
	}
}
