package com.x.okr.assemble.control.jaxrs.statistic;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.x.base.core.bean.BeanCopyTools;
import com.x.base.core.bean.BeanCopyToolsBuilder;
import com.x.base.core.gson.XGsonBuilder;
import com.x.base.core.http.ActionResult;
import com.x.base.core.http.EffectivePerson;
import com.x.base.core.http.HttpMediaType;
import com.x.base.core.http.WrapOutId;
import com.x.base.core.http.annotation.HttpMethodDescribe;
import com.x.base.core.logger.Logger;
import com.x.base.core.logger.LoggerFactory;
import com.x.base.core.project.jaxrs.ResponseFactory;
import com.x.base.core.project.jaxrs.StandardJaxrsAction;
import com.x.okr.assemble.common.date.DateOperation;
import com.x.okr.assemble.common.date.MonthOfYear;
import com.x.okr.assemble.common.date.WeekOfYear;
import com.x.okr.assemble.common.excel.writer.WorkReportStatusExportExcelWriter;
import com.x.okr.assemble.control.jaxrs.okrtask.WrapOutOkrTask;
import com.x.okr.assemble.control.jaxrs.statistic.exception.QueryEndDateEmptyException;
import com.x.okr.assemble.control.jaxrs.statistic.exception.QueryEndDateInvalidException;
import com.x.okr.assemble.control.jaxrs.statistic.exception.QueryStartDateEmptyException;
import com.x.okr.assemble.control.jaxrs.statistic.exception.QueryStartDateInvalidException;
import com.x.okr.assemble.control.jaxrs.statistic.exception.QueryWithConditionException;
import com.x.okr.assemble.control.jaxrs.statistic.exception.ReportStatisitcWrapOutException;
import com.x.okr.assemble.control.jaxrs.statistic.exception.WrapInConvertException;
import com.x.okr.assemble.control.service.ExcuteSt_WorkReportStatusService;
import com.x.okr.assemble.control.service.OkrStatisticReportStatusService;
import com.x.okr.assemble.control.timertask.entity.WorkBaseReportSubmitEntity;
import com.x.okr.entity.OkrStatisticReportStatus;


@Path( "streportstatus" )
public class OkrStatisticReportStatusAction extends StandardJaxrsAction{
	private Logger logger = LoggerFactory.getLogger( OkrStatisticReportStatusAction.class );
	private BeanCopyTools<OkrStatisticReportStatus, WrapOutOkrStatisticReportStatus> wrapout_copier = BeanCopyToolsBuilder.create( OkrStatisticReportStatus.class, WrapOutOkrStatisticReportStatus.class, null, WrapOutOkrStatisticReportStatus.Excludes);
	private OkrStatisticReportStatusService okrReportStatusStatisticService = new OkrStatisticReportStatusService();
	private DateOperation dateOperation = new DateOperation();

	@HttpMethodDescribe(value = "测试定时代理，对工作的汇报提交情况进行统计分析.", response = WrapOutOkrTask.class)
	@GET
	@Path( "excute" )
	@Produces(HttpMediaType.APPLICATION_JSON_UTF_8)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response excute(@Context HttpServletRequest request ) {
		EffectivePerson effectivePerson = this.effectivePerson( request );
		ActionResult<WrapOutOkrTask> result = new ActionResult<>();
		try {
			new ExcuteSt_WorkReportStatusService().execute();
		} catch (Exception e) {
			result = new ActionResult<>();
			result.error( e );
			logger.warn( "OKR_St_WorkReportStatus completed and excute got an exception." );
			logger.error( e, effectivePerson, request, null);
		}
		return ResponseFactory.getDefaultActionResultResponse(result);
	}
	
	@HttpMethodDescribe(value = "测试定时代理，对工作的汇报提交情况进行统计分析.", response = WrapOutOkrTask.class)
	@GET
	@Path( "excute/all" )
	@Produces(HttpMediaType.APPLICATION_JSON_UTF_8)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response excuteAll(@Context HttpServletRequest request ) {
		EffectivePerson effectivePerson = this.effectivePerson( request );
		ActionResult<WrapOutOkrTask> result = new ActionResult<>();
		try {
			new ExcuteSt_WorkReportStatusService().executeAll();
		} catch (Exception e) {
			result = new ActionResult<>();
			result.error( e );
			logger.warn( "OKR_St_WorkReportStatus completed and excute got an exception." );
			logger.error( e, effectivePerson, request, null);
		}
		return ResponseFactory.getDefaultActionResultResponse(result);
	}
	
	@Path( "export" )
	@HttpMethodDescribe( value = "根据条件获取OkrStatisticReportStatus部分信息对象.", request = WrapInFilterOkrStatisticReportStatus.class, response = WrapOutOkrStatisticReportStatusTable.class)
	@PUT
	@Produces( HttpMediaType.APPLICATION_JSON_UTF_8 )
	@Consumes( MediaType.APPLICATION_JSON )
	public Response export( @Context HttpServletRequest request , JsonElement jsonElement ) {
		ActionResult<WrapOutId> result = new ActionResult<>();
		
		List<WrapOutOkrStatisticReportStatus> wrapOutOkrReportSubmitStatusStatisticList = null;
		List<OkrStatisticReportStatus> okrReportStatusStatisticList = null;
		List<WrapOutOkrStatisticReportStatusEntity> organizationLayer = null;
		List<WrapOutOkrStatisticReportStatusHeader> headers = null;
		List<WeekOfYear> weeks = null;
		List<MonthOfYear> months = null;
		WrapInFilterOkrStatisticReportStatus wrapIn = null;
		EffectivePerson effectivePerson = this.effectivePerson( request );
		WrapOutOkrStatisticReportStatusTable wrapOutOkrReportSubmitStatusTable = new WrapOutOkrStatisticReportStatusTable();
		Date startDate = null;
		Date endDate = null;
		String workTitle = null;
		String startDateString = null;
		String endDateString = null;
		String workType = null;
		String cycleType = "每周汇报";
		String centerId = null;
		String workId = null;
		String organization = null;
		String fileName = null;
		
		Boolean check = true;
		
		try {
			wrapIn = this.convertToWrapIn( jsonElement, WrapInFilterOkrStatisticReportStatus.class );
		} catch (Exception e ) {
			check = false;
			Exception exception = new WrapInConvertException( e, jsonElement );
			result.error( exception );
			logger.error( e, effectivePerson, request, null);
		}
		
		if( check ){
			startDateString = wrapIn.getStartDate();
			if( startDateString != null ){
				try {
					startDate = dateOperation.getDateFromString( startDateString );
				} catch (Exception e) {
					check = false;
					Exception exception = new QueryStartDateInvalidException( e, startDateString );
					result.error( exception );
					logger.error( e, effectivePerson, request, null);
				}
			}else{
				check = false;
				Exception exception = new QueryStartDateEmptyException();
				result.error( exception );
				//logger.error( e, effectivePerson, request, null);
			}
			endDateString = wrapIn.getEndDate();
			if( endDateString != null ){
				try {
					endDate = dateOperation.getDateFromString( endDateString );
				} catch (Exception e) {
					check = false;
					Exception exception = new QueryEndDateInvalidException( e, endDateString );
					result.error( exception );
					logger.error( e, effectivePerson, request, null);
				}
			}else{
				check = false;
				Exception exception = new QueryEndDateEmptyException();
				result.error( exception );
				//logger.error( e, effectivePerson, request, null);
			}
		}
		if( check ){
			//查询过滤条件
			workTitle = wrapIn.getCenterTitle();
			workType = wrapIn.getWorkTypeName();
			cycleType = wrapIn.getCycleType();
			centerId = wrapIn.getCenterId();
			workId = wrapIn.getWorkId();
			organization = wrapIn.getOrganization();
		}
		if( check ){
			try {
				okrReportStatusStatisticList = okrReportStatusStatisticService.list( centerId, workTitle, workId, workType, organization, cycleType, "正常" );
			} catch (Exception e) {
				check = false;
				Exception exception = new QueryWithConditionException( e );
				result.error( exception );
				logger.error( e, effectivePerson, request, null);
			}
		}
		if( check ){
			if( okrReportStatusStatisticList != null ){
				try {
					wrapOutOkrReportSubmitStatusStatisticList = wrapout_copier.copy( okrReportStatusStatisticList );
				} catch (Exception e) {
					check = false;
					Exception exception = new ReportStatisitcWrapOutException( e );
					result.error( exception );
					logger.error( e, effectivePerson, request, null);
				}
			}
		}
		if( "每月汇报".equals( cycleType ) ){
			months = dateOperation.getMonthsOfYear( startDate, endDate );
			headers = getHeaderForOrganizationMonthStatistic( months );
			organizationLayer = getMonthFirstLayerArray( wrapOutOkrReportSubmitStatusStatisticList, months, startDate, endDate );
		}else{
			weeks = dateOperation.getWeeksOfYear( startDate, endDate );
			headers = getHeaderForOrganizationWeekStatistic( weeks );
			organizationLayer = getWeekFirstLayerArray( wrapOutOkrReportSubmitStatusStatisticList, weeks, startDate, endDate );
		}
		wrapOutOkrReportSubmitStatusTable.setHeader(headers);
		wrapOutOkrReportSubmitStatusTable.setContent( organizationLayer );
		
		//将所有的数据组织成EXCEL文件
		if ( check ) {
			try {
				fileName = new WorkReportStatusExportExcelWriter().writeExcel( wrapOutOkrReportSubmitStatusTable );
				result.setData( new WrapOutId( fileName ) );
			} catch ( Exception e ) {
				logger.warn( "system write export data to excel file got an exception. " );
				logger.error( e );
			}
		}
		return ResponseFactory.getDefaultActionResultResponse(result);
	}
	
	@Path( "filter/list" )
	@HttpMethodDescribe( value = "根据条件获取OkrStatisticReportStatus部分信息对象.", request = WrapInFilterOkrStatisticReportStatus.class, response = WrapOutOkrStatisticReportStatusTable.class)
	@PUT
	@Produces( HttpMediaType.APPLICATION_JSON_UTF_8 )
	@Consumes( MediaType.APPLICATION_JSON )
	public Response listByCondition( @Context HttpServletRequest request , JsonElement jsonElement ) {
		ActionResult<WrapOutOkrStatisticReportStatusTable> result = new ActionResult<>();
		List<WrapOutOkrStatisticReportStatus> wrapOutOkrReportSubmitStatusStatisticList = null;
		List<OkrStatisticReportStatus> okrReportStatusStatisticList = null;
		List<WrapOutOkrStatisticReportStatusEntity> organizationLayer = null;
		List<WrapOutOkrStatisticReportStatusHeader> headers = null;
		List<WeekOfYear> weeks = null;
		List<MonthOfYear> months = null;
		WrapInFilterOkrStatisticReportStatus wrapIn = null;
		EffectivePerson effectivePerson = this.effectivePerson( request );
		WrapOutOkrStatisticReportStatusTable wrapOutOkrReportSubmitStatusTable = new WrapOutOkrStatisticReportStatusTable();
		Date startDate = null;
		Date endDate = null;
		String workTitle = null;
		String startDateString = null;
		String endDateString = null;
		String workType = null;
		String cycleType = "每周汇报";
		String centerId = null;
		String workId = null;
		String organization = null;
		
		
		Boolean check = true;
		
		try {
			wrapIn = this.convertToWrapIn( jsonElement, WrapInFilterOkrStatisticReportStatus.class );
		} catch (Exception e ) {
			check = false;
			Exception exception = new WrapInConvertException( e, jsonElement );
			result.error( exception );
			logger.error( e, effectivePerson, request, null);
		}
		
		if( check ){
			startDateString = wrapIn.getStartDate();
			if( startDateString != null ){
				try {
					startDate = dateOperation.getDateFromString( startDateString );
				} catch (Exception e) {
					check = false;
					Exception exception = new QueryStartDateInvalidException( e, startDateString );
					result.error( exception );
					logger.error( e, effectivePerson, request, null);
				}
			}else{
				check = false;
				Exception exception = new QueryStartDateEmptyException();
				result.error( exception );
				//logger.error( e, effectivePerson, request, null);
			}
			endDateString = wrapIn.getEndDate();
			if( endDateString != null ){
				try {
					endDate = dateOperation.getDateFromString( endDateString );
				} catch (Exception e) {
					check = false;
					Exception exception = new QueryEndDateInvalidException( e, endDateString );
					result.error( exception );
					logger.error( e, effectivePerson, request, null);
				}
			}else{
				check = false;
				Exception exception = new QueryEndDateEmptyException();
				result.error( exception );
				//logger.error( e, effectivePerson, request, null);
			}
		}
		if( check ){
			//查询过滤条件
			workTitle = wrapIn.getCenterTitle();
			workType = wrapIn.getWorkTypeName();
			cycleType = wrapIn.getCycleType();
			centerId = wrapIn.getCenterId();
			workId = wrapIn.getWorkId();
			organization = wrapIn.getOrganization();
		}
		if( check ){
			try {
				okrReportStatusStatisticList = okrReportStatusStatisticService.list( centerId, workTitle, workId, workType, organization, cycleType, "正常" );
			} catch (Exception e) {
				check = false;
				Exception exception = new QueryWithConditionException( e );
				result.error( exception );
				logger.error( e, effectivePerson, request, null);
			}
		}
		if( check ){
			if( okrReportStatusStatisticList != null ){
				try {
					wrapOutOkrReportSubmitStatusStatisticList = wrapout_copier.copy( okrReportStatusStatisticList );
				} catch (Exception e) {
					check = false;
					Exception exception = new ReportStatisitcWrapOutException( e );
					result.error( exception );
					logger.error( e, effectivePerson, request, null);
				}
			}
		}
		if( "每月汇报".equals( cycleType ) ){
			months = dateOperation.getMonthsOfYear( startDate, endDate );
			headers = getHeaderForOrganizationMonthStatistic( months );
			organizationLayer = getMonthFirstLayerArray( wrapOutOkrReportSubmitStatusStatisticList, months, startDate, endDate );
		}else{
			weeks = dateOperation.getWeeksOfYear( startDate, endDate );
			headers = getHeaderForOrganizationWeekStatistic( weeks );
			organizationLayer = getWeekFirstLayerArray( wrapOutOkrReportSubmitStatusStatisticList, weeks, startDate, endDate );
		}
		wrapOutOkrReportSubmitStatusTable.setHeader(headers);
		wrapOutOkrReportSubmitStatusTable.setContent( organizationLayer );
		result.setData( wrapOutOkrReportSubmitStatusTable );
		return ResponseFactory.getDefaultActionResultResponse(result);
	}

	private List<WrapOutOkrStatisticReportStatusHeader> getHeaderForOrganizationMonthStatistic( List<MonthOfYear> months ) {
		List<WrapOutOkrStatisticReportStatusHeader> headers = new ArrayList<>();
		WrapOutOkrStatisticReportStatusHeader header = null;
		//组织一个表格头		
		if( months != null && !months.isEmpty() ){
			header = new WrapOutOkrStatisticReportStatusHeader();
			header.setTitle( "组织名称" );
			headers.add( header );
			
			header = new WrapOutOkrStatisticReportStatusHeader();
			header.setTitle( "中心工作" );
			headers.add( header );
			
			header = new WrapOutOkrStatisticReportStatusHeader();
			header.setTitle( "工作内容" );
			headers.add( header );
			for( MonthOfYear month : months ){
				header = new WrapOutOkrStatisticReportStatusHeader();
				header.setTitle( month.getYear() + "年第"+ month.getMonth() +"月" );
				header.setStartDate( month.getStartDateString() );
				header.setEndDate( month.getEndDateString() );
				headers.add( header );
			}
		}
		return headers;
	}

	private List<WrapOutOkrStatisticReportStatusHeader> getHeaderForOrganizationWeekStatistic(List<WeekOfYear> weeks) {
		List<WrapOutOkrStatisticReportStatusHeader> headers = new ArrayList<>();
		WrapOutOkrStatisticReportStatusHeader header = null;
		//组织一个表格头		
		if( weeks != null && !weeks.isEmpty() ){
			header = new WrapOutOkrStatisticReportStatusHeader();
			header.setTitle( "组织名称" );
			headers.add( header );
			
			header = new WrapOutOkrStatisticReportStatusHeader();
			header.setTitle( "中心工作" );
			headers.add( header );
			
			header = new WrapOutOkrStatisticReportStatusHeader();
			header.setTitle( "工作内容" );
			headers.add( header );
			for( WeekOfYear week : weeks ){
				header = new WrapOutOkrStatisticReportStatusHeader();
				header.setTitle( week.getYear() + "年第"+ week.getWeekNo() +"周" );
				header.setStartDate( week.getStartDateString() );
				header.setEndDate( week.getEndDateString() );
				headers.add( header );
			}
		}
		return headers;
	}

	private List<WrapOutOkrStatisticReportStatusEntity> getMonthFirstLayerArray( List<WrapOutOkrStatisticReportStatus> wrapOutOkrReportSubmitStatusStatisticList, List<MonthOfYear> months, Date startDate, Date endDate) {
		Date startDate_entity = null;
		Date endDate_entity = null;
		String statisticContent = null;
		List<WrapOutOkrStatisticReportStatusEntity> organizationLayer = new ArrayList<>();
		List<WrapOutOkrStatisticReportStatusEntity> centerLayer = null;
		List<WrapOutOkrStatisticReportStatusEntity> workLayer = null;
		WrapOutOkrStatisticReportStatusEntity organizationStatistic = null;
		WrapOutOkrStatisticReportStatusEntity centerWorkStatistic = null;
		WrapOutOkrStatisticReportStatusEntity workStatistic = null;
		Gson gson = XGsonBuilder.pureGsonDateFormated();
		List<WorkBaseReportSubmitEntity> list = null;
		List<WorkBaseReportSubmitEntity> wrapList = null;
		WorkBaseReportSubmitEntity temp = null;
		Boolean statisticExists = false;
		Boolean check = true;
		if( check ){
			//按组织排个序
			if( wrapOutOkrReportSubmitStatusStatisticList != null && !wrapOutOkrReportSubmitStatusStatisticList.isEmpty() ){
				for( WrapOutOkrStatisticReportStatus statistic : wrapOutOkrReportSubmitStatusStatisticList ){
					organizationStatistic = null;
					centerLayer = null;
					centerWorkStatistic = null;
					workLayer = null;
					workStatistic = null;
					
					//查找对应的组织的统计数据对象是否存在
					if( getFormOrganizationLayer( statistic.getResponsibilityOrganizationName(), organizationLayer ) == null ){
						organizationStatistic = new WrapOutOkrStatisticReportStatusEntity();
						organizationStatistic.setTitle( statistic.getResponsibilityOrganizationName() );
						organizationStatistic.setId( statistic.getResponsibilityOrganizationName() );
						organizationLayer.add( organizationStatistic );
					}else{
						organizationStatistic = getFormOrganizationLayer( statistic.getResponsibilityOrganizationName(), organizationLayer );
					}
					//获取组织统计对象里的中心工作列表
					if( organizationStatistic.getArray() == null ){
						centerLayer = new ArrayList<>();
						organizationStatistic.setArray( centerLayer );						
					}else{
						centerLayer = organizationStatistic.getArray();
					}
					//在组织统计对象的中心工作列表里查找应用的中心工作是否存在
					if( getFormCenterLayer( statistic.getCenterId(), centerLayer ) == null ){
						centerWorkStatistic = new WrapOutOkrStatisticReportStatusEntity();
						centerWorkStatistic.setId( statistic.getCenterId() );
						centerWorkStatistic.setTitle( statistic.getCenterTitle() );
						centerLayer.add( centerWorkStatistic );
					}else{
						centerWorkStatistic = getFormCenterLayer( statistic.getCenterId(), centerLayer );
					}
					//获取组织统计对象里的工作列表
					if( centerWorkStatistic.getArray() == null ){
						workLayer = new ArrayList<>();
						centerWorkStatistic.setArray( workLayer );					
					}else{
						workLayer = centerWorkStatistic.getArray();
					}
					if( getFormWorkLayer( statistic.getWorkId(), workLayer ) == null ){
						workStatistic = new WrapOutOkrStatisticReportStatusEntity();
						workStatistic.setId( statistic.getWorkId() );
						workStatistic.setTitle( statistic.getWorkTitle() );
						workStatistic.setDeployDate( statistic.getDeployDateStr() );
						workStatistic.setCompleteLimitDate( statistic.getCompleteDateLimitStr() );
						//过滤一下不需要的周期
						wrapList = new ArrayList<>();
						statisticContent = statistic.getReportStatistic();
						list = gson.fromJson( statisticContent, new TypeToken<List<WorkBaseReportSubmitEntity>>(){}.getType() );
						if( list != null && !list.isEmpty() ){
							if( months != null && !months.isEmpty() ){
								for( MonthOfYear month : months ){
									statisticExists = false;
									for( WorkBaseReportSubmitEntity entity : list ){
										if( month.getStartDateString().equals( entity.getStartDate() ) ){
											if( entity.getEndDate() != null ){
												try {
													endDate_entity = dateOperation.getDateFromString( entity.getEndDate() );
													startDate_entity = dateOperation.getDateFromString( entity.getStartDate() );
													if( startDate.before( endDate_entity ) || endDate.after( startDate_entity )){
														statisticExists = true;
														wrapList.add( entity );
													}
												} catch (Exception e) {
													logger.warn( "system format date got an exception." );
													logger.error(e);
												}
											}
										}
									}
									//这里如果没有的要补齐
									if( !statisticExists ){
										temp = new WorkBaseReportSubmitEntity();
										temp.setCycleNumber( month.getMonth() );
										temp.setCycleType( "每月汇报" );
										temp.setDescription( "查询时间未在工作执行时间周期内" );
										temp.setEndDate( month.getEndDateString() );
										temp.setReportId( null );
										temp.setReportStatus( -1 );
										temp.setStartDate( month.getStartDateString() );
										temp.setSubmitTime(null);
										wrapList.add( temp );
									}
								}
							}
						}
						workStatistic.setFields( wrapList );
						workLayer.add( workStatistic );
						organizationStatistic.addRowCount( 1 );
						centerWorkStatistic.addRowCount( 1 );
					}
				}
			}
		}
		return organizationLayer;
	}
	
	private List<WrapOutOkrStatisticReportStatusEntity> getWeekFirstLayerArray( List<WrapOutOkrStatisticReportStatus> wrapOutOkrReportSubmitStatusStatisticList, List<WeekOfYear> weeks, Date startDate, Date endDate ) {
		Date startDate_entity = null;
		Date endDate_entity = null;
		String statisticContent = null;
		List<WrapOutOkrStatisticReportStatusEntity> organizationLayer = new ArrayList<>();
		List<WrapOutOkrStatisticReportStatusEntity> centerLayer = null;
		List<WrapOutOkrStatisticReportStatusEntity> workLayer = null;
		WrapOutOkrStatisticReportStatusEntity organizationStatistic = null;
		WrapOutOkrStatisticReportStatusEntity centerWorkStatistic = null;
		WrapOutOkrStatisticReportStatusEntity workStatistic = null;
		Gson gson = XGsonBuilder.pureGsonDateFormated();
		List<WorkBaseReportSubmitEntity> list = null;
		List<WorkBaseReportSubmitEntity> wrapList = null;
		WorkBaseReportSubmitEntity temp = null;
		Boolean statisticExists = false;
		Boolean check = true;
		if( check ){
			//按组织排个序
			if( wrapOutOkrReportSubmitStatusStatisticList != null && !wrapOutOkrReportSubmitStatusStatisticList.isEmpty() ){
				for( WrapOutOkrStatisticReportStatus statistic : wrapOutOkrReportSubmitStatusStatisticList ){
					organizationStatistic = null;
					centerLayer = null;
					centerWorkStatistic = null;
					workLayer = null;
					workStatistic = null;
					
					//查找对应的组织的统计数据对象是否存在
					if( getFormOrganizationLayer( statistic.getResponsibilityOrganizationName(), organizationLayer ) == null ){
						organizationStatistic = new WrapOutOkrStatisticReportStatusEntity();
						organizationStatistic.setTitle( statistic.getResponsibilityOrganizationName() );
						organizationStatistic.setId( statistic.getResponsibilityOrganizationName() );
						organizationLayer.add( organizationStatistic );
					}else{
						organizationStatistic = getFormOrganizationLayer( statistic.getResponsibilityOrganizationName(), organizationLayer );
					}
					//获取组织统计对象里的中心工作列表
					if( organizationStatistic.getArray() == null ){
						centerLayer = new ArrayList<>();
						organizationStatistic.setArray( centerLayer );						
					}else{
						centerLayer = organizationStatistic.getArray();
					}
					//在组织统计对象的中心工作列表里查找应用的中心工作是否存在
					if( getFormCenterLayer( statistic.getCenterId(), centerLayer ) == null ){
						centerWorkStatistic = new WrapOutOkrStatisticReportStatusEntity();
						centerWorkStatistic.setId( statistic.getCenterId() );
						centerWorkStatistic.setTitle( statistic.getCenterTitle() );
						centerLayer.add( centerWorkStatistic );
					}else{
						centerWorkStatistic = getFormCenterLayer( statistic.getCenterId(), centerLayer );
					}
					//获取组织统计对象里的工作列表
					if( centerWorkStatistic.getArray() == null ){
						workLayer = new ArrayList<>();
						centerWorkStatistic.setArray( workLayer );					
					}else{
						workLayer = centerWorkStatistic.getArray();
					}
					if( getFormWorkLayer( statistic.getWorkId(), workLayer ) == null ){
						workStatistic = new WrapOutOkrStatisticReportStatusEntity();
						workStatistic.setId( statistic.getWorkId() );
						workStatistic.setTitle( statistic.getWorkTitle() );
						workStatistic.setDeployDate( statistic.getDeployDateStr() );
						workStatistic.setCompleteLimitDate( statistic.getCompleteDateLimitStr() );
						//过滤一下不需要的周期
						wrapList = new ArrayList<>();
						statisticContent = statistic.getReportStatistic();
						list = gson.fromJson( statisticContent, new TypeToken<List<WorkBaseReportSubmitEntity>>(){}.getType() );
						if( list != null && !list.isEmpty() ){
							if( weeks != null && !weeks.isEmpty() ){
								for( WeekOfYear week : weeks ){
									statisticExists = false;
									for( WorkBaseReportSubmitEntity entity : list ){
										if( week.getStartDateString().equals( entity.getStartDate() ) ){
											if( entity.getEndDate() != null ){
												try {
													endDate_entity = dateOperation.getDateFromString( entity.getEndDate() );
													startDate_entity = dateOperation.getDateFromString( entity.getStartDate() );
													if( startDate.before( endDate_entity ) || endDate.after( startDate_entity )){
														statisticExists = true;
														wrapList.add( entity );
													}
												} catch (Exception e) {
													logger.warn( "system format date got an exception." );
													logger.error(e );
												}
											}
										}
									}
									//这里如果没有的要补齐
									if( !statisticExists ){
										temp = new WorkBaseReportSubmitEntity();
										temp.setCycleNumber( week.getWeekNo() );
										temp.setCycleType( "每周汇报" );
										temp.setDescription( "查询时间未在工作执行时间周期内" );
										temp.setEndDate( week.getEndDateString() );
										temp.setReportId( null );
										temp.setReportStatus( -1 );
										temp.setStartDate( week.getStartDateString() );
										temp.setSubmitTime(null);
										wrapList.add( temp );
									}
								}
							}
						}
						workStatistic.setFields( wrapList );
						workLayer.add( workStatistic );
						organizationStatistic.addRowCount( 1 );
						centerWorkStatistic.addRowCount( 1 );
					}
				}
			}
		}
		return organizationLayer;
	}

	private WrapOutOkrStatisticReportStatusEntity getFormWorkLayer(String workId, List<WrapOutOkrStatisticReportStatusEntity> workLayer) {
		if( workId == null || workId.isEmpty() ){
			return null;
		}
		if( workLayer == null || workLayer.isEmpty() ){
			return null;
		}
		for( WrapOutOkrStatisticReportStatusEntity entity : workLayer ){
			if( workId.equals( entity.getId() ) ){
				return entity;
			}
		}
		return null;
	}

	private WrapOutOkrStatisticReportStatusEntity getFormCenterLayer(String centerId, List<WrapOutOkrStatisticReportStatusEntity> centerLayer) {
		if( centerId == null || centerId.isEmpty() ){
			return null;
		}
		if( centerLayer == null || centerLayer.isEmpty() ){
			return null;
		}
		for( WrapOutOkrStatisticReportStatusEntity entity : centerLayer ){
			if( centerId.equals( entity.getId() ) ){
				return entity;
			}
		}
		return null;
	}


	private WrapOutOkrStatisticReportStatusEntity getFormOrganizationLayer( String responsibilityOrganizationName, List<WrapOutOkrStatisticReportStatusEntity> organizationLayer) {
		if( responsibilityOrganizationName == null || responsibilityOrganizationName.isEmpty() ){
			return null;
		}
		if( organizationLayer == null || organizationLayer.isEmpty() ){
			return null;
		}
		for( WrapOutOkrStatisticReportStatusEntity entity : organizationLayer ){
			if( responsibilityOrganizationName.equals( entity.getTitle() ) ){
				return entity;
			}
		}
		return null;
	}

}
