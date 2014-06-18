//
// (C) 2013 Soffid
//
//

package com.soffid.iam.addons.report.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import com.soffid.iam.addons.report.api.Report;

/**
 * DAO ReportEntity implementation
 */
public class ReportEntityDaoImpl extends ReportEntityDaoBase
{

	@Override
	public void toReport(ReportEntity source, Report target) {
		super.toReport(source, target);
		Collection<String> users = new LinkedList<String>();
		for (ReportACLEntity ace: source.getAcl())
		{
			if (ace.getUser() != null)
				users.add(ace.getUser().getCodi());
			else if (ace.getGroup() != null)
				users.add(ace.getGroup().getCodi());
			else if (ace.getRole() != null)
				users.add(ace.getRole().getNom()+"@"+ace.getRole().getBaseDeDades());
		}
		target.setAcl(users);
		target.setParameters(getReportParameterEntityDao().toReportParameterList(source.getParameters()));
	}

	@Override
	public void reportToEntity(Report source, ReportEntity target, boolean copyIfNull) {
		super.reportToEntity(source, target, copyIfNull);
		// Populate identities
		LinkedList<ReportACLEntity> olds = new LinkedList( target.getAcl() );
		for (String user: source.getAcl())
		{
			boolean found = false;
			for (ReportACLEntity old: olds)
			{
				if (old.getUser() != null && old.getUser().getCodi().equals (user) ||
						old.getGroup() != null && old.getGroup().getCodi().equals (user) ||
						old.getRole() != null && user.equals(old.getRole().getNom()+"@"+old.getRole().getBaseDeDades().getCodi()))
				{
					olds.remove(old);
					found = true;
					break;
				}
			}
			if (! found)
			{
				ReportACLEntity ace = getReportACLEntityDao().newReportACLEntity();
				ace.setUser( getUsuariEntityDao().findByCodi(user) );
				ace.setGroup(getGrupEntityDao().findByCodi(user) );
				int i = user.indexOf('@');
				if ( i > 0)
					ace.setRole( getRolEntityDao().findByNameAndDispatcher(user.substring(0, i), user.substring(i+1)));
				ace.setReport(target);
				target.getAcl().add(ace);
			}
		}
		target.getAcl().removeAll(olds);
		
		// Populate params
		target.setParameters( 
				new HashSet<ReportParameterEntity>(
						getReportParameterEntityDao().reportParameterToEntityList(
								source.getParameters())));
		for (ReportParameterEntity rpe: target.getParameters())
			rpe.setReport(target);
	}
}
