package com.soffid.iam.addons.report.service;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.soffid.iam.addons.acl.service.ACLService;
import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.Report;
import com.soffid.iam.addons.report.api.ScheduledReport;
import com.soffid.iam.addons.report.model.ExecutedReportEntity;
import com.soffid.iam.addons.report.model.ExecutedReportParameterEntity;
import com.soffid.iam.addons.report.model.ExecutedReportTargetEntity;
import com.soffid.iam.addons.report.model.ReportEntity;
import com.soffid.iam.addons.report.model.ScheduledReportEntity;
import com.soffid.iam.doc.api.DocumentReference;
import com.soffid.iam.doc.service.DocumentService;
import com.soffid.iam.service.MailService;
import com.soffid.mda.annotation.Depends;
import com.soffid.mda.annotation.Description;
import com.soffid.mda.annotation.Service;

import es.caib.seycon.ng.model.UsuariEntity;
import es.caib.seycon.ng.servei.ConfiguracioService;

@Service(internal=true)
@Depends({ReportService.class, ExecutedReportEntity.class, ScheduledReportEntity.class, 
		ReportEntity.class, UsuariEntity.class, ExecutedReportTargetEntity.class, ExecutedReportParameterEntity.class,
		MailService.class,
		DocumentService.class,
		ConfiguracioService.class,
		ACLService.class})
public class ReportSchedulerService {

	@Description ("Guess the next scheduled report to schedule")
	List<ScheduledReport> getScheduledReport () { return null ; }
	
	@Description ("Guess pending reports to inmediately run")
	List<ExecutedReport> getPendingReports () { return null ; }
	
	@Description ("Gets a report document reference")
	DocumentReference getReportDocument (long id) { return null ; }
	
	// Internal service
	@Description ("Executes a report")
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void updateReport(ExecutedReport report) {}
	
	@Description ("Creates a report")
	public ExecutedReport startReport(ScheduledReport report) {return null;}

}

