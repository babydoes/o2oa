/** 
 *  Generated by OpenJPA MetaModel Generator Tool.
**/

package com.x.attendance.entity;

import com.x.base.core.entity.SliceJpaObject_;
import java.lang.String;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel
(value=com.x.attendance.entity.AttendanceWorkDayConfig.class)
@javax.annotation.Generated
(value="org.apache.openjpa.persistence.meta.AnnotationProcessor6",date="Sat May 06 19:33:53 CST 2017")
public class AttendanceWorkDayConfig_ extends SliceJpaObject_  {
    public static volatile SingularAttribute<AttendanceWorkDayConfig,String> configDate;
    public static volatile SingularAttribute<AttendanceWorkDayConfig,String> configMonth;
    public static volatile SingularAttribute<AttendanceWorkDayConfig,String> configName;
    public static volatile SingularAttribute<AttendanceWorkDayConfig,String> configType;
    public static volatile SingularAttribute<AttendanceWorkDayConfig,String> configYear;
    public static volatile SingularAttribute<AttendanceWorkDayConfig,Date> createTime;
    public static volatile SingularAttribute<AttendanceWorkDayConfig,String> description;
    public static volatile SingularAttribute<AttendanceWorkDayConfig,String> id;
    public static volatile SingularAttribute<AttendanceWorkDayConfig,String> sequence;
    public static volatile SingularAttribute<AttendanceWorkDayConfig,Date> updateTime;
}
