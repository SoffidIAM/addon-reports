package com.soffid.iam.addons.report.service;

import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.soffid.iam.addons.report.service.ejb.ReportSchedulerBean;
import com.soffid.iam.addons.report.service.timer.ReportAddonTimer;
import com.soffid.iam.api.Configuration;
import com.soffid.iam.api.Tenant;
import com.soffid.iam.sync.SoffidApplication;
import com.soffid.iam.sync.jetty.JettyServer;
import com.soffid.iam.utils.ConfigurationCache;

public class ReportSchedulerBootServiceImpl extends
		ReportSchedulerBootServiceBase implements  ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	protected void handleSyncServerBoot() throws Exception {
		if ("syncserver".equals(ConfigurationCache.getMasterProperty("addon.report.engine"))) {
			SoffidApplication.getJetty(). 
			publish(getReportRunnerService(), ReportRunnerService.REMOTE_PATH, "SEU_CONSOLE");
		}
	}

	@Override
	protected void handleConsoleBoot() throws Exception {
		org.apache.commons.logging.Log log = LogFactory.getLog(getClass());
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	protected void handleTenantBoot(Tenant tenant) throws Exception {
		URL url = getClass().getResource("/com/soffid/iam/addons/report/service/ReportSchedulerServiceImpl.class");
        if (url.getProtocol().equals("jar")) { //$NON-NLS-1$
			Configuration cfg = getConfigurationService().findParameterByNameAndNetworkName("addon.report.reports", null);
			if (cfg == null || ! cfg.getValue().equals("1")) {
				if (cfg == null) {
					cfg = new Configuration();
					cfg.setName("addon.report.reports");
					cfg.setDescription("Standard reports library version");
					cfg.setValue("1");
					getConfigurationService().create(cfg);
				} else {
					cfg.setValue("1");
					getConfigurationService().update(cfg);
				}
				String jar = url.getFile();
				int i = jar.lastIndexOf("!"); //$NON-NLS-1$
				if (i >= 0)
					jar = jar.substring(0, i);
				System.out.println(jar);
				ZipInputStream zip = new ZipInputStream(new URL(jar).openStream());
				for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
					if (entry.getName().endsWith(".jasper")) {
						getReportService().upload(zip);
					}
				}
				
			}
        }


	}

}
