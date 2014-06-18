package com.soffid.iam.addons.report.model;

import com.soffid.iam.addons.report.api.ReportParameter;
import com.soffid.iam.addons.report.api.ParameterType;
import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.Depends;
import com.soffid.mda.annotation.Entity;
import com.soffid.mda.annotation.Identifier;

@Entity(table="SCR_PARAM")
@Depends ({
	ReportParameter.class
})
public class ReportParameterEntity {
	@Identifier
	@Column(name="PAR_ID")
	Long id;
	
	@Column(name="PAR_NAME", length=128)
	String name;
	
	@Column(name="PAR_DESCR", length=512)
	String description;
	
	@Column(name="PAR_TYPE", length=32)
	ParameterType type;

	@Column(name="PAR_REP_ID", composition=true)
	ReportEntity report;
}
