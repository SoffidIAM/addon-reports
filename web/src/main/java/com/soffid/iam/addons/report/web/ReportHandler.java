package com.soffid.iam.addons.report.web;

import javax.servlet.http.HttpServletRequest;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Tab;

import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ScheduledReport;
import com.soffid.iam.web.component.FrameHandler;

import es.caib.seycon.ng.exception.InternalErrorException;
import es.caib.zkib.component.DataTable;
import es.caib.zkib.component.Embed;
import es.caib.zkib.datamodel.DataNode;
import es.caib.zkib.datamodel.DataNodeCollection;

public class ReportHandler extends FrameHandler {
	com.soffid.iam.addons.report.web.Messages msg = new com.soffid.iam.addons.report.web.Messages();
	private String reportIdString;
	private String wizard;

	public ReportHandler() throws InternalErrorException {
		super();
        HttpServletRequest req = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
        reportIdString = req.getParameter("id");
        wizard = req.getParameter("wizard");
	}

	@Override
	public void afterCompose() {
		super.afterCompose();
		
		if ("openSchedule".equals(wizard)) {
			Tab tab = (Tab) getFellow("schedule_tab");
			tab.setSelected(true);
			Events.sendEvent(new Event("onSelect", tab));
			Component e = (Component) tab.getFellow("scheduled");
 			ScheduledHandler h = (ScheduledHandler) e.getFellow("frame");
			DataTable table = (DataTable) h.getFellow("listbox");
			DataNodeCollection c = (DataNodeCollection) getModel().getJXPathContext().getValue("/scheduledReport");
			for (int i = 0; i < c.getSize(); i++) {
				DataNode dn = (DataNode) c.getDataModel(i);
				if (!dn.isDeleted()) {
					ScheduledReport sr = (ScheduledReport) dn.getInstance();
					if (sr.getId().toString().equals(reportIdString)) {
						table.setSelectedIndex(i);
						h.showDetails();
					}
				}
			}
		}
		else if (reportIdString != null)
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
