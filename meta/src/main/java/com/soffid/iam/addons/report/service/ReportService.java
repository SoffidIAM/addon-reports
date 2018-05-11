package com.soffid.iam.addons.report.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import com.soffid.iam.addons.acl.service.ACLService;
import com.soffid.iam.doc.service.DocumentService;
import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ExecutedReportCriteria;
import com.soffid.iam.addons.report.api.Report;
import com.soffid.iam.addons.report.api.ScheduledReport;
import com.soffid.iam.addons.report.model.ExecutedReportEntity;
import com.soffid.iam.addons.report.model.ExecutedReportParameterEntity;
import com.soffid.iam.addons.report.model.ExecutedReportTargetEntity;
import com.soffid.iam.addons.report.model.ReportACLEntity;
import com.soffid.iam.addons.report.model.ReportEntity;
import com.soffid.iam.addons.report.model.ReportParameterEntity;
import com.soffid.iam.addons.report.model.ScheduledReportEntity;
import com.soffid.iam.addons.report.model.ScheduledReportParameterEntity;
import com.soffid.iam.addons.report.model.ScheduledReportTargetEntity;
import com.soffid.iam.addons.report.roles.Admin;
import com.soffid.iam.addons.report.roles.Query;
import com.soffid.iam.addons.report.roles.Schedule;
import com.soffid.mda.annotation.Depends;
import com.soffid.mda.annotation.Description;
import com.soffid.mda.annotation.Nullable;
import com.soffid.mda.annotation.Operation;
import com.soffid.mda.annotation.Service;

import es.caib.seycon.ng.model.UsuariEntity;
import es.caib.seycon.ng.servei.AplicacioService;
import es.caib.seycon.ng.servei.ConfiguracioService;
import es.caib.seycon.ng.servei.GrupService;
import es.caib.seycon.ng.servei.UsuariService;

@Service
@Depends({
	// DAOS
		ReportEntity.class, ReportACLEntity.class, ReportParameterEntity.class,
		ScheduledReportEntity.class, ScheduledReportTargetEntity.class, ScheduledReportParameterEntity.class,
		ExecutedReportEntity.class, ExecutedReportTargetEntity.class, ExecutedReportParameterEntity.class,
	// Services
		ReportSchedulerService.class,
		
		DocumentService.class, ACLService.class,

		UsuariService.class, GrupService.class, AplicacioService.class, ConfiguracioService.class,
		
		UsuariEntity.class
		  
})
public class ReportService {
	
	// Operations of report definitions
	
	@Operation(grantees={Admin.class, Query.class, Schedule.class})
	@Description("Uploads a crystal report. It's parsed and registered into database")
	public Collection<Report> findReports (@Nullable String name, boolean exactMatch) { return null; }
	
	@Operation(grantees={Admin.class})
	@Description("Uploads a crystal report. It's parsed and registered into database")
	public Report upload (InputStream report) { return null; }

	@Operation(grantees={Admin.class})
	@Description("Removes a report definition, its executions and schedules")
	public void remove (Report report) {}
	
	@Operation(grantees={Admin.class})
	@Description("Updates a report definition. No parameter can be created, only updates are allowed")
	public void update (Report report) {}
	
	// Schedule reports
	
	@Operation(grantees={Schedule.class})
	@Description("Schedules a report to be executed on a given time")
	public ScheduledReport create (ScheduledReport schedule) {return null;}
	
	@Operation(grantees={Schedule.class})
	@Description("Changes a scheduled report")
	public void update (ScheduledReport schedule) {}

	@Operation(grantees={Schedule.class})
	@Description("Removes a scheduled report")
	public void remove (ScheduledReport schedule) {}


	@Operation(grantees={Schedule.class})
	@Description("Finds scheduled reports")
	public Collection<ScheduledReport> findScheduledReports (@Nullable String name) { return null; }
	
	// Execute reports
	
	@Operation(grantees={Query.class})
	@Description("Schedules a report to be executed immediately")
	public ExecutedReport launchReport (ScheduledReport schedule) {return null;}
	
	@Operation(grantees={Query.class})
	@Description("Gets a report content")
	public ExecutedReport getExecutedReportStatus (long reportI) { return null; }
	
	@Operation(grantees={Query.class})
	@Description("Gets a report content. Format can be 'xml', 'html' or 'pdf'")
	public void writeReportContent (ExecutedReport report, String format, OutputStream pdfStream) { }
	
	
	// Query executed (stored) reports
	
	@Operation(grantees={Query.class})
	@Description("Gets a report content")
	public Collection<ExecutedReport> findExecutedReports (ExecutedReportCriteria criteria) { return null; }
	
	@Operation(grantees={Query.class})
	@Description("Removes a executed report. In fact, removes the user from the targes of the report, and only remove the report if the user is the last on on the targets list")
	public void remove (ExecutedReport report) {
		
	}
	
	@Operation(grantees={Admin.class})
	public void generateDevelopmentEnvironment (OutputStream out) {}
	
	@Operation
	@Description("Process to remove expired reports")
	public void purgeExpiredReports () {}
}
