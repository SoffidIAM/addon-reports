package com.soffid.iam.addons.report.api;

import com.soffid.iam.addons.report.model.ReportEntity;
import com.soffid.mda.annotation.Attribute;
import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.Identifier;
import com.soffid.mda.annotation.Nullable;
import com.soffid.mda.annotation.ValueObject;

import es.caib.seycon.ng.comu.TypeEnumeration;

@ValueObject
public class ReportParameter {
		@Nullable Long id;
		String name;
		@Nullable Long order;
		@Nullable String description;
		@Nullable ParameterType type;
		@Nullable TypeEnumeration dataType;
		Long reportId;
}
