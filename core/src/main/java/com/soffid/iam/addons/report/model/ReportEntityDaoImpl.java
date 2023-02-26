//
// (C) 2013 Soffid
//
//

package com.soffid.iam.addons.report.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.soffid.iam.addons.report.api.ParameterType;
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
				users.add(ace.getUser().getUserName());
			else if (ace.getGroup() != null)
				users.add(ace.getGroup().getName());
			else if (ace.getRole() != null)
				users.add(ace.getRole().getName()+" @ "+ace.getRole().getSystem().getName());
		}
		target.setAcl(users);
		final List<ReportParameter> list = getReportParameterEntityDao().toReportParameterList(source.getParameters());
		target.setParameters(list);
		Collections.sort(list, new Comparator<ReportParameter>() {
		    public int compare(ReportParameter p1, ReportParameter p2) {
		    	long o1 = p1.getOrder() == null ? 0: p1.getOrder().longValue();
		    	long o2 = p2.getOrder() == null ? 0: p2.getOrder().longValue();
		    	return o2 > o1 ? -1: o2 < 1 ? +1: 0;
		    }
		});
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
				if (old.getUser() != null && old.getUser().getUserName().equals (user) ||
						old.getGroup() != null && old.getGroup().getName().equals (user) ||
						old.getRole() != null && user.equals(old.getRole().getName()+" @ "+
								old.getRole().getSystem().getName()))
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
			ace.setUser( getUserEntityDao().findByUserName(user) );
			ace.setGroup(getGroupEntityDao().findByName(user) );
			int i = user.indexOf(" @ ");
			if ( i > 0)
				ace.setRole( getRoleEntityDao().findByNameAndSystem(user.substring(0, i), user.substring(i+3)));
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
				if (rp.getId() != null &&  rp.getId().equals (rpe.getId()))
				{
					getReportParameterEntityDao().reportParameterToEntity(rp, rpe, true);
					rpe.setReport(target);
					it2.remove();
					found = true;
					break;
				}
			}
			if (! found)
				it.remove(); 
		}
		for (ReportParameter rp: newParams) {
			ReportParameterEntity rpe = getReportParameterEntityDao().reportParameterToEntity(rp);
			rpe.setReport(target);
			target.getParameters().add(rpe);
		}
	}

	@Override
	public void create(ReportEntity entity) {
		super.create(entity);
		updateParams(entity);
	}

	@Override
	public void update(ReportEntity entity) {
		super.update(entity);
		updateParams(entity);
	}

	private void updateParams(ReportEntity entity) {
		for ( ReportParameterEntity p: entity.getParameters()) {
			p.setReport(entity);
			if (p.getId() == null)
				getReportParameterEntityDao().create(p);
			else
				getReportParameterEntityDao().update(p);
		}
	}
}
