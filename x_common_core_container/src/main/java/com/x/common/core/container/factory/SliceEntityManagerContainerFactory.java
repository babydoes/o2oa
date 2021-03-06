package com.x.common.core.container.factory;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.x.base.core.entity.JpaObject;
import com.x.base.core.entity.annotation.CheckPersist;
import com.x.base.core.entity.annotation.CheckRemove;
import com.x.common.core.container.DataMappings;

public abstract class SliceEntityManagerContainerFactory {

	protected static String persistence_xml_path = "META-INF/x_persistence.xml";

	// class 与 entityManagerFactory 映射表
	protected Map<Class<? extends JpaObject>, EntityManagerFactory> entityManagerFactoryMap = new ConcurrentHashMap<Class<? extends JpaObject>, EntityManagerFactory>();
	// class 与 class 中需要检查 Persist 字段的对应表
	protected Map<Class<? extends JpaObject>, Map<Field, CheckPersist>> checkPersistFieldMap = new ConcurrentHashMap<Class<? extends JpaObject>, Map<Field, CheckPersist>>();
	// class 与 class 中需要检查 Remove 字段的对应表
	protected Map<Class<? extends JpaObject>, Map<Field, CheckRemove>> checkRemoveFieldMap = new ConcurrentHashMap<Class<? extends JpaObject>, Map<Field, CheckRemove>>();

	protected SliceEntityManagerContainerFactory(String webApplicationDirectory,DataMappings dataMappings) throws Exception {
		Set<Class<? extends JpaObject>> classes = mergePersistenceXml(webApplicationDirectory, dataMappings);
		for (Class<? extends JpaObject> clz : classes) {
			checkPersistFieldMap.put(clz, this.loadCheckPersistField(clz));
			checkRemoveFieldMap.put(clz, loadCheckRemoveField(clz));
			entityManagerFactoryMap.put(clz,
					OpenJPAPersistence.createEntityManagerFactory(clz.getCanonicalName(), persistence_xml_path));
		}
	}

	/* 扫描受管实体,生成 x_perisitence.xml */
	@SuppressWarnings("unchecked")
	private Set<Class<? extends JpaObject>> mergePersistenceXml(String webApplicationDirectory, DataMappings dataMappings)
			throws Exception {
		try {
			Set<Class<? extends JpaObject>> classes = new HashSet<>();
			File file = new File(webApplicationDirectory + "/WEB-INF/classes/" + persistence_xml_path);
			SAXReader reader = new SAXReader();
			Document document = reader.read(file);
			for (Object o : document.getRootElement().elements("persistence-unit")) {
				Element unit = (Element) o;
				String name = unit.attribute("name").getValue();
				System.out.println("try to load entity class:" + name);
				Element properties = unit.element("properties");
				properties.clearContent();
				for (Entry<String, String> entry : SlicePropertiesBuilder.getPropertiesDBCP(dataMappings.get(name))
						.entrySet()) {
					Element property = properties.addElement("property");
					property.addAttribute("name", entry.getKey());
					property.addAttribute("value", entry.getValue());
				}
				classes.add((Class<JpaObject>) Class.forName(name));
			}
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");
			XMLWriter writer = new XMLWriter(new FileWriter(file), format);
			writer.write(document);
			writer.close();
			return classes;
		} catch (Exception e) {
			throw new Exception("registContainerEntity error.", e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Class<T> assignableFrom(Class<T> cls) throws Exception {
		for (Class<?> clazz : this.entityManagerFactoryMap.keySet()) {
			if (clazz.isAssignableFrom(cls)) {
				return (Class<T>) clazz;
			}
		}
		throw new Exception("can not find jpa assignable class for " + cls + ".");
	}

	private <T extends JpaObject> Map<Field, CheckPersist> loadCheckPersistField(Class<T> cls) throws Exception {
		Map<Field, CheckPersist> map = new HashMap<Field, CheckPersist>();
		for (Field fld : cls.getDeclaredFields()) {
			CheckPersist checkPersist = fld.getAnnotation(CheckPersist.class);
			if (null != checkPersist) {
				map.put(fld, checkPersist);
			}

		}
		return map;
	}

	private <T extends JpaObject> Map<Field, CheckRemove> loadCheckRemoveField(Class<T> cls) throws Exception {
		Map<Field, CheckRemove> map = new HashMap<Field, CheckRemove>();
		for (Field fld : cls.getDeclaredFields()) {
			CheckRemove checkRemove = fld.getAnnotation(CheckRemove.class);
			if (null != checkRemove) {
				map.put(fld, checkRemove);
			}
		}
		return map;
	}

}