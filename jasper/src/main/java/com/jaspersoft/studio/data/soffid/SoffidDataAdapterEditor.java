/*******************************************************************************
 * Copyright (C) 2010 - 2016. TIBCO Software Inc. 
 * All Rights Reserved. Confidential & Proprietary.
 ******************************************************************************/
package com.jaspersoft.studio.data.soffid;

import net.sf.jasperreports.engine.JasperReportsContext;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import com.jaspersoft.studio.data.ADataAdapterComposite;
import com.jaspersoft.studio.data.DataAdapterDescriptor;
import com.jaspersoft.studio.data.DataAdapterEditor;

/*
 * @author gtoffoli
 *
 */
public class SoffidDataAdapterEditor implements DataAdapterEditor {

	protected SoffidDataAdapterComposite composite = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jaspersoft.studio.data.DataAdapterEditor#getComposite(org.eclipse
	 * .swt.widgets.Composite, int)
	 */
	public ADataAdapterComposite getComposite(Composite parent, int style,
 WizardPage wizardPage, JasperReportsContext jrContext) {

		if (composite == null || composite.getParent() != parent) {
			if (composite != null)
				composite.dispose();
			composite = new SoffidDataAdapterComposite(parent, style, jrContext);
		}
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jaspersoft.studio.data.DataAdapterEditor#getDataAdapter()
	 */
	public DataAdapterDescriptor getDataAdapter() {

		if (composite != null) {
			return composite.getDataAdapter();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jaspersoft.studio.data.DataAdapterEditor#getHelpContextId()
	 */
	public String getHelpContextId() {
		return composite.getHelpContextId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jaspersoft.studio.data.DataAdapterEditor#setDataAdapter(com.jaspersoft
	 * .studio.data.DataAdapter)
	 */
	public void setDataAdapter(DataAdapterDescriptor dataAdapter) {

		if (composite != null
				&& dataAdapter instanceof SoffidDataAdapterDescriptor) {
			composite.setDataAdapter((SoffidDataAdapterDescriptor) dataAdapter);
		}
	}
}
