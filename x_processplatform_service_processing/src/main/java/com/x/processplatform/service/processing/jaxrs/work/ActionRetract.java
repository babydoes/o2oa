package com.x.processplatform.service.processing.jaxrs.work;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.list.SetUniqueList;
import org.apache.commons.lang3.StringUtils;

import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.http.ActionResult;
import com.x.base.core.http.WrapOutId;
import com.x.processplatform.core.entity.content.Read;
import com.x.processplatform.core.entity.content.ReadCompleted;
import com.x.processplatform.core.entity.content.Review;
import com.x.processplatform.core.entity.content.Task;
import com.x.processplatform.core.entity.content.TaskCompleted;
import com.x.processplatform.core.entity.content.Work;
import com.x.processplatform.core.entity.content.WorkLog;
import com.x.processplatform.service.processing.Business;
import com.x.processplatform.service.processing.Processing;
import com.x.processplatform.service.processing.ProcessingAttributes;
import com.x.processplatform.service.processing.configurator.ProcessingConfigurator;

/**
 * 根据WorkLogId进行召回
 * 
 * @author Rui
 *
 */
class ActionRetract extends ActionBase {

	ActionResult<WrapOutId> execute(String id, String workLogId) throws Exception {
		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			ActionResult<WrapOutId> result = new ActionResult<>();
			Business business = new Business(emc);
			Work work = null;
			WorkLog workLog = null;
			work = emc.find(id, Work.class);
			if (null == work) {
				throw new WorkNotExistedException(id);
			}
			workLog = emc.find(workLogId, WorkLog.class);
			if (null == workLog) {
				throw new WorkLogNotExistedException(workLogId);
			}
			if (!StringUtils.equals(work.getJob(), workLog.getJob())) {
				throw new WorkNotMatchWithWorkLogException(work.getTitle(), work.getId(), work.getJob(),
						workLog.getId(), workLog.getJob());
			}
			List<Work> works = this.listForwardWork(business, workLog);
			emc.beginTransaction(Work.class);
			emc.beginTransaction(Task.class);
			emc.beginTransaction(TaskCompleted.class);
			emc.beginTransaction(Read.class);
			emc.beginTransaction(ReadCompleted.class);
			emc.beginTransaction(Review.class);
			emc.beginTransaction(WorkLog.class);
			for (Work o : works) {
				this.cleanComplex(business, o);
				if (!StringUtils.equals(work.getId(), o.getId())) {
					work.getAttachmentList().addAll(o.getAttachmentList());
					work.setAttachmentList(SetUniqueList.setUniqueList(work.getAttachmentList()));
					business.entityManagerContainer().remove(o);
				}
			}
			this.retractAsWorkLog(work, workLog);
			emc.delete(WorkLog.class,
					business.workLog().listWithFromActivityTokenForward(workLog.getFromActivityToken()));
			emc.commit();
			Processing processing = new Processing(0, new ProcessingAttributes(), emc);
			ProcessingConfigurator processingConfigurator = new ProcessingConfigurator();
			processingConfigurator.setContinueLoop(false);
			processingConfigurator.setActivityCreateRead(false);
			processingConfigurator.setActivityCreateReview(false);
			processingConfigurator.setChangeActivityToken(false);
			processingConfigurator.setJoinAtExecute(false);
			processingConfigurator.setActivityStampArrivedWorkLog(false);
			processing.processing(work.getId(), processingConfigurator);
			WrapOutId wrap = new WrapOutId(id);
			result.setData(wrap);
			return result;
		}
	}

	private void cleanComplex(Business business, Work work) throws Exception {
		business.entityManagerContainer().delete(Task.class,
				business.task().listWithActivityToken(work.getActivityToken()));
		business.entityManagerContainer().delete(TaskCompleted.class,
				business.taskCompleted().listWithActivityToken(work.getActivityToken()));
		business.entityManagerContainer().delete(Read.class,
				business.read().listWithActivityToken(work.getActivityToken()));
		business.entityManagerContainer().delete(ReadCompleted.class,
				business.readCompleted().listWithActivityToken(work.getActivityToken()));
		business.entityManagerContainer().delete(Review.class,
				business.review().listWithActivityToken(work.getActivityToken()));
	}

	private List<Work> listForwardWork(Business business, WorkLog workLog) throws Exception {
		List<String> ids = business.workLog()
				.listWithFromActivityTokenForwardNotConnected(workLog.getFromActivityToken());
		List<String> activityTokens = SetUniqueList.setUniqueList(new ArrayList<String>());
		for (WorkLog o : business.entityManagerContainer().fetchAttribute(ids, WorkLog.class, "fromActivityToken")) {
			activityTokens.add(o.getFromActivityToken());
		}
		List<String> workIds = business.work().listWithActivityToken(activityTokens);
		return business.entityManagerContainer().list(Work.class, workIds);
	}

	private void retractAsWorkLog(Work work, WorkLog workLog) throws Exception {
		work.setDestinationActivity(workLog.getFromActivity());
		work.setDestinationActivityType(workLog.getFromActivityType());
		work.setActivityToken(workLog.getFromActivityToken());
		work.setSplitting(workLog.getSplitting());
		work.setSplitValue(workLog.getSplitValue());
		work.setSplitToken(workLog.getSplitToken());
		work.setSplitTokenList(workLog.getSplitTokenList());
	}

}
