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

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.hibernate.Session;

import com.soffid.iam.addons.report.service.SessionHolder;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRQuery;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.query.JRAbstractQueryExecuter;
import net.sf.jasperreports.engine.query.QueryExecutionContext;

/**
 * HQL query executer that uses Hibernate 3.
 * 
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 */
public class JRSqlExecuter extends JRAbstractQueryExecuter
{

	public JRSqlExecuter(
			QueryExecutionContext context, 
			JRDataset dataset, 
			Map<String, ? extends JRValueParameter> parametersMap
			)
	{
		super (context, dataset, parametersMap);
	}


	protected JRSqlExecuter(JasperReportsContext jasperReportsContext, JRDataset dataset,
			Map<String, ? extends JRValueParameter> parametersMap) {
		super(jasperReportsContext, dataset, parametersMap);
	}

	public JRDataSource createDatasource() throws JRException {
		JRQuery query = dataset.getQuery();
		
		if (query != null)
		{
			parseQuery();
			try {
				Session session = SessionHolder.hibernateSession.get(); 
				PreparedStatement q = session.connection().prepareStatement(getQueryString());
				int position = 1;
				for (QueryParameter param: getCollectedParameters()) {
					JRValueParameter p = getValueParameter(param.getName());
					Object value = p.getValue();
					if (value == null)
						q.setNull(position, getSqlType(p.getValueClass()));
					else
						setParameter(q, position, p);
					position++;
				}
				return new JRSqlDataSource(q);
			} catch (SQLException e) {
				throw new JRException("Error executing query", e);
			}
		}
		return null;
	}


	public void setParameter(PreparedStatement q, int position, JRValueParameter p) throws SQLException {
		Class<?> javaType = p.getValueClass();
		if (String.class.isAssignableFrom( javaType))
			q.setString(position, p.getValue().toString());
		else if (BigDecimal.class.isAssignableFrom( javaType))
			q.setBigDecimal(position, (BigDecimal) p.getValue());
		else if (BigInteger.class.isAssignableFrom( javaType))
			q.setLong(position, ((BigInteger) p.getValue()).longValue());
		else if (byte[].class.isAssignableFrom( javaType))
			q.setBinaryStream(position, new ByteArrayInputStream((byte[]) p.getValue()));
		else if (Boolean.class.isAssignableFrom( javaType) ||
				boolean.class.isAssignableFrom(javaType))
			q.setBoolean(position, (Boolean) p.getValue());
		else if (Calendar.class.isAssignableFrom( javaType))
			q.setDate(position, new java.sql.Date(((Calendar) p.getValue()).getTime().getTime()));
		else if (Date.class.isAssignableFrom( javaType))
			q.setDate(position, new java.sql.Date(((Date) p.getValue()).getTime()));
		else if (Double.class.isAssignableFrom( javaType) ||
				double.class.isAssignableFrom(javaType))
			q.setDouble(position, ((Double) p.getValue()).doubleValue());
		else if (Float.class.isAssignableFrom( javaType) ||
				float.class.isAssignableFrom(javaType))
			q.setFloat(position, ((Float) p.getValue()).floatValue());
		else if (Integer.class.isAssignableFrom( javaType) ||
				int.class.isAssignableFrom(javaType))
			q.setInt(position, ((Integer) p.getValue()).intValue());
		else if (Long.class.isAssignableFrom( javaType) ||
				long.class.isAssignableFrom(javaType))
			q.setLong(position, ((Long) p.getValue()).longValue());
		else if (Short.class.isAssignableFrom( javaType) ||
				short.class.isAssignableFrom(javaType))
			q.setShort(position, ((Short) p.getValue()).shortValue());
		else 
			q.setObject(position, p.getValue());
	}


	public int getSqlType(java.lang.Class javaType) {
		int type;
		if (String.class.isAssignableFrom( javaType))
			type = Types.VARCHAR;
		else if (BigDecimal.class.isAssignableFrom( javaType))
			type = Types.DECIMAL;
		else if (BigInteger.class.isAssignableFrom( javaType))
			type = Types.BIGINT;
		else if (byte[].class.isAssignableFrom( javaType))
			type = Types.VARBINARY;
		else if (Boolean.class.isAssignableFrom( javaType) ||
				boolean.class.isAssignableFrom(javaType))
			type = Types.BOOLEAN;
		else if (Calendar.class.isAssignableFrom( javaType))
			type = Types.DATE;
		else if (Date.class.isAssignableFrom( javaType))
			type = Types.DATE;
		else if (Double.class.isAssignableFrom( javaType) ||
				double.class.isAssignableFrom(javaType))
			type = Types.DOUBLE;
		else if (Float.class.isAssignableFrom( javaType) ||
				float.class.isAssignableFrom(javaType))
			type = Types.FLOAT;
		else if (Integer.class.isAssignableFrom( javaType) ||
				int.class.isAssignableFrom(javaType))
			type = Types.INTEGER;
		else if (Long.class.isAssignableFrom( javaType) ||
				long.class.isAssignableFrom(javaType))
			type = Types.BIGINT;
		else if (Short.class.isAssignableFrom( javaType) ||
				short.class.isAssignableFrom(javaType))
			type = Types.SMALLINT;
		else if (String.class.isAssignableFrom( javaType) )
			type = Types.VARCHAR;
		else 
			type = Types.JAVA_OBJECT;
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
		return "?";
	}

}
