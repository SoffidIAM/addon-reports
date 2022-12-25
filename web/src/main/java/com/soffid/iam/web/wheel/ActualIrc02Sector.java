package com.soffid.iam.web.wheel;

import java.util.Date;
import java.util.LinkedList;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.UiException;

import com.soffid.iam.EJBLocator;
import com.soffid.iam.addons.report.api.ParameterType;
import com.soffid.iam.addons.report.api.ParameterValue;
import com.soffid.iam.addons.report.api.Report;
import com.soffid.iam.addons.report.api.ScheduledReport;
import com.soffid.iam.addons.report.service.ejb.ReportServiceHome;

import es.caib.seycon.ng.exception.InternalErrorException;
import es.caib.zkib.zkiblaf.Application;
import es.caib.zkib.zkiblaf.Missatgebox;

public class ActualIrc02Sector {
	
	private com.soffid.iam.addons.report.service.ejb.ReportService reportService;

	public ActualIrc02Sector() throws NamingException {
		reportService = (com.soffid.iam.addons.report.service.ejb.ReportService) new InitialContext().lookup(ReportServiceHome.JNDI_NAME);
	}
	
	public boolean isDone() throws InternalErrorException, NamingException {
		return ! reportService.findScheduledReports(null).isEmpty();
	}
	
	public void activate() {
		Missatgebox.avis(Labels.getLabel("report.wheel.explanation"), e -> {
			for (Report rep: reportService.findReports("Risk report", true)) {
				ScheduledReport r = new ScheduledReport();
				r.setCreationDate(new Date());
				r.setCronDayOfMonth("*");
				r.setCronDayOfWeek("1");
				r.setCronHour("6");
				r.setCronMinute("0");
				r.setCronMonth("*");
				r.setName("Weekly risk report");
				r.setScheduled(true);
				r.setTarget(new LinkedList<>());
				r.getTarget().add("SOFFID_ADMIN@soffid");
				r.setReportId(rep.getId());
				r.setParams(new LinkedList<>());
				r.getParams().add(new ParameterValue(null, "all", ParameterType.BOOLEAN_PARAM , true));
				r.getParams().add(new ParameterValue(null, "forbiddenRisk", ParameterType.BOOLEAN_PARAM, true));
				r.getParams().add(new ParameterValue(null, "highRisk", ParameterType.BOOLEAN_PARAM, true));
				r.getParams().add(new ParameterValue(null, "app", ParameterType.STRING_PARAM, ""));
				r.getParams().add(new ParameterValue(null, "lowRisk", ParameterType.BOOLEAN_PARAM, true));
				r = reportService.create(r);
				Application.jumpTo("/addon/report/report.zul?wizard=openSchedule&id="+r.getId());
				break;
			}
		});
	}
}
