package com.soffid.iam.addons.acl.service;

import java.util.HashSet;
import java.util.Set;

import com.soffid.iam.addons.acl.api.AccessControlList;
import com.soffid.iam.api.RoleGrant;
import com.soffid.iam.model.GroupEntity;
import com.soffid.iam.model.UserEntity;
import com.soffid.iam.model.UserGroupEntity;

import es.caib.seycon.ng.comu.RolGrant;

public class ACLServiceImpl extends ACLServiceBase {

	@Override
	protected AccessControlList handleExpandUser(long userId) throws Exception {
		AccessControlList acl = new AccessControlList();
		acl.setGroups( new HashSet<Long>());
		acl.setUsers( new HashSet<Long>());
		acl.setRoles( new HashSet<Long>());
		
		acl.getUsers().add (userId);
		for (RoleGrant rg: getApplicationService().findEffectiveRoleGrantByUser(userId) )
		{
			acl.getRoles().add(rg.getRoleId());
		}
		
		UserEntity ue = getUserEntityDao().load(userId);
		if (ue != null)
		{
			recursivelyAddGroup (acl, ue.getPrimaryGroup());
			for (UserGroupEntity uge: ue.getSecondaryGroups())
			{
				recursivelyAddGroup (acl, uge.getGroup());
			}
		}
		
		return acl;
	}

	private void recursivelyAddGroup(AccessControlList acl,
			GroupEntity grupPrimary) {
		GroupEntity g = grupPrimary;
		while ( g != null && ! acl.getGroups().contains(g.getId()))
		{
			acl.getGroups().add(g.getId());
			g = g.getParent();
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
		for (RoleGrant rg: getApplicationService().findEffectiveRoleGrantByAccount(accountId) )
		{
			if (acl.getRoles().contains(rg.getRoleId()))
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
		
		acl2.getUsers().addAll(acl2.getUsers());
		for (Long groupId: acl.getGroups())
		{
			GroupEntity ge = getGroupEntityDao().load(groupId);
			addGroupMembers (ge, acl2.getUsers());
		}

		for (Long roleId: acl.getRoles())
		{
			for (RoleGrant grant: getApplicationService().findEffectiveRoleGrantsByRoleId(roleId))
			{
				if (grant.getUser() != null)
				{
					UserEntity ue = getUserEntityDao().findByUserName(grant.getUser());
					if (ue != null)
						acl2.getUsers().add(ue.getId());
				}
			}
		}
		
		return acl2;
		
	}

	private void addGroupMembers(GroupEntity ge, Set<Long> users) {
		for ( UserEntity ue: ge.getPrimaryGroupUsers())
			users.add(ue.getId());

		for ( UserGroupEntity ue: ge.getSecondaryGroupUsers())
			users.add(ue.getUser().getId());
		
		for (GroupEntity child: ge.getChildrens())
			addGroupMembers(child, users);
}


}
