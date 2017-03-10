package com.x.cms.assemble.control.jaxrs.queryview;

import com.google.gson.JsonElement;
import com.x.base.core.gson.GsonPropertyObject;
import com.x.cms.core.entity.query.DateRangeEntry;


public class WrapInQueryExecute extends GsonPropertyObject {

	private DateRangeEntry date;

	private JsonElement filter;

	private JsonElement column;

	private JsonElement application;
	
	private JsonElement appIdList;

	private JsonElement category;

	private JsonElement company;

	private JsonElement department;

	private JsonElement person;

	private JsonElement identity;

	public DateRangeEntry getDate() {
		return date;
	}

	public void setDate(DateRangeEntry date) {
		this.date = date;
	}

	public JsonElement getFilter() {
		return filter;
	}

	public void setFilter(JsonElement filter) {
		this.filter = filter;
	}

	public JsonElement getColumn() {
		return column;
	}

	public void setColumn(JsonElement column) {
		this.column = column;
	}

	public JsonElement getApplication() {
		return application;
	}

	public void setApplication(JsonElement application) {
		this.application = application;
	}	

	public JsonElement getCategory() {
		return category;
	}

	public void setCategory(JsonElement category) {
		this.category = category;
	}

	public JsonElement getCompany() {
		return company;
	}

	public void setCompany(JsonElement company) {
		this.company = company;
	}

	public JsonElement getDepartment() {
		return department;
	}

	public void setDepartment(JsonElement department) {
		this.department = department;
	}

	public JsonElement getPerson() {
		return person;
	}

	public void setPerson(JsonElement person) {
		this.person = person;
	}

	public JsonElement getIdentity() {
		return identity;
	}

	public void setIdentity(JsonElement identity) {
		this.identity = identity;
	}

	public JsonElement getAppIdList() {
		return appIdList;
	}

	public void setAppIdList(JsonElement appIdList) {
		this.appIdList = appIdList;
	}

}