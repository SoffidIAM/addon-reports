package com.soffid.iam.addons.report.service.ejb;

import java.net.Inet4Address;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;

import org.apache.commons.logging.LogFactory;

import com.soffid.iam.ServiceLocator;
import com.soffid.iam.addons.report.service.timer.ClusterManagerTimer;
import com.soffid.iam.api.Configuration;
import com.soffid.iam.service.ConfigurationService;

@Singleton(name="ReportClusterBean")
@Startup
@javax.ejb.TransactionManagement(value=javax.ejb.TransactionManagementType.CONTAINER)
@javax.ejb.TransactionAttribute(value=javax.ejb.TransactionAttributeType.SUPPORTS)
public class ClusterManagerBean  {
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass());


	@Resource
	private SessionContext context;
	boolean disabled;
	
	@PostConstruct
	public void init() throws Exception {
		log.info("Started report bean");
		if ("true".equals(System.getProperty("soffid.reportscheduler.disabled"))) {
			log.info("Report scheduler is disabled in this node");
		} else {
			context.getTimerService().createTimer(60000, 60000, "Cluster manager");
		}
	}

	@Timeout	
	@javax.ejb.TransactionAttribute(value=javax.ejb.TransactionAttributeType.SUPPORTS)
	public void timeOutHandler(Timer timer) throws Exception {
		new ClusterManagerTimer().run();
	}
	
}
