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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JRQuery;
import net.sf.jasperreports.engine.JRQueryChunk;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.fill.JRFillParameter;
import net.sf.jasperreports.engine.query.JRAbstractQueryExecuter;
import net.sf.jasperreports.engine.query.JRHibernateQueryExecuter;
import net.sf.jasperreports.engine.query.QueryExecutionContext;
import net.sf.jasperreports.engine.util.JRStringUtil;

/**
 * HQL query executer that uses Hibernate 3.
 * 
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 */
public class JRSoffidExecuter extends JRAbstractQueryExecuter
{

	private Map<String, ? extends JRValueParameter> params;

	public JRSoffidExecuter(
			QueryExecutionContext context, 
			JRDataset dataset, 
			Map<String, ? extends JRValueParameter> parametersMap
			)
	{
		super (context, dataset, parametersMap);
		this.params = parametersMap;
	}


	protected JRSoffidExecuter(JasperReportsContext jasperReportsContext, JRDataset dataset,
			Map<String, ? extends JRValueParameter> parametersMap) {
		super(jasperReportsContext, dataset, parametersMap);
		this.params = parametersMap;
	}

	public JRDataSource createDatasource() throws JRException {
		JRQuery query = dataset.getQuery();
		return new JRSoffidDataSource(getJasperReportsContext(), dataset, params);
	}

	public void close() {
	}

	public boolean cancelQuery() throws JRException {
		return false;
	}


	@Override
	protected String getParameterReplacement(String arg0) {
		return arg0;
	}


}
