package com.jaspersoft.studio.data.soffid;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;

public class JRSoffidDataSource implements JRDataSource {

	Object current = null;
	private Iterator<Object> iterator;
	private JasperReportsContext jasperReportsContext;
	private JRDataset dataset;
	private QueryResponse result;
	private int position;
	private int first;
	private Map<String, ? extends JRValueParameter> params;
	
	public JRSoffidDataSource(JasperReportsContext jasperReportsContext, JRDataset dataset, Map<String, ? extends JRValueParameter> params) {
		this.jasperReportsContext = jasperReportsContext;
		this.dataset = dataset;
		this.result = null;
		this.position = 0;
		this.first = 0;
		this.params = params;
	}

	public boolean next() throws JRException {
		if (result == null || position >= result.getValues().size() - 1) {
			if (result != null && result.getNextRecord() == null)
				return false;
			QueryRequest request = new QueryRequest();
			request.setAuthorization("---");
			request.setLanguage(dataset.getQuery().getLanguage());
			request.setQuery(dataset.getQuery().getText());
			request.setFirstRecord(result == null? null: result.getNextRecord());
			List<String> names = new LinkedList<>();
			List<String> values = new LinkedList<>();
			List<String> types = new LinkedList<>();
			RemoteInvoker remoteInvoker = new RemoteInvoker();
			for (Entry<String, ? extends JRValueParameter> entry: params.entrySet()) {
				String name = entry.getKey();
				JRValueParameter v = entry.getValue();
				names.add(v.getName());
				values.add(remoteInvoker.tojson(v.getValue()));
				types.add(v.getValueClassName());
			}
			request.setParameterNames(names.toArray(new String[names.size()]));
			request.setParameterClasses(types.toArray(new String[types.size()]));
			request.setParameterValues(values.toArray(new String[values.size()]));
			try {
				Map<String, String> m = (Map<String, String>) params.get("REPORT_PARAMETERS_MAP").getValue();
				remoteInvoker.setUrl(m.get("soffid.url"));
				remoteInvoker.setPassword(m.get("soffid.password"));
				remoteInvoker.setUser(m.get("soffid.user"));
				result = remoteInvoker.query(request);
				position = 0;
			} catch (IOException e) {
				throw new JRException("Error fetching data", e);
			}
			System.out.println("Got %d rows " + result.getValues().size());
			return !result.getValues().isEmpty();
		} 
		else
			position ++;
		return true;
	}

	public Object getFieldValue(JRField jrField) throws JRException 
	{
		try {
			System.out.println("Getting row "+position+" att "+jrField.getName());
			Object[] r = result.getValues().get(position);
			for (int i = 0; i < result.getColumnNames().length && i < r.length; i++) 
				if (result.getColumnNames()[i].equals(jrField.getName())) {
					Class clazz = Object.class;
					try {
						if (result.getColumnClasses() != null && i < result.getColumnClasses().length &&
								result.getColumnClasses()[i] != null)
							clazz = Class.forName(result.getColumnClasses()[i]);
					} catch (ClassNotFoundException e) {}
					Object value = castObject( r[i], clazz);
					if (value != null)
						System.out.println("Class "+value.getClass()+": "+value.toString());
					return value;
				}
			System.out.println("Not found");
			return null;
		} catch (Throwable t) {
			throw new JRException("Error evaluating field "+jrField.getName(), t);
		}
	}

	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private Object castObject(Object object, Class clazz) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ParseException {
		if (object == null)
			return null;
		System.out.println(" >> clazz "+clazz.getName());
		if (Number.class.isAssignableFrom(clazz))
			return clazz.getMethod("decode", String.class).invoke(null, object);
		if (java.sql.Date.class.isAssignableFrom(clazz))
			return new java.sql.Date( df.parse(object.toString()).getTime() );
		if (java.util.Date.class.isAssignableFrom(clazz))
			return df.parse(object.toString()).getTime();
		if (Calendar.class.isAssignableFrom(clazz)) {
			Calendar c = Calendar.getInstance();
			c.setTime ( df.parse(object.toString()) );
			return c;
		}
		if (Boolean.class.isAssignableFrom(clazz))
			return Boolean.getBoolean(object.toString());
		
		return object.toString();
	}
}
