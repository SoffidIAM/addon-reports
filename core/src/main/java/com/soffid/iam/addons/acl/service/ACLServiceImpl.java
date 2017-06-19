package com.soffid.iam.addons.acl.service;

import java.util.HashSet;
import java.util.Set;

import com.soffid.iam.addons.acl.api.AccessControlList;

import es.caib.seycon.ng.comu.RolGrant;
import es.caib.seycon.ng.model.GrupEntity;
import es.caib.seycon.ng.model.RolEntity;
import es.caib.seycon.ng.model.UsuariEntity;
import es.caib.seycon.ng.model.UsuariGrupEntity;

public class ACLServiceImpl extends ACLServiceBase {

	@Override
	protected AccessControlList handleExpandUser(long userId) throws Exception {
		AccessControlList acl = new AccessControlList();
		acl.setGroups( new HashSet<Long>());
		acl.setUsers( new HashSet<Long>());
		acl.setRoles( new HashSet<Long>());
		
		acl.getUsers().add (userId);
		for (es.caib.seycon.ng.comu.RolGrant rg: getAplicacioService().findEffectiveRolGrantByUser(userId) )
		{
			acl.getRoles().add(rg.getIdRol());
		}
		
		es.caib.seycon.ng.model.UsuariEntity ue = getUsuariEntityDao().load(userId);
		if (ue != null)
		{
			recursivelyAddGroup (acl, ue.getGrupPrimari());
			for (es.caib.seycon.ng.model.UsuariGrupEntity uge: ue.getGrupsSecundaris())
			{
				recursivelyAddGroup (acl, uge.getGrup());
			}
		}
		
		return acl;
	}

	private void recursivelyAddGroup(AccessControlList acl,
			es.caib.seycon.ng.model.GrupEntity grupPrimary) {
		es.caib.seycon.ng.model.GrupEntity g = grupPrimary;
		while ( g != null && ! acl.getGroups().contains(g.getId()))
		{
			acl.getGroups().add(g.getId());
			g = g.getPare();
		}
	}

	@Override
	protected boolean handleIsUserIncluded(long userId, AccessControlList acl)
			throws Exception {
		for (Long userId2: acl.getUsers())
		{
			if (userId2.equals(userId)) return true;
		}
		
		AccessControlList userAcl = expandUser(userId);
		for ( Long groupId: acl.getGroups())
		{
			if (userAcl.getGroups().contains(groupId))
				return true;
		}
		for ( Long roleId: acl.getRoles())
		{
			if (userAcl.getRoles().contains(roleId))
				return true;
		}
		return false;
	}

	@Override
	protected boolean handleIsAccountIncluded(long accountId, AccessControlList acl)
			throws Exception {
		for (es.caib.seycon.ng.comu.RolGrant rg: getAplicacioService().findEffectiveRolGrantByAccount(accountId) )
		{
			if (acl.getRoles().contains(rg.getIdRol()))
				return true;
		}
		return false;
	}

	@Override
	protected AccessControlList handleExpandACL(AccessControlList acl)
			throws Exception {
		AccessControlList acl2 = new AccessControlList();
		acl2.setGroups( new HashSet<Long>());
		acl2.setUsers( new HashSet<Long>());
		acl2.setRoles( new HashSet<Long>());
		
		acl2.getUsers().addAll(acl.getUsers());
		for (Long groupId: acl.getGroups())
		{
			GrupEntity ge = getGrupEntityDao().load(groupId);
			addGroupMembers (ge, acl2.getUsers());
		}

		for (Long roleId: acl.getRoles())
		{
			for (RolGrant grant: getAplicacioService().findEffectiveRolGrantsByRolId(roleId))
			{
				if (grant.getUser() != null)
				{
					UsuariEntity ue = getUsuariEntityDao().findByCodi(grant.getUser());
					if (ue != null)
						acl2.getUsers().add(ue.getId());
				}
			}
		}
		
		return acl2;
		
	}

	private void addGroupMembers(GrupEntity ge, Set<Long> users) {
		for ( UsuariEntity ue: ge.getUsuarisGrupPrimari())
			users.add(ue.getId());

		for ( UsuariGrupEntity ue: ge.getUsuarisGrupSecundari())
			users.add(ue.getUsuari().getId());
		
		for (GrupEntity child: ge.getFills())
			addGroupMembers(child, users);
}


}
