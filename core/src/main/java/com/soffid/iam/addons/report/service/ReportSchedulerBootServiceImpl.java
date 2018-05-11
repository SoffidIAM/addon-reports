package com.soffid.iam.addons.report.service;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.soffid.iam.addons.report.service.ejb.ReportSchedulerBean;
import com.soffid.iam.api.Tenant;

public class ReportSchedulerBootServiceImpl extends
		ReportSchedulerBootServiceBase implements  ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	protected void handleSyncServerBoot() throws Exception {
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
	}

}
