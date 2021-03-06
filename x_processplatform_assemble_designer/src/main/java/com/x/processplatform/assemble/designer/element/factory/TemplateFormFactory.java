package com.x.processplatform.assemble.designer.element.factory;

import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.x.processplatform.assemble.designer.AbstractFactory;
import com.x.processplatform.assemble.designer.Business;
import com.x.processplatform.core.entity.element.TemplateForm;
import com.x.processplatform.core.entity.element.TemplateForm_;

public class TemplateFormFactory extends AbstractFactory {

	public TemplateFormFactory(Business business) throws Exception {
		super(business);
	}

	public List<String> list() throws Exception {
		EntityManager em = this.entityManagerContainer().get(TemplateForm.class);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<TemplateForm> root = cq.from(TemplateForm.class);
		cq.select(root.get(TemplateForm_.id));
		return em.createQuery(cq).getResultList();
	}

	public List<String> listWithCategory(String category) throws Exception {
		EntityManager em = this.entityManagerContainer().get(TemplateForm.class);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<TemplateForm> root = cq.from(TemplateForm.class);
		Predicate p = cb.equal(root.get(TemplateForm_.category), Objects.toString(category, ""));
		cq.select(root.get(TemplateForm_.id)).where(p);
		return em.createQuery(cq).getResultList();
	}

}