package com.x.cms.assemble.control.jaxrs.templateform;

import java.util.ArrayList;
import java.util.List;

import com.x.base.core.entity.JpaObject;
import com.x.base.core.http.annotation.Wrap;
import com.x.cms.assemble.control.Control;
import com.x.cms.core.entity.element.TemplateForm;

@Wrap(TemplateForm.class)
public class WrapOutTemplateForm extends TemplateForm {

	private static final long serialVersionUID = 1551592776065130757L;
	public static List<String> Excludes = new ArrayList<>(JpaObject.FieldsInvisible);

	private Control control;

	public Control getControl() {
		return control;
	}

	public void setControl(Control control) {
		this.control = control;
	}

}
