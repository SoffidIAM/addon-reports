package com.soffid.iam.addons.report.api;

import java.util.List;

import com.soffid.mda.annotation.Nullable;
import com.soffid.mda.annotation.ValueObject;

@ValueObject
public class QueryResponse {
	@Nullable String[] columnNames;
	@Nullable String[] columnClasses;
	@Nullable List<Object[]> values;
	@Nullable Long nextRecord;
}
