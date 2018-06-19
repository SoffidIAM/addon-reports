package com.soffid.iam.addons.report.model;

import java.util.Collection;
import java.util.Date;

import com.soffid.iam.addons.report.api.ScheduledReport;
import com.soffid.iam.model.TenantEntity;
import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.DaoFinder;
import com.soffid.mda.annotation.Depends;
import com.soffid.mda.annotation.Description;
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

	@Nullable
	@Column(name="SRE_LASEXE")
	Date lastExecution;
	
	@Column(name="SRE_CREATION")
	Date creationDate;

	@Column(name="SRE_TEN_ID")
	TenantEntity tenant;

	//////////            Finders
	@DaoFinder("select re from com.soffid.iam.addons.report.model.ScheduledReportEntity as re "
			+ "where re.name like :name and re.tenant.id=:tenantId")
	Collection<ScheduledReportEntity> findByNameFilter (String name ) {
		return null;
	}

	
	@Description("Finder to find any scheduled report regardless the tenant it belongs")
	@DaoFinder("select re from com.soffid.iam.addons.report.model.ScheduledReportEntity as re ")
	Collection<ScheduledReportEntity> findAll ( ) {
		return null;
	}
	
	@Description("Finder to load a report regardless the tenant it belongs")
	@DaoFinder("select r from com.soffid.iam.addons.report.model.ScheduledReportEntity as r "
			+ "where r.id=:id")
	ScheduledReportEntity findById (Long id) { return null; }

}
