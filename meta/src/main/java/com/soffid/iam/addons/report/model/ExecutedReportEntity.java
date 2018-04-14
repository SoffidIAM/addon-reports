package com.soffid.iam.addons.report.model;

import java.util.Collection;
import java.util.Date;

import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ExecutedReportCriteria;
import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.DaoFinder;
import com.soffid.mda.annotation.Depends;
import com.soffid.mda.annotation.Entity;
import com.soffid.mda.annotation.Identifier;
import com.soffid.mda.annotation.Nullable;

import es.caib.seycon.ng.model.UsuariEntity;

@Entity (table="SCR_EXEREP")
@Depends( { ExecutedReport.class, UsuariEntity.class} )
public class ExecutedReportEntity {
	@Identifier
	@Nullable
	@Column(name="ERE_ID")
	Long id;
	
	@Column(name="ERE_NAME", length=128)
	String name;
	
	@Nullable
	@Column(name="ERE_PDFDOC", length=128)
	String pdfDocument;
	
	@Nullable
	@Column(name="ERE_HTMLDOC", length=128)
	String htmlDocument;
	
	@Nullable
	@Column(name="ERE_XMLDOC", length=128)
	String xmlDocument;
	
	@Nullable
	@Column(name="ERE_CSVDOC", length=128)
	String csvDocument;

	@Nullable
	@Column(name="ERE_XLSDOC", length=128)
	String xlsDocument;

	@Column(name="ERE_DONE", defaultValue="false")
	boolean done;
	
	@Column(name="ERE_ERROR", defaultValue="false")
	boolean error;
	
	@Column(name="ERE_ERRMSG", length=1024)
	@Nullable
	String errorMessage;
	
	@Column(name="ERE_REP_ID", reverseAttribute="executions")
	ReportEntity report;
	
	@Column(name="ERE_DATE")
	Date date;
	
	// DAO Methods
	
	@DaoFinder
	Collection<ExecutedReportEntity> findByCriteria (ExecutedReportCriteria criteria) { return null; }
	
	@DaoFinder("select r from com.soffid.iam.addons.report.model.ExecutedReportEntity as r where r.done=:done")
	Collection<ExecutedReportEntity> findPendingReports (boolean done) { return null; }
}
