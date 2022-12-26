package com.soffid.iam.addons.report.service.timer;

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

public class ClusterManagerTimer implements Runnable  {
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass());

	public void run()  {
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
			log.warn("Error on report cluster manager", e);
		}
	}
	
}
