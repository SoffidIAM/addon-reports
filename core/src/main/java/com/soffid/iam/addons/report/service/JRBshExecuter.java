package com.soffid.iam.addons.report.service;

import java.util.Collection;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRQueryExecuter;

public class JRBshExecuter implements JRQueryExecuter{

	private Collection<Object> coll;

	public JRBshExecuter(Collection<Object> coll) {
		this.coll = coll;
	}

	public JRDataSource createDatasource() throws JRException {
		return new SoffidJRDataSource(coll);
	}

	public void close() {
	}

	public boolean cancelQuery() throws JRException {
		return true;
	}

}
