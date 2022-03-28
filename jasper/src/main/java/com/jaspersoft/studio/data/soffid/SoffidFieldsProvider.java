/*******************************************************************************
 * Copyright (C) 2010 - 2016. TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 ******************************************************************************/
package com.jaspersoft.studio.data.soffid;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.jaspersoft.studio.data.fields.IFieldsProvider;
import com.jaspersoft.studio.property.dataset.dialog.DataQueryAdapters;
import com.jaspersoft.studio.utils.jasper.JasperReportsConfiguration;
import com.jaspersoft.studio.utils.parameter.ParameterUtil;
import com.jaspersoft.studio.utils.parameter.SimpleValueParameter;

import net.sf.jasperreports.data.DataAdapterService;
import net.sf.jasperreports.eclipse.util.Misc;
import net.sf.jasperreports.eclipse.util.StringUtils;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.query.JRJdbcQueryExecuter;
import net.sf.jasperreports.engine.query.JRJdbcQueryExecuterFactory;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.query.QueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRQueryExecuterUtils;

public class SoffidFieldsProvider implements IFieldsProvider {

	public boolean supportsGetFieldsOperation(JasperReportsConfiguration jConfig, JRDataset jDataset) {
		return true;
	}

	public List<JRDesignField> getFields(DataAdapterService con, JasperReportsConfiguration jConfig, JRDataset dataset)
			throws JRException, UnsupportedOperationException {
		Map<String, Object> parameters = new HashMap<>();
		con.contributeParameters(parameters);

		ParameterUtil.setParameters(jConfig, dataset, parameters);
		parameters.put(JRJdbcQueryExecuterFactory.PROPERTY_JDBC_FETCH_SIZE, 0);
		parameters.put(JRParameter.REPORT_MAX_COUNT, 1);
		List<JRDesignField> columns = null;
		Connection c = null;
		try {
			// JasperReports query executer instances require
			// REPORT_PARAMETERS_MAP parameter to be defined and not null
			Map<String, JRValueParameter> tmpMap = ParameterUtil.convertMap(parameters, dataset);
			tmpMap.put(JRParameter.REPORT_PARAMETERS_MAP,
					new SimpleValueParameter(new HashMap<String, JRValueParameter>()));

			QueryRequest request = new QueryRequest();

			request.setAuthorization("---");
			request.setLanguage(dataset.getQuery().getLanguage());
			request.setQuery(dataset.getQuery().getText());
			List<String> names = new LinkedList<>();
			List<String> values = new LinkedList<>();
			List<String> types = new LinkedList<>();
			RemoteInvoker remoteInvoker = new RemoteInvoker();
			for (Entry<String, JRValueParameter> entry: tmpMap.entrySet()) {
				String name = entry.getKey();
				JRValueParameter v = entry.getValue();
				names.add(name);
				values.add(remoteInvoker.tojson(v.getValue()));
				types.add(v.getValueClassName());
			}
			request.setParameterNames(names.toArray(new String[names.size()]));
			request.setParameterClasses(types.toArray(new String[types.size()]));
			request.setParameterValues(values.toArray(new String[values.size()]));
			remoteInvoker.setUrl((String) parameters.get("soffid.url"));
			remoteInvoker.setPassword((String) parameters.get("soffid.password"));
			remoteInvoker.setUser((String) parameters.get("soffid.user"));
			QueryResponse result = remoteInvoker.query(request);
			List<JRDesignField> fields =  new LinkedList<>();
			for (int i = 0; i < result.getColumnNames().length; i++) {
				String at = result.getColumnNames()[i];
				JRDesignField field = new JRDesignField();
				field.setName(at);
				field.setValueClassName(String.class.getName());
				if (result.getColumnClasses() != null && 
						result.getColumnClasses().length > i &&
						result.getColumnClasses()[i] != null) {
					field.setValueClassName(result.getColumnClasses()[i]);
				} else {
					field.setValueClass(String.class);
				}
				fields.add(field);
			}
			return fields;
		} catch (IOException e) {
			throw new JRException("Error fetching data", e);
		}
	}

	private static String JAVA_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
	private static Pattern PATTERN = Pattern.compile(JAVA_PATTERN + "(\\." + JAVA_PATTERN + ")*");

	public static String getJdbcTypeClass(java.sql.ResultSetMetaData rsmd, int t) {
		try {
			String cl = rsmd.getColumnClassName(t);
			if (Misc.isNullOrEmpty(cl) || !PATTERN.matcher(cl).matches())
				return getColumnType(rsmd, t);
			return getJRFieldType(cl);
		} catch (SQLException ex) {
			return getColumnType(rsmd, t);
		}
	}

	protected static String getColumnType(java.sql.ResultSetMetaData rsmd, int ind) {
		try {
			return getColumnType(rsmd.getColumnType(ind));
		} catch (SQLException ex2) {
			ex2.printStackTrace();
		}
		return null;
	}

	protected static String getColumnType(int type) {
		switch (type) {
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.NVARCHAR:
		case Types.LONGVARCHAR:
			return String.class.getCanonicalName();
		case Types.NUMERIC:
			return Number.class.getCanonicalName();
		case Types.DECIMAL:
			return BigDecimal.class.getCanonicalName();
		case Types.BIT:
		case Types.BOOLEAN:
			return Boolean.class.getCanonicalName();
		case Types.TINYINT:
			return Byte.class.getCanonicalName();
		case Types.SMALLINT:
			return Short.class.getCanonicalName();
		case Types.INTEGER:
			return Integer.class.getCanonicalName();
		case Types.BIGINT:
			return BigInteger.class.getCanonicalName();
		case Types.REAL:
			return Float.class.getCanonicalName();
		case Types.FLOAT:
		case Types.DOUBLE:
			return Double.class.getCanonicalName();
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			return byte[].class.getCanonicalName();
		case Types.DATE:
			return Date.class.getCanonicalName();
		case Types.TIME:
		case Types.TIME_WITH_TIMEZONE:
			return Time.class.getCanonicalName();
		case Types.TIMESTAMP:
		case Types.TIMESTAMP_WITH_TIMEZONE:
			return Timestamp.class.getCanonicalName();
		case Types.CLOB:
			return Clob.class.getCanonicalName();
		case Types.NCLOB:
			return NClob.class.getCanonicalName();
		case Types.BLOB:
			return Blob.class.getCanonicalName();
		case Types.ARRAY:
			return Array.class.getCanonicalName();
		case Types.STRUCT:
			return Struct.class.getCanonicalName();
		case Types.ROWID:
			return RowId.class.getCanonicalName();
		case Types.REF:
			return Ref.class.getCanonicalName();
		case Types.DATALINK:
			return URL.class.getCanonicalName();
		case Types.SQLXML:
			return SQLXML.class.getCanonicalName();
		case Types.REF_CURSOR:
			return ResultSet.class.getCanonicalName();
		case Types.JAVA_OBJECT:
		case Types.OTHER:
		default:
			return Object.class.getCanonicalName();
		}
	}

	public static String getJRFieldType(String type) {
		if (type == null)
			return Object.class.getName();
		if (type.equals(boolean.class.getName()))
			return Boolean.class.getName();
		if (type.equals(byte.class.getName()))
			return Byte.class.getName();
		if (type.equals(int.class.getName()))
			return Integer.class.getName();
		if (type.equals(long.class.getName()))
			return Long.class.getName();
		if (type.equals(double.class.getName()))
			return Double.class.getName();
		if (type.equals(float.class.getName()))
			return Float.class.getName();
		if (type.equals(short.class.getName()))
			return Short.class.getName();
		if (type.startsWith("["))
			return Object.class.getName();
		return type;
	}

}
