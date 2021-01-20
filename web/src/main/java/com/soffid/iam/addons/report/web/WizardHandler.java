package com.soffid.iam.addons.report.web;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;

import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ParameterType;
import com.soffid.iam.addons.report.api.ScheduledReport;
import com.soffid.iam.addons.report.service.ejb.ReportService;
import com.soffid.iam.api.Group;
import com.soffid.iam.api.Role;
import com.soffid.iam.api.User;
import com.soffid.iam.doc.service.ejb.DocumentService;
import com.soffid.iam.web.component.CustomField3;
import com.soffid.iam.web.component.Identity;
import com.soffid.iam.web.component.InputField3;
import com.soffid.iam.web.component.ObjectAttributesDiv;
import com.soffid.iam.web.popup.IdentityHandler;

import es.caib.seycon.ng.exception.InternalErrorException;
import es.caib.zkib.binder.BindContext;
import es.caib.zkib.component.DataModel;
import es.caib.zkib.component.DataTable;
import es.caib.zkib.component.Div;
import es.caib.zkib.component.Wizard;
import es.caib.zkib.datamodel.DataNode;
import es.caib.zkib.datamodel.DataNodeCollection;
import es.caib.zkib.datasource.CommitException;
import es.caib.zkib.datasource.XPathUtils;
import es.caib.zkib.zkiblaf.Missatgebox;

public class WizardHandler extends Window {
	private ExecutedReport waitingForReport;

	public void afterCompose() {
		
	}

	public Wizard getWizard() {
		return (Wizard) getFellow("wizard");
	}
	
	public void doStart(Event event) {
		getWizard().setSelected(0);
		doHighlighted();
		DataTable lb = (DataTable) getFellow("listbox");
		lb.setSelectedIndex(-1);
		selectedReport(event);
		displayRemoveButton(getFellow("targetGrid"), false);
	}
	
	public void close() {
		setVisible(false);
	}

	public void selectedReport(Event event) {
		DataTable lb = (DataTable) getFellow("listbox");
		Button b = (Button) getFellow("selectReportButton");
		b.setDisabled(lb.getSelectedIndex() < 0);
	}
	
	public void selectReport(Event event) {
		DataTable lb = (DataTable) getFellow("listbox");
		if (lb.getSelectedIndex() >= 0)
			getWizard().next();
		
        String name = (String) lb.getJXPathContext().getValue("@name");
		CustomField3 scheduleTextbox = (CustomField3) getFellow("scheduleTextbox");
		scheduleTextbox.setValue(name);
        
		Radiogroup scheduleRadio = (Radiogroup) getFellow("scheduleRadio");
		scheduleRadio.setSelectedIndex(0);
		onEnableSchedule();
	}
	
	public void next(Event event) {
		if (validateAttributes(this))
			getWizard().next();
	}

	public boolean validateAttributes(Component form) {
		if (form == null || !form.isVisible()) return true;
		if (form instanceof ObjectAttributesDiv) {
			return ((ObjectAttributesDiv) form).validate();
		}
		if (form instanceof InputField3) {
			InputField3 inputField = (InputField3)form;
			if (inputField.isReadonly() || inputField.isDisabled())
				return true;
			else
				return inputField.attributeValidateAll();
		}
		boolean ok = true;
		for (Component child = form.getFirstChild(); child != null; child = child.getNextSibling())
			if (! validateAttributes(child))
				ok = false;
		return ok;
	}


	public void back(Event event) {
		getWizard().previous();
	}
	
	public void onEnableSchedule ()
	{
		Radiogroup scheduleRadio = (Radiogroup) getFellow("scheduleRadio");
		Radio radioschedule = (Radio) getFellow("radioschedule");
		boolean sch = scheduleRadio.getSelectedItem() == radioschedule;
		
		CustomField3 schMonth = (CustomField3) getFellow("schMonth");
		CustomField3 schDayOfMonth = (CustomField3) getFellow("schDayOfMonth");
		CustomField3 schDayOfWeek = (CustomField3) getFellow("schDayOfWeek");
		CustomField3 schMinute = (CustomField3) getFellow("schMinute");
		CustomField3 schHour = (CustomField3) getFellow("schHour");
		CustomField3 scheduleTextbox = (CustomField3) getFellow("scheduleTextbox");
		
		schMonth.setDisabled( ! sch );
		schDayOfMonth.setDisabled( ! sch );
		schDayOfWeek.setDisabled(! sch );
		schHour.setDisabled( !sch );
		schMinute.setDisabled( !sch );
		scheduleTextbox.setDisabled( !sch );
		
		getFellow("targetDiv").setVisible( sch );
		
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
		BindContext ctx = XPathUtils.getComponentContext(getFellow("targetDiv"));
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
	

	public void finish (Event event) throws Exception {
		DataModel model = (DataModel) Path.getComponent("/model");
        model.commit();
        com.soffid.iam.addons.report.api.ScheduledReport report = (ScheduledReport) model.getJXPathContext().getValue("/newReport[1]/instance");
        com.soffid.iam.addons.report.service.ejb.ReportService reportService =
        		(ReportService) new javax.naming.InitialContext()
			.lookup (com.soffid.iam.addons.report.service.ejb.ReportServiceHome.JNDI_NAME);
        // Assign params
        report.setParams(new LinkedList<>());
        Grid params = (Grid) getFellow("params");
        for (Row row: (List<Row>)params.getRows().getChildren())
        {
        	com.soffid.iam.addons.report.api.ParameterValue pv = new com.soffid.iam.addons.report.api.ParameterValue ();
        	es.caib.zkib.binder.BindContext ctx = XPathUtils.getComponentContext(row);
        	pv.setName((String) XPathUtils.eval(ctx, "@name"));
        	pv.setType((ParameterType) XPathUtils.eval(ctx, "@type"));
        	Component d = row.getFirstChild();
        	InputField3 inputField  = (InputField3) d.getFirstChild();
       		pv.setValue( inputField.getValue() );
        	report.getParams().add(pv);
        }
        // Assign targets
        report.setTarget(new LinkedList());
        DataNodeCollection coll = (DataNodeCollection) model.getJXPathContext().getValue("/newReport[1]/target");
        for (int i  = 0; i < coll.size(); i++) 
        {
        	DataNode node = (DataNode) coll.getDataModel(i);
        	if ( node != null &&  !node.isDeleted()) {
	        	String name = (String) node.getInstance();
	        	report.getTarget().add(name);
        	}
        }
        // Assign report i
        DataTable  dt = (DataTable)getFellow("listbox");
        Long reportId = (Long) dt.getJXPathContext().getValue("@id");
        report.setReportId(reportId);
		Radiogroup scheduleRadio = (Radiogroup) getFellow("scheduleRadio");
		Radio radioschedule = (Radio) getFellow("radioschedule");
        if (scheduleRadio.getSelectedItem() == radioschedule)
        {
        	report.setScheduled(true);
        	report = reportService.create(report);
        	setVisible(false);
        	es.caib.zkib.zkiblaf.Missatgebox.avis(new Messages().get("msg.scheduled"));
        }
        else
        {
        	getWizard().next();
        	report.setTarget(new LinkedList());
        	report.getTarget().add( es.caib.seycon.ng.utils.Security.getCurrentUser() );
        	report.setScheduled(false);
        	waitingForReport = reportService.launchReport(report);
        	Timer t = (Timer) getFellow("timer");
        	t.start();
        }
	}
	
 	public void waitForReport(Event event) throws Exception
 	{
        com.soffid.iam.addons.report.service.ejb.ReportService reportService =
        		(ReportService) new javax.naming.InitialContext()
			.lookup (com.soffid.iam.addons.report.service.ejb.ReportServiceHome.JNDI_NAME);
        waitingForReport = reportService.getExecutedReportStatus(waitingForReport.getId());
    	Timer t = (Timer) getFellow("timer");
 		if (waitingForReport.isError())
 		{
 			t.stop();
 			setVisible(false);
 			es.caib.zkib.zkiblaf.Missatgebox.avis(waitingForReport.getErrorMessage(), new Messages().get("result.failed"));
 		}
 		else if (waitingForReport.isDone())
 		{
 			DataModel model = (DataModel) Path.getComponent("/model");
 			DataNodeCollection coll = (DataNodeCollection) model.getJXPathContext().getValue("/executedReport");
			coll.refresh();
 			t.stop();
 			setVisible(false);
 			if (waitingForReport.getPdfDocument() == null)
 			{
					es.caib.zkib.zkiblaf.Missatgebox.avis(new Messages().get("reportIsEmpty"));
 				
 			} else {
 				
				com.soffid.iam.doc.service.ejb.DocumentService svc =
							(DocumentService) new javax.naming.InitialContext()
					.lookup (com.soffid.iam.doc.service.ejb.DocumentServiceHome.JNDI_NAME);
					svc.openDocument(new com.soffid.iam.doc.api.DocumentReference(waitingForReport.getPdfDocument()));

					org.zkoss.util.media.AMedia media = new org.zkoss.util.media.AMedia(
							waitingForReport.getName()+".pdf", "pdf", "binary/octet-stream", 
							new com.soffid.iam.doc.api.DocumentInputStream(svc) );
					
					Filedownload.save(media);
					
 			}

 		}
 	}

	public void displayRemoveButton(Component lb, boolean display) {
		HtmlBasedComponent d = (HtmlBasedComponent) lb.getNextSibling();
		if (d != null && d instanceof Div) {
			d =  (HtmlBasedComponent) d.getFirstChild();
			if (d != null && "deleteButton".equals(d.getSclass())) {
				d.setVisible(display);
			}
		}
	}
	
	public void multiSelect(Event event) {
		DataTable lb = (DataTable) event.getTarget();
		displayRemoveButton( lb, lb.getSelectedIndexes() != null && lb.getSelectedIndexes().length > 0);
	}

	public void deleteSelected(Event event0) {
		Component b = event0.getTarget();
		final Component lb = b.getParent().getPreviousSibling();
		if (lb instanceof DataTable) {
			final DataTable dt = (DataTable) lb;
			if (dt.getSelectedIndexes() == null || dt.getSelectedIndexes().length == 0) return;
			String msg = dt.getSelectedIndexes().length == 1 ? 
					Labels.getLabel("common.delete") :
					String.format(Labels.getLabel("common.deleteMulti"), dt.getSelectedIndexes().length);
				
			Missatgebox.confirmaOK_CANCEL(msg, 
					(event) -> {
						if (event.getName().equals("onOK")) {
							dt.delete();
							displayRemoveButton(lb, false);
						}
					});
		}
	}
}
