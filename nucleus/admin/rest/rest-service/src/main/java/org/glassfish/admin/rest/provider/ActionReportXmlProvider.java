/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.admin.rest.provider;

import com.sun.enterprise.v3.common.ActionReporter;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.admin.rest.utils.xml.XmlArray;
import org.glassfish.admin.rest.utils.xml.XmlMap;
import org.glassfish.admin.rest.utils.xml.XmlObject;
import org.glassfish.api.ActionReport.MessagePart;

/**
 * @author Ludovic Champenois
 * @author mmares
 */
@Provider
@Produces(MediaType.APPLICATION_XML)
public class ActionReportXmlProvider extends BaseProvider<ActionReporter> {

    public ActionReportXmlProvider() {
        super(ActionReporter.class, MediaType.APPLICATION_XML_TYPE);
    }

    @Override
    public String getContent(ActionReporter ar) {
        XmlObject result = processReport(ar);
        return result.toString(getFormattingIndentLevel());
    }

    @Override
    protected boolean isGivenTypeWritable(Class<?> type, Type genericType) {
        return desiredType.isAssignableFrom(type);
    }

    protected XmlObject processReport(ActionReporter ar) {
        XmlMap result = new XmlMap("map");
        result.put("message", (ar instanceof RestActionReporter) ? ((RestActionReporter) ar).getCombinedMessage() : ar.getMessage());
        result.put("command", ar.getActionDescription());
        result.put("exit_code", ar.getActionExitCode().toString());

        Properties properties = ar.getTopMessagePart().getProps();
        if ((properties != null) && (!properties.isEmpty())) {
            result.put("properties", new XmlMap("properties", properties));
        }

        Properties extraProperties = ar.getExtraProperties();
        if ((extraProperties != null) && (!extraProperties.isEmpty())) {
            result.put("extraProperties", getExtraProperties(result, extraProperties));
        }

        List<MessagePart> children = ar.getTopMessagePart().getChildren();
        if ((children != null) && (!children.isEmpty())) {
            result.put("children", processChildren(children));
        }

        List<ActionReporter> subReports = ar.getSubActionsReport();
        if ((subReports != null) && (!subReports.isEmpty())) {
            result.put("subReports", processSubReports(subReports));
        }

        return result;
    }

    protected XmlArray processChildren(List<MessagePart> parts) {
        XmlArray array = new XmlArray("children");

        for (MessagePart part : parts) {
            XmlMap object = new XmlMap("part");
            object.put("message", part.getMessage());
            object.put("properties", new XmlMap("properties", part.getProps()));
            List<MessagePart> children = part.getChildren();
            if (children.size() > 0) {
                object.put("children", processChildren(part.getChildren()));
            }
            array.put(object);
        }

        return array;
    }

    protected XmlArray processSubReports(List<ActionReporter> subReports) {
        XmlArray array = new XmlArray("subReports");

        for (ActionReporter subReport : subReports) {
            array.put(processReport(subReport));
        }

        return array;
    }

    protected XmlMap getExtraProperties(XmlObject object, Properties props) {
        XmlMap extraProperties = new XmlMap("extraProperties");
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = entry.getKey().toString();
            Object value = getXmlObject(entry.getValue());
            if (value != null) {
                extraProperties.put(key, value);
            }
        }

        return extraProperties;
    }

    protected Object getXmlObject(Object object) {
        Object result = null;
        if (object == null) {
            result = "";
        } else if (object instanceof Collection) {
            result = getXml((Collection) object);
        } else if (object instanceof Map) {
            result = getXml((Map) object);
        } else if (object instanceof Number) {
            result = new XmlObject("number", (Number) object);
        } else if (object instanceof String) {
            result = object;
        } else {
            result = new XmlObject(object.getClass().getSimpleName(), object);
        }

        return result;
    }

    protected XmlArray getXml(Collection c) {
        XmlArray result = new XmlArray("list");
        Iterator i = c.iterator();
        while (i.hasNext()) {
            Object item = i.next();
            Object obj = getXmlObject(item);
            if (!(obj instanceof XmlObject)) {
                obj = new XmlObject(obj.getClass().getSimpleName(), obj);
            }
            result.put((XmlObject) obj);
        }

        return result;
    }

    protected XmlMap getXml(Map map) {
        XmlMap result = new XmlMap("map");

        for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
            result.put(entry.getKey().toString(), getXmlObject(entry.getValue()));
        }

        return result;
    }

}
