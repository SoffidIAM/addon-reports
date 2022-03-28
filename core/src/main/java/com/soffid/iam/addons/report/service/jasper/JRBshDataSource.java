package com.soffid.iam.addons.report.service.jasper;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataSourceProvider;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReport;

public class JRBshDataSource implements JRSoffidDataSource {

	Object current = null;
	private Iterator<Object> iterator;
	
	public JRBshDataSource(Collection<Object> results)
	{
		iterator = results.iterator();
	}
	
	public boolean next() throws JRException {
		if (iterator.hasNext())
		{
			current = iterator.next();
			return true;
		}
		else
			return false;
	}

	public Object getFieldValue(JRField jrField) throws JRException 
	{
		try {
			if (current instanceof Map)
				return ((Map)current).get(jrField.getName());
			else
				return PropertyUtils.getNestedProperty(current, jrField.getName());
		} catch (Throwable t) {
			throw new JRException("Error evaluating field "+jrField.getName(), t);
		}
	}

	@Override
	public Map<String, Object> getFields() throws JRException {
		try {
			Map<String,Object> fields = new HashMap<>();
			for (PropertyDescriptor pd: PropertyUtils.getPropertyDescriptors(current) ) {
				final Object value = PropertyUtils.getProperty(current, pd.getName());
				fields.put(pd.getName(), value);
			}
			return fields;
		} catch (Exception e) {
			throw new JRException("Unable to read object of class "+current.getClass().getName(), e);
		}
	}

	@Override
	public List<String> getColumns() throws JRException {
		return new LinkedList<String>();
	}

	@Override
	public List<String> getColumnClasses() throws JRException {
		return new LinkedList<String>();
	}
	
}
