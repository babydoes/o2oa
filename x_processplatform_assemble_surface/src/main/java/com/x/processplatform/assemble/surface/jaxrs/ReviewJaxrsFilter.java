package com.x.processplatform.assemble.surface.jaxrs;

import javax.servlet.annotation.WebFilter;

import com.x.base.core.application.jaxrs.CipherManagerUserJaxrsFilter;

@WebFilter(urlPatterns = "/jaxrs/review/*", asyncSupported = true)
public class ReviewJaxrsFilter extends CipherManagerUserJaxrsFilter {

}
