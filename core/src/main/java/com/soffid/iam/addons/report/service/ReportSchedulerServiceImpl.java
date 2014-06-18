package com.soffid.iam.addons.report.service;

import java.util.Date;
import java.util.List;

import com.soffid.iam.addons.doc.api.DocumentReference;
import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ParameterValue;
import com.soffid.iam.addons.report.api.Report;
import com.soffid.iam.addons.report.api.ScheduledReport;
import com.soffid.iam.addons.report.model.ExecutedReportEntity;
import com.soffid.iam.addons.report.model.ExecutedReportEntityDao;
import com.soffid.iam.addons.report.model.ExecutedReportParameterEntity;
import com.soffid.iam.addons.report.model.ExecutedReportTargetEntity;
import com.soffid.iam.addons.report.model.ReportEntity;
import com.soffid.iam.addons.report.model.ReportEntityDao;
import com.soffid.iam.addons.report.model.ScheduledReportEntity;
import com.soffid.iam.addons.report.model.ScheduledReportEntityDao;

public class ReportSchedulerServiceImpl extends ReportSchedulerServiceBase {

	@Override
	protected List<ScheduledReport> handleGetScheduledReport() throws Exception {
		ScheduledReportEntityDao dao = getScheduledReportEntityDao();
		return dao.toScheduledReportList(dao.loadAll());
	}

	@Override
	protected List<ExecutedReport> handleGetPendingReports() throws Exception {
		ExecutedReportEntityDao dao = getExecutedReportEntityDao();
		return dao.toExecutedReportList(dao.findPendingReports());
	}

	@Override
	protected void handleUpdateReport(ExecutedReport report) throws Exception {
		ExecutedReportEntity ere = getExecutedReportEntityDao().load(report.getId());
		ere.setError(report.isError());
		ere.setErrorMessage(report.getErrorMessage());
		ere.setDone(report.isDone());
		ere.setPdfDocument(report.getPdfDocument());
		ere.setHtmlDocument(report.getHtmlDocument());
		ere.setXmlDocument(report.getXmlDocument());
		getExecutedReportEntityDao().update(ere);
	}

	@Override
	protected ExecutedReport handleStartReport(ScheduledReport report)
			throws Exception {
		ExecutedReportEntity ere = getExecutedReportEntityDao().newExecutedReportEntity();
		ere.setName(report.getName());
		ere.setDate(new Date());
		ere.setDone(false);
		ere.setError(false);
		ere.setReport(getReportEntityDao().load(report.getReportId()));
		
		for (String user: report.getTarget())
		{
			ExecutedReportTargetEntity erte = getExecutedReportTargetEntityDao().newExecutedReportTargetEntity();
			
			erte.setUser(getUsuariEntityDao().findByCodi(user));
			erte.setReport(ere);
			ere.getAcl().add(erte);
		}
		
		for (ParameterValue pm: report.getParams())
		{
			ExecutedReportParameterEntity erpe = getExecutedReportParameterEntityDao().parameterValueToEntity(pm);
			erpe.setReport(ere);
			ere.getParameters().add(erpe);
		}
		
		getExecutedReportEntityDao().create(ere);
		
		ScheduledReportEntity sre = getScheduledReportEntityDao().load(report.getId());
		sre.setLastExecution(new Date());
		getScheduledReportEntityDao().update(sre);
		
		return getExecutedReportEntityDao().toExecutedReport(ere);
	}

	@Override
	protected DocumentReference handleGetReportDocument(long id) throws Exception {
		ReportEntityDao dao = getReportEntityDao();
		ReportEntity re = dao.load(id);
		return new DocumentReference(re.getDocId());
	}

}
