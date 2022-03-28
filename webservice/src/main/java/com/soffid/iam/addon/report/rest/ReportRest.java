package com.soffid.iam.addon.report.rest;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.naming.InitialContext;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.soffid.iam.addons.report.api.QueryRequest;
import com.soffid.iam.addons.report.api.QueryResponse;
import com.soffid.iam.addons.report.service.ejb.ReportService;
import com.soffid.iam.addons.report.service.ejb.ReportServiceHome;
import com.soffid.iam.common.security.SoffidPrincipal;
import com.soffid.iam.utils.Security;

@Path("/addon/report")
@Produces({"application/scim+json", "application/json"})
@Consumes({"application/scim+json", "application/json"})
@ServletSecurity(@HttpConstraint(rolesAllowed = {"scim:invoke"}))
public class ReportRest{
	Log log = LogFactory.getLog(getClass());
	
	public ReportRest() {
	}
	
	
	@Path("/query")
	@POST
	public Response query(String data, @Context HttpServletRequest request) throws URISyntaxException {
		Security.nestedLogin( (SoffidPrincipal) ((HttpServletRequest) request).getUserPrincipal());
		try {
			JSONObject o = new JSONObject(data);
			QueryRequest q = new QueryRequest();
			q.setAuthorization(o.optString("authorization"));
			if (o.has("firstRecord"))
				q.setFirstRecord(o.getLong("firstRecord"));
			q.setLanguage(o.optString("language"));
			q.setQuery(o.optString("query"));
			q.setParameterClasses(parseArray(o.getJSONArray("parameterClasses")));
			q.setParameterNames(parseArray(o.getJSONArray("parameterNames")));
			q.setParameterValues(parseArray(o.getJSONArray("parameterValues")));
			
			ReportService ejb = (ReportService) new InitialContext().lookup(ReportServiceHome.JNDI_NAME);
			
			QueryResponse r = ejb.query(q);
			
			o = new JSONObject();
			o.put("success", true);
			o.put("nextRecord", r.getNextRecord());
			final JSONArray cn = new JSONArray();
			o.put("columnNames", cn);
			for (String name: r.getColumnNames())
				cn.put(name);
			final JSONArray ct = new JSONArray();
			o.put("columnClasses", ct);
			for (String type: r.getColumnClasses())
				ct.put(type);
			
			final JSONArray rows = new JSONArray();
			o.put("values", rows);
			for (Object[] value: r.getValues()) {
				JSONArray row = new JSONArray();
				rows.put(row);
				for (Object v: value) {
					row.put(tojson(v));
				}
			}
			
			return Response.status(Response.Status.OK).entity(o.toString()).build();
		} catch (Exception e) {
			log.warn("Error processing SCIM Request "+request.getRequestURL(), e);
			JSONObject error = new JSONObject();
			error.put("success", false);
			error.put("error", e.toString());
			return Response.status(Status.OK).entity(error.toString()).build();
		} finally {
			Security.nestedLogoff();
		}
	}

	private String tojson(Object v) {
		if (v == null)
			return null;
		
		if (v instanceof Date) {
			return new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss").format((Date) v);
		}
		if (v instanceof Calendar) {
			return new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss").format(((Calendar) v).getTime());
		}
		
		return v.toString();
	}


	private String[] parseArray(JSONArray jsonArray) {
		String r[] = new String[jsonArray.length()];
		for  (int i  = 0; i < jsonArray.length(); i++)
			r[i] = jsonArray.optString(i, null);
		return r;
	}


}

