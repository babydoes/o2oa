/** 
 *  Generated by OpenJPA MetaModel Generator Tool.
**/

package com.x.organization.core.entity;

import com.x.base.core.entity.SliceJpaObject_;
import java.lang.String;
import java.util.Date;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel
(value=com.x.organization.core.entity.DepartmentDuty.class)
@javax.annotation.Generated
(value="org.apache.openjpa.persistence.meta.AnnotationProcessor6",date="Sat May 06 19:35:40 CST 2017")
public class DepartmentDuty_ extends SliceJpaObject_  {
    public static volatile SingularAttribute<DepartmentDuty,Date> createTime;
    public static volatile SingularAttribute<DepartmentDuty,String> department;
    public static volatile SingularAttribute<DepartmentDuty,String> id;
    public static volatile ListAttribute<DepartmentDuty,String> identityList;
    public static volatile SingularAttribute<DepartmentDuty,String> name;
    public static volatile SingularAttribute<DepartmentDuty,String> pinyin;
    public static volatile SingularAttribute<DepartmentDuty,String> pinyinInitial;
    public static volatile SingularAttribute<DepartmentDuty,String> sequence;
    public static volatile SingularAttribute<DepartmentDuty,String> unique;
    public static volatile SingularAttribute<DepartmentDuty,Date> updateTime;
}
