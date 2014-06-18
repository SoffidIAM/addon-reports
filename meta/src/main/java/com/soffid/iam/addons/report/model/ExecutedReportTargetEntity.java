package com.soffid.iam.addons.report.model;

import java.util.Date;

import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.Entity;
import com.soffid.mda.annotation.Identifier;
import com.soffid.mda.annotation.Nullable;

import es.caib.seycon.ng.model.UsuariEntity;

@Entity(table="SCR_EXETAR")
public class ExecutedReportTargetEntity {
	@Identifier
	@Nullable
	@Column(name="RET_ID")
	Long id;
	
	@Column(name="RET_USU_ID", cascadeDelete=true)
	UsuariEntity user;
		
	@Column(name="RET_ERE_ID", reverseAttribute="acl", composition=true) 
	ExecutedReportEntity report;
}
