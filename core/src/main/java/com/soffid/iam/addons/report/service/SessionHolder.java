package com.soffid.iam.addons.report.service;

import org.hibernate.Session;

public class SessionHolder {
	public static ThreadLocal<Session> hibernateSession = new ThreadLocal<Session>();
}
