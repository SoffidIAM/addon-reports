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
public class ScheduledReportParameterEntity {
	@Identifier
	@Nullable
	@Column(name = "SRP_ID")
	Long id;
	
	@Column(name = "SRP_ERE_ID", reverseAttribute="parameters", composition=true)
	ScheduledReportEntity report;

	@Column(name = "SRP_NAME")
	String name;
	
	@Column(name = "SRP_STRVAL")
	@Nullable
	String stringValue;

	@Column(name = "SRP_DATVAL")
	@Nullable
	Date dateValue;

	@Column(name = "SRP_LONVAL")
	@Nullable
	Long longValue;

	@Column(name = "SRP_DOUVAL")
	@Nullable
	Double doubleValue;
	
	@Column(name = "SRP_BOOVAL")
	@Nullable
	Boolean booleanValue;

	@Column(name= "SRP_TYPE")
	ParameterType type;
}
