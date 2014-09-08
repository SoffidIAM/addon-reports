package com.soffid.iam.addons.report.service;

import com.soffid.mda.annotation.Depends;
import com.soffid.mda.annotation.Description;
import com.soffid.mda.annotation.Service;

import es.caib.seycon.ng.servei.ApplicationShutdownService;

@Service
@Description ("Stops the report scheduler and executor threads")
@Depends({ReportSchedulerService.class})
public class ReportSchedulerShudownService extends ApplicationShutdownService {

}
