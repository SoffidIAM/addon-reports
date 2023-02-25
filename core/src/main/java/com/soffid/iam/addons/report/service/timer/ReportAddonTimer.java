package com.soffid.iam.addons.report.service.timer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.soffid.iam.ServiceLocator;
import com.soffid.iam.api.Tenant;
import com.soffid.iam.utils.Security;

import es.caib.seycon.ng.exception.InternalErrorException;

public class ReportAddonTimer implements Runnable {
	boolean started = false;
	boolean finished = false;
	ReportSchedulerTimer scheduler = new ReportSchedulerTimer();
	ClusterManagerTimer cluster = new ClusterManagerTimer();
	ReportExecutorTimer executor = new ReportExecutorTimer();
	Object semaphore = new Object();
	Log log = LogFactory.getLog(getClass());
	static Map<String, ReportAddonTimer> timers = new HashMap<>();
		
	public static ReportAddonTimer instance() throws InternalErrorException {
		ReportAddonTimer instance = timers.get(Security.getCurrentTenantName());
		if (instance == null) {
			instance = new ReportAddonTimer();
			timers.put(Security.getCurrentTenantName(), instance);
		}
		return instance;
	}
	
	public synchronized void start() throws InternalErrorException {
		if (!started) {
			started = true;
			timers.put(Security.getCurrentTenantName(), this);
			new Thread(this).start();
		}
	}
	
	public void runNow() {
		synchronized(semaphore)
		{
			semaphore.notifyAll();
		}
	}
	
	public void stop() {
		synchronized (semaphore) {
			if (started) {
				finished = true;
				semaphore.notifyAll();
			}
		}
	}

	@Override
	public void run() {
		log.info("Starting report executor daemon");
		try {
			while (!finished) {
				Date absoluteNext = null;
				try {
					for (Tenant tenant: ServiceLocator.instance().getTenantService().listTenants()) {
						Security.nestedLogin(tenant.getName(),"-", Security.ALL_PERMISSIONS);
						try {
							if (!finished)
								cluster.run();
							Date next = null;
							try {
								next = scheduler.searchNextScheduledReport();
								if (next != null && (absoluteNext == null || absoluteNext.after(next)))
									absoluteNext = next;
							} catch (InternalErrorException e1) {
							}
						} catch (Exception e) {
							log.warn("Error processing reports for tenant " + tenant.getName(), e);
						} finally {
							Security.nestedLogoff();
						}
						if (!finished)
							executor.run();
						if (!finished)
							scheduler.run();
					}
				} catch (Exception e) {
					log.warn("Error processing reports", e);
				}
				synchronized(semaphore) {
					try {
						// Two minutes
						long time = 10 * 60 * 1000;
						if ( absoluteNext != null && absoluteNext.getTime() - System.currentTimeMillis() < time)
							time = absoluteNext.getTime() - System.currentTimeMillis();
						semaphore.wait(time);
					} catch (InterruptedException e) {
					} 
				}
			}
		} finally {
			started = false;
		}
	}
	
	

}
