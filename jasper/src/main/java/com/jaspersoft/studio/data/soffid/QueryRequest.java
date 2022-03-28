//
// (C) 2020 Soffid
//
//

package com.jaspersoft.studio.data.soffid;
/**
 * ValueObject QueryRequest
 **/
public class QueryRequest

		implements java.io.Serializable
 {

	/**
	 + The serial version UID of this class. Needed for serialization.
	 */
	private static final long serialVersionUID = 1;
	/**
	 * Attribute authorization

	 */
	private java.lang.String authorization;

	/**
	 * Attribute firstRecord

	 */
	private java.lang.Long firstRecord;

	/**
	 * Attribute language

	 */
	private java.lang.String language;

	/**
	 * Attribute query

	 */
	private java.lang.String query;

	/**
	 * Attribute parameterNames

	 */
	private java.lang.String[] parameterNames;

	/**
	 * Attribute parameterClasses

	 */
	private java.lang.String[] parameterClasses;

	/**
	 * Attribute parameterValues

	 */
	private java.lang.String[] parameterValues;

	public QueryRequest()
	{
	}

	public QueryRequest(java.lang.String authorization, java.lang.Long firstRecord, java.lang.String language, java.lang.String query, java.lang.String[] parameterNames, java.lang.String[] parameterClasses, java.lang.String[] parameterValues)
	{
		super();
		this.authorization = authorization;
		this.firstRecord = firstRecord;
		this.language = language;
		this.query = query;
		this.parameterNames = parameterNames;
		this.parameterClasses = parameterClasses;
		this.parameterValues = parameterValues;
	}

	public QueryRequest(java.lang.String language, java.lang.String query)
	{
		super();
		this.language = language;
		this.query = query;
	}

	public QueryRequest(QueryRequest otherBean)
	{
		this(otherBean.authorization, otherBean.firstRecord, otherBean.language, otherBean.query, otherBean.parameterNames, otherBean.parameterClasses, otherBean.parameterValues);
	}

	/**
	 * Gets value for attribute authorization
	 */
	public java.lang.String getAuthorization() {
		return this.authorization;
	}

	/**
	 * Sets value for attribute authorization
	 */
	public void setAuthorization(java.lang.String authorization) {
		this.authorization = authorization;
	}

	/**
	 * Gets value for attribute firstRecord
	 */
	public java.lang.Long getFirstRecord() {
		return this.firstRecord;
	}

	/**
	 * Sets value for attribute firstRecord
	 */
	public void setFirstRecord(java.lang.Long firstRecord) {
		this.firstRecord = firstRecord;
	}

	/**
	 * Gets value for attribute language
	 */
	public java.lang.String getLanguage() {
		return this.language;
	}

	/**
	 * Sets value for attribute language
	 */
	public void setLanguage(java.lang.String language) {
		this.language = language;
	}

	/**
	 * Gets value for attribute query
	 */
	public java.lang.String getQuery() {
		return this.query;
	}

	/**
	 * Sets value for attribute query
	 */
	public void setQuery(java.lang.String query) {
		this.query = query;
	}

	/**
	 * Gets value for attribute parameterNames
	 */
	public java.lang.String[] getParameterNames() {
		return this.parameterNames;
	}

	/**
	 * Sets value for attribute parameterNames
	 */
	public void setParameterNames(java.lang.String[] parameterNames) {
		this.parameterNames = parameterNames;
	}

	/**
	 * Gets value for attribute parameterClasses
	 */
	public java.lang.String[] getParameterClasses() {
		return this.parameterClasses;
	}

	/**
	 * Sets value for attribute parameterClasses
	 */
	public void setParameterClasses(java.lang.String[] parameterClasses) {
		this.parameterClasses = parameterClasses;
	}

	/**
	 * Gets value for attribute parameterValues
	 */
	public java.lang.String[] getParameterValues() {
		return this.parameterValues;
	}

	/**
	 * Sets value for attribute parameterValues
	 */
	public void setParameterValues(java.lang.String[] parameterValues) {
		this.parameterValues = parameterValues;
	}

	/**
	 * Returns a string representation of the value object.
	 */
	public String toString()
	{
		StringBuffer b = new StringBuffer();
		b.append (getClass().getName());
		b.append ("[authorization: ");
		b.append (this.authorization);
		b.append (", firstRecord: ");
		b.append (this.firstRecord);
		b.append (", language: ");
		b.append (this.language);
		b.append (", query: ");
		b.append (this.query);
		b.append (", parameterNames: ");
		b.append (this.parameterNames);
		b.append (", parameterClasses: ");
		b.append (this.parameterClasses);
		b.append (", parameterValues: ");
		b.append (this.parameterValues);
		b.append ("]");
		return b.toString();
	}

}
