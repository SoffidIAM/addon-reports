package com.soffid.iam.addons.report.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;

import com.soffid.iam.ServiceLocator;
import com.soffid.iam.doc.api.DocumentReference;
import com.soffid.iam.doc.exception.DocumentBeanException;
import com.soffid.iam.doc.service.DocumentService;
import com.soffid.iam.utils.Security;
import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ParameterType;
import com.soffid.iam.addons.report.api.ParameterValue;
import com.soffid.iam.addons.report.api.Report;
import com.soffid.iam.addons.report.api.ScheduledReport;
import com.soffid.iam.addons.report.service.ReportService;
import com.soffid.iam.addons.report.service.ejb.ReportExecutorBean;
import com.soffid.iam.addons.report.service.ReportSchedulerBootService;
import com.soffid.test.AbstractHibernateTest;

import es.caib.seycon.ng.comu.Configuracio;
import es.caib.seycon.ng.exception.InternalErrorException;
import es.caib.seycon.ng.servei.ApplicationBootService;
import es.caib.seycon.ng.servei.ConfiguracioService;
import es.caib.seycon.ng.servei.UsuariService;
import junit.framework.TestCase;

public class Test extends AbstractHibernateTest {
	
	ReportService reportSvc;
	private ConfiguracioService configService;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		File f1 = new File ("target/surefire/docs");
		FileUtils.deleteDirectory(f1);
		f1.mkdirs();
		
		File f2 = new File ("target/surefire/temp");
		FileUtils.deleteDirectory(f2);
		f2.mkdirs();

		ServiceLocator.instance().init("testBeanRefFactory.xml", "beanRefFactory");

//		Security.onSyncServer();
		Security.nestedLogin("master", "anonymous", Security.ALL_PERMISSIONS);
		
		configService = (ConfiguracioService) context.getBean (ConfiguracioService.SERVICE_NAME);
		
		config ("soffid.ui.docStrategy", "com.soffid.iam.doc.nas.comm.LocalFileSystemStrategy");
		config ("soffid.ui.docPath", f1.getPath());
		config("soffid.ui.docTempPath", f2.getPath());
		UsuariService usuariSvc = (UsuariService) context.getBean(UsuariService.SERVICE_NAME);

		reportSvc = (ReportService) context.getBean(ReportService.SERVICE_NAME);
		ApplicationBootService applicationBoot = ( ApplicationBootService) context.getBean(ApplicationBootService.SERVICE_NAME);
		applicationBoot.consoleBoot();
		ReportSchedulerBootService reportBoot = ( ReportSchedulerBootService) context.getBean(ReportSchedulerBootService.SERVICE_NAME);
		reportBoot.consoleBoot();
		
	}

	private void config(String code, String value) throws InternalErrorException {
		Configuracio c = new Configuracio(code, value);
		configService.create(c);
		
	}

	public void testDevelopment () throws InternalErrorException, IOException
	{
		FileOutputStream out = new FileOutputStream("target/surefire/ireport-addon.jar");
		reportSvc.generateDevelopmentEnvironment(out);
		out.close();
	}

	public void testUpload () throws Exception
	{
		Security.nestedLogin("admin", new String [] {});
		try {
			FileInputStream in = new FileInputStream ("./src/test/reports/users.jasper");
			Report r = reportSvc.upload(in);
			in.close();
			
			assertNotNull(r.getId());
			
			assert(r.getParameters().size() == 1);
			
			executeReport(r);
		}
		finally {
			Security.nestedLogoff();
		}
	}

	private void executeReport(Report r) throws Exception {
		ScheduledReport schedule = new ScheduledReport();
		schedule.setCreationDate(new Date());
		schedule.setName("Report test");
		schedule.setReportId(r.getId());
		LinkedList<ParameterValue> params = new LinkedList<ParameterValue>();
		ParameterValue param = new ParameterValue();
		param.setName("userName");
		param.setType(ParameterType.STRING_PARAM);
		param.setValue("%");
		params.add(param);
		schedule.setParams(params);
		schedule.setTarget(new LinkedList<String>());
		schedule.getTarget().add("admin");
		
		ExecutedReport execution = reportSvc.launchReport(schedule);
		
		for (int i = 0; !execution.isDone() && i < 100; i++)
		{
			System.out.println ("Waiting for report");
			ReportExecutorBean rb = new ReportExecutorBean();
			rb.connectServices();
			rb.timeOutHandler(null);
			execution = reportSvc.getExecutedReportStatus(execution.getId());
		}
		
		if (execution.isError())
			throw new InternalErrorException ("Report generation has failed :"+execution.getErrorMessage());
		
		downloadDocument ("target/surefire/report.pdf", execution.getPdfDocument());
		downloadDocument ("target/surefire/report.zip", execution.getHtmlDocument());
		downloadDocument ("target/surefire/report.xml", execution.getXmlDocument());
	}

	private void downloadDocument(String fileName, String docId) throws IllegalArgumentException, InternalErrorException, IOException, DocumentBeanException {
		FileOutputStream out = new FileOutputStream (fileName);
		DocumentService docSvc = (DocumentService) context.getBean(DocumentService.SERVICE_NAME);
		docSvc.openDocument(new DocumentReference(docId));
		docSvc.openDownloadTransfer();
		byte [] b;
		while ( (b = docSvc.nextDownloadPackage(4096)) != null)
			out.write(b);
		out.close ();
		docSvc.endDownloadTransfer();
		docSvc.closeDocument();
	}

	public void testUpload2 () throws Exception
	{
		Security.nestedLogin("admin", new String [] {});
		try {
			FileInputStream in = new FileInputStream ("./src/test/reports/users-bsh.jasper");
			Report r = reportSvc.upload(in);
			in.close();
			
			assertNotNull(r.getId());
			
			assert(r.getParameters().size() == 1);
			
			executeReport(r);
		}
		finally {
			Security.nestedLogoff();
		}
	}

}
