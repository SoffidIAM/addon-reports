package com.soffid.iam.addons.report.service.timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;

import com.soffid.iam.ServiceLocator;
import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ParameterValue;
import com.soffid.iam.addons.report.api.Report;
import com.soffid.iam.addons.report.service.ReportSchedulerService;
import com.soffid.iam.addons.report.service.ReportService;
import com.soffid.iam.addons.report.service.SessionHolder;
import com.soffid.iam.api.Configuration;
import com.soffid.iam.doc.api.DocumentOutputStream;
import com.soffid.iam.doc.api.DocumentReference;
import com.soffid.iam.doc.exception.DocumentBeanException;
import com.soffid.iam.doc.service.DocumentService;
import com.soffid.iam.service.ConfigurationService;
import com.soffid.iam.utils.Security;

import bsh.EvalError;
import es.caib.seycon.ng.exception.InternalErrorException;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRChild;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.query.JRHibernateQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

public class ReportExecutorTimer implements Runnable {
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass());

	private ReportSchedulerService reportSchedulerService;
	private ReportService reportService;
	private SessionFactory sessionFactory;
	private DocumentService documentService;


	private File srcdir;


	private File jasperFile;


	private LinkedList<File> children;


	@Override
	public void run() {
		executePendingReports(true);
	}

	private void executePendingReports(boolean nestedLogin) {
		try
		{
			connectServices();
			if (isMaster()) {
				log.info("Looking for reports to execute");
				for (ExecutedReport sr : reportSchedulerService.getPendingReports())
				{
					if (sr.getUser() == null)
						sr.setUser("guest");
					if (sr.getUsers().isEmpty())
						sr.getUsers().add("guest");
					Long tenantId = reportSchedulerService.getReportTenantId(sr.getId());
					if (nestedLogin) {
						String tenant = Security.getTenantName(tenantId);
						Security.nestedLogin(tenant, "report #"+sr.getReportId(), Security.ALL_PERMISSIONS);
					}
					try {
						JasperReport jasper = generateJasper(sr);
						log.info("Executing report "+sr.getName());
						execute (sr, jasper);
						sr.setDone(true);
						sr.setError(false);
						sr.setErrorMessage(null);
						reportSchedulerService.updateReport(sr);
					} catch (Throwable e) {
						log.info("Error executing report "+sr.getName(), e);
						sr.setDone(true);
						sr.setError(true);
						String msg = e.toString();
						if (msg.length() > 1000)
							msg = msg.substring(0, 1000);
						sr.setErrorMessage(msg);
						reportSchedulerService.updateReport(sr);
					} finally {
						if (nestedLogin)
							Security.nestedLogoff();
					}
					removeJasperFile();
				}
				reportService.purgeExpiredReports();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			log.warn("Error on report executor", e);
		}
	}

	private void execute(ExecutedReport sr, JasperReport jasperReport) throws InternalErrorException, ParseException, DocumentBeanException, IOException, JRException, EvalError 
	{
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
			SessionHolder.hibernateSession.set(session);
			v.put(JRHibernateQueryExecuterFactory.PARAMETER_HIBERNATE_SESSION, session);
			v.put("net.sf.jasperreports.subreport.runner.factory", "net.sf.jasperreports.engine.fill.JRContinuationSubreportRunnerFactory");
			v.put("net.sf.jasperreports.awt.ignore.missing.font", "true");
			v.put("SUBREPORT_DIR", srcdir.getPath()+"/");
			v.put("tenant", Security.getCurrentTenantName());
			v.put("tenantId", Security.getCurrentTenantId());
			v.put("net.sf.jasperreports.export.csv.exclude.origin.band.1", "pageHeader");
			v.put("net.sf.jasperreports.export.csv.exclude.origin.band.2", "pageFooter");
			v.put("net.sf.jasperreports.export.csv.exclude.origin.keep.first.band.3", "columnHeader");
			v.put("net.sf.jasperreports.export.xls.exclude.origin.band.1", "pageHeader");
			v.put("net.sf.jasperreports.export.xls.exclude.origin.band.2", "pageFooter");
			v.put("net.sf.jasperreports.export.xls.exclude.origin.keep.first.band.1", "columnHeader");
			if (sr.getUser() != null)
				v.put("soffid.user", sr.getUser());
			
			// preparamos para imprimir
			File jasperPrint = File.createTempFile("repert", "jasper");

			Connection conn = session.connection();
			
			JasperFillManager.fillReportToFile(jasperReport, jasperPrint.getPath(), v, conn);
			
	        if (jasperPrint.length() > 0) {
	        	documentService.createDocument("application/pdf", sr.getName()+".pdf", "report");
	        	DocumentOutputStream out = new DocumentOutputStream(documentService);
	        	JasperExportManager.exportReportToPdfStream (new FileInputStream(jasperPrint), out);
	        	out.close ();
	        	sr.setPdfDocument(documentService.getReference().toString());
	        	documentService.closeDocument();
	        	
	        	documentService.createDocument("text/xml", sr.getName()+".xml", "report");
	        	out = new DocumentOutputStream(documentService);
	        	JasperExportManager.exportReportToXmlStream (new FileInputStream(jasperPrint), out);
	        	out.close ();
	        	sr.setXmlDocument(documentService.getReference().toString());
	        	documentService.closeDocument();

	        	documentService.createDocument("application/zip", sr.getName()+".zip", "report");
	        	File htmlFile = File.createTempFile("soffid"+sr.getId(), ".html");
	        	JasperExportManager.exportReportToHtmlFile(jasperPrint.getPath(), htmlFile.getPath());
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

	        	try {
		        	documentService.createDocument("text/csv", sr.getName()+".csv", "report");
		        	out = new DocumentOutputStream(documentService);
		        	JRCsvExporter exporterCSV = new JRCsvExporter();
		        	SimpleCsvExporterConfiguration c = new SimpleCsvExporterConfiguration();
		        	c.setFieldDelimiter(";");
		        	c.setFieldEnclosure("\"");
		        	c.setForceFieldEnclosure(true);
		        	exporterCSV.setConfiguration(c);
		        	exporterCSV.setExporterInput( new SimpleExporterInput(jasperPrint)  );
		        	exporterCSV.setExporterOutput(new SimpleWriterExporterOutput(out));
		        	exporterCSV.exportReport();
		        	out.close ();
		        	sr.setCsvDocument(documentService.getReference().toString());
		        	documentService.closeDocument();
	        	} catch (Exception e) {
	        		log.warn("Unable to generate CSV file");
	        	}

	        	try {
		        	documentService.createDocument("application/xls", sr.getName()+".xls", "report");
		        	out = new DocumentOutputStream(documentService);
		        	JRXlsxExporter exporterXls = new JRXlsxExporter(); 
		        	SimpleXlsxExporterConfiguration ec = new SimpleXlsxExporterConfiguration();
		        	ec.setMetadataAuthor("Soffid");
		        	ec.setMetadataTitle(sr.getName());
		        	exporterXls.setConfiguration(ec);
		        	
		        	SimpleXlsxReportConfiguration rc = new SimpleXlsxReportConfiguration();
		        	rc.setRemoveEmptySpaceBetweenColumns(true);
		        	rc.setRemoveEmptySpaceBetweenRows(true);
		        	rc.setWhitePageBackground(true);
		        	rc.setMaxRowsPerSheet(65000);
		        	rc.setIgnorePageMargins(true);
		        	exporterXls.setConfiguration(rc);

		        	exporterXls.setExporterInput( new SimpleExporterInput(jasperPrint)  );
		        	exporterXls.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
		        	exporterXls.exportReport(); 
		        	
		        	out.close ();
		        	sr.setXlsDocument(documentService.getReference().toString());
		        	documentService.closeDocument();
	        	} catch (Throwable e) {
	        		log.warn("Unable to generate XLS file");
	        	}

	        }
		} finally {
			SessionHolder.hibernateSession.remove();
			documentService.closeDocument();
			session.clear();
			session.close();
		}
	}



	private void removeJasperFile() throws InternalErrorException {
		jasperFile.delete();
		for (File f2: children)
			f2.delete();
		srcdir.delete();
	}



	private JasperReport generateJasper(ExecutedReport sr)
			throws IOException, InternalErrorException, DocumentBeanException, JRException {
		children = new LinkedList<File>();
		srcdir = Files.createTempDirectory("soffid-report").toFile();
		srcdir.mkdir();
		DocumentReference r = reportSchedulerService.getReportDocument(sr.getReportId());
		
		jasperFile = new File(srcdir, sr.getName()+".jasper");
		getDocument(jasperFile, r);
		
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject (jasperFile);
		jasperReport.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
		jasperReport.setProperty("net.sf.jasperreports.subreport.runner.factory", "net.sf.jasperreports.engine.fill.JRContinuationSubreportRunnerFactory");
		jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.1", "pageHeader");
		jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.2", "pageFooter");
		jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.keep.first.band.3", "columnHeader");
		jasperReport.setProperty("net.sf.jasperreports.export.xls.exclude.origin.band.1", "pageHeader");
		jasperReport.setProperty("net.sf.jasperreports.export.xls.exclude.origin.band.2", "pageFooter");
		jasperReport.setProperty("net.sf.jasperreports.export.xls.exclude.origin.keep.first.band.1", "columnHeader");

		downloadChildren(srcdir, children, jasperReport);
		return jasperReport;
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
						log.info("Generating "+f.getPath());
						Collection<Report> reports = reportService.findReports(expression, true);
						if (reports == null || reports.isEmpty())
							throw new InternalErrorException ("Cannot find subreport "+expression);
						for (Report report: reports)
						{
							DocumentReference ref = reportSchedulerService.getReportDocument(report.getId());
							getDocument(f, ref);
							files.add (f);
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

	private void getDocument(File f, DocumentReference ref) throws DocumentBeanException, InternalErrorException, IOException {
		documentService.openDocument(ref);
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
	}

	boolean isMaster () throws InternalErrorException, UnknownHostException
	{
		ConfigurationService svc = ServiceLocator.instance().getConfigurationService();
		Configuration cfg = svc.findParameterByNameAndNetworkName("addon.report.server", null);
		if (cfg != null)
		{
			String [] split = cfg.getValue().isEmpty() ? new String[0]: cfg.getValue().split(" ");
			return split.length == 2 && 
					split[0].equals(InetAddress.getLocalHost().getHostName());
		}
		else
			return false;
	}

	public void connectServices() {
		reportSchedulerService = (ReportSchedulerService) ServiceLocator.instance().getService( ReportSchedulerService.SERVICE_NAME);
		reportService = (ReportService) ServiceLocator.instance().getService(ReportService.SERVICE_NAME);
		sessionFactory = (SessionFactory) ServiceLocator.instance().getService("sessionFactory");
		documentService = (DocumentService) ServiceLocator.instance().getService( DocumentService.SERVICE_NAME);
	}

	public void runInNestedLogin() {
		executePendingReports(false);
	}
	


}
