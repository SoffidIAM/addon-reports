package com.soffid.iam.addons.report.service.ejb;

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

@Singleton(name="ReportSchedulerBean")
@Startup
@javax.ejb.TransactionManagement(value=javax.ejb.TransactionManagementType.CONTAINER)
@javax.ejb.TransactionAttribute(value=javax.ejb.TransactionAttributeType.SUPPORTS)
public class ReportSchedulerBean {
	private static ReportSchedulerBean schedulerThread = null;
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass());
	
	@Resource
	private SessionContext context;

	@EJB
	private ReportExecutor reportExecutor;
	
	
	@PostConstruct
	public void init() throws Exception {
		log.info("Started report scheduler bean");
		context.getTimerService().createTimer(120000, 120000, "Schedule report");
	}

	@Timeout	
	@javax.ejb.TransactionAttribute(value=javax.ejb.TransactionAttributeType.REQUIRES_NEW)
	public void timeOutHandler(Timer timer) throws Exception {
		if (isMaster ())
		{
			Date next = null;
			Date now = new Date();
			try
			{
				ReportSchedulerService reportSchedulerService = (ReportSchedulerService) ServiceLocator.instance().getService( ReportSchedulerService.SERVICE_NAME );
				ReportService reportService = (ReportService) ServiceLocator.instance().getService( ReportService.SERVICE_NAME );
				for (ScheduledReport sr : reportSchedulerService .getScheduledReport())
				{
					if (sr.getNextExecution().before(now))
					{
						reportSchedulerService.startReport(sr);
//						reportExecutor.newReportCreated();
					}
					else if (next == null || next.after(sr.getNextExecution()))
						next = sr.getNextExecution();
				}
			} catch (Throwable e) {
				log.warn("Error on report scheduler", e);
			}
		}
	}

	public ReportSchedulerBean ()
	{
		
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
}
