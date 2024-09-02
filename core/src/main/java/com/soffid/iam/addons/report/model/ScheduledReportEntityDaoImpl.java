//
// (C) 2013 Soffid
//
//

package com.soffid.iam.addons.report.model;

import it.sauronsoftware.cron4j.Predictor;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.soffid.iam.addons.report.api.ParameterValue;
import com.soffid.iam.addons.report.api.ScheduledReport;

/**
 * DAO ScheduledReportEntity implementation
 */
public class ScheduledReportEntityDaoImpl extends ScheduledReportEntityDaoBase
{

	@Override
	public void toScheduledReport(ScheduledReportEntity source,
			ScheduledReport target) {
		super.toScheduledReport(source, target);
		Collection<String> users = new LinkedList<String>();
		for (ScheduledReportTargetEntity ace: source.getAcl())
		{
			if (ace.getUser() != null)
				users.add(ace.getUser().getUserName());
			else if (ace.getGroup() != null)
				users.add(ace.getGroup().getName());
			else if (ace.getRole() != null)
				users.add(ace.getRole().getName()+" @ "+ace.getRole().getSystem().getName());
		}
		target.setTarget(users);
		target.setReportId(source.getReport().getId());
		target.setReportName(source.getReport().getName());
		
		target.setParams(getScheduledReportParameterEntityDao().toParameterValueList(source.getParameters()));

		String [] parse = source.getCronExpression().split(" ");
		if (parse.length > 0) target.setCronMinute(parse[0]);
		if (parse.length > 1) target.setCronHour(parse[1]);
		if (parse.length > 2) target.setCronDayOfMonth(parse[2]);
		if (parse.length > 3) target.setCronMonth(parse[3]);
		if (parse.length > 4) target.setCronDayOfWeek(parse[4]);
		
		if (source.getLastExecution() == null)
		{
			Predictor predictor = new Predictor (source.getCronExpression(), source.getCreationDate() );
			target.setNextExecution(predictor.nextMatchingDate());
		} else {
			Predictor predictor = new Predictor (source.getCronExpression(), source.getLastExecution() );
			target.setNextExecution(predictor.nextMatchingDate());
		}
	}

	@Override
	public void scheduledReportToEntity(ScheduledReport source,
			ScheduledReportEntity target, boolean copyIfNull) {
		super.scheduledReportToEntity(source, target, copyIfNull);
		// Populate identities
		LinkedList<ScheduledReportTargetEntity> olds = new LinkedList( target.getAcl() );
		for (String user: source.getTarget())
		{
			boolean found = false;
			for (ScheduledReportTargetEntity old: olds)
			{
				if (old.getUser() != null && old.getUser().getUserName().equals (user) ||
						old.getGroup() != null && old.getGroup().getName().equals (user) ||
						old.getRole() != null && user.equals(old.getRole().getName()+" @ "+
								old.getRole().getSystem().getName()))
				{
					olds.remove(old);
					found = true;
					break;
				}
			}
			if (! found)
			{
				ScheduledReportTargetEntity ace = getScheduledReportTargetEntityDao().newScheduledReportTargetEntity();
				ace.setUser( getUserEntityDao().findByUserName(user) );
				ace.setGroup(getGroupEntityDao().findByName(user) );
				int i = user.indexOf(" @ ");
				if ( i > 0)
					ace.setRole( getRoleEntityDao().findByNameAndSystem(user.substring(0, i), user.substring(i+3)));
				else {
					i = user.indexOf("@");
					if ( i > 0)
						ace.setRole( getRoleEntityDao().findByNameAndSystem(user.substring(0, i), user.substring(i+1)));
					
				}
				ace.setReport(target);
				target.getAcl().add(ace);
			}
		}
		target.getAcl().removeAll(olds);
		
		// Populate params
		target.getParameters().clear();
		
		for (ParameterValue pv: source.getParams())
		{
			if (pv.isMulti()) {
				java.util.List list = (List) pv.getValue();
				if (list == null || list.isEmpty()) {
					ParameterValue pv2 = new ParameterValue(pv);
					pv2.setValue(null);
					ScheduledReportParameterEntity erpe = getScheduledReportParameterEntityDao().parameterValueToEntity(pv2);
					erpe.setReport(target);
					target.getParameters().add(erpe);
					getScheduledReportParameterEntityDao().create(erpe);
				} else for (Object v: list) {
					ParameterValue pv2 = new ParameterValue(pv);
					pv2.setValue(v);
					ScheduledReportParameterEntity erpe = getScheduledReportParameterEntityDao().parameterValueToEntity(pv2);
					erpe.setReport(target);
					target.getParameters().add(erpe);
					getScheduledReportParameterEntityDao().create(erpe);
				}
			} else {
				ScheduledReportParameterEntity erpe = getScheduledReportParameterEntityDao().parameterValueToEntity(pv);
				erpe.setReport(target);
				target.getParameters().add(erpe);
				getScheduledReportParameterEntityDao().create(erpe);
			}
		}
		
		// Adjusta cron expression
		target.setCronExpression(normalize(source.getCronMinute())+ " "+
				normalize(source.getCronHour())+" " +
				normalize(source.getCronDayOfMonth())+ " "+
				normalize(source.getCronMonth())+ " "+
				normalize(source.getCronDayOfWeek()));
		if (target.getCreationDate() == null)
			target.setCreationDate(new Date());
	
		target.setReport(getReportEntityDao().load(source.getReportId()));
	}

	private String normalize(String part) {
		if (part == null || part.trim().length() == 0)
			return "*";
		else
			return part.replace(' ', ',');
	}
}
