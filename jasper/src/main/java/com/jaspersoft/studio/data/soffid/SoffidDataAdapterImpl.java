package com.jaspersoft.studio.data.soffid;

import net.sf.jasperreports.data.AbstractDataAdapter;
import net.sf.jasperreports.data.qe.QueryExecuterDataAdapter;

public class SoffidDataAdapterImpl extends AbstractDataAdapter {
	private String userName;
	private String password = null;
	private String url;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
