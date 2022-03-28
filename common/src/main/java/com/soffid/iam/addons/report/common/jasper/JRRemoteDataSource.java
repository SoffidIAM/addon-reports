package com.soffid.iam.addons.report.common.jasper;

import com.soffid.iam.addons.report.api.QueryResponse;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class JRRemoteDataSource implements JRDataSource {
	QueryResponse response = null;
	Integer nextRequest;
	int index;
	public JRRemoteDataSource(JRDataset mainDataset) {
		index = 0;
		
	}

	@Override
	public boolean next() throws JRException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getFieldValue(JRField jrField) throws JRException {
		// TODO Auto-generated method stub
		return null;
	}

}
