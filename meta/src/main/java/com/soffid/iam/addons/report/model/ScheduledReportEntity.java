package com.soffid.iam.addons.report.model;

import java.util.Date;

import com.soffid.iam.addons.report.api.ScheduledReport;
import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.Depends;
import com.soffid.mda.annotation.Entity;
import com.soffid.mda.annotation.Identifier;
import com.soffid.mda.annotation.Nullable;

import es.caib.seycon.ng.model.GrupEntity;
import es.caib.seycon.ng.model.RolEntity;
import es.caib.seycon.ng.model.ServerEntity;
import es.caib.seycon.ng.model.UsuariEntity;

@Entity(table="SCR_SCHREP")
@Depends ({ ScheduledReport.class, UsuariEntity.class, GrupEntity.class, RolEntity.class})
public class ScheduledReportEntity {
	@Identifier
	@Nullable
	@Column(name="SRE_ID")
	Long id;
	
	@Column(name="SRE_NAME")
	String name;
	
	@Column(name="SRE_REP_ID", reverseAttribute="schedules")
	ReportEntity report;
	
	@Column(name="SRE_CRON")
	String cronExpression;

	@Column(name="SRE_SER_ID")
	ServerEntity server;
	
	@Column(name="SRE_LASEXE")
	Date lastExecution;
	
	@Column(name="SRE_CREATION")
	Date creationDate;

}
