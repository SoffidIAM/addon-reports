package com.soffid.iam.addons.acl.service;

import java.util.HashSet;

import com.soffid.iam.addons.acl.api.AccessControlList;

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


}
