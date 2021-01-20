package com.soffid.iam.addons.report.web;

import javax.servlet.http.HttpServletRequest;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Events;

import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.web.component.FrameHandler;

import es.caib.seycon.ng.exception.InternalErrorException;

public class ReportHandler extends FrameHandler {
	com.soffid.iam.addons.report.web.Messages msg = new com.soffid.iam.addons.report.web.Messages();
	private String reportIdString;

	public ReportHandler() throws InternalErrorException {
		super();
        HttpServletRequest req = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
        reportIdString = req.getParameter("id");
	}

	@Override
	public void afterCompose() {
		if (reportIdString != null)
		{
		        ExecutedReport r = new  com.soffid.iam.addons.report.api.ExecutedReport();
		        r.setId( Long.parseLong (reportIdString) );
		        Events.postEvent("onStart", getFellow("wizardEmbed").getFellow("waitingForReport"), r);
		}

	}

	@Override
	public void onPageAttached(Page newpage, Page oldpage) {
		super.onPageAttached(newpage, oldpage);
		setVariable("msg", msg, true);
	}
}
