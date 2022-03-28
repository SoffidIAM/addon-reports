package com.soffid.iam.addons.report.common.jasper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.soffid.iam.addons.report.api.QueryRequest;
import com.soffid.iam.addons.report.api.QueryResponse;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataSourceProvider;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignField;

public class JRRemoteDataSourceProvider implements JRDataSourceProvider {

	@Override
	public boolean supportsGetFieldsOperation() {
		return true;
	}

	@Override
	public JRField[] getFields(JasperReport report) throws JRException, UnsupportedOperationException {
		List<JRField> fields = new LinkedList<>();
		JRDataset ds = report.getMainDataset();
		QueryRequest request = new QueryRequest();
		if (ds.getQuery() != null && ds.getQuery().getText() != null && 
				!ds.getQuery().getText().trim().isEmpty()) {
			request.setLanguage(ds.getQuery().getLanguage());
			request.setQuery(ds.getQuery().getText());
			JRParameter[] params = report.getParameters();
			request.setParameterClasses(new String[params.length]);
			request.setParameterNames(new String[params.length]);
			request.setParameterValues(new String[params.length]);
			for (int i = 0; i < params.length; i++) {
				request.getParameterClasses()[i] = params[i].getValueClassName();
				request.getParameterNames()[i] = params[i].getName();
				request.getParameterValues()[i] = null;
			}
			QueryResponse response;
			try {
				response = new RemoteInvoker().query(request);
			} catch (IOException e) {
				throw new JRException("Error parsing query "+request.getQuery(), e);
			}
			for (String field: response.getColumnNames()) {
				JRDesignField f  = new JRDesignField();
				f.setName(field);
				f.setValueClassName("java.lang.String");
				fields.add(f);
			}
		}
		return fields.toArray(new JRField[fields.size()]);
	}

	@Override
	public JRDataSource create(JasperReport report) throws JRException {
		return new JRRemoteDataSource(report.getMainDataset());
	}

	@Override
	public void dispose(JRDataSource dataSource) throws JRException {
	}

}
