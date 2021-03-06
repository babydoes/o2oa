package com.x.cms.assemble.control.jaxrs.appinfo;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.x.base.core.cache.ApplicationCache;
import com.x.base.core.http.ActionResult;
import com.x.base.core.http.EffectivePerson;
import com.x.base.core.logger.Logger;
import com.x.base.core.logger.LoggerFactory;
import com.x.cms.assemble.control.WrapTools;
import com.x.cms.assemble.control.jaxrs.appinfo.exception.AppInfoIdEmptyException;
import com.x.cms.assemble.control.jaxrs.appinfo.exception.AppInfoNotExistsException;
import com.x.cms.assemble.control.jaxrs.appinfo.exception.AppInfoProcessException;
import com.x.cms.core.entity.AppInfo;

import net.sf.ehcache.Element;

public class ExcuteGetByAlias extends ExcuteBase {

	private Logger logger = LoggerFactory.getLogger( ExcuteGetByAlias.class );
	
	protected ActionResult<WrapOutAppInfo> execute( HttpServletRequest request, EffectivePerson effectivePerson, String alias ) throws Exception {
		ActionResult<WrapOutAppInfo> result = new ActionResult<>();
		WrapOutAppInfo wrap = null;
		List<String> ids = null;
		AppInfo appInfo = null;
		Boolean check = true;
		
		if( alias == null || alias.isEmpty() ){
			check = false;
			Exception exception = new AppInfoIdEmptyException();
			result.error( exception );
			//logger.error( e, effectivePerson, request, null);
		}
		
		String cacheKey = ApplicationCache.concreteCacheKey( "alias", alias );
		Element element = cache.get(cacheKey);
		
		if ((null != element) && ( null != element.getObjectValue()) ) {
			wrap = ( WrapOutAppInfo ) element.getObjectValue();
			result.setData(wrap);
		} else {
			if( check ){
				try {
					ids = appInfoServiceAdv.getWithAlias( alias );
					if( ids == null || ids.isEmpty() ){
						check = false;
						Exception exception = new AppInfoNotExistsException( alias );
						result.error( exception );
						//logger.error( e, effectivePerson, request, null);
					}
				} catch (Exception e) {
					check = false;
					Exception exception = new AppInfoProcessException( e, "根据指定标识查询应用栏目信息对象时发生异常。ALIAS:" + alias );
					result.error( exception );
					logger.error( e, effectivePerson, request, null);
				}
			}
			
			if( check ){
				try {
					appInfo = appInfoServiceAdv.get( ids.get( 0 ) );
					if( appInfo == null ){
						check = false;
						Exception exception = new AppInfoNotExistsException( ids.get( 0 ) );
						result.error( exception );
						//logger.error( e, effectivePerson, request, null);
					}
				} catch (Exception e) {
					check = false;
					Exception exception = new AppInfoProcessException( e, "根据指定ID查询应用栏目信息对象时发生异常。ID:" + ids.get( 0 ) );
					result.error( exception );
					logger.error( e, effectivePerson, request, null);
				}
			}
			
			if( check ){
				try {
					wrap = WrapTools.appInfo_wrapout_copier.copy( appInfo );
					cache.put(new Element( cacheKey, wrap ));
					result.setData( wrap );
				} catch (Exception e) {
					Exception exception = new AppInfoProcessException( e, "将查询出来的应用栏目信息对象转换为可输出的数据信息时发生异常。" );
					result.error( exception );
					logger.error( e, effectivePerson, request, null);
				}
			}
		}
		
		return result;
	}

}