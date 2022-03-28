package com.soffid.iam.addons.report.service.jasper;

import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;

public interface JRSoffidDataSource extends JRDataSource {
	Map<String,Object> getFields() throws JRException;
	List<String> getColumns() throws JRException;
	List<String> getColumnClasses() throws JRException;
}
