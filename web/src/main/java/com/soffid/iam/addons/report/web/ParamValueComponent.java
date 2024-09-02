package com.soffid.iam.addons.report.web;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zul.Div;

import com.soffid.iam.addons.report.api.ParameterType;
import com.soffid.iam.api.DataType;
import com.soffid.iam.web.component.InputField3;

import es.caib.seycon.ng.comu.TypeEnumeration;
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
			dt.setMultiValued(Boolean.TRUE.equals(XPathUtils.eval(ctx, "@multi")) );
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
