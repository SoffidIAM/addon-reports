package com.soffid.iam.addons.report.model;

import java.util.Date;

import com.soffid.iam.addons.report.api.ParameterType;
import com.soffid.iam.addons.report.api.ParameterValue;
import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.Depends;
import com.soffid.mda.annotation.Description;
import com.soffid.mda.annotation.Entity;
import com.soffid.mda.annotation.Identifier;
import com.soffid.mda.annotation.Nullable;

@Entity(table="SCR_EXREPA")
@Depends({ParameterValue.class})
public class ExecutedReportParameterEntity {
	@Identifier
	@Nullable
	@Column(name = "ERP_ID")
	Long id;
	
	@Column(name = "ERP_ERE_ID", reverseAttribute="parameters")
	ExecutedReportEntity report;

	@Column(name = "ERP_NAME")
	String name;
	
	@Column(name = "ERP_STRVAL", length=1024)
	@Nullable
	String stringValue;

	@Column(name = "ERP_DATVAL")
	@Nullable
	Date dateValue;

	@Column(name = "ERP_LONVAL")
	@Nullable
	Long longValue;

	@Column(name = "ERP_DOUVAL")
	@Nullable
	Double doubleValue;
	
	@Column(name = "ERP_BOOVAL")
	@Nullable
	Boolean booleanValue;

	@Column(name= "ERP_TYPE")
	ParameterType type;
}
