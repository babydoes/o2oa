package com.x.okr.assemble.control.jaxrs.okrtask.exception;

import com.x.base.core.exception.PromptException;

public class PersonNotExistsException extends PromptException {

	private static final long serialVersionUID = 1859164370743532895L;

	public PersonNotExistsException( String flag ) {
		super("用户信息不存在!Flag:" + flag );
	}
}
