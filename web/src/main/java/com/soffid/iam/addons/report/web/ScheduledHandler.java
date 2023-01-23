package com.soffid.iam.addons.report.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Window;

import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.api.Group;
import com.soffid.iam.api.Role;
import com.soffid.iam.api.User;
import com.soffid.iam.web.component.FrameHandler;
import com.soffid.iam.web.component.Identity;
import com.soffid.iam.web.popup.IdentityHandler;

import es.caib.seycon.ng.exception.InternalErrorException;
import es.caib.zkib.binder.BindContext;
import es.caib.zkib.component.DataModel;
import es.caib.zkib.component.DataTable;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.XPathUtils;

public class ScheduledHandler extends FrameHandler {
	com.soffid.iam.addons.report.web.Messages msg = new com.soffid.iam.addons.report.web.Messages();
	private String reportIdString;

	public ScheduledHandler() throws InternalErrorException {
		super();
        HttpServletRequest req = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
        reportIdString = req.getParameter("id");
	}

	@Override
	public void afterCompose() {
	}

	@Override
	public void onPageAttached(Page newpage, Page oldpage) {
		super.onPageAttached(newpage, oldpage);
		setVariable("msg", msg, true);
	}
	
	@Override
	public void showDetails() {
		Window w = (Window) getFellow("detailWindow");
		w.doHighlighted();
	}
	
	public void addIdentity(Event ev) throws IOException {
		IdentityHandler.selectIdentity( Labels.getLabel("accounts.addIdentity"),
				new com.soffid.iam.web.component.Identity.Type[] {
						com.soffid.iam.web.component.Identity.Type.USER,
						com.soffid.iam.web.component.Identity.Type.GROUP,
						com.soffid.iam.web.component.Identity.Type.ROLE
				},
				ev.getTarget(), "onAddIdentity");
	}

	public void addIdentity2(Event ev) throws Exception {
		Component form = getFellow("detailWindow").getFellow("detailForm");
		BindContext ctx = XPathUtils.getComponentContext(form);
		List<Identity> identities = (List<Identity>) ev.getData();
		for (Identity identity: identities) {
			Object o = identity.getObject();
			if (o instanceof Group) {
				XPathUtils.createPath(ctx.getDataSource(), XPathUtils.concat(ctx.getXPath(),"target"), ((Group) o).getName());
			}
			if (o instanceof User) {
				XPathUtils.createPath(ctx.getDataSource(), XPathUtils.concat(ctx.getXPath(),"target"), ((User) o).getUserName());
			}
			if (o instanceof Role) {
				Role r = (Role) o;
				XPathUtils.createPath(ctx.getDataSource(), XPathUtils.concat(ctx.getXPath(),"target"), r.getName()+" @ "+r.getSystem());
			}
		}
	}

	public void acceptDetails(Event event) throws CommitException {
		getModel().commit();
		getModel().refresh();
		getFellow("detailWindow").setVisible(false);
		((DataTable)getListbox()).setSelectedIndex(-1);
	}

	public void cancelDetails(Event event) throws CommitException {
		getModel().refresh();
		getFellow("detailWindow").setVisible(false);
		((DataTable)getListbox()).setSelectedIndex(-1);
	}
	
	@Override
	public DataModel getModel() {
		return (DataModel) Path.getComponent("/model");
	}
	
	public void newReport(Event event) {
		Events.sendEvent(new Event("onStart", getPage().getFellow("wizardEmbed").getFellow("handler")));

	}

}
