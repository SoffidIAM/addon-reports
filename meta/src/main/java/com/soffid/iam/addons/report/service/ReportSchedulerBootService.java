package com.soffid.iam.addons.report.service;

import com.soffid.iam.doc.service.DocumentService;
import com.soffid.mda.annotation.Depends;
import com.soffid.mda.annotation.Description;
import com.soffid.mda.annotation.Service;

import es.caib.seycon.ng.servei.ApplicationBootService;

@Service
@Description ("Starts the scheduler and executor threads")
@Depends({ReportSchedulerService.class, DocumentService.class, ReportService.class})
public class ReportSchedulerBootService extends ApplicationBootService {

}
