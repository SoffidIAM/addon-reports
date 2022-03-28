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
package com.soffid.iam.addons.report.service.jasper;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.ParseException;
import bsh.TargetError;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.query.AbstractQueryExecuterFactory;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.util.Designated;
import net.sf.jasperreports.properties.PropertyConstants;

/**
 * Query executer factory for HQL queries that uses Hibernate 3.
 * <p/>
 * The factory creates {@link net.sf.jasperreports.engine.query.JRHibernateQueryExecuter JRHqlExecuter}
 * query executers. 
 * 
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 */
public class JRHqlExecuterFactory extends AbstractQueryExecuterFactory 
{
	public Object[] getBuiltinParameters() {
		return null;
	}

	public JRQueryExecuter createQueryExecuter(
			JasperReportsContext jasperReportsContext, 
			JRDataset dataset, 
			Map<String,? extends JRValueParameter> parameters
			) throws JRException
	{		
		return new JRHqlExecuter(jasperReportsContext, dataset, parameters);
	}

	public boolean supportsQueryParameterType(String className) {
		return true;
	}
}
