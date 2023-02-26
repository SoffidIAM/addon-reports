package com.soffid.iam.addons.report.service.jasper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;

import com.soffid.iam.addons.report.api.ParameterType;
import com.soffid.iam.addons.report.api.QueryRequest;
import com.soffid.iam.addons.report.api.QueryResponse;
import com.soffid.iam.addons.report.api.Report;
import com.soffid.iam.addons.report.api.ReportParameter;
import com.soffid.iam.addons.report.model.ReportACLEntityDao;
import com.soffid.iam.addons.report.model.ReportEntity;
import com.soffid.iam.addons.report.model.ReportEntityDao;
import com.soffid.iam.addons.report.model.ReportParameterEntityDao;
import com.soffid.iam.addons.report.service.SessionHolder;
import com.soffid.iam.doc.api.DocumentReference;
import com.soffid.iam.doc.exception.DocumentBeanException;
import com.soffid.iam.doc.service.DocumentService;
import com.soffid.iam.utils.Security;

import es.caib.seycon.ng.comu.TypeEnumeration;
import es.caib.seycon.ng.exception.InternalErrorException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignDataset;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.query.QueryExecutionContext;
import net.sf.jasperreports.engine.query.SimpleQueryExecutionContext;
import net.sf.jasperreports.engine.util.JRLoader;

public class QueryExecutor {
	Log log = LogFactory.getLog(getClass());
	
	public QueryResponse executeQuery(QueryRequest request, SessionFactory sf) throws JRException, Exception {
		JRQueryExecuter executor = null; 
		QueryExecutionContext context = new SimpleQueryExecutionContext();
		JRDesignDataset dataSet = new JRDesignDataset(true);
		final JRDesignQuery query = new JRDesignQuery();
		query.setLanguage(request.getLanguage());
		query.setText(request.getQuery());
		dataSet.setQuery(query);
		Map<String, JRValueParameter> parameters = new HashMap<>();
		for (int i = 0; i < request.getParameterNames().length; i++) {
			try {
				JRValueParameter p = new DummyParameter(request.getParameterNames()[i], request.getParameterValues()[i], request.getParameterClasses()[i]);
				p.getValueClass();
				parameters.put(request.getParameterNames()[i], p);
			} catch (RuntimeException e) {
				
			}
		}
		if ("hql".equalsIgnoreCase(request.getLanguage())) {
			executor = new JRHqlExecuter(context, dataSet, parameters);
		}
		else if ("sql".equalsIgnoreCase(request.getLanguage())) {
			executor = new JRSqlExecuter(context, dataSet, parameters);
		}
		else if ("bsh".equalsIgnoreCase(request.getLanguage())) {
			JRBshExecuterFactory factory = new JRBshExecuterFactory();
			executor = factory.createQueryExecuter(dataSet, parameters);
		}
		else
		{
			throw new Exception("Unsupported langage "+request.getLanguage());
		}
		
		try {
			SessionHolder.hibernateSession.set(sf.getCurrentSession());
			JRSoffidDataSource ds = (JRSoffidDataSource) executor.createDatasource();
			int first = request.getFirstRecord() == null ? 0: request.getFirstRecord().intValue();
			int last = first + 2000;
			
			QueryResponse r = new QueryResponse();
			List<String> columns = new LinkedList<>(ds.getColumns());
			List<String> types = new LinkedList<>(ds.getColumnClasses());
			List<Object[]> rows = new LinkedList<>();
			
			int position = 0;
			while (position < first && ds.next()) {
				position ++;
			}
			while (position < last && ds.next()) {
				Map<String, Object> fields = ds.getFields();
				Vector<Object> values = new Vector<>();
				for (Entry<String, Object> entry: fields.entrySet()) {
					int pos = findColumn(entry.getKey(), columns, types, entry.getValue() );
					values.ensureCapacity(pos+1);
					while (values.size() <= pos)
						values.add(null);
					values.set(pos, entry.getValue());
				}
				rows.add(values.toArray());
				position ++;
			}
			r.setColumnNames(columns.toArray(new String[columns.size()]));
			r.setColumnClasses(types.toArray(new String[types.size()]));
			r.setValues(rows);
			r.setNextRecord(position < last ? null: Long.valueOf(last));
			return r;
		} finally {
			SessionHolder.hibernateSession.remove();
		}
	}

	private int findColumn(String key, List<String> columns, List<String> types, Object value) {
		int pos = columns.indexOf(key);
		if (pos < 0) {
			pos = columns.size();
			columns.add(key);
		}
		if (types.size() <= pos) {
			types.add(null);
		}
		if (types.get(pos) == null && value != null)
			types.set(pos, value.getClass().getName());
		return pos;
	}

	public Report upload(ReportEntityDao reportEntityDao, ReportParameterEntityDao reportParameterEntityDao, 
			ReportACLEntityDao reportACLEntityDao, DocumentService doc,
			InputStream report) throws IOException, JRException, NamingException, CreateException, DocumentBeanException, InternalErrorException {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		int read;
		byte buffer[] = new byte[4096];
		while ( (read = report.read(buffer)) > 0)
		{
			data.write(buffer, 0, read);
		}
		data.close();
        JasperReport jr = (JasperReport) JRLoader.loadObject(new ByteArrayInputStream(data.toByteArray()));
        String name = jr.getName();

        Report r;
		ReportEntity re = reportEntityDao.findByName(name);
		if (re == null)
		{
			r = new Report();
			r.setParameters(new LinkedList<ReportParameter>());
		}
		else
		{
			log.info("Upgrading report definition");
			r = reportEntityDao.toReport(re);
		}

		Collection<ReportParameter> oldParameters = r.getParameters();
		r.setName(jr.getName());
		r.setAuthor(Security.getCurrentUser());
		r.setDate(new Date());
        List<ReportParameter> rp = new LinkedList<ReportParameter>();
        long order = 0;
        for (JRParameter jp: jr.getParameters())
        {
        	if ( jp.isForPrompting() && ! jp.isSystemDefined())
        	{
        		// Search existing parameter
        		log.info("Analyzing parameter "+jp.getName());
        		boolean found = false;
        		for (ReportParameter existingParameter: oldParameters)
        		{
        			if (existingParameter.getOrder() != null && existingParameter.getOrder().longValue() >= order)
        				order = existingParameter.getOrder().longValue() + 1;
            		log.info("Is "+existingParameter.getName()+"?");
        			if (existingParameter.getName().equals(jp.getName()))
        			{
        				log.info("Found");
        				found = true;
    		        	if (jp.getDescription() != null)
    		        		existingParameter.setDescription(jp.getDescription());
        				rp.add(existingParameter);
        				existingParameter.setOrder(order ++);
        				String dataType = jp.getPropertiesMap().getProperty("soffid.data.type");
        				if (dataType != null) {
        					TypeEnumeration desired = guessParameterType(jp);
        					existingParameter.setDataType(desired);
        				}
    					existingParameter.setType( guessParameterType2(jp));
        			}
        		}
        		if (! found )
        		{
		        	ReportParameter p = new ReportParameter();
		        	p.setName(jp.getName());
		        	p.setOrder(order++);
		        	p.setDescription (jp.getDescription());
		        	if (p.getDescription() == null)
		        		p.setDescription("No description available");
		        	p.setDataType(guessParameterType(jp));
		        	p.setType(guessParameterType2(jp));
		        	rp.add(p);
        		}
	    	}
        }
        
        r.setParameters(rp);
        if (r.getAcl() == null || r.getAcl().isEmpty())
        {
        	log.info("Reseting ACL");
	        r.setAcl(new LinkedList<String>());
	        if (Security.getCurrentUser() != null)
	        	r.getAcl().add(Security.getCurrentUser());
        }

        DocumentReference ref = storeDocument (name, data.toByteArray(), doc);
        
        re = reportEntityDao.reportToEntity(r);
        re.setDocId(ref.toString());
        if (r.getId() == null)
        	reportEntityDao.create(re);
        else
        {
        	reportEntityDao.update(re);
       		reportParameterEntityDao.update( re.getParameters());
       		reportACLEntityDao.update(re.getAcl());
        }
        
        return reportEntityDao.toReport(re);
	}

	private ParameterType guessParameterType2(JRParameter jp) {
    	if (jp.getValueClass().isAssignableFrom(String.class))
    		return ParameterType.STRING_PARAM;
    	else if (jp.getValueClass().isAssignableFrom(int.class) ||
    			jp.getValueClass().isAssignableFrom(long.class) ||
    			jp.getValueClass().isAssignableFrom(Integer.class) ||
    			jp.getValueClass().isAssignableFrom(Long.class) )
    	{
    		return ParameterType.LONG_PARAM;
    	}
    	else if (jp.getValueClass().isAssignableFrom(double.class) ||
    			jp.getValueClass().isAssignableFrom(float.class) ||
    			jp.getValueClass().isAssignableFrom(Double.class) ||
    			jp.getValueClass().isAssignableFrom(Float.class) )
    	{
    		return ParameterType.DOUBLE_PARAM;
    	}
    	else if (jp.getValueClass().isAssignableFrom(Date.class) ||
    			jp.getValueClass().isAssignableFrom(java.sql.Date.class) ||
    			jp.getValueClass().isAssignableFrom(Date.class) ||
    			jp.getValueClass().isAssignableFrom(java.sql.Date.class) ||
    			jp.getValueClass().isAssignableFrom(Calendar.class) )
    	{
    		return ParameterType.DATE_PARAM;
    	}
    	else if (jp.getValueClass().isAssignableFrom(boolean.class) ||
    			jp.getValueClass().isAssignableFrom(Boolean.class)  )
    	{
    		return ParameterType.BOOLEAN_PARAM;
    	} 
    	else
    	{
    		return ParameterType.STRING_PARAM;
    	}
	}

	private DocumentReference storeDocument(String name, byte[] data, DocumentService doc) throws NamingException, RemoteException, CreateException, DocumentBeanException, InternalErrorException {
		doc.createDocument("application/x-rpt", name+".rpt", "report");
		doc.openUploadTransfer();
		doc.nextUploadPackage(data, data.length);
		doc.endUploadTransfer();
		return doc.getReference();
	}

	@SuppressWarnings("unchecked")
	private TypeEnumeration guessParameterType (JRParameter jp)
	{
		String dataType = jp.getPropertiesMap().getProperty("soffid.data.type");
		if (dataType != null && ! dataType.trim().isEmpty()) {
			List literals = TypeEnumeration.literals();
			List names = TypeEnumeration.names();
			for (int i = 0; i < names.size(); i++)
				if ( dataType.equalsIgnoreCase((String) names.get(i)))
					return TypeEnumeration.fromString((String) literals.get(i));
			for (int i = 0; i < names.size(); i++)
				if ( (dataType+"_TYPE").equalsIgnoreCase((String) names.get(i)))
					return TypeEnumeration.fromString((String) literals.get(i));
		}
		
    	if (jp.getValueClass().isAssignableFrom(String.class))
    		return TypeEnumeration.STRING_TYPE;
    	else if (jp.getValueClass().isAssignableFrom(int.class) ||
    			jp.getValueClass().isAssignableFrom(long.class) ||
    			jp.getValueClass().isAssignableFrom(Integer.class) ||
    			jp.getValueClass().isAssignableFrom(Long.class) )
    	{
    		return TypeEnumeration.NUMBER_TYPE;
    	}
    	else if (jp.getValueClass().isAssignableFrom(double.class) ||
    			jp.getValueClass().isAssignableFrom(float.class) ||
    			jp.getValueClass().isAssignableFrom(Double.class) ||
    			jp.getValueClass().isAssignableFrom(Float.class) )
    	{
    		return TypeEnumeration.NUMBER_TYPE;
    	}
    	else if (jp.getValueClass().isAssignableFrom(Date.class) ||
    			jp.getValueClass().isAssignableFrom(java.sql.Date.class) ||
    			jp.getValueClass().isAssignableFrom(Date.class) ||
    			jp.getValueClass().isAssignableFrom(java.sql.Date.class) ||
    			jp.getValueClass().isAssignableFrom(Calendar.class) )
    	{
    		return TypeEnumeration.DATE_TIME_TYPE;
    	}
    	else if (jp.getValueClass().isAssignableFrom(boolean.class) ||
    			jp.getValueClass().isAssignableFrom(Boolean.class)  )
    	{
    		return TypeEnumeration.BOOLEAN_TYPE;
    	} 
    	else
    	{
    		return TypeEnumeration.STRING_TYPE;
    	}
	}
	
}
