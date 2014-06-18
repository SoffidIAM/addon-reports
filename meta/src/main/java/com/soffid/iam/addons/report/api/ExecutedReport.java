package com.soffid.iam.addons.report.api;

import java.util.Collection;
import java.util.Date;

import com.soffid.mda.annotation.ValueObject;

@ValueObject
public class ExecutedReport {
	Long id;
	Long reportId;
	String name;
	Collection<String> users;
	Collection<ParameterValue> params;
	Date date;
	boolean done;
	boolean error;
	String errorMessage;
	
	String pdfDocument;
	String xmlDocument;
	String htmlDocument;
}
