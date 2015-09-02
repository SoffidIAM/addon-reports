package com.soffid.iam.addons.report.service;

import java.net.InetAddress;

import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.soffid.iam.api.Configuration;
import com.soffid.iam.service.ConfigurationService;

import es.caib.seycon.ng.comu.Configuracio;
import es.caib.seycon.ng.servei.ConfiguracioService;

public class ReportSchedulerBootServiceImpl extends
		ReportSchedulerBootServiceBase implements  ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	protected void handleSyncServerBoot() throws Exception {
	}

	@Override
	protected void handleConsoleBoot() throws Exception {
		
		org.apache.commons.logging.Log log = LogFactory.getLog(getClass());
		
		ConfigurationService cfgSvc = getConfigurationService();
		Configuration cfg = cfgSvc.findParameterByNameAndNetworkName("addon.report.server", null);
		String hostName = InetAddress.getLocalHost().getHostName();
		if (cfg == null)
		{
			cfg = new Configuration ();
			cfg.setCode("addon.report.server");
			cfg.setValue(hostName);
			cfg.setDescription("Console to execute reports");
			cfgSvc.create(cfg);
		} else if (! cfg.getValue().equals(hostName)) {
			log.info("This host is not the report server ("+cfg.getValue()+")");
			return;
		}
		
		SessionFactory sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		ExecutorThread executorThread = ExecutorThread.getInstance();
		executorThread.setReportSchedulerService(getReportSchedulerService());
		executorThread.setReportService(getReportService());
		executorThread.setSessionFactory(sessionFactory);
		executorThread.setDocumentService(getDocumentService());
		
		SchedulerThread schedulerThread = SchedulerThread.getInstance();
		schedulerThread.setReportSchedulerService(getReportSchedulerService());
		schedulerThread.setExecutorThread(executorThread);

		if (! executorThread.isAlive())
			executorThread.start();
		if (! schedulerThread.isAlive())
			schedulerThread.start();
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
