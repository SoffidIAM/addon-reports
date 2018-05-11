package com.soffid.iam.addons.report.service.ejb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;

import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;

import com.soffid.iam.ServiceLocator;
import com.soffid.iam.addons.report.api.ExecutedReport;
import com.soffid.iam.addons.report.api.ParameterValue;
import com.soffid.iam.addons.report.api.Report;
import com.soffid.iam.addons.report.service.ReportSchedulerService;
import com.soffid.iam.addons.report.service.ReportService;
import com.soffid.iam.api.Configuration;
import com.soffid.iam.doc.api.DocumentOutputStream;
import com.soffid.iam.doc.api.DocumentReference;
import com.soffid.iam.doc.exception.DocumentBeanException;
import com.soffid.iam.doc.service.DocumentService;
import com.soffid.iam.service.ConfigurationService;
import com.soffid.iam.utils.Security;

import bsh.EvalError;
import es.caib.seycon.ng.exception.InternalErrorException;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRChild;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.query.JRHibernateQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;

@Singleton(name="ReportClusterBean")
@Startup
@javax.ejb.TransactionManagement(value=javax.ejb.TransactionManagementType.CONTAINER)
@javax.ejb.TransactionAttribute(value=javax.ejb.TransactionAttributeType.SUPPORTS)
public class ClusterManagerBean  {
	org.apache.commons.logging.Log log = LogFactory.getLog(getClass());


	@Resource
	private SessionContext context;

	@PostConstruct
	public void init() throws Exception {
		log.info("Started report bean");
		context.getTimerService().createTimer(60000, 60000, "Cluster manager");
	}

	@Timeout	
	@javax.ejb.TransactionAttribute(value=javax.ejb.TransactionAttributeType.SUPPORTS)
	public void timeOutHandler(Timer timer) throws Exception {
		try
		{
			String hostName = Inet4Address.getLocalHost().getHostName();
			long now = System.currentTimeMillis();
			long timeout = now - 30000; // 5 minutes ago
			ConfigurationService svc = ServiceLocator.instance().getConfigurationService();
			Configuration cfg = svc.findParameterByNameAndNetworkName("addon.report.server", null);
			if (cfg == null)
			{
				cfg = new Configuration();
				cfg.setCode("addon.report.server");
				cfg.setValue(hostName+" "+now);
				svc.create(cfg);
			}
			else
			{
				String [] split = cfg.getValue().isEmpty() ? new String[0]: cfg.getValue().split(" ");
				if (split.length != 2 ||
						split[0].equals(hostName) ||
						Integer.parseInt(split[1]) < timeout)
				{
					cfg.setValue(hostName+" "+now);
					svc.update(cfg);
				}
						
			}
		} catch (Throwable e) {
			e.printStackTrace();
			log.warn("Error on report cluster manager", e);
		}
	}
	
}
