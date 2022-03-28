package com.soffid.iam.addons.report.service.jasper;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Query;
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

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class JRHqlDataSource implements JRSoffidDataSource {
	private Query query;
	boolean started = false;
	private Iterator iterator;
	private Object data;
	private String[] alias;
	
	public JRHqlDataSource(Query q) {
		this.query = q;
	}

	public boolean next() throws JRException {
		if (iterator == null) { 
			iterator = query.iterate();
			alias = query.getReturnAliases();
		}
		if (iterator.hasNext()) {
			data = iterator.next();
			return true;
		} else {
			return false;
		}
	}

	public Object getFieldValue(JRField jrField) throws JRException {
		if (data == null)
			return null;
		for (int i = 0; i < alias.length; i++) {
			if (alias[i].equalsIgnoreCase(jrField.getName())) {
				if (data.getClass().isArray()) {
					return Array.get(data, i);
				} else {
					return data;
				}
			}
		}
		if (!data.getClass().isArray()) {
			try {
				return PropertyUtils.getProperty(data, jrField.getName());
			} catch (Exception e) {
			}
		}
		return null;
	}

	@Override
	public Map<String, Object> getFields() throws JRException {
		try {
			Map<String, Object> r = new HashMap<>(); 
			if (!data.getClass().isArray()) {
				if (data.getClass().getName().startsWith("java")) {
					r.put(alias[0], data);
				}
				else
				{
					for (PropertyDescriptor pd: PropertyUtils.getPropertyDescriptors(data) ) {
						final Object value = PropertyUtils.getProperty(data, pd.getName());
						if (value != null && value.getClass().getName().startsWith("java"))
							r.put(pd.getName(), value);
					}
				}
			} else {
				int i = 0;
				for (String a: alias) {
					r.put(a, Array.get(data, i++));
				}
				
			}
			return r;
		} catch (Exception e) {
			throw new JRException("Error executing stmt", e);
		}
	}

	@Override
	public List<String> getColumns() throws JRException {
		if (iterator == null) { 
			iterator = query.iterate();
			alias = query.getReturnAliases();
		}
		LinkedList<String> l = new LinkedList<String>();
		if (alias != null) for (String a: alias) l.add(a);
		return l;
	}

	@Override
	public List<String> getColumnClasses() throws JRException {
		if (iterator == null) { 
			iterator = query.iterate();
		}
		LinkedList<String> l = new LinkedList<String>();
		for (Type type: query.getReturnTypes()) {
			l.add(type.getReturnedClass().getName());
		}
		return l;
	}
}
