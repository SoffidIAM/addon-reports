package com.soffid.iam.addons.report.api;

import java.util.Collection;
import java.util.Date;

import com.soffid.mda.annotation.Attribute;
import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.Nullable;
import com.soffid.mda.annotation.ValueObject;

@ValueObject
public class ScheduledReport {
	@Nullable  Long id;
	Long reportId;
	String name;
	@Nullable
	String reportName;
	Collection<String> target;
	@Nullable Collection<ParameterValue> params;
	@Attribute(defaultValue="false")
	boolean scheduled;
	
	@Nullable String cronMinute;
	@Nullable String cronHour;
	@Nullable String cronDayOfMonth;
	@Nullable String cronMonth;
	@Nullable String cronDayOfWeek;
	
	@Nullable Date lastExecution;
	@Nullable Date creationDate;
	@Nullable Date nextExecution; 
}
