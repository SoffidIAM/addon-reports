package com.jaspersoft.studio.data.soffid;

import java.util.Map;

import net.sf.jasperreports.data.AbstractDataAdapterService;
import net.sf.jasperreports.data.DataAdapter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.ParameterContributorContext;

public class SoffidDataAdapterService extends AbstractDataAdapterService {

	private SoffidDataAdapterImpl adapter;

	public SoffidDataAdapterService(ParameterContributorContext paramContribContext, DataAdapter dataAdapter) {
		super(paramContribContext, dataAdapter);
		System.out.println("Instance soffid data adapter service");
		adapter = (SoffidDataAdapterImpl) dataAdapter;
	}

	protected SoffidDataAdapterService(JasperReportsContext jasperReportsContext, DataAdapter dataAdapter) {
		super(jasperReportsContext, dataAdapter);
		System.out.println("Instance soffid data adapter service");
		adapter = (SoffidDataAdapterImpl) dataAdapter;
	}

	@Override
	public void contributeParameters(Map<String, Object> params) throws JRException {
		System.out.println("///////////////// Data adapter service");
		System.out.println("///////////////// url="+adapter.getUrl());
		params.put("soffid.url", adapter.getUrl());
		params.put("soffid.user", adapter.getUserName());
		params.put("soffid.password", adapter.getPassword());
	}

}
