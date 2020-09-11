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

	@PostConstruct
	public void init() throws Exception {
		log.info("Started report bean");
		context.getTimerService().createTimer(60000, 60000, "Cluster manager");
	}

	@Timeout	
	@javax.ejb.TransactionAttribute(value=javax.ejb.TransactionAttributeType.SUPPORTS)
	public void timeOutHandler(Timer timer) throws Exception {
		try
		{
			String hostName = Inet4Address.getLocalHost().getHostName();
			long now = System.currentTimeMillis();
			long timeout = now - 300000; // 5 minutes ago
			ConfigurationService svc = ServiceLocator.instance().getConfigurationService();
			Configuration cfg = svc.findParameterByNameAndNetworkName("addon.report.server", null);
			if (cfg == null)
			{
				cfg = new Configuration();
				cfg.setCode("addon.report.server");
				cfg.setValue(hostName+" "+now);
				svc.create(cfg);
			}
			else
			{
				String [] split = cfg.getValue().isEmpty() ? new String[0]: cfg.getValue().split(" ");
				if (split.length != 2 ||
						split[0].equals(hostName) ||
						Long.parseLong(split[1]) < timeout)
				{
					log.warn("Setting reports server: "+hostName);
					cfg.setValue(hostName+" "+now);
					svc.update(cfg);
				}
						
			}
		} catch (Throwable e) {
			e.printStackTrace();
			log.warn("Error on report cluster manager", e);
		}
	}
	
}
