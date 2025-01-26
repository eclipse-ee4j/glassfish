/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.wadl;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.SchemaOutputResolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.xml.transform.stream.StreamResult;

import org.glassfish.admin.rest.RestLogging;
import org.glassfish.admin.rest.composite.LegacyCompositeResource;
import org.glassfish.admin.rest.composite.RestModel;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author jdlee
 */
@Path("/schema.xsd")
@Service
public class RestModelSchemaResource extends LegacyCompositeResource {

    @GET
    @Path("old")
    public String getSchema() throws JAXBException, IOException {
        Set<Class<?>> classes = new TreeSet<>(new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> t, Class<?> t1) {
                return t.getName().compareTo(t1.getName());
            }
        });
        for (String c : locateRestModels()) {
            try {
                Class<?> modelClass = loadClass(c);
                if (modelClass.getSimpleName().charAt(0) < 'C') {
                    classes.add(getCompositeUtil().getModel(modelClass).getClass());
                }
            } catch (ClassNotFoundException ex) {
                RestLogging.restLogger.log(Level.WARNING, null, ex);
            }
        }
        JAXBContext jc = JAXBContext.newInstance(classes.toArray(Class[]::new));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jc.generateSchema(new MySchemaOutputResolver(baos));
        return new String(baos.toByteArray(), UTF_8);
    }

    @GET
    @Path("test1")
    public String getSchema1() throws JAXBException, IOException {
        Set<Class<?>> classes = new TreeSet<>(new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> t, Class<?> t1) {
                return t.getName().compareTo(t1.getName());
            }
        });
        try {
            Class<?> modelClass = loadClass("org.glassfish.admin.rest.resources.composite.Job");
            classes.add(getCompositeUtil().getModel(modelClass).getClass());
            modelClass = loadClass("org.glassfish.admin.rest.resources.composite.Dummy");
            classes.add(getCompositeUtil().getModel(modelClass).getClass());
        } catch (ClassNotFoundException ex) {
            RestLogging.restLogger.log(Level.WARNING, null, ex);
        }
        JAXBContext jc = JAXBContext.newInstance(classes.toArray(Class[]::new));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jc.generateSchema(new MySchemaOutputResolver(baos));
        return new String(baos.toByteArray(), UTF_8);
    }

    @GET
    public String getSchemaManually() {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<xs:schema version=\"1.0\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n");
        //                + "\t<xs:element type=\"xs:object\" name=\"object\"/>\n");
        StringBuilder complexTypes = new StringBuilder();
        addElement(sb, "object");
        processClass(complexTypes, Object.class, "object");
        for (String c : locateRestModels()) {
            try {
                Class<?> modelClass = getCompositeUtil().getModel(loadClass(c)).getClass();
                String simpleName = modelClass.getSimpleName().toLowerCase(Locale.getDefault());
                if (simpleName.endsWith("impl")) {
                    simpleName = simpleName.substring(0, simpleName.length() - 4);
                }
                addElement(sb, simpleName);
                processClass(complexTypes, modelClass, simpleName);
            } catch (ClassNotFoundException ex) {
                RestLogging.restLogger.log(Level.WARNING, null, ex);
            }
        }
        sb.append(complexTypes);

        return sb.append("</xs:schema>\n").toString();
    }

    /*
     * TODO: This is a bit ugly, but JAXB doesn't seem to like the way we're doing models. When
     * time permits, it may be best to revisit this and see why JAXB dies when given, for example,
     * a List or Object return type.  Once that's resolved, we may be able to use JAXB serialization.
     */
    protected void processClass(StringBuilder sb, Class<?> c, String simpleName) {
        sb.append("\t<xs:complexType name=\"").append(simpleName).append("\">\n\t\t<xs:sequence>\n");
        for (Class<?> i : c.getInterfaces()) {
            for (Method m : i.getDeclaredMethods()) {
                String name = m.getName();
                if (name.startsWith("get") && !"getClass".equals(name)) {
                    name = name.substring(3, 4).toLowerCase(Locale.getDefault()) + name.substring(4);
                    Class<?> returnType = m.getReturnType();
                    sb.append("\t\t\t<xs:element name=\"").append(name).append("\" ");
                    if (returnType.isPrimitive()) {
                        sb.append(getType(returnType));
                    } else if (WRAPPER_TYPES.contains(returnType) || String.class.equals(returnType) || Object.class.equals(returnType)) {
                        sb.append(getType(returnType)).append(" minOccurs=\"0\"");
                    } else if (List.class.equals(returnType)) {
                        ParameterizedType rt = (ParameterizedType) m.getGenericReturnType();
                        returnType = (Class<?>) rt.getActualTypeArguments()[0];
                        sb.append(getType(returnType)).append(" nillable=\"true\" minOccurs=\"0\" maxOccurs=\"unbounded\"");
                    }
                    sb.append("/>\n");
                }
            }
        }

        sb.append("\t\t</xs:sequence>\n\t</xs:complexType>\n");
    }

    private String getType(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return "type=\"xs:" + clazz.getSimpleName() + "\"";
        } else if (WRAPPER_TYPES.contains(clazz) || String.class.equals(clazz)) {
            return "type=\"xs:" + clazz.getSimpleName().toLowerCase(Locale.getDefault()) + "\"";
        } else {
            return "type=\"" + clazz.getSimpleName().toLowerCase(Locale.getDefault()) + "\"";
        }
    }

    private static final Set<Class<?>> WRAPPER_TYPES = new HashSet() {
        {
            add(Boolean.class);
            add(Character.class);
            add(Byte.class);
            add(Short.class);
            add(Integer.class);
            add(Long.class);
            add(Float.class);
            add(Double.class);
            add(Void.class);
        }
    };

    private Set<String> locateRestModels() {
        Set<String> classes = new HashSet<>();

        List<ActiveDescriptor<?>> widgetDescriptors = Globals.getDefaultBaseServiceLocator()
                .getDescriptors(BuilderHelper.createContractFilter(RestModel.class.getName()));
        for (ActiveDescriptor ad : widgetDescriptors) {
            classes.add(ad.getImplementation());
        }

        return classes;
    }

    private Class<?> loadClass(String className) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException ex) {
            return getClass().getClassLoader().loadClass(className);
        }
    }

    private void addElement(StringBuilder sb, String simpleName) {
        sb.append("\t<xs:element name=\"").append(simpleName).append("\" type=\"").append(simpleName).append("\"/>\n");
    }

    private static class MySchemaOutputResolver extends SchemaOutputResolver {

        ByteArrayOutputStream baos;

        private MySchemaOutputResolver(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        @Override
        public javax.xml.transform.Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            StreamResult r = new StreamResult("argh");
            r.setOutputStream(baos);
            return r;
        }
    }
}
