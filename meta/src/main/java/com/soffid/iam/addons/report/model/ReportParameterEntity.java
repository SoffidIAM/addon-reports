package com.soffid.iam.addons.report.model;

import com.soffid.iam.addons.report.api.ReportParameter;
import com.soffid.iam.addons.report.api.ParameterType;
import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.Depends;
import com.soffid.mda.annotation.Entity;
import com.soffid.mda.annotation.Identifier;
import com.soffid.mda.annotation.Nullable;

import es.caib.seycon.ng.comu.TypeEnumeration;

@Entity(table="SCR_PARAM")
@Depends ({
	ReportParameter.class
})
public class ReportParameterEntity {
	@Identifier
	@Column(name="PAR_ID")
	Long id;
	
	@Nullable @Column(name="PAR_ORDER")
	Long order;
	
	@Column(name="PAR_NAME", length=128)
	String name;
	
	@Column(name="PAR_DESCR", length=512)
	String description;
	
	@Nullable
	@Column(name="PAR_TYPE", length=32)
	ParameterType type;

	@Nullable
	@Column(name="PAR_DATATYP", length=32)
	TypeEnumeration dataType;

	@Column(name="PAR_REP_ID")
	ReportEntity report;
}
