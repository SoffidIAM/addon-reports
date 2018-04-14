//
// (C) 2013 Soffid
//
//

package com.soffid.iam.addons.report.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.hibernate.LockMode;

import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ParameterValue;

/**
 * DAO ExecutedReportEntity implementation
 */
public class ExecutedReportEntityDaoImpl extends ExecutedReportEntityDaoBase
{

	@Override
	public void toExecutedReport(ExecutedReportEntity source,
			ExecutedReport target) {
		super.toExecutedReport(source, target);
		Collection<String> users = new LinkedList<String>();
		for (ExecutedReportTargetEntity erte: source.getAcl())
		{
			users.add(erte.getUser().getUserName());
		}
		target.setUsers(users);
		target.setParams(getExecutedReportParameterEntityDao().toParameterValueList(source.getParameters()));
		
		target.setReportId(source.getReport().getId());
	}

	@Override
	public void executedReportToEntity(ExecutedReport source,
			ExecutedReportEntity target, boolean copyIfNull) {
		super.executedReportToEntity(source, target, copyIfNull);
		
		// Populate users
		LinkedList<ExecutedReportTargetEntity> olds = new LinkedList( target.getAcl() );
		for (String user: source.getUsers())
		{
			boolean found = false;
			for (ExecutedReportTargetEntity old: olds)
			{
				if (old.getUser().getUserName().equals (user))
				{
					olds.remove(old);
					found = true;
					break;
				}
			}
			if (! found)
			{
				ExecutedReportTargetEntity erte = getExecutedReportTargetEntityDao().newExecutedReportTargetEntity();
				erte.setReport(target);
				erte.setUser( getUserEntityDao().findByUserName(user));
				target.getAcl().add(erte);
			}
		}
		target.getAcl().removeAll(olds);
		
		target.setReport(getReportEntityDao().load(source.getReportId()));
		
		// Populate params
		target.setParameters( 
				new HashSet<ExecutedReportParameterEntity>(
						getExecutedReportParameterEntityDao().parameterValueToEntityList(
								source.getParams())));
		for (ExecutedReportParameterEntity erpe: target.getParameters())
			erpe.setReport(target);
	}

	@Override
	protected void handleLock(ExecutedReportEntity entity) throws Exception {
		getSession().lock(entity, LockMode.UPGRADE);
	}
	
}
