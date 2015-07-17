package com.soffid.iam.addons.report.api;

import java.util.Collection;
import java.util.Date;

import com.soffid.mda.annotation.Nullable;
import com.soffid.mda.annotation.ValueObject;

@ValueObject
public class ExecutedReport {
	@Nullable Long id;
	Long reportId;
	String name;
	Collection<String> users;
	@Nullable Collection<ParameterValue> params;
	Date date;
	boolean done;
	boolean error;
	@Nullable String errorMessage;
	
	@Nullable String pdfDocument;
	@Nullable String xmlDocument;
	@Nullable String htmlDocument;
	@Nullable String csvDocument;
}
