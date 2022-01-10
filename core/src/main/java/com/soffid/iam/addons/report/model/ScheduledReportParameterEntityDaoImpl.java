//
// (C) 2013 Soffid
//
//

package com.soffid.iam.addons.report.model;

import java.util.Calendar;
import java.util.Date;

import com.soffid.iam.addons.report.api.ParameterType;
import com.soffid.iam.addons.report.api.ParameterValue;

/**
 * DAO ScheduledReportParameterEntity implementation
 */
public class ScheduledReportParameterEntityDaoImpl extends ScheduledReportParameterEntityDaoBase
{

	@Override
	public void toParameterValue(ScheduledReportParameterEntity source,
			ParameterValue target) {
		super.toParameterValue(source, target);
		
		if (source.getType().equals (ParameterType.DATE_PARAM))
			target.setValue(source.getDateValue());
		else if (source.getType().equals (ParameterType.DISPATCHER_PARAM))
			target.setValue(source.getStringValue());
		else if (source.getType().equals (ParameterType.DOUBLE_PARAM))
			target.setValue(source.getDoubleValue());
		else if (source.getType().equals (ParameterType.GROUP_PARAM))
			target.setValue(source.getStringValue());
		else if (source.getType().equals (ParameterType.IS_PARAM))
			target.setValue(source.getStringValue());
		else if (source.getType().equals (ParameterType.LONG_PARAM))
			target.setValue(source.getStringValue());
		else if (source.getType().equals (ParameterType.ROLE_PARAM))
			target.setValue(source.getStringValue());
		else if (source.getType().equals (ParameterType.USER_PARAM))
			target.setValue(source.getStringValue());
		else if (source.getType().equals (ParameterType.BOOLEAN_PARAM))
			target.setValue(source.getBooleanValue());
		else 
			target.setValue(source.getStringValue());
	}

	@Override
	public void parameterValueToEntity(ParameterValue source,
			ScheduledReportParameterEntity target, boolean copyIfNull) {
		// TODO Auto-generated method stub
		super.parameterValueToEntity(source, target, copyIfNull);

		if (source.getType().equals (ParameterType.DATE_PARAM))
			target.setDateValue(source.getValue() == null ? null:
								source.getValue() instanceof Calendar ?
										((Calendar) source.getValue()).getTime():
										(Date)source.getValue());
		else if (source.getType().equals (ParameterType.LONG_PARAM))
			target.setLongValue(source.getValue() == null ? null:
				source.getValue() instanceof Integer ?
						((Integer) source.getValue()).longValue():
							source.getValue() instanceof Long ?
									(Long)source.getValue():
									Long.decode(source.getValue().toString()));
		else if (source.getType().equals (ParameterType.DOUBLE_PARAM))
			target.setDoubleValue(source.getValue() == null ? null:
				source.getValue() instanceof Float ?
						((Float) source.getValue()).doubleValue():
						source.getValue() instanceof Double ?
								(Double)source.getValue():
								Double.parseDouble(source.toString()));
		else if (source.getType().equals (ParameterType.BOOLEAN_PARAM))
			target.setBooleanValue(source.getValue() == null ? null:
				source.getValue() instanceof Boolean ?
						(Boolean)source.getValue():
						Boolean.parseBoolean(source.toString()));
		else 
			target.setStringValue(source.getValue() == null ? null: source.getValue().toString());
	}
}
