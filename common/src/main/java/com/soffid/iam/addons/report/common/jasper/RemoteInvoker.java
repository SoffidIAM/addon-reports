package com.soffid.iam.addons.report.common.jasper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.soffid.iam.addons.report.api.QueryRequest;
import com.soffid.iam.addons.report.api.QueryResponse;

public class RemoteInvoker {
	public QueryResponse query(QueryRequest request) throws IOException {
		Properties prop = new Properties();
		prop.load(getClass().getResourceAsStream("/soffid-connection.properties"));
		String url = prop.getProperty("url");
		JSONObject o = new JSONObject(request);
		
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestMethod("POST");
		conn.addRequestProperty("Content-Type", "application/json");
		conn.addRequestProperty("Authorization", "Basic YWRtaW46Y2hhbmdlaXQ=");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.connect();
		OutputStream out = conn.getOutputStream();
		out.write(o.toString().getBytes("UTF-8"));
		out.close();
		JSONTokener t = new JSONTokener(conn.getInputStream());
		JSONObject response = new JSONObject(t);
		if (! response.optBoolean("success", false)) {
			throw new IOException(response.optString("error", "Cannot parse response"));
		}
		QueryResponse r = new QueryResponse();
		r.setColumnNames(parseArray(response.optJSONArray("columnNames")));
		r.setValues(new LinkedList<Object[]>());
		JSONArray v = response.optJSONArray("values");
		if (v != null) {
			for (int i = 0; i < v.length(); i++) {
				r.getValues().add(parseArray(v.getJSONArray(i)));
			}
		}
		return r;
	}

	private String[] parseArray(JSONArray optJSONArray) {
		if (optJSONArray == null)
			return null;
		String[] r = new String[optJSONArray.length()];
		for (int i = 0; i < r.length; i++)
			r[i] = optJSONArray.optString(i);
		return r;
	}
}
