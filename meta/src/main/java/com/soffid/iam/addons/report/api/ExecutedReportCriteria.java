package com.soffid.iam.addons.report.api;

import java.util.Date;

import com.soffid.mda.annotation.Criteria;
import com.soffid.mda.annotation.CriteriaColumn;

@Criteria
public class ExecutedReportCriteria {
	@CriteriaColumn(comparator="LIKE_COMPARATOR") 
	String name;
	
	@CriteriaColumn(comparator="GREATER_THAN_OR_EQUAL_COMPARATOR", parameter="date") 
	Date fromDate;
	
	@CriteriaColumn(comparator="LESS_THAN_OR_EQUAL_COMPARATOR", parameter="date") 
	Date untilDate;
	
}
