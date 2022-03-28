package com.jaspersoft.studio.data.soffid;

import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.extensions.ExtensionsRegistry;
import net.sf.jasperreports.extensions.ListExtensionRegistry;
import net.sf.jasperreports.util.CastorMapping;
import net.sf.jasperreports.util.CastorMappingExtensionsRegistryFactory;

public class SoffidExtensionRegistry extends CastorMappingExtensionsRegistryFactory {

	@Override
	public ExtensionsRegistry createRegistry(String arg0, JRPropertiesMap arg1) {
		ListExtensionRegistry<CastorMapping> prev =  (ListExtensionRegistry<CastorMapping>) super.createRegistry(arg0, arg1);
		return prev;
	}

}
