/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2019 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaspersoft.studio.data.soffid;

import net.sf.jasperreports.data.DataAdapter;
import net.sf.jasperreports.data.DataAdapterContributorFactory;
import net.sf.jasperreports.data.DataAdapterService;
import net.sf.jasperreports.data.DataAdapterServiceFactory;
import net.sf.jasperreports.data.DefaultDataAdapterServiceFactory;
import net.sf.jasperreports.data.bean.BeanDataAdapter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.ParameterContributorContext;


/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 */
public class SoffidDataAdapterServiceFactory implements DataAdapterContributorFactory, DataAdapterServiceFactory
{

	/**
	 *
	 */
	private static final SoffidDataAdapterServiceFactory INSTANCE = new SoffidDataAdapterServiceFactory();

	/**
	 *
	 */
	private SoffidDataAdapterServiceFactory()
	{
		System.out.println("**************** data adapter service factory");
	}

	/**
	 *
	 */
	public static SoffidDataAdapterServiceFactory getInstance()
	{
		System.out.println("**************** Creating data adapter service factory");
		return INSTANCE;
	}
	
	@Override
	public DataAdapterService getDataAdapterService(ParameterContributorContext context, DataAdapter dataAdapter)
	{
		//JasperReportsContext jasperReportsContext = context.getJasperReportsContext();
		DataAdapterService dataAdapterService = null;
		
		System.out.println("**************** Getting data adapter service");
		if (dataAdapter instanceof SoffidDataAdapterImpl)
		{
			System.out.println("**************** Returning Soffid Data Adapter service");
			dataAdapterService = new SoffidDataAdapterService(context, dataAdapter);
		}
		else
		{
			DefaultDataAdapterServiceFactory.getInstance().getDataAdapterService(context, dataAdapter);
		}
		return dataAdapterService;
	}
	
	/**
	 * @deprecated Replaced by {@link #getDataAdapterService(ParameterContributorContext, DataAdapter)}.
	 */
	@Override
	public DataAdapterService getDataAdapterService(JasperReportsContext jasperReportsContext, DataAdapter dataAdapter)
	{
		System.out.println("**************** Creating data adapter service factory 2");
		return getDataAdapterService(new ParameterContributorContext(jasperReportsContext, null, null), dataAdapter);
	}
  
}
