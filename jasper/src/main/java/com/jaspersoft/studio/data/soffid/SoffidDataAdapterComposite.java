package com.jaspersoft.studio.data.soffid;

import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.jaspersoft.studio.JaspersoftStudioPlugin;
import com.jaspersoft.studio.data.ADataAdapterComposite;
import com.jaspersoft.studio.data.DataAdapterDescriptor;
import com.jaspersoft.studio.data.messages.Messages;
import com.jaspersoft.studio.data.secret.DataAdaptersSecretsProvider;
import com.jaspersoft.studio.swt.widgets.WSecretText;
import com.jaspersoft.studio.utils.UIUtil;

import net.sf.jasperreports.data.DataAdapter;
import net.sf.jasperreports.engine.JasperReportsContext;

public class SoffidDataAdapterComposite extends ADataAdapterComposite {

	protected Text textJDBCUrl;
	protected Text textUsername;
	protected WSecretText textPassword;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public SoffidDataAdapterComposite(Composite parent, int style, JasperReportsContext jrContext) {
		super(parent, style, jrContext);
		System.out.println("---------------------------- !XXX! -----------------------------------");
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);

		createPreWidgets(this);

		Label lbl = new Label(this, SWT.NONE);
		lbl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		lbl.setText(Messages.SoffidDataAdapterComposite_url);
		textJDBCUrl = new Text(this, SWT.BORDER);
		textJDBCUrl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createUserPass(this);
		
		contextId = "adapter_SOFFID";
	}

	protected void createPreWidgets(Composite parent) {

	}

	protected void createUserPass(final Composite composite) {
		Label lbl = new Label(composite, SWT.NONE);
		lbl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		lbl.setText(Messages.JDBCDataAdapterComposite_username);

		textUsername = new Text(composite, SWT.BORDER);
		textUsername.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lbl = new Label(composite, SWT.NONE);
		lbl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		lbl.setText(Messages.JDBCDataAdapterComposite_password);

		textPassword = new WSecretText(composite, SWT.BORDER | SWT.PASSWORD);
		textPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// btnSavePassword = new Button(this, SWT.CHECK);
		// btnSavePassword.setText("Save Password");
		// new Label(this, SWT.NONE);

		new Label(composite, SWT.NONE);
		Composite c = new Composite(composite, SWT.NONE);
		c.setLayout(new GridLayout(2, false));

		lbl = new Label(c, SWT.NONE | SWT.BOLD);
		lbl.setText(Messages.JDBCDataAdapterComposite_attentionlable);
		UIUtil.setBold(lbl);

		lbl = new Label(c, SWT.NONE);
		lbl.setText(Messages.JDBCDataAdapterComposite_attention);
	}

	/**
	 * Set the DataAdapter to edit. The UI will be updated with the content of this
	 * adapter
	 * 
	 * @param dataAdapter
	 */
	public void setDataAdapter(SoffidDataAdapterDescriptor editingDataAdapter) {
		super.setDataAdapter(editingDataAdapter);

		SoffidDataAdapterImpl jdbcDataAdapter = (SoffidDataAdapterImpl) dataAdapterDesc.getDataAdapter();
		if (!textPassword.isWidgetConfigured())
			textPassword.loadSecret(DataAdaptersSecretsProvider.SECRET_NODE_ID, textPassword.getText());

	}

	@Override
	protected void bindWidgets(DataAdapter dataAdapter) {
		SoffidDataAdapterImpl jdbcDataAdapter = (SoffidDataAdapterImpl) dataAdapter;

		bindingContext.bindValue(SWTObservables.observeText(textUsername, SWT.Modify),
				PojoObservables.observeValue(dataAdapter, "userName")); //$NON-NLS-1$
		bindingContext.bindValue(SWTObservables.observeText(textPassword, SWT.Modify),
				PojoObservables.observeValue(dataAdapter, "password")); //$NON-NLS-1$
		bindingContext.bindValue(SWTObservables.observeText(textJDBCUrl, SWT.Modify),
				PojoObservables.observeValue(dataAdapter, "url")); //$NON-NLS-1$
	}

	public DataAdapterDescriptor getDataAdapter() {
		if (dataAdapterDesc == null) {
			dataAdapterDesc = new SoffidDataAdapterDescriptor();
		}

		SoffidDataAdapterImpl jdbcDataAdapter = (SoffidDataAdapterImpl) dataAdapterDesc.getDataAdapter();

		jdbcDataAdapter.setUserName(textUsername.getText());
		jdbcDataAdapter.setPassword(textPassword.getText());
		jdbcDataAdapter.setUrl(textJDBCUrl.getText());

		return dataAdapterDesc;
	}

	protected String contextId;

	@Override
	public String getHelpContextId() {
		return PREFIX.concat(contextId);
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	@Override
	public void performAdditionalUpdates() {
		if (JaspersoftStudioPlugin.shouldUseSecureStorage()) {
			textPassword.persistSecret();
			// update the "password" replacing it with the UUID key saved in secure
			// preferences
			SoffidDataAdapterImpl jdbcDataAdapter = (SoffidDataAdapterImpl) dataAdapterDesc.getDataAdapter();
			jdbcDataAdapter.setPassword(textPassword.getUUIDKey());
		}
	}
}
