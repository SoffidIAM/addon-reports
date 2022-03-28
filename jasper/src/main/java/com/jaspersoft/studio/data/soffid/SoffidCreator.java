/*******************************************************************************
 * Copyright (C) 2010 - 2016. TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 ******************************************************************************/
package com.jaspersoft.studio.data.soffid;

import java.util.UUID;

import net.sf.jasperreports.eclipse.util.SecureStorageUtils;

import org.eclipse.equinox.security.storage.StorageException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaspersoft.studio.JaspersoftStudioPlugin;
import com.jaspersoft.studio.data.Activator;
import com.jaspersoft.studio.data.DataAdapterDescriptor;
import com.jaspersoft.studio.data.adapter.IDataAdapterCreator;
import com.jaspersoft.studio.data.messages.Messages;
import com.jaspersoft.studio.data.secret.DataAdaptersSecretsProvider;

/**
 * Creator to build a JSS JDBC data adapter from the xml definition of an iReport JDBC 
 * data adapter
 * 
 * @author Orlandin Marco
 */
public class SoffidCreator implements IDataAdapterCreator {

	@Override
	public DataAdapterDescriptor buildFromXML(Document docXML) {
		SoffidDataAdapterImpl result = new SoffidDataAdapterImpl();
		
		System.out.println("**************** Creating data adapter");
		NamedNodeMap rootAttributes = docXML.getChildNodes().item(0).getAttributes();
		String connectionName = rootAttributes.getNamedItem("name").getTextContent(); //$NON-NLS-1$
		result.setName(connectionName);
		
		NodeList children = docXML.getChildNodes().item(0).getChildNodes();
		for(int i=0; i<children.getLength(); i++){
			Node node = children.item(i);
			if (node.getNodeName().equals("connectionParameter")){ //$NON-NLS-1$
				String paramName = node.getAttributes().getNamedItem("name").getTextContent(); //$NON-NLS-1$
				
				if (paramName.equals("Url")) result.setUrl(node.getTextContent()); //$NON-NLS-1$
				if (paramName.equals("Password")) result.setPassword(getPasswordValue(node.getTextContent())); //$NON-NLS-1$
				if (paramName.equals("UserName")) result.setUserName(node.getTextContent()); //$NON-NLS-1$
			}
		}
		SoffidDataAdapterDescriptor desc = new SoffidDataAdapterDescriptor();
		desc.setDataAdapter(result);
		return desc;
	}

	/* 
	 * Gets the secret storage key or the plain text password value.
	 */
	private String getPasswordValue(String passwordFieldTxt) {
		return JaspersoftStudioPlugin.shouldUseSecureStorage() 
				? getSecretStorageKey(passwordFieldTxt) : passwordFieldTxt;
	}

	@Override
	public String getID() {
		return "com.jaspersoft.ireport.designer.connection.SoffidConnection"; //$NON-NLS-1$
	}
	
	/*
	 * Returns the key that will be used to retrieve the information from 
	 * the secure preferences.
	 */
	private String getSecretStorageKey(String pass) {
		try {
			UUID uuidKey = UUID.randomUUID();
			SecureStorageUtils.saveToDefaultSecurePreferences(
					DataAdaptersSecretsProvider.SECRET_NODE_ID, uuidKey.toString(), pass);
			return uuidKey.toString();
		} catch (StorageException e) {
			Activator.getDefault().logError(Messages.JDBCCreator_ErrSecurPrefStorage,e);
		};
		// in case something goes wrong return the clear-text password
		// we will rely on back-compatibility
		return pass;
	}

}
