package com.x.organization.assemble.control.jaxrs.personattribute;

import com.x.base.core.cache.ApplicationCache;
import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.entity.annotation.CheckPersistType;
import com.x.base.core.exception.ExceptionWhen;
import com.x.base.core.http.EffectivePerson;
import com.x.base.core.http.WrapOutId;
import com.x.organization.assemble.control.Business;
import com.x.organization.assemble.control.wrapin.WrapInPersonAttribute;
import com.x.organization.core.entity.Person;
import com.x.organization.core.entity.PersonAttribute;

public class ActionCreate extends ActionBase {

	protected WrapOutId execute(Business business, EffectivePerson effectivePerson, WrapInPersonAttribute wrapIn)
			throws Exception {
		EntityManagerContainer emc = business.entityManagerContainer();
		PersonAttribute o = inCopier.copy(wrapIn);
		Person person = emc.find(o.getPerson(), Person.class, ExceptionWhen.not_found);
		if (!business.personUpdateAvailable(effectivePerson, person)) {
			throw new Exception("person{name:" + effectivePerson.getName() + "} has sufficient permissions");
		}
		emc.beginTransaction(PersonAttribute.class);
		emc.persist(o, CheckPersistType.all);
		emc.commit();
		ApplicationCache.notify(PersonAttribute.class);
		WrapOutId wrap = new WrapOutId(o.getId());
		return wrap;
	}

}
