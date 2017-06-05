/*
Copyright (c) 2016, Shanghai YUEWEN Information Technology Co., Ltd. 
All rights reserved.
Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, 
* this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, 
* this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of Shanghai YUEWEN Information Technology Co., Ltd. 
* nor the names of its contributors may be used to endorse or promote products derived from 
* this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY SHANGHAI YUEWEN INFORMATION TECHNOLOGY CO., LTD. 
AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Copyright (c) 2016 著作权由上海阅文信息技术有限公司所有。著作权人保留一切权利。

这份授权条款，在使用者符合以下三条件的情形下，授予使用者使用及再散播本软件包装原始码及二进位可执行形式的权利，无论此包装是否经改作皆然：

* 对于本软件源代码的再散播，必须保留上述的版权宣告、此三条件表列，以及下述的免责声明。
* 对于本套件二进位可执行形式的再散播，必须连带以文件以及／或者其他附于散播包装中的媒介方式，重制上述之版权宣告、此三条件表列，以及下述的免责声明。
* 未获事前取得书面许可，不得使用柏克莱加州大学或本软件贡献者之名称，来为本软件之衍生物做任何表示支持、认可或推广、促销之行为。

免责声明：本软件是由上海阅文信息技术有限公司及本软件之贡献者以现状提供，本软件包装不负任何明示或默示之担保责任，
包括但不限于就适售性以及特定目的的适用性为默示性担保。加州大学董事会及本软件之贡献者，无论任何条件、无论成因或任何责任主义、
无论此责任为因合约关系、无过失责任主义或因非违约之侵权（包括过失或其他原因等）而起，对于任何因使用本软件包装所产生的任何直接性、间接性、
偶发性、特殊性、惩罚性或任何结果的损害（包括但不限于替代商品或劳务之购用、使用损失、资料损失、利益损失、业务中断等等），
不负任何责任，即在该种使用已获事前告知可能会造成此类损害的情形下亦然。
*/
package org.albianj.persistence.impl.mapping;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.albianj.loader.AlbianClassLoader;
import org.albianj.logger.IAlbianLoggerService;
import org.albianj.persistence.impl.object.AlbianObjectAttribute;
import org.albianj.persistence.impl.object.CacheAttribute;
import org.albianj.persistence.impl.object.DataRouterAttribute;
import org.albianj.persistence.impl.object.MemberAttribute;
import org.albianj.persistence.impl.routing.AlbianDataRouterParserService;
import org.albianj.persistence.impl.storage.AlbianStorageParserService;
import org.albianj.persistence.impl.toolkit.Convert;
import org.albianj.persistence.object.AlbianObjectMemberAttribute;
import org.albianj.persistence.object.IAlbianObject;
import org.albianj.persistence.object.IAlbianObjectAttribute;
import org.albianj.persistence.object.IAlbianObjectDataRouter;
import org.albianj.persistence.object.ICacheAttribute;
import org.albianj.persistence.object.IDataRouterAttribute;
import org.albianj.persistence.object.IMemberAttribute;
import org.albianj.persistence.service.MappingAttributeException;
import org.albianj.reflection.AlbianReflect;
import org.albianj.service.AlbianServiceRouter;
import org.albianj.service.parser.AlbianParserException;
import org.albianj.verify.Validate;
import org.albianj.xml.XmlParser;
import org.dom4j.Element;
import org.dom4j.Node;

public class AlbianMappingParserService extends FreeAlbianMappingParserService {

	private static final String cacheTagName = "Cache";
	private static final String memberTagName = "Members/Member";

	@Override
	protected void parserAlbianObjects(@SuppressWarnings("rawtypes") List nodes) throws AlbianParserException {
		if (Validate.isNullOrEmpty(nodes)) {
			throw new IllegalArgumentException("nodes");
		}
		String inter = null;
		for (Object node : nodes) {
			IAlbianObjectAttribute albianObjectAttribute = null;
			Element ele = (Element) node;
			try {
				albianObjectAttribute = parserAlbianObject(ele);
			} catch (Exception e) {
				AlbianServiceRouter.getLogger().errorAndThrow(IAlbianLoggerService.AlbianRunningLoggerName,
						AlbianParserException.class, e, "PersistenService is error.",
						"parser persisten node is fail,xml:%s", ele.asXML());
			}
			if (null == albianObjectAttribute) {
				AlbianServiceRouter.getLogger().errorAndThrow(IAlbianLoggerService.AlbianRunningLoggerName,
						AlbianParserException.class, "PersistenService is error.",
						"parser persisten node is fail,the node attribute is null,xml:%s", ele.asXML());
			}
			inter = albianObjectAttribute.getInterface();
			addAlbianObjectAttribute(inter, albianObjectAttribute);
			// AlbianObjectsMap.insert(inter, albianObjectAttribute);
		}

	}

	@Override
	protected IAlbianObjectAttribute parserAlbianObject(Element node) throws AlbianParserException {
		String type = XmlParser.getAttributeValue(node, "Type");
		if (Validate.isNullOrEmptyOrAllSpace(type)) {
			AlbianServiceRouter.getLogger().error(IAlbianLoggerService.AlbianRunningLoggerName,
					"The albianObject's type is empty or null.");
			return null;
		}

		String inter = XmlParser.getAttributeValue(node, "Interface");
		if (Validate.isNullOrEmptyOrAllSpace(inter)) {
			AlbianServiceRouter.getLogger().error(IAlbianLoggerService.AlbianRunningLoggerName,
					"The albianObject's Interface is empty or null.");
			return null;
		}

		try {
			Class<?> cls = AlbianClassLoader.getInstance().loadClass(type);
			Class<?> itf = AlbianClassLoader.getInstance().loadClass(inter);
			if (!itf.isAssignableFrom(cls)) {
				AlbianServiceRouter.getLogger().errorAndThrow(IAlbianLoggerService.AlbianRunningLoggerName,
						new TypeNotPresentException("assignable is fail.", null),
						"the albian-object class:%s is not implements from interface:%s.", type, inter);
			}

			if (!IAlbianObject.class.isAssignableFrom(cls)) {
				AlbianServiceRouter.getLogger().errorAndThrow(IAlbianLoggerService.AlbianRunningLoggerName,
						new TypeNotPresentException("assignable is fail.", null),
						"the albian-object class:%s is not implements from interface: IAlbianObject.", type);
			}

			if (!IAlbianObject.class.isAssignableFrom(itf)) {
				AlbianServiceRouter.getLogger().errorAndThrow(IAlbianLoggerService.AlbianRunningLoggerName,
						new TypeNotPresentException("assignable is fail.", null),
						"the albian-object interface:%s is not implements from interface: IAlbianObject.", inter);
			}

		} catch (ClassNotFoundException e1) {
			AlbianServiceRouter.getLogger().errorAndThrow(IAlbianLoggerService.AlbianRunningLoggerName,
					AlbianParserException.class, e1, "not found type.", "the type:%s is not found", type);
		}

		addAlbianObjectClassToInterface(type, inter);
		Map<String, IMemberAttribute> map = reflexAlbianObjectMembers(type);
		Node cachedNode = node.selectSingleNode(cacheTagName);
		ICacheAttribute cached;
		if (null == cachedNode) {
			cached = new CacheAttribute();
			cached.setEnable(false);
			cached.setLifeTime(300);
		} else {
			cached = parserAlbianObjectCache(cachedNode);
		}

		IDataRouterAttribute defaultRouting = new DataRouterAttribute();
		defaultRouting.setName(AlbianDataRouterParserService.DEFAULT_ROUTING_NAME);
		defaultRouting.setOwner("dbo");
		defaultRouting.setStorageName(AlbianStorageParserService.DEFAULT_STORAGE_NAME);
		String csn = null;
		try {
			csn = AlbianReflect.getClassSimpleName(AlbianClassLoader.getInstance(), type);
		} catch (ClassNotFoundException e) {
			AlbianServiceRouter.getLogger().errorAndThrow(IAlbianLoggerService.AlbianRunningLoggerName,
					AlbianParserException.class, e, "not found type.", "the type:%s is not found", type);
		}
		if (null != csn) {
			defaultRouting.setTableName(csn);
		}

		IAlbianObjectAttribute albianObjectAttribute = new AlbianObjectAttribute();
		Node tnode = node.selectSingleNode("Transaction");
		if(null != tnode){
			String sCompensating = XmlParser.getAttributeValue(node, "Compensating");
			if(!Validate.isNullOrEmptyOrAllSpace(sCompensating)){
				 albianObjectAttribute.setCompensating(new Boolean(sCompensating));
			}
		}

		@SuppressWarnings("rawtypes")
		List nodes = node.selectNodes(memberTagName);
		if (!Validate.isNullOrEmpty(nodes)) {
			parserAlbianObjectMembers(type, nodes, map);
		}

		albianObjectAttribute.setCache(cached);
		albianObjectAttribute.setMembers(map);
		albianObjectAttribute.setType(type);
		albianObjectAttribute.setInterface(inter);
		albianObjectAttribute.setDefaultRouting(defaultRouting);
		return albianObjectAttribute;
	}

	private static ICacheAttribute parserAlbianObjectCache(Node node) {
		String enable = XmlParser.getAttributeValue(node, "Enable");
		String lifeTime = XmlParser.getAttributeValue(node, "LifeTime");
		String name = XmlParser.getAttributeValue(node, "Name");
		ICacheAttribute cache = new CacheAttribute();
		cache.setEnable(Validate.isNullOrEmptyOrAllSpace(enable) ? true : new Boolean(enable));
		cache.setLifeTime(Validate.isNullOrEmptyOrAllSpace(lifeTime) ? 300 : new Integer(lifeTime));
		cache.setName(Validate.isNullOrEmptyOrAllSpace(name) ? "Default" : name);
		return cache;
	}

	private static void parserAlbianObjectMembers(String type, @SuppressWarnings("rawtypes") List nodes,
			Map<String, IMemberAttribute> map) throws AlbianParserException {
		for (Object node : nodes) {
			parserAlbianObjectMember(type, (Element) node, map);
		}
	}

	private static void parserAlbianObjectMember(String type, Element elt, Map<String, IMemberAttribute> map)
			throws AlbianParserException {
		String name = XmlParser.getAttributeValue(elt, "Name");
		if (Validate.isNullOrEmpty(name)) {
			AlbianServiceRouter.getLogger().errorAndThrow(IAlbianLoggerService.AlbianRunningLoggerName,
					AlbianParserException.class, "PersistenService is error.",
					"the persisten node name is null or empty.type:%s,node xml:%s.", type, elt.asXML());
		}
		IMemberAttribute member = (IMemberAttribute) map.get(name.toLowerCase());
		if (null == member) {
			AlbianServiceRouter.getLogger().errorAndThrow(IAlbianLoggerService.AlbianRunningLoggerName,
					AlbianParserException.class, "PersistenService is error.",
					"the field: %1$s is not found in the %2$s.", name, type);
		}

		String fieldName = XmlParser.getAttributeValue(elt, "FieldName");
		String allowNull = XmlParser.getAttributeValue(elt, "AllowNull");
		String length = XmlParser.getAttributeValue(elt, "Length");
		String primaryKey = XmlParser.getAttributeValue(elt, "PrimaryKey");
		String dbType = XmlParser.getAttributeValue(elt, "DbType");
		String isSave = XmlParser.getAttributeValue(elt, "IsSave");
		if (!Validate.isNullOrEmpty(fieldName)) {
			member.setSqlFieldName(fieldName);
		}
		if (!Validate.isNullOrEmpty(allowNull)) {
			member.setAllowNull(new Boolean(allowNull));
		}
		if (!Validate.isNullOrEmpty(length)) {
			member.setLength(new Integer(length));
		}
		if (!Validate.isNullOrEmpty(primaryKey)) {
			member.setPrimaryKey(new Boolean(primaryKey));
		}
		if (!Validate.isNullOrEmpty(dbType)) {
			member.setDatabaseType(Convert.toSqlType(dbType));
		}
		if (!Validate.isNullOrEmpty(isSave)) {
			member.setIsSave(new Boolean(isSave));
		}
	}

	private Map<String, IMemberAttribute> reflexAlbianObjectMembers(String type) throws AlbianParserException {
		Map<String, IMemberAttribute> map = new LinkedHashMap<String, IMemberAttribute>();
		PropertyDescriptor[] propertyDesc = null;
		try {
			propertyDesc = AlbianReflect.getBeanPropertyDescriptor(AlbianClassLoader.getInstance(), type);
		} catch (ClassNotFoundException e) {
			AlbianServiceRouter.getLogger().errorAndThrow(IAlbianLoggerService.AlbianRunningLoggerName,
					AlbianParserException.class, e, "not found type.", "the type:%s is not found", type);
		} catch (IntrospectionException e) {
			AlbianServiceRouter.getLogger().errorAndThrow(IAlbianLoggerService.AlbianRunningLoggerName,
					AlbianParserException.class, e, "not found type.", "the type:%s is not found", type);
		}
		if (null == propertyDesc)
			AlbianServiceRouter.getLogger().errorAndThrow(IAlbianLoggerService.AlbianRunningLoggerName,
					AlbianParserException.class, "not found type.", "the type:%s is not found", type);
		addAlbianObjectPropertyDescriptor(type, propertyDesc);
		for (PropertyDescriptor p : propertyDesc) {
			IMemberAttribute member = reflexAlbianObjectMember(type, p);
			if (null == member) {
				throw new MappingAttributeException(String.format("reflx albianobject:%s is fail.", type));
			}
			map.put(member.getName().toLowerCase(), member);
		}
		return map;
	}

	private static IMemberAttribute reflexAlbianObjectMember(String type, PropertyDescriptor propertyDescriptor) {
		Method mr = propertyDescriptor.getReadMethod();
		Method mw = propertyDescriptor.getWriteMethod();
		if (null == mr || null == mw) {
			AlbianServiceRouter.getLogger().error(IAlbianLoggerService.AlbianRunningLoggerName,
					"property:%s of type:%s is not exist readerMethod or writeMethon.", propertyDescriptor.getName(),
					type);
			return null;
		}
		AlbianObjectMemberAttribute attr = null;
		if (mr.isAnnotationPresent(AlbianObjectMemberAttribute.class))
			attr = mr.getAnnotation(AlbianObjectMemberAttribute.class);
		if (mw.isAnnotationPresent(AlbianObjectMemberAttribute.class))
			attr = mw.getAnnotation(AlbianObjectMemberAttribute.class);

		IMemberAttribute member = new MemberAttribute();
		if (null != attr) {
			member.setName(propertyDescriptor.getName());

			if (Validate.isNullOrEmptyOrAllSpace(attr.FieldName())) {
				member.setSqlFieldName(propertyDescriptor.getName());
			} else {
				member.setSqlFieldName(attr.FieldName());
			}
			member.setAllowNull(attr.IsAllowNull());
			if (0 == attr.DbType()) {
				member.setDatabaseType(Convert.toSqlType(propertyDescriptor.getPropertyType()));
			} else {
				member.setDatabaseType(attr.DbType());
			}
			member.setIsSave(attr.IsSave());
			member.setLength(attr.Length());
			member.setPrimaryKey(attr.IsPrimaryKey());
			return member;
		}

		if ("isAlbianNew".equals(propertyDescriptor.getName())) {
			member.setIsSave(false);
			member.setName(propertyDescriptor.getName());
			return member;
		}
		member.setAllowNull(true);
		member.setDatabaseType(Convert.toSqlType(propertyDescriptor.getPropertyType()));
		member.setSqlFieldName(propertyDescriptor.getName());
		member.setIsSave(true);
		member.setLength(-1);
		member.setPrimaryKey(false);
		member.setName(propertyDescriptor.getName());
		return member;
	}
}
