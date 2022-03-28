//
// (C) 2020 Soffid
//
//

package com.jaspersoft.studio.data.soffid;
/**
 * ValueObject QueryResponse
 **/
public class QueryResponse

		implements java.io.Serializable
 {

	/**
	 + The serial version UID of this class. Needed for serialization.
	 */
	private static final long serialVersionUID = 1;
	/**
	 * Attribute columnNames

	 */
	private java.lang.String[] columnNames;

	private java.lang.String[] columnClasses;

	/**
	 * Attribute values

	 */
	private java.util.List<java.lang.Object[]> values;

	/**
	 * Attribute nextRecord

	 */
	private java.lang.Long nextRecord;

	public QueryResponse()
	{
	}

	public QueryResponse(java.lang.String[] columnNames, java.util.List<java.lang.Object[]> values, java.lang.Long nextRecord)
	{
		super();
		this.columnNames = columnNames;
		this.values = values;
		this.nextRecord = nextRecord;
	}

	public QueryResponse(QueryResponse otherBean)
	{
		this(otherBean.columnNames, otherBean.values, otherBean.nextRecord);
	}

	/**
	 * Gets value for attribute columnNames
	 */
	public java.lang.String[] getColumnNames() {
		return this.columnNames;
	}

	/**
	 * Sets value for attribute columnNames
	 */
	public void setColumnNames(java.lang.String[] columnNames) {
		this.columnNames = columnNames;
	}

	/**
	 * Gets value for attribute values
	 */
	public java.util.List<java.lang.Object[]> getValues() {
		return this.values;
	}

	/**
	 * Sets value for attribute values
	 */
	public void setValues(java.util.List<java.lang.Object[]> values) {
		this.values = values;
	}

	/**
	 * Gets value for attribute nextRecord
	 */
	public java.lang.Long getNextRecord() {
		return this.nextRecord;
	}

	/**
	 * Sets value for attribute nextRecord
	 */
	public void setNextRecord(java.lang.Long nextRecord) {
		this.nextRecord = nextRecord;
	}

	/**
	 * Returns a string representation of the value object.
	 */
	public String toString()
	{
		StringBuffer b = new StringBuffer();
		b.append (getClass().getName());
		b.append ("[columnNames: ");
		b.append (this.columnNames);
		b.append (", values: ");
		b.append (this.values);
		b.append (", nextRecord: ");
		b.append (this.nextRecord);
		b.append ("]");
		return b.toString();
	}

	public java.lang.String[] getColumnClasses() {
		return columnClasses;
	}

	public void setColumnClasses(java.lang.String[] columnClasses) {
		this.columnClasses = columnClasses;
	}

}
