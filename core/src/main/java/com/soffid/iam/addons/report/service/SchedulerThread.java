package com.soffid.iam.addons.report.service;

import java.util.Date;

import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.soffid.iam.addons.report.api.ScheduledReport;

public class SchedulerThread extends Thread {
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass());
	
	private ReportService reportService;
	private boolean end = false;
	private ReportSchedulerService reportSchedulerService;

	private ExecutorThread executorThread;

	public void end() {
		end  = true;
		this.notify();
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		reportService = (ReportService) applicationContext.getBean(ReportService.SERVICE_NAME);
		reportSchedulerService = (ReportSchedulerService) applicationContext.getBean(ReportSchedulerService.SERVICE_NAME);
	}

	@Override
	public void run() {
		while ( ! end )
		{
			Date next = null;
			Date now = new Date();
			try
			{
				for (ScheduledReport sr : reportSchedulerService.getScheduledReport())
				{
					if (sr.getNextExecution().after(now))
					{
						reportSchedulerService.startReport(sr);
						executorThread.newReportCreated();
					}
					else if (next == null || next.after(sr.getNextExecution()))
						next = sr.getNextExecution();
				}
				long millis = next.getTime() - now.getTime();
				if (millis > 120000) // 2 minutes 
					millis = 120000;
				wait (millis);
			} catch (Exception e) {
				log.warn("Error on report scheduler", e);
			}
		}
	}

	public void setExecutorThread(ExecutorThread executorThread) {
		this.executorThread = executorThread;
	}

}
