package com.soffid.iam.addons.report.service;

import java.util.Date;

import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.soffid.iam.addons.report.api.ScheduledReport;

public class SchedulerThread extends Thread {
	private static SchedulerThread schedulerThread = null;
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass());
	
	public ReportSchedulerService getReportSchedulerService() {
		return reportSchedulerService;
	}

	public void setReportSchedulerService(
			ReportSchedulerService reportSchedulerService) {
		this.reportSchedulerService = reportSchedulerService;
	}

	private boolean end = false;
	private ReportSchedulerService reportSchedulerService;

	private ExecutorThread executorThread;

	public void end() {
		end  = true;
		synchronized (this)
		{
			this.notify();
		}
	}

	@Override
	public void run() {
		setName ("ReportSchedulerThread");
		while ( ! end )
		{
			Date next = null;
			Date now = new Date();
			try
			{
				for (ScheduledReport sr : reportSchedulerService.getScheduledReport())
				{
					if (sr.getNextExecution().before(now))
					{
						reportSchedulerService.startReport(sr);
						executorThread.newReportCreated();
					}
					else if (next == null || next.after(sr.getNextExecution()))
						next = sr.getNextExecution();
				}
				long millis = next == null ? 120000 : next.getTime() - now.getTime();
				if (millis > 120000) // 2 minutes 
					millis = 120000;
				synchronized(this)
				{
					this.wait (millis);
				}
			} catch (Throwable e) {
				log.warn("Error on report scheduler", e);
				try {
					sleep(5000);
				} catch (InterruptedException e1) {
				}
			}
		}
		log.info("Finished.");
	}

	public void setExecutorThread(ExecutorThread executorThread) {
		this.executorThread = executorThread;
	}

	private SchedulerThread ()
	{
		
	}
	
	public static SchedulerThread getInstance ()
	{
		if (schedulerThread == null)
			schedulerThread  = new SchedulerThread();
		
		return schedulerThread;
	}
}
