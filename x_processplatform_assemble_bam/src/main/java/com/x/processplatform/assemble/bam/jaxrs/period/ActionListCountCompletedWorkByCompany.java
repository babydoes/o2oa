package com.x.processplatform.assemble.bam.jaxrs.period;

import java.util.ArrayList;
import java.util.List;

import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.http.ActionResult;
import com.x.base.core.http.WrapOutMap;
import com.x.base.core.project.jaxrs.StandardJaxrsAction;
import com.x.base.core.utils.DateRange;
import com.x.base.core.utils.DateTools;
import com.x.processplatform.assemble.bam.Business;
import com.x.processplatform.assemble.bam.ThisApplication;
import com.x.processplatform.assemble.bam.stub.CompanyStub;

class ActionListCountCompletedWorkByCompany extends ActionListCountCompletedWork {

	ActionResult<WrapOutMap> execute(String applicationId, String processId) throws Exception {
		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			ActionResult<WrapOutMap> result = new ActionResult<>();
			Business business = new Business(emc);
			List<DateRange> os = this.listDateRange();
			WrapOutMap wrap = new WrapOutMap();
			for (DateRange o : os) {
				String str = DateTools.format(o.getStart(), DateTools.format_yyyyMM);
				List<WrapOutMap> list = new ArrayList<>();
				wrap.put(str, list);
				for (CompanyStub stub : this.listStub()) {
					WrapOutMap pair = new WrapOutMap();
					pair.put("name", stub.getName());
					pair.put("value", stub.getValue());
					pair.put("level", stub.getLevel());
					Long count = this.count(business, o, applicationId, processId, stub.getValue(),
							StandardJaxrsAction.EMPTY_SYMBOL, StandardJaxrsAction.EMPTY_SYMBOL);
					pair.put("count", count);
					Long duration = this.duration(business, o, applicationId, processId, stub.getValue(),
							StandardJaxrsAction.EMPTY_SYMBOL,StandardJaxrsAction.EMPTY_SYMBOL);
					pair.put("duration", duration);
					Long times = this.times(business, o, applicationId, processId, stub.getValue(),
							StandardJaxrsAction.EMPTY_SYMBOL, StandardJaxrsAction.EMPTY_SYMBOL);
					pair.put("times", times);
					list.add(pair);
				}
			}
			result.setData(wrap);
			return result;
		}
	}

	private List<CompanyStub> listStub() throws Exception {
		List<CompanyStub> list = new ArrayList<>();
		for (CompanyStub o : ThisApplication.period.getCompletedWorkCompanyStubs()) {
			list.add(o);
		}
		return list;
	}
}