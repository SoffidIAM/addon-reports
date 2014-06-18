package com.soffid.iam.addons.report.api;

import com.soffid.iam.addons.report.model.ReportEntity;
import com.soffid.mda.annotation.Attribute;
import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.Identifier;
import com.soffid.mda.annotation.Nullable;
import com.soffid.mda.annotation.ValueObject;

@ValueObject
public class ParameterValue {
		@Nullable Long id;
		String name;
		@Nullable ParameterType type;
		Object value;
}
