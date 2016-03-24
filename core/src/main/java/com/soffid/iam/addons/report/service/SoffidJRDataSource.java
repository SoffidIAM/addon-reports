package com.soffid.iam.addons.report.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataSourceProvider;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReport;

public class SoffidJRDataSource implements JRDataSource {

	Object current = null;
	private Iterator<Object> iterator;
	
	public SoffidJRDataSource(Collection<Object> results)
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

}
