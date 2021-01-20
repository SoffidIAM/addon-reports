package com.soffid.iam.addons.report.web;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Filedownload;

import com.soffid.iam.doc.exception.DocumentBeanException;
import com.soffid.iam.web.component.FrameHandler;

import es.caib.seycon.ng.exception.InternalErrorException;
import es.caib.zkib.datasource.XPathUtils;

public class ExecutedReportHandler extends FrameHandler {
	com.soffid.iam.addons.report.web.Messages msg = new com.soffid.iam.addons.report.web.Messages();

	public ExecutedReportHandler() throws InternalErrorException {
		super();
	}

	public void download (Event event) throws IllegalArgumentException, NamingException, CreateException, InternalErrorException, DocumentBeanException
	{
		Component c = event.getTarget();
		es.caib.zkib.binder.BindContext ctx = es.caib.zkib.datasource.XPathUtils.getComponentContext(c);
		Boolean error = (Boolean) XPathUtils.eval(ctx, "@error");
		if (error)
		{
			String errorMsg = (String) XPathUtils.eval(ctx, "@errorMessage");
			es.caib.zkib.zkiblaf.Missatgebox.avis(errorMsg, "ERROR");	
		}
		else
		{
			String doc = (String) XPathUtils.eval(ctx, "@pdfDocument");
			String name = (String) XPathUtils.eval(ctx, "@name");
			download (name+".pdf", doc, "pdf", "binary/octet-stream");
		}
	}
	
	public void downloadHtml (Event event) throws IllegalArgumentException, NamingException, CreateException, InternalErrorException, DocumentBeanException
	{
		Component c = event.getTarget();
		es.caib.zkib.binder.BindContext ctx = es.caib.zkib.datasource.XPathUtils.getComponentContext(c);
		Boolean error = (Boolean) XPathUtils.eval(ctx, "@error");
		if (error)
		{
			String errorMsg = (String) XPathUtils.eval(ctx, "@errorMessage");
			es.caib.zkib.zkiblaf.Missatgebox.avis(errorMsg, "ERROR");	
		}
		else
		{
			String doc = (String) XPathUtils.eval(ctx, "@htmlDocument");
			String name = (String) XPathUtils.eval(ctx, "@name");
			download (name+".zip", doc, "zip", "binary/x-zip-compressed");
		}
	}
	
	public void downloadXml (Event event) throws IllegalArgumentException, NamingException, CreateException, InternalErrorException, DocumentBeanException
	{
		Component c = event.getTarget();
		es.caib.zkib.binder.BindContext ctx = es.caib.zkib.datasource.XPathUtils.getComponentContext(c);
		Boolean error = (Boolean) XPathUtils.eval(ctx, "@error");
		if (error)
		{
			String errorMsg = (String) XPathUtils.eval(ctx, "@errorMessage");
			es.caib.zkib.zkiblaf.Missatgebox.avis(errorMsg, "ERROR");	
		}
		else
		{
			String doc = (String) XPathUtils.eval(ctx, "@xmlDocument");
			String name = (String) XPathUtils.eval(ctx, "@name");
			download (name+".xml", doc, "xml", "binary/octet-stream");
		}
	}
	
	public void downloadAsCsv (Event event) throws IllegalArgumentException, NamingException, CreateException, InternalErrorException, DocumentBeanException
	{
		Component c = event.getTarget();
		es.caib.zkib.binder.BindContext ctx = es.caib.zkib.datasource.XPathUtils.getComponentContext(c);
		Boolean error = (Boolean) XPathUtils.eval(ctx, "@error");
		if (error)
		{
			String errorMsg = (String) XPathUtils.eval(ctx, "@errorMessage");
			es.caib.zkib.zkiblaf.Missatgebox.avis(errorMsg, "ERROR");	
		}
		else
		{
			String doc = (String) XPathUtils.eval(ctx, "@csvDocument");
			String name = (String) XPathUtils.eval(ctx, "@name");
			download (name+".csv", doc, "csv", "text/csv");
		}
	}

	public void downloadXls (Event event) throws IllegalArgumentException, NamingException, CreateException, InternalErrorException, DocumentBeanException
	{
		Component c = event.getTarget();
		es.caib.zkib.binder.BindContext ctx = es.caib.zkib.datasource.XPathUtils.getComponentContext(c);
		Boolean error = (Boolean) XPathUtils.eval(ctx, "@error");
		if (error)
		{
			String errorMsg = (String) XPathUtils.eval(ctx, "@errorMessage");
			es.caib.zkib.zkiblaf.Missatgebox.avis(errorMsg, "ERROR");	
		}
		else
		{
			String doc = (String) XPathUtils.eval(ctx, "@xlsDocument");
			String name = (String) XPathUtils.eval(ctx, "@name");
			download (name+".xls", doc, "xls", "application/xls");
		}
	}

	public void download (String name, String doc, String format, String mimeFormat) throws NamingException, CreateException, IllegalArgumentException, InternalErrorException, DocumentBeanException
	{
		if (doc == null)
		{
			es.caib.zkib.zkiblaf.Missatgebox.avis(msg.get("reportIsEmpty"));
		}
		else
		{
			com.soffid.iam.doc.service.ejb.DocumentService svc =
				com.soffid.iam.EJBLocator.getDocumentService();
			svc.openDocument(new com.soffid.iam.doc.api.DocumentReference(doc));
			org.zkoss.util.media.AMedia media = new org.zkoss.util.media.AMedia(
					name, format, mimeFormat, new com.soffid.iam.doc.api.DocumentInputStream(svc) );
			Filedownload.save(media);
		}
	}
	
	public void newReport(Event event) {
		Events.sendEvent(new Event("onStart", getPage().getFellow("wizardEmbed").getFellow("handler")));

	}

	@Override
	public void onPageAttached(Page newpage, Page oldpage) {
		super.onPageAttached(newpage, oldpage);
		setVariable("msg", msg, true);
	}
}
