package com.soffid.iam.addons.report.service.timer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;

import org.apache.commons.logging.LogFactory;

import com.soffid.iam.ServiceLocator;
import com.soffid.iam.addons.report.api.ScheduledReport;
import com.soffid.iam.addons.report.service.ReportSchedulerService;
import com.soffid.iam.addons.report.service.ReportService;
import com.soffid.iam.api.Configuration;
import com.soffid.iam.service.ConfigurationService;

import es.caib.seycon.ng.exception.InternalErrorException;

public class ReportSchedulerTimer implements Runnable {
	private static ReportSchedulerTimer schedulerThread = null;
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass());
	
	public void run () {
		try
		{
			if (isMaster ())
			{
				Date next = null;
				Date now = new Date();
				ReportSchedulerService reportSchedulerService = (ReportSchedulerService) ServiceLocator.instance().getService( ReportSchedulerService.SERVICE_NAME );
				for (ScheduledReport sr : reportSchedulerService .getScheduledReport())
				{
					if (sr.getNextExecution().before(now))
					{
						reportSchedulerService.startReport(sr);
					}
					else if (next == null || next.after(sr.getNextExecution()))
						next = sr.getNextExecution();
				}
			}
		} catch (Throwable e) {
			log.warn("Error on report scheduler", e);
		}
	}

	public ReportSchedulerTimer ()
	{
		
	}
	
	public boolean isMaster () throws InternalErrorException, UnknownHostException
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


	Date searchNextScheduledReport() throws InternalErrorException {
		Date first = null;
		ReportSchedulerService svc = (ReportSchedulerService) ServiceLocator.instance().getService(ReportSchedulerService.SERVICE_NAME);
		for (ScheduledReport sr : svc.getScheduledReport())
		{
			if (first == null || 
					sr.getNextExecution() != null &&
					sr.getNextExecution().before(first))
				first = sr.getNextExecution();
		}
		return first;
	}
}
