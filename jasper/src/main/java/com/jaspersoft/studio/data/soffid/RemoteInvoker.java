package com.jaspersoft.studio.data.soffid;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class RemoteInvoker {
	String user;
	URL url;
	String password;
	
	public QueryResponse query(QueryRequest request) throws IOException {
		JSONObject o = new JSONObject(request);
		System.out.println("Invoking "+url);
		System.out.println("User "+user);
		System.out.println("Password "+password);
		System.out.println(o.toString(4));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.addRequestProperty("Content-Type", "application/json");
		String tag = Base64.getEncoder().encodeToString((user+":"+password).getBytes("UTF-8"));
		conn.addRequestProperty("Authorization", "Basic "+tag);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.connect();
		OutputStream out = conn.getOutputStream();
		out.write(o.toString().getBytes("UTF-8"));
		out.close();
		JSONTokener t = new JSONTokener(conn.getInputStream());
		JSONObject response = new JSONObject(t);
		System.out.println("-----------");
		System.out.println(response.toString(4));
		if (! response.optBoolean("success", false)) {
			throw new IOException(response.optString("error", "Cannot parse response"));
		}
		QueryResponse r = new QueryResponse();
		r.setColumnNames(parseArray(response.optJSONArray("columnNames")));
		r.setColumnClasses(parseArray(response.optJSONArray("columnClasses")));
		r.setValues(new LinkedList<Object[]>());
		JSONArray v = response.optJSONArray("values");
		if (v != null) {
			for (int i = 0; i < v.length(); i++) {
				r.getValues().add(parseArray(v.getJSONArray(i)));
			}
		}
		return r;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUrl() {
		return url.toString();
	}

	public void setUrl(String urlText) throws MalformedURLException {
		this.url = new URL(urlText);
		this.url = new URL(url.getProtocol(), url.getHost(), url.getPort(), "/soffid/webservice/addon/report/query");
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private String[] parseArray(JSONArray optJSONArray) {
		if (optJSONArray == null)
			return null;
		String[] r = new String[optJSONArray.length()];
		for (int i = 0; i < r.length; i++)
			r[i] = optJSONArray.optString(i, null);
		return r;
	}

	public String tojson(Object v) {
		if (v == null)
			return null;
		
		if (v instanceof Date) {
			return new SimpleDateFormat("dd-MM-YYYY'T'HH:mm:ss").format((Date) v);
		}
		if (v instanceof Calendar) {
			return new SimpleDateFormat("dd-MM-YYYY'T'HH:mm:ss").format(((Calendar) v).getTime());
		}
		
		return v.toString();
	}

}
