package com.soffid.iam.addons.report.api;

import java.util.Collection;
import java.util.Date;

import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.Nullable;
import com.soffid.mda.annotation.ValueObject;

@ValueObject
public class Report {
	@Nullable
	Long id;
	String name;
	Collection<String> acl;
	@Nullable String author;
	@Nullable Date date;

	@Nullable Collection<ReportParameter> parameters;
	
	@Nullable FormatEnumeration format;
}
