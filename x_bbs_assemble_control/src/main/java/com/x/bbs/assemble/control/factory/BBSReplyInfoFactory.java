package com.x.bbs.assemble.control.factory;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.x.base.core.exception.ExceptionWhen;
import com.x.base.core.utils.annotation.MethodDescribe;
import com.x.bbs.assemble.common.date.DateOperation;
import com.x.bbs.assemble.control.AbstractFactory;
import com.x.bbs.assemble.control.Business;
import com.x.bbs.entity.BBSReplyInfo;
import com.x.bbs.entity.BBSReplyInfo_;

/**
 * 类   名：BBSReplyInfoFactory<br/>
 * 实体类：BBSReplyInfo<br/>
 * 作   者：Liyi<br/>
 * 单   位：O2 Team<br/>
 * 日   期：2016-05-20 17:17:26
**/
public class BBSReplyInfoFactory extends AbstractFactory {

	public BBSReplyInfoFactory( Business business ) throws Exception {
		super(business);
	}
	
	@MethodDescribe( "获取指定Id的BBSReplyInfo实体信息对象" )
	public BBSReplyInfo get( String id ) throws Exception {
		return this.entityManagerContainer().find( id, BBSReplyInfo.class, ExceptionWhen.none );
	}
	
	@MethodDescribe( "列示指定Id的BBSReplyInfo实体信息列表" )
	public List<BBSReplyInfo> list(List<String> ids) throws Exception {
		if( ids == null || ids.size() == 0 ){
			return new ArrayList<BBSReplyInfo>();
		}
		EntityManager em = this.entityManagerContainer().get(BBSReplyInfo.class);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<BBSReplyInfo> cq = cb.createQuery(BBSReplyInfo.class);
		Root<BBSReplyInfo> root = cq.from(BBSReplyInfo.class);
		Predicate p = root.get(BBSReplyInfo_.id).in(ids);
		return em.createQuery( cq.where(p) ).getResultList();
	}

	@MethodDescribe( "根据主贴ID统计主贴的回复数量" )
	public Long countBySubjectId( String subjectId ) throws Exception {
		EntityManager em = this.entityManagerContainer().get( BBSReplyInfo.class );
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<BBSReplyInfo> root = cq.from( BBSReplyInfo.class);
		Predicate p = cb.equal( root.get( BBSReplyInfo_.subjectId ), subjectId );
		cq.select( cb.count( root ) );		
		return em.createQuery(cq.where(p)).getSingleResult();
	}
	
	@MethodDescribe( "根据版块信息查询版块内主题数量，包括子版块内的主题数量" )
	public Long countBySectionId( String sectionId ) throws Exception {
		EntityManager em = this.entityManagerContainer().get( BBSReplyInfo.class );
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<BBSReplyInfo> root = cq.from( BBSReplyInfo.class);
		Predicate p = cb.equal( root.get( BBSReplyInfo_.mainSectionId ), sectionId );
		p = cb.or( p, cb.equal( root.get( BBSReplyInfo_.sectionId ), sectionId ) );
		cq.select( cb.count( root ) );		
		return em.createQuery(cq.where(p)).getSingleResult();
	}

	@MethodDescribe( "根据版块信息查询论坛内主题数量" )
	public Long countByForumId(String forumId) throws Exception {
		EntityManager em = this.entityManagerContainer().get( BBSReplyInfo.class );
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<BBSReplyInfo> root = cq.from( BBSReplyInfo.class);
		Predicate p = cb.equal( root.get( BBSReplyInfo_.forumId ), forumId );
		cq.select( cb.count( root ) );		
		return em.createQuery(cq.where(p)).getSingleResult();
	}
	
	@MethodDescribe( "根据主题ID获取该主题所有的回复信息对象列表" )
	public List<BBSReplyInfo> listWithSubjectForPage( String subjectId, Integer maxCount ) throws Exception {
		if( subjectId == null ){
			throw new Exception( "subjectId can not null." );
		}
		EntityManager em = this.entityManagerContainer().get( BBSReplyInfo.class );
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<BBSReplyInfo> cq = cb.createQuery( BBSReplyInfo.class );
		Root<BBSReplyInfo> root = cq.from( BBSReplyInfo.class );
		Predicate p = cb.equal( root.get( BBSReplyInfo_.subjectId ), subjectId );
		cq.orderBy( cb.asc( root.get( BBSReplyInfo_.orderNumber ) ) );
		if( maxCount == null ){
			return em.createQuery(cq.where(p)).getResultList();
		}else{
			return em.createQuery(cq.where(p)).setMaxResults( maxCount ).getResultList();
		}
	}

	@MethodDescribe( "根据主题ID获取该主题最大的回复编号（楼层）" )
	public Integer getMaxOrderNumber( String subjectId ) throws Exception {
		if( subjectId == null ){
			throw new Exception( "subjectId can not null." );
		}
		List<BBSReplyInfo> replyInfoList = null;
		EntityManager em = this.entityManagerContainer().get( BBSReplyInfo.class );
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<BBSReplyInfo> cq = cb.createQuery( BBSReplyInfo.class );
		Root<BBSReplyInfo> root = cq.from( BBSReplyInfo.class );
		Predicate p = cb.equal( root.get( BBSReplyInfo_.subjectId ), subjectId );
		cq.orderBy( cb.desc( root.get( BBSReplyInfo_.orderNumber ) ) );
		replyInfoList = em.createQuery(cq.where(p)).setMaxResults( 1 ).getResultList();
		if( replyInfoList != null && !replyInfoList.isEmpty() ){
			return replyInfoList.get(0).getOrderNumber();
		}else{
			return 0;
		}
	}

	@MethodDescribe( "根据指定用户姓名、论坛ID，主版块ID， 版块ID查询符合条件的所有回复的数量" )
	public Long countReplyForPage( String creatorName, String forumId, String mainSectionId, String sectionId, String subjectId ) throws Exception {
		Boolean allFilterNull = true;
		if( creatorName != null && !creatorName.isEmpty() ){
			allFilterNull = false;
		}
		if( forumId != null && !forumId.isEmpty() ){
			allFilterNull = false;
		}
		if( mainSectionId != null && !mainSectionId.isEmpty() ){
			allFilterNull = false;
		}
		if( sectionId != null && !sectionId.isEmpty() ){
			allFilterNull = false;
		}
		if( subjectId != null && !subjectId.isEmpty() ){
			allFilterNull = false;
		}
		if( allFilterNull ){
			throw new Exception( "count filter can not all null." );
		}
		EntityManager em = this.entityManagerContainer().get( BBSReplyInfo.class );
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<BBSReplyInfo> root = cq.from( BBSReplyInfo.class);
		Predicate p = cb.isNotNull( root.get( BBSReplyInfo_.id ) );
		if( creatorName != null && !creatorName.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.creatorName ), creatorName ) );
		}
		if( forumId != null && !forumId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.forumId ), forumId ) );
		}
		if( mainSectionId != null && !mainSectionId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.mainSectionId ), mainSectionId ) );
		}
		if( sectionId != null && !sectionId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.sectionId ), sectionId ) );
		}
		if( subjectId != null && !subjectId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.subjectId ), subjectId ) );
		}
		cq.select( cb.count( root ) );		
		return em.createQuery(cq.where(p)).getSingleResult();
	}

	@MethodDescribe( "根据指定用户姓名、论坛ID，主版块ID， 版块ID查询符合条件的所有回复对象列表" )
	public List<BBSReplyInfo> listReplyForPage( String creatorName, String forumId, String mainSectionId, String sectionId, String subjectId, Integer maxCount ) throws Exception {
		Boolean allFilterNull = true;
		if( creatorName != null && !creatorName.isEmpty() ){
			allFilterNull = false;
		}
		if( forumId != null && !forumId.isEmpty() ){
			allFilterNull = false;
		}
		if( mainSectionId != null && !mainSectionId.isEmpty() ){
			allFilterNull = false;
		}
		if( sectionId != null && !sectionId.isEmpty() ){
			allFilterNull = false;
		}
		if( subjectId != null && !subjectId.isEmpty() ){
			allFilterNull = false;
		}
		if( allFilterNull ){
			throw new Exception( "list filter can not all null." );
		}
		if( maxCount == null ){
			maxCount = 20;
		}
		EntityManager em = this.entityManagerContainer().get( BBSReplyInfo.class );
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<BBSReplyInfo> cq = cb.createQuery( BBSReplyInfo.class );
		Root<BBSReplyInfo> root = cq.from( BBSReplyInfo.class );
		Predicate p = cb.isNotNull( root.get( BBSReplyInfo_.id ) );
		if( creatorName != null && !creatorName.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.creatorName ), creatorName ) );
		}
		if( forumId != null && !forumId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.forumId ), forumId ) );
		}
		if( mainSectionId != null && !mainSectionId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.mainSectionId ), mainSectionId ) );
		}
		if( sectionId != null && !sectionId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.sectionId ), sectionId ) );
		}
		if( subjectId != null && !subjectId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.subjectId ), subjectId ) );
		}
		cq.orderBy( cb.asc( root.get( BBSReplyInfo_.orderNumber ) ) );
		return em.createQuery(cq.where(p)).setMaxResults( maxCount ).getResultList();
	}
	
	@MethodDescribe( "（今日）根据指定用户姓名、论坛ID，主版块ID， 版块ID查询符合条件的所有回复的数量" )
	public Long countReplyForTodayByUserName( String creatorName, String forumId, String mainSectionId, String sectionId, String subjectId ) throws Exception {
		Boolean allFilterNull = true;
		if( creatorName != null && !creatorName.isEmpty() ){
			allFilterNull = false;
		}
		if( forumId != null && !forumId.isEmpty() ){
			allFilterNull = false;
		}
		if( mainSectionId != null && !mainSectionId.isEmpty() ){
			allFilterNull = false;
		}
		if( sectionId != null && !sectionId.isEmpty() ){
			allFilterNull = false;
		}
		if( subjectId != null && !subjectId.isEmpty() ){
			allFilterNull = false;
		}
		if( allFilterNull ){
			throw new Exception( "list filter can not all null." );
		}
		DateOperation dateOperation = new DateOperation();
		EntityManager em = this.entityManagerContainer().get( BBSReplyInfo.class );
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<BBSReplyInfo> root = cq.from( BBSReplyInfo.class);
		Predicate p = cb.greaterThanOrEqualTo( root.get( BBSReplyInfo_.createTime ), dateOperation.getTodayStartTime() );
		if( creatorName != null && !creatorName.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.creatorName ), creatorName ) );
		}
		if( forumId != null && !forumId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.forumId ), forumId ) );
		}
		if( mainSectionId != null && !mainSectionId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.mainSectionId ), mainSectionId ) );
		}
		if( sectionId != null && !sectionId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.sectionId ), sectionId ) );
		}
		if( subjectId != null && !subjectId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.subjectId ), subjectId ) );
		}
		cq.select( cb.count( root ) );		
		return em.createQuery(cq.where(p)).getSingleResult();
	}
	
	@MethodDescribe( "（今日）根据指定用户姓名、论坛ID，主版块ID， 版块ID查询符合条件的所有回复对象列表" )
	public List<BBSReplyInfo> listReplyForTodayByUserName( String creatorName, String forumId, String mainSectionId, String sectionId, String subjectId, Integer maxCount ) throws Exception {
		Boolean allFilterNull = true;
		if( creatorName != null && !creatorName.isEmpty() ){
			allFilterNull = false;
		}
		if( forumId != null && !forumId.isEmpty() ){
			allFilterNull = false;
		}
		if( mainSectionId != null && !mainSectionId.isEmpty() ){
			allFilterNull = false;
		}
		if( sectionId != null && !sectionId.isEmpty() ){
			allFilterNull = false;
		}
		if( subjectId != null && !subjectId.isEmpty() ){
			allFilterNull = false;
		}
		if( allFilterNull ){
			throw new Exception( "list filter can not all null." );
		}
		if( maxCount == null ){
			maxCount = 20;
		}
		DateOperation dateOperation = new DateOperation();
		EntityManager em = this.entityManagerContainer().get( BBSReplyInfo.class );
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<BBSReplyInfo> cq = cb.createQuery( BBSReplyInfo.class );
		Root<BBSReplyInfo> root = cq.from( BBSReplyInfo.class );
		Predicate p = cb.greaterThanOrEqualTo( root.get( BBSReplyInfo_.createTime ), dateOperation.getTodayStartTime() );
		if( creatorName != null && !creatorName.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.creatorName ), creatorName ) );
		}
		if( forumId != null && !forumId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.forumId ), forumId ) );
		}
		if( mainSectionId != null && !mainSectionId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.mainSectionId ), mainSectionId ) );
		}
		if( sectionId != null && !sectionId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.sectionId ), sectionId ) );
		}
		if( subjectId != null && !subjectId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.subjectId ), subjectId ) );
		}
		cq.orderBy( cb.asc( root.get( BBSReplyInfo_.orderNumber ) ) );
		return em.createQuery(cq.where(p)).setMaxResults( maxCount ).getResultList();
	}

	public Long countReplyForTodayBySectionId( String sectionId ) throws Exception {
		DateOperation dateOperation = new DateOperation();
		EntityManager em = this.entityManagerContainer().get( BBSReplyInfo.class );
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<BBSReplyInfo> root = cq.from(BBSReplyInfo.class);
		Predicate p = cb.greaterThanOrEqualTo( root.get( BBSReplyInfo_.createTime ), dateOperation.getTodayStartTime() );
		if( sectionId != null && !sectionId.isEmpty() ){
			Predicate or = cb.equal( root.get( BBSReplyInfo_.mainSectionId ), sectionId );
			or = cb.or( or, cb.equal( root.get( BBSReplyInfo_.sectionId ), sectionId ) );
			p = cb.and( p, or );
		}
		cq.select( cb.count( root ) );		
		return em.createQuery(cq.where(p)).getSingleResult();
	}

	public Long countForTodayByForumId( String forumId ) throws Exception {
		DateOperation dateOperation = new DateOperation();
		EntityManager em = this.entityManagerContainer().get( BBSReplyInfo.class );
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<BBSReplyInfo> root = cq.from(BBSReplyInfo.class);
		Predicate p = cb.greaterThanOrEqualTo( root.get( BBSReplyInfo_.createTime ), dateOperation.getTodayStartTime() );
		if( forumId != null && !forumId.isEmpty() ){
			p = cb.and( p, cb.equal( root.get( BBSReplyInfo_.forumId ), forumId ) );
		}
		cq.select( cb.count( root ) );		
		return em.createQuery(cq.where(p)).getSingleResult();
	}
}
