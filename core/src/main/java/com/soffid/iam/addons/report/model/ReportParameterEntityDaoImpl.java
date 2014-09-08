//
// (C) 2013 Soffid
//
//

package com.soffid.iam.addons.report.model;

import com.soffid.iam.addons.report.api.ReportParameter;

/**
 * DAO ReportParameterEntity implementation
 */
public class ReportParameterEntityDaoImpl extends ReportParameterEntityDaoBase
{

	@Override
	public void toReportParameter(ReportParameterEntity source,
			ReportParameter target) {
		super.toReportParameter(source, target);
		target.setReportId(source.getReport().getId());
	}

	@Override
	public void reportParameterToEntity(ReportParameter source,
			ReportParameterEntity target, boolean copyIfNull) {
		super.reportParameterToEntity(source, target, copyIfNull);
		if (source.getReportId() != null)
			target.setReport(getReportEntityDao().load(source.getReportId()));
	}
}
