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
 * DAO ExecutedReportParameterEntity implementation
 */
public class ExecutedReportParameterEntityDaoImpl extends ExecutedReportParameterEntityDaoBase
{

	@Override
	public void toParameterValue(ExecutedReportParameterEntity source,
			ParameterValue target) {
		super.toParameterValue(source, target);
		if (source.getType().equals (ParameterType.DATE_PARAM))
			target.setValue(source.getDateValue());
		else if (source.getType().equals (ParameterType.DISPATCHER_PARAM))
			target.setValue(source.getLongValue());
		else if (source.getType().equals (ParameterType.DOUBLE_PARAM))
			target.setValue(source.getDoubleValue());
		else if (source.getType().equals (ParameterType.GROUP_PARAM))
			target.setValue(source.getLongValue());
		else if (source.getType().equals (ParameterType.IS_PARAM))
			target.setValue(source.getLongValue());
		else if (source.getType().equals (ParameterType.LONG_PARAM))
			target.setValue(source.getLongValue());
		else if (source.getType().equals (ParameterType.ROLE_PARAM))
			target.setValue(source.getLongValue());
		else if (source.getType().equals (ParameterType.USER_PARAM))
			target.setValue(source.getLongValue());
		else if (source.getType().equals (ParameterType.BOOLEAN_PARAM))
			target.setValue(source.getBooleanValue());
		else 
			target.setValue(source.getStringValue());
	}

	@Override
	public void parameterValueToEntity(ParameterValue source,
			ExecutedReportParameterEntity target, boolean copyIfNull) {
		super.parameterValueToEntity(source, target, copyIfNull);
		if (source.getType().equals (ParameterType.DATE_PARAM))
			target.setDateValue(source.getValue() == null ? null:
								source.getValue() instanceof Calendar ?
										((Calendar) source.getValue()).getTime():
										(Date)source.getValue());
		else if (source.getType().equals (ParameterType.DISPATCHER_PARAM) ||
				source.getType().equals (ParameterType.GROUP_PARAM) ||
				source.getType().equals (ParameterType.IS_PARAM) ||
				source.getType().equals (ParameterType.LONG_PARAM) ||
				source.getType().equals (ParameterType.ROLE_PARAM) ||
				source.getType().equals (ParameterType.USER_PARAM))
			target.setLongValue(source.getValue() == null ? null:
				source.getValue() instanceof Integer ?
						((Integer) source.getValue()).longValue():
						(Long)source.getValue());
		else if (source.getType().equals (ParameterType.DOUBLE_PARAM))
			target.setDoubleValue(source.getValue() == null ? null:
				source.getValue() instanceof Float ?
						((Float) source.getValue()).doubleValue():
						(Double)source.getValue());
		else if (source.getType().equals (ParameterType.BOOLEAN_PARAM))
			target.setBooleanValue(source.getValue() == null ? null:
				(Boolean)source.getValue());
		else 
			target.setStringValue(source.getValue() == null ? null: source.toString());
	}
}
