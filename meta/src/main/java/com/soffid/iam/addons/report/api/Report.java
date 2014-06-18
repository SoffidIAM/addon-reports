package com.soffid.iam.addons.report.api;

import java.util.Collection;

import com.soffid.mda.annotation.Nullable;
import com.soffid.mda.annotation.ValueObject;

@ValueObject
public class Report {
	@Nullable
	Long id;
	String name;
	Collection<String> acl;
	Collection<ReportParameter> parameters;
}
