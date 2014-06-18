package com.soffid.iam.addons.report.model;

import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.Entity;
import com.soffid.mda.annotation.Identifier;
import com.soffid.mda.annotation.Nullable;

import es.caib.seycon.ng.model.GrupEntity;
import es.caib.seycon.ng.model.RolEntity;
import es.caib.seycon.ng.model.UsuariEntity;

@Entity(table="SCR_SCRETA")
public class ScheduledReportTargetEntity {
	@Identifier
	@Column(name="SRT_ID")
	Long id;
	
	@Nullable
	@Column(name="SRT_IDUSU", cascadeDelete = true)
	UsuariEntity user;
	
	@Nullable
	@Column(name="SRT_IDGRU", cascadeDelete = true)
	GrupEntity group;
	
	@Nullable
	@Column(name="SRT_IDROL", cascadeDelete = true)
	RolEntity role;
	
	@Column(name="SRT_SRE_ID", reverseAttribute="acl", composition=true)
	ScheduledReportEntity report;

}
