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
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.BigIntegerType;
import org.hibernate.type.BinaryType;
import org.hibernate.type.BooleanType;
import org.hibernate.type.CalendarType;
import org.hibernate.type.DateType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.FloatType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.ShortType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import com.soffid.iam.addons.report.service.SessionHolder;

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
public class JRHqlExecuter extends JRAbstractQueryExecuter
{

	public JRHqlExecuter(
			QueryExecutionContext context, 
			JRDataset dataset, 
			Map<String, ? extends JRValueParameter> parametersMap
			)
	{
		super (context, dataset, parametersMap);
	}


	protected JRHqlExecuter(JasperReportsContext jasperReportsContext, JRDataset dataset,
			Map<String, ? extends JRValueParameter> parametersMap) {
		super(jasperReportsContext, dataset, parametersMap);
	}

	public JRDataSource createDatasource() throws JRException {
		JRQuery query = dataset.getQuery();
		
		if (query != null)
		{
			parseQuery();
			Session session = SessionHolder.hibernateSession.get(); 
			Query q = session.createQuery(getQueryString());
			for (QueryParameter param: getCollectedParameters()) {
				JRValueParameter p = getValueParameter(param.getName());
				String hqlParam = getHqlParameterName(param.getName());
				Type type = getHibernateType(p.getValueClass());
				if (type == null) {
					java.lang.reflect.Type paramType = p.getValueClass();
					if (p.getValueClass().isAssignableFrom(Collection.class) && 
							(paramType instanceof ParameterizedType)) {
						java.lang.reflect.Type[] args = ((ParameterizedType)paramType).getActualTypeArguments();
						type = getHibernateType((Class) args[0]);
						if (type == null)
							throw new JRException("Unsupported parameter type "+p.getValueClassName()+" for "+p.getName());
						q.setParameterList(getHqlParameterName(param.getName()), (Collection) p.getValue(), type);
					} else if (p.getValueClass().isArray()) {
						Class member = p.getValueClass().getComponentType();
						type = getHibernateType(member);
						if (type == null)
							throw new JRException("Unsupported parameter type "+p.getValueClassName()+" for "+p.getName());
						q.setParameterList(getHqlParameterName(param.getName()), (Object[]) p.getValue(), type);
					} else {
						throw new JRException("Unsupported parameter type "+p.getValueClassName()+" for "+p.getName());
					}
				} else {
					q.setParameter(getHqlParameterName(param.getName()), p.getValue(), type);
				}
			}
			return new JRHqlDataSource(q);
		}
		return null;
	}


	public Type getHibernateType(java.lang.Class javaType) {
		Type type;
		if (String.class.isAssignableFrom( javaType))
			type = new StringType();
		else if (BigDecimal.class.isAssignableFrom( javaType))
			type = new BigDecimalType();
		else if (BigInteger.class.isAssignableFrom( javaType))
			type = new BigIntegerType();
		else if (byte[].class.isAssignableFrom( javaType))
			type = new BinaryType();
		else if (Boolean.class.isAssignableFrom( javaType) ||
				boolean.class.isAssignableFrom(javaType))
			type = new BooleanType();
		else if (Calendar.class.isAssignableFrom( javaType))
			type = new CalendarType();
		else if (Date.class.isAssignableFrom( javaType))
			type = new DateType();
		else if (Double.class.isAssignableFrom( javaType) ||
				double.class.isAssignableFrom(javaType))
			type = new DoubleType();
		else if (Float.class.isAssignableFrom( javaType) ||
				float.class.isAssignableFrom(javaType))
			type = new FloatType();
		else if (Integer.class.isAssignableFrom( javaType) ||
				int.class.isAssignableFrom(javaType))
			type = new IntegerType();
		else if (Long.class.isAssignableFrom( javaType) ||
				long.class.isAssignableFrom(javaType))
			type = new LongType();
		else if (Short.class.isAssignableFrom( javaType) ||
				short.class.isAssignableFrom(javaType))
			type = new ShortType();
		else if (String.class.isAssignableFrom( javaType) )
			type = new StringType();
		else 
			type = null;
		return type;
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public boolean cancelQuery() throws JRException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected String getParameterReplacement(String parameterName) {
		return ':' + getHqlParameterName(parameterName);
	}

	protected String getHqlParameterName(String parameterName)
	{
		return '_' + JRStringUtil.getJavaIdentifier(parameterName);
	}



}
