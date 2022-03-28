package test;

import java.io.IOException;

import com.soffid.iam.addons.report.api.QueryRequest;
import com.soffid.iam.addons.report.api.QueryResponse;
import com.soffid.iam.addons.report.common.jasper.RemoteInvoker;

public class RemoteTest  {
	public static void main(String args[]) throws IOException {
		QueryRequest r = new QueryRequest();
		r.setAuthorization("xxx");
		r.setLanguage("sql");
		r.setParameterClasses(new String[0]);
		r.setParameterNames(new String[0]);
		r.setParameterValues(new String[0]);
		r.setQuery("SELECT USU_CODI FROM SC_USUARI");
		
		QueryResponse response = new RemoteInvoker().query(r);
		
		System.out.println(response);
	}

}
