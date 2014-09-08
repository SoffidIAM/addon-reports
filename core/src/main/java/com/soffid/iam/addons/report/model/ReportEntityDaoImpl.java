//
// (C) 2013 Soffid
//
//

package com.soffid.iam.addons.report.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.soffid.iam.addons.report.api.Report;
import com.soffid.iam.addons.report.api.ReportParameter;

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
				users.add(ace.getRole().getNom()+" @ "+ace.getRole().getBaseDeDades().getCodi());
		}
		target.setAcl(users);
		target.setParameters(getReportParameterEntityDao().toReportParameterList(source.getParameters()));
	}

	@Override
	public void reportToEntity(Report source, ReportEntity target, boolean copyIfNull) {
		super.reportToEntity(source, target, copyIfNull);
		// Remove former identities
		LinkedList<String> newElements = new LinkedList( source.getAcl() );
		for ( Iterator<ReportACLEntity> it = target.getAcl().iterator(); it.hasNext();)
		{
			ReportACLEntity old = it.next();
			boolean found = false;
			for (Iterator<String> it2 = newElements.iterator(); it2.hasNext(); )
			{
				String user = it2.next();
				if (old.getUser() != null && old.getUser().getCodi().equals (user) ||
						old.getGroup() != null && old.getGroup().getCodi().equals (user) ||
						old.getRole() != null && user.equals(old.getRole().getNom()+" @ "+old.getRole().getBaseDeDades().getCodi()))
				{
					it2.remove();
					found = true;
					break;
				}
			}
			if (! found )
			{
				it.remove();
			}
		}
		// Add new identities
		for (String user: newElements)
		{
			ReportACLEntity ace = getReportACLEntityDao().newReportACLEntity();
			ace.setUser( getUsuariEntityDao().findByCodi(user) );
			ace.setGroup(getGrupEntityDao().findByCodi(user) );
			int i = user.indexOf(" @ ");
			if ( i > 0)
				ace.setRole( getRolEntityDao().findByNameAndDispatcher(user.substring(0, i), user.substring(i+3)));
			ace.setReport(target);
			target.getAcl().add(ace);
		}
		
		// Remove/update params
		List<ReportParameter> newParams = new LinkedList(source.getParameters());
		for (Iterator<ReportParameterEntity> it = target.getParameters().iterator(); it.hasNext();)
		{
			ReportParameterEntity rpe = it.next();
			boolean found = false;
			for (Iterator<ReportParameter> it2 = newParams.iterator(); it2.hasNext();)
			{
				ReportParameter rp = it2.next();
				if (rp.getId().equals (rpe.getId()))
				{
					getReportParameterEntityDao().reportParameterToEntity(rp, rpe, true);
					it2.remove();
					found = true;
					break;
				}
			}
			if (! found)
				it.remove(); 
		}
		// Add new parameters
		target.getParameters().addAll( 
				new HashSet<ReportParameterEntity>(
						getReportParameterEntityDao().reportParameterToEntityList(
								newParams)));
		for (ReportParameterEntity rpe: target.getParameters())
			rpe.setReport(target);
	}
}
