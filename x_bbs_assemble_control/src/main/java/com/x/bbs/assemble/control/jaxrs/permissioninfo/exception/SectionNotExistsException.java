package com.x.bbs.assemble.control.jaxrs.permissioninfo.exception;

import com.x.base.core.exception.PromptException;

public class SectionNotExistsException extends PromptException {

	private static final long serialVersionUID = 1859164370743532895L;

	public SectionNotExistsException( String id ) {
		super("指定ID的版块信息不存在.ID:" + id );
	}
}
