package com.soffid.iam.addons.report.service.jasper;

import java.lang.reflect.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Query;
import org.hibernate.Session;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class JRSqlDataSource implements JRSoffidDataSource {
	private PreparedStatement stmt;
	boolean started = false;
	private ResultSet iterator;
	private ResultSetMetaData alias;
	
	public JRSqlDataSource(PreparedStatement q) {
		this.stmt = q;
	}

	public boolean next() throws JRException {
		try {
			init();
			if (!iterator.next()) {
				iterator.close();
				stmt.close();
				return false;
			} else {
				return true;
			}
		} catch (SQLException e) {
			throw new JRException("Error executing stmt", e);
		}
	}

	public void init() throws JRException {
		try {
			if (iterator == null) { 
				iterator = stmt.executeQuery();
				alias = iterator.getMetaData();
			}
		} catch (SQLException e) {
			throw new JRException("Error executing stmt", e);
		}
	}

	public Object getFieldValue(JRField jrField) throws JRException {
		try {
			for (int i = 0; i < alias.getColumnCount(); i++) {
				if (alias.getColumnLabel(i+1).equalsIgnoreCase(jrField.getName())) {
					return iterator.getObject(i+1);
				}
			}
		} catch (SQLException e) {
			throw new JRException("Error executing stmt", e);
		}
		return null;
	}

	@Override
	public Map<String, Object> getFields() throws JRException {
		try {
			Map<String, Object> r = new HashMap<>(); 
			for (int i = 0; i < alias.getColumnCount(); i++) {
				r.put(alias.getColumnLabel(i+1), iterator.getObject(i+1));
			}
			return r;
		} catch (SQLException e) {
			throw new JRException("Error executing stmt", e);
		}
	}

	@Override
	public List<String> getColumns() throws JRException {
		init();
		LinkedList<String> l = new LinkedList<String>();
		try {
			if (alias != null) 
				for (int i = 0; i < alias.getColumnCount(); i++) {
					l.add(alias.getColumnLabel(i+1));
				}
			return l;
		} catch (SQLException e) {
			throw new JRException("Error executing stmt", e);
		}
	}

	@Override
	public List<String> getColumnClasses() throws JRException {
		init();
		LinkedList<String> l = new LinkedList<String>();
		try {
			if (alias != null) 
				for (int i = 0; i < alias.getColumnCount(); i++) {
					l.add(alias.getColumnClassName(i+1));
				}
			return l;
		} catch (SQLException e) {
			throw new JRException("Error executing stmt", e);
		}
	} 
}
