package com.x.base.core.application;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.x.base.core.gson.GsonPropertyObject;

public class Application extends GsonPropertyObject {

	private String host;
	private String context;
	private Integer port;
	private String token;

	private String proxyHost;
	private Integer proxyPort;

	private Integer weight;

	private Date reportDate;

//	public Integer getProxyPort() {
//		if (null == proxyPort) {
//			return this.port;
//		}
//		return proxyPort;
//	}
//
//	public String getProxyHost() {
//		if (StringUtils.isEmpty(proxyHost)) {
//			return this.host;
//		}
//		return proxyHost;
//	}

	public String getUrlRoot() {
		return "http://" + (StringUtils.isNotEmpty(host) ? host : "127.0.0.1") + (port == 80 ? "" : (":" + port))
				+ context + "/jaxrs/";
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}

}