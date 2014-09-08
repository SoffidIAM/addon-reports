package com.soffid.iam.addons.report.web;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Longbox;
import org.zkoss.zul.Textbox;

import com.soffid.iam.addons.report.api.ParameterType;

import es.caib.seycon.ng.exception.InternalErrorException;
import es.caib.zkib.datasource.XPathUtils;

public class ParamValueComponent extends Div {

	public void createParam ()
	{
		@SuppressWarnings("unchecked")
		List<Component> children = getChildren();
		while (! children.isEmpty())
		{
			children.get(0).setParent(null);
		}
		
		
		if (getPage() == null)
			return;
		
		es.caib.zkib.binder.BindContext ctx = XPathUtils.getComponentContext(this);

		ParameterType type = (ParameterType) XPathUtils.getValue(ctx, "@type");
		if (type.equals(ParameterType.DATE_PARAM))
		{
			Datebox db = new Datebox();
			db.setParent(this);
		} else if (type.equals(ParameterType.BOOLEAN_PARAM))
		{
			Checkbox cb = new Checkbox();
			cb.setParent(this);
		} else if (type.equals(ParameterType.DOUBLE_PARAM))
		{
			Doublebox db = new Doublebox ();
			db.setParent(this);
		} else if (type.equals(ParameterType.STRING_PARAM))
		{
			Textbox tb = new Textbox();
			tb.setWidth("70%");
			tb.setParent (this);
		} else if (type.equals(ParameterType.LONG_PARAM))
		{
			Longbox lb = new Longbox();
			lb.setParent (this);
		}
		else if (type.equals(ParameterType.DISPATCHER_PARAM))
		{
			Combobox cb = new Combobox();
			cb.setWidth("70%");
			cb.setParent (this);
			try {
				for (es.caib.seycon.ng.comu.Dispatcher di : es.caib.seycon.ng.ServiceLocator
						.instance().getDispatcherService()
						.findAllActiveDispatchers()) {
					cb.appendItem(di.getCodi());
				}
			} catch (Exception e) {
				// Ignore exception
			}
		}
		else if (type.equals(ParameterType.GROUP_PARAM))
		{
			Textbox tb = new Textbox();
			tb.setWidth("70%");
			tb.setParent (this);
			es.caib.zkib.zkiblaf.ImageClic ic = new es.caib.zkib.zkiblaf.ImageClic();
			ic.setParent(this);
			ic.setSrc("/img/group.png");
			ic.addEventListener("onClick", new org.zkoss.zk.ui.event.EventListener()
				{
					public void onEvent(Event event) throws Exception
					{
						Desktop desktop = Executions.getCurrent().getDesktop();
      					desktop.getPage("grupsLlista").setAttribute("tipus", "");
       					desktop.getPage("grupsLlista").setAttribute("llistaObsolets", false);
	   					Events.postEvent("onInicia",
   							desktop.getPage("grupsLlista").getFellow("esquemaLlista"), event.getTarget());
					}
				}
			);
			ic.addEventListener("onActualitza", new org.zkoss.zk.ui.event.EventListener()
				{
					public void onEvent(Event event) throws Exception
					{
						String [] data = (String[]) event.getData();
   	   					String group = data[0];
   	   					((Textbox)event.getTarget().getPreviousSibling()).setValue(group);
					}
				}
			);
			
		}
		else if (type.equals(ParameterType.IS_PARAM))
		{
			Textbox tb = new Textbox();
			tb.setWidth("70%");
			tb.setParent (this);
			es.caib.zkib.zkiblaf.ImageClic ic = new es.caib.zkib.zkiblaf.ImageClic();
			ic.setParent(this);
			ic.setSrc("/img/auditoria.png");
			ic.addEventListener("onClick", new org.zkoss.zk.ui.event.EventListener()
				{
					public void onEvent(Event event) throws Exception
					{
						Desktop desktop = Executions.getCurrent().getDesktop();
	   					Events.postEvent("onInicia",
   							desktop.getPage("aplicacionsLlista").
   								getFellow("esquemaLlista"), event.getTarget());
					}
				}
			);
			ic.addEventListener("onActualitza", new org.zkoss.zk.ui.event.EventListener()
				{
					public void onEvent(Event event) throws Exception
					{
   	   					String app = (String) event.getData();
   	   					((Textbox)event.getTarget().getPreviousSibling()).setValue(app);
					}
				}
			);
			
		}
		else if (type.equals(ParameterType.ROLE_PARAM))
		{
			Textbox tb = new Textbox();
			tb.setWidth("70%");
			tb.setParent (this);
			es.caib.zkib.zkiblaf.ImageClic ic = new es.caib.zkib.zkiblaf.ImageClic();
			ic.setSrc("/img/auditoria.png");
			ic.setParent(this);
			ic.addEventListener("onClick", new org.zkoss.zk.ui.event.EventListener()
				{
					public void onEvent(Event event) throws Exception
					{
						Desktop desktop = Executions.getCurrent().getDesktop();
	   					desktop.getPage("rolsLlista").setAttribute("tipus", "cap");
						desktop.getPage("rolsLlista").setAttribute("mostraGestionableWF",
								"true");//perquè mostre rols gestionableWF	
						desktop.getPage("rolsLlista").setAttribute("usuari", ""); //??	
						Events.postEvent("onInicia", desktop.getPage("rolsLlista")
								.getFellow("esquemaLlista"), event.getTarget());
					}
				}
			);
			ic.addEventListener("onActualitza", new org.zkoss.zk.ui.event.EventListener()
				{
					public void onEvent(Event event) throws Exception
					{
						String [] data = (String[]) event.getData();
	   					String role = (String) data[0];
	   					String system = (String) data[5];
   	   					((Textbox)event.getTarget().getPreviousSibling()).setValue(role+"@"+system);
					}
				}
			);
		}
		else if (type.equals(ParameterType.USER_PARAM))
		{
			Textbox tb = new Textbox();
			tb.setWidth("70%");
			tb.setParent (this);
			es.caib.zkib.zkiblaf.ImageClic ic = new es.caib.zkib.zkiblaf.ImageClic();
			ic.setSrc("/img/user.png");
			ic.setParent(this);
			ic.addEventListener("onClick", new org.zkoss.zk.ui.event.EventListener()
				{
					public void onEvent(Event event) throws Exception
					{
						Desktop desktop = Executions.getCurrent().getDesktop();
						Events.postEvent("onInicia", desktop.getPage("usuarisLlista")
								.getFellow("esquemaLlista"), event.getTarget());
					}
				}
			);
			ic.addEventListener("onActualitza", new org.zkoss.zk.ui.event.EventListener()
				{
					public void onEvent(Event event) throws Exception
					{
						String [] data = (String[]) event.getData();
	   					String user = data[0];
   	   					((Textbox)event.getTarget().getPreviousSibling()).setValue(user);
					}
				}
			);
		}
	}
	
	public void onCreate ()
	{
		createParam();
	}

	@Override
	public void setPage(Page page) {
		super.setPage(page);
		createParam();
	}

	@Override
	public void setParent(Component parent) {
		super.setParent(parent);
		createParam();
	}
	
	
}
