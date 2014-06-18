package com.soffid.iam.addons.report.model;

import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.Entity;
import com.soffid.mda.annotation.Identifier;
import com.soffid.mda.annotation.Nullable;

import es.caib.seycon.ng.model.GrupEntity;
import es.caib.seycon.ng.model.RolEntity;
import es.caib.seycon.ng.model.UsuariEntity;


@Entity(table="SCR_REPACL")
public class ReportACLEntity {
	@Identifier
	@Column(name="RAC_ID")
	Long id;
	
	@Nullable
	@Column(name="RAC_IDUSU", cascadeDelete = true)
	UsuariEntity user;
	
	@Nullable
	@Column(name="RAC_IDGRU", cascadeDelete = true)
	GrupEntity group;
	
	@Nullable
	@Column(name="RAC_IDROL", cascadeDelete = true)
	RolEntity role;
	
	@Column(name="RAC_REP_ID", composition=true)
	ReportEntity report;
}
