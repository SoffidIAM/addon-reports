package com.soffid.iam.addons.report.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.naming.NamingException;

import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

import com.soffid.iam.addons.report.service.ejb.ReportService;
import com.soffid.iam.api.Group;
import com.soffid.iam.api.Role;
import com.soffid.iam.api.User;
import com.soffid.iam.utils.Security;
import com.soffid.iam.web.component.FrameHandler;
import com.soffid.iam.web.component.Identity;
import com.soffid.iam.web.popup.FileUpload2;
import com.soffid.iam.web.popup.IdentityHandler;

import es.caib.seycon.ng.exception.InternalErrorException;
import es.caib.zkib.binder.BindContext;
import es.caib.zkib.component.DataModel;
import es.caib.zkib.component.DataTable;
import es.caib.zkib.component.Form;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.XPathUtils;

public class UploadedReportHandler extends FrameHandler {
	public UploadedReportHandler() throws InternalErrorException {
		super();
	}

	public void upload ()
	{
		FileUpload2.get( event -> {
			try {
				Media amedia = ((UploadEvent)event).getMedia();
				com.soffid.iam.addons.report.service.ejb.ReportService ejb = (ReportService) new javax.naming.InitialContext().lookup
						( com.soffid.iam.addons.report.service.ejb.ReportServiceHome.JNDI_NAME );
				java.io.InputStream in;
				if (amedia.isBinary())
					in = amedia.getStreamData();
				else if (! amedia.isBinary() && amedia.inMemory())
					in = new java.io.ByteArrayInputStream (amedia.getStringData().getBytes());
				else
					throw new UiException("unsupported text file");
				ejb.upload(in);
				getModel().refresh();
			} catch (Exception e) {
				throw new UiException(e);
			}
		});
	}

	public void download () throws FileNotFoundException, NamingException, InternalErrorException
	{
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		File f = new File (tmpDir, "ireport-addon.jar");
		com.soffid.iam.addons.report.service.ejb.ReportService ejb = (ReportService) new javax.naming.InitialContext().lookup
			( com.soffid.iam.addons.report.service.ejb.ReportServiceHome.JNDI_NAME );
		ejb.generateDevelopmentEnvironment(new java.io.FileOutputStream (f));

		org.zkoss.util.media.AMedia media = new org.zkoss.util.media.AMedia(f, "binary/octet-stream", null);
		
		Filedownload.save(media);
	}

	@Override
	protected DataModel getModel() {
		return (DataModel) getPage().getFellow("model");
	}

	public void showDetails(Event event) {
		if ( Security.isUserInRole("report:admin")) {
			Component table = getListbox();
			es.caib.zkib.binder.BindContext ctx = es.caib.zkib.datasource.XPathUtils.getComponentContext( table );
			Window w = (Window) getFellow("detailWindow");
//			String path = org.zkoss.zk.ui.Path.getPath(table)+":/";
//			((Form)w.getFellow("detailForm")).setDataPath(path);
			w.setTitle((String) es.caib.zkib.datasource.XPathUtils.eval(ctx, "@name"));
			w.doHighlighted();	
		}
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
		DataTable ctx = (DataTable) getListbox();
		List<Identity> identities = (List<Identity>) ev.getData();
		for (Identity identity: identities) {
			Object o = identity.getObject();
			if (o instanceof Group) {
				XPathUtils.createPath(ctx, "acl", ((Group) o).getName());
			}
			if (o instanceof User) {
				XPathUtils.createPath(ctx, "acl", ((User) o).getUserName());
			}
			if (o instanceof Role) {
				Role r = (Role) o;
				XPathUtils.createPath(ctx, "acl", r.getName()+" @ "+r.getSystem());
			}
		}
	}
	
	public void hideDetails() throws CommitException {
		Window w = (Window) getFellow("detailWindow");
		w.setVisible(false);
		super.hideDetails();
	}
}
