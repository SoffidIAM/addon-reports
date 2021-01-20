package com.soffid.iam.addons.report.web;

import java.io.IOException;
import java.util.List;

import javax.ejb.CreateException;
import javax.naming.NamingException;

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
import com.soffid.iam.api.DataType;
import com.soffid.iam.web.component.CustomField3;
import com.soffid.iam.web.component.InputField3;

import es.caib.seycon.ng.comu.TypeEnumeration;
import es.caib.seycon.ng.exception.InternalErrorException;
import es.caib.zkib.datasource.XPathUtils;
import es.caib.zkib.jxpath.JXPathNotFoundException;

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

		try {
			String description = (String) XPathUtils.eval(ctx, "description");
			DataType dt = new DataType();
			dt.setLabel(description);
			ParameterType type = (ParameterType) XPathUtils.eval(ctx, "@type");
			if (type.equals(ParameterType.DATE_PARAM))
			{
				dt.setType(TypeEnumeration.DATE_TIME_TYPE);
			} else if (type.equals(ParameterType.BOOLEAN_PARAM))
			{
				dt.setType(TypeEnumeration.BOOLEAN_TYPE);
			} else if (type.equals(ParameterType.DOUBLE_PARAM))
			{
				dt.setType(TypeEnumeration.NUMBER_TYPE);
			} else if (type.equals(ParameterType.STRING_PARAM))
			{
				dt.setType(TypeEnumeration.STRING_TYPE);
			} else if (type.equals(ParameterType.LONG_PARAM))
			{
				dt.setType(TypeEnumeration.NUMBER_TYPE);
			}
			else if (type.equals(ParameterType.DISPATCHER_PARAM))
			{
				dt.setType(TypeEnumeration.STRING_TYPE);
				dt.setBuiltinHandler(SystemFieldHandler.class.getName());
			}
			else if (type.equals(ParameterType.GROUP_PARAM))
			{
				dt.setType(TypeEnumeration.GROUP_TYPE);
			}
			else if (type.equals(ParameterType.IS_PARAM))
			{
				dt.setType(TypeEnumeration.APPLICATION_TYPE);
			}
			else if (type.equals(ParameterType.ROLE_PARAM))
			{
				dt.setType(TypeEnumeration.ROLE_TYPE);
			}
			else if (type.equals(ParameterType.USER_PARAM))
			{
				dt.setType(TypeEnumeration.USER_TYPE);
			}
			else {
				dt.setType(TypeEnumeration.STRING_TYPE);
			}
			InputField3 inputField = new InputField3();
			inputField.setDataType(dt);
			inputField.setParent(this);
			inputField.setLabel(description);
			inputField.afterCompose();
			inputField.createField();
			
		} catch (Exception e) {
		}
	}
	
	public void onCreate ()
	{
		createParam();
	}

	
	@Override
	public void onPageAttached(Page newpage, Page oldpage) {
		super.onPageAttached(newpage, oldpage);
		createParam();
	}

}
