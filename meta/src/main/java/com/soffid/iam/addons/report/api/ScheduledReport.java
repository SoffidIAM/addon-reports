package com.soffid.iam.addons.report.api;

import java.util.Collection;
import java.util.Date;

import com.soffid.mda.annotation.Attribute;
import com.soffid.mda.annotation.Column;
import com.soffid.mda.annotation.ValueObject;

@ValueObject
public class ScheduledReport {
	Long id;
	Long reportId;
	String name;
	Collection<String> target;
	Collection<ParameterValue> params;
	@Attribute(defaultValue="false")
	boolean scheduled;
	
	String cronMinute;
	String cronHour;
	String cronDayOfMonth;
	String cronMonth;
	String cronDayOfWeek;
	
	Date lastExecution;
	Date creationDate;
	Date nextExecution; 
}
