package com.soffid.iam.addons.report.service.jasper;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.ParseException;
import bsh.TargetError;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.query.JRQueryExecuterFactory;

public class JRBshExecuterFactory implements JRQueryExecuterFactory {
	Log log = LogFactory.getLog(getClass());
	
	public Object[] getBuiltinParameters() {
		return null;
	}

	public JRQueryExecuter createQueryExecuter(JRDataset dataset,
			Map<String, ? extends JRValueParameter> parameters)
			throws JRException {
		
		try {
			Interpreter interp = new Interpreter();
			interp.eval("import es.caib.seycon.ng.comu.*;");
			interp.eval("import es.caib.seycon.ng.servei.*;");
			interp.eval("import es.caib.seycon.ng.ServiceLocator;");			
			interp.eval("import com.soffid.iam.api.*;");
			interp.eval("import com.soffid.iam.service.*;");			interp.eval("serviceLocator = com.soffid.iam.ServiceLocator.instance();");
			try {
				String hostName = InetAddress.getLocalHost().getHostName();
				interp.set("serverName", hostName);
			} catch (Throwable t) {
			}
			for (String p : parameters.keySet()) {
				JRValueParameter v = parameters.get(p);
				interp.set(p, v.getValue());
			}
			Object result = interp.eval(dataset.getQuery().getText());
			@SuppressWarnings("unchecked")
			Collection<Object> coll = result instanceof Collection ? (Collection<Object>) result
					: Collections.singleton(result);
			return new JRBshExecuter(coll);
		} catch (ParseException ex) {
			log.info("ERROR EXECUTING SCRIPT");
			log.info(dataset.getQuery().getText());
			log.info("======================");
			log.info("BSH Parse error", ex);
			throw new JRException("Error executing bsh script", ex);
		} catch (TargetError ex) {
			log.info("ERROR EXECUTING SCRIPT");
			log.info(dataset.getQuery().getText());
			log.info("======================");
			log.info("BSH Target error", ex.getTarget());
			throw new JRException("Error executing bsh script", ex.getTarget());
		} catch (EvalError ex) {
			log.info("ERROR EXECUTING SCRIPT");
			log.info(dataset.getQuery().getText());
			log.info("======================");
			log.info("BSH Target error", ex);
			throw new JRException("Error executing bsh script", ex);
		} catch (Exception e) {
			log.info("ERROR EXECUTING SCRIPT");
			log.info(dataset.getQuery().getText());
			log.info("======================");
			log.info("Unexpected error", e);
			throw new JRException("Error executing bsh script", e);
		}
	}

	public boolean supportsQueryParameterType(String className) {
		return true;
	}

}
