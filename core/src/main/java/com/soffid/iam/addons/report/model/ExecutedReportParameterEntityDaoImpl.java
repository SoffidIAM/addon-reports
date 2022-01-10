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
		else if (source.getType().equals (ParameterType.DOUBLE_PARAM))
			target.setValue(source.getDoubleValue());
		else if (source.getType().equals (ParameterType.LONG_PARAM))
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
		else if (source.getType().equals (ParameterType.LONG_PARAM))
			target.setLongValue(source.getValue() == null ? null:
				source.getValue() instanceof Integer ?
						((Integer) source.getValue()).longValue():
						Long.valueOf(source.getValue().toString()));
		else if (source.getType().equals (ParameterType.DOUBLE_PARAM))
			target.setDoubleValue(source.getValue() == null ? null:
				source.getValue() instanceof Float ?
						((Float) source.getValue()).doubleValue():
						Double.valueOf(source.getValue().toString()));
		else if (source.getType().equals (ParameterType.BOOLEAN_PARAM))
			target.setBooleanValue(source.getValue() == null ? null:
				(Boolean)source.getValue());
		else 
			target.setStringValue(source.getValue() == null ? null: source.getValue().toString());
	}
}
