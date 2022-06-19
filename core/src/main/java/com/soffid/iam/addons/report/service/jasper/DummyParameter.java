package com.soffid.iam.addons.report.service.jasper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

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
import org.joda.time.format.DateTimeFormat;

import es.caib.seycon.util.Base64;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.type.ParameterEvaluationTimeEnum;

public class DummyParameter implements JRValueParameter {

	private String name;
	private String value;
	private String className;

	public DummyParameter(String name, String value, String className) {
		this.name = name;
		this.value = value;
		this.className = className;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return name;
	}

	@Override
	public void setDescription(String description) {
	}

	@Override
	public Class<?> getValueClass() {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Error fetching class"+className, e);
		}
	}

	@Override
	public String getValueClassName() {
		return className;
	}

	@Override
	public boolean isSystemDefined() {
		return false;
	}

	@Override
	public boolean isForPrompting() {
		return false;
	}

	@Override
	public ParameterEvaluationTimeEnum getEvaluationTime() {
		return ParameterEvaluationTimeEnum.EARLY;
	}

	@Override
	public JRExpression getDefaultValueExpression() {
		return null;
	}

	@Override
	public Class<?> getNestedType() {
		return null;
	}

	@Override
	public String getNestedTypeName() {
		return null;
	}

	@Override
	public boolean hasProperties() {
		return false;
	}

	@Override
	public JRPropertiesMap getPropertiesMap() {
		return null;
	}

	@Override
	public JRPropertiesHolder getParentProperties() {
		return null;
	}

	@Override
	public Object getValue() {
		if (value == null)
			return null;
		Class<?> javaType = getValueClass();
		if (String.class.isAssignableFrom( javaType))
			return value;
		else if (BigDecimal.class.isAssignableFrom( javaType))
			return new BigDecimal(value);
		else if (BigInteger.class.isAssignableFrom( javaType))
			return new BigInteger(value);
		else if (byte[].class.isAssignableFrom( javaType))
			return Base64.decode(value);
		else if (Boolean.class.isAssignableFrom( javaType) ||
				boolean.class.isAssignableFrom(javaType))
			return Boolean.parseBoolean(value);
		else if (Calendar.class.isAssignableFrom( javaType)) {
			Calendar c = Calendar.getInstance();
			try {
				c.setTime( new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(value));
			} catch (ParseException e) {
				try {
					c.setTime( new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss").parse(value));
				} catch (ParseException e1) {
					throw new RuntimeException("Unable to parser date "+value,e1);
				}
			}
			return c;
		}
		else if (Date.class.isAssignableFrom( javaType)) {
			try {
				return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(value);
			} catch (ParseException e) {
				try {
					return new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss").parse(value);
				} catch (ParseException e1) {
					throw new RuntimeException("Unable to parser date "+value,e1);
				}
			}
		}
		else if (Double.class.isAssignableFrom( javaType) ||
				double.class.isAssignableFrom(javaType))
			return new Double(value);
		else if (Float.class.isAssignableFrom( javaType) ||
				float.class.isAssignableFrom(javaType))
			return new Float(value);
		else if (Integer.class.isAssignableFrom( javaType) ||
				int.class.isAssignableFrom(javaType))
			return new Integer(value);
		else if (Long.class.isAssignableFrom( javaType) ||
				long.class.isAssignableFrom(javaType))
			return new Long(value);
		else if (Short.class.isAssignableFrom( javaType) ||
				short.class.isAssignableFrom(javaType))
			return new Integer(value);
		else 
			return value;
	}

	@Override
	public void setValue(Object value) {
	}

	@Override
	public Object clone()  {
		return new DummyParameter(name, value, className);
	}

}
