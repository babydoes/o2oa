package com.x.cms.assemble.control.jaxrs.script;

import java.util.Date;
import java.util.List;

import com.x.base.core.gson.GsonPropertyObject;
import com.x.base.core.http.annotation.Wrap;
import com.x.cms.core.entity.element.Script;

@Wrap(Script.class)
public class WrapInScript extends GsonPropertyObject {

	private Date createTime;
	private Date updateTime;
	private String id;
	private String name;
	private String alias;
	private String description;
	private Boolean validated;
	private String appId;
	private String text;
	private List<String> dependScriptList;

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getValidated() {
		return validated;
	}

	public void setValidated(Boolean validated) {
		this.validated = validated;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<String> getDependScriptList() {
		return dependScriptList;
	}

	public void setDependScriptList(List<String> dependScriptList) {
		this.dependScriptList = dependScriptList;
	}

}
