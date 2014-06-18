package com.soffid.iam.addons.acl.api;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import com.soffid.mda.annotation.Attribute;
import com.soffid.mda.annotation.ValueObject;

@ValueObject
public class AccessControlList {
	@Attribute (defaultValue="new java.util.HashSet<Long>()")
	Set<Long> users;
	@Attribute (defaultValue="new java.util.HashSet<Long>()")
	Set<Long> roles; 
	@Attribute (defaultValue="new java.util.HashSet<Long>()")
	Set<Long> groups;	
}
 