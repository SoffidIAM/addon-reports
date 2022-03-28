package com.soffid.iam.addons.report.api;

import com.soffid.mda.annotation.Nullable;
import com.soffid.mda.annotation.ValueObject;

@ValueObject
public class QueryRequest {
	@Nullable String authorization;
	@Nullable Long firstRecord;
	String language;
	String query;
	@Nullable String[] parameterNames;
	@Nullable String[] parameterClasses;
	@Nullable String[] parameterValues;
}
