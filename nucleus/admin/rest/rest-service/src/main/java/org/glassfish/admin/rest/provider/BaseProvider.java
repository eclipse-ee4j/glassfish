/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.admin.rest.Constants;
import static org.glassfish.admin.rest.provider.ProviderUtil.*;
import org.glassfish.admin.rest.utils.ConfigModelComparator;
import org.glassfish.admin.rest.utils.DomConfigurator;
import org.glassfish.admin.rest.utils.ResourceUtil;
import org.glassfish.admin.restconnector.RestConfig;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.Dom;

/**
 * @author Jason Lee
 */
@Provider
public abstract class BaseProvider<T> implements MessageBodyWriter<T> {
    public static final String HEADER_DEBUG = "__debug";

    public static final String JSONP_CALLBACK = "jsoncallback";

    @Context
    protected jakarta.inject.Provider<UriInfo> uriInfo;

    @Context
    protected jakarta.inject.Provider<HttpHeaders> requestHeaders;

    @Context
    protected ServiceLocator habitat;

    protected Class desiredType;

    protected MediaType[] supportedMediaTypes;

    public BaseProvider(Class desiredType, MediaType... mediaType) {
        this.desiredType = desiredType;
        if (mediaType == null) {
            mediaType = new MediaType[0];
        }
        this.supportedMediaTypes = mediaType;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] antns, MediaType mt) {
        if (isGivenTypeWritable(type, genericType)) {
            for (MediaType supportedMediaType : supportedMediaTypes) {
                if (mt.isCompatible(supportedMediaType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Overwrite this if you need different test of type compatibility. Used from isWritable method.
     */
    protected boolean isGivenTypeWritable(Class<?> type, Type genericType) {
        return desiredType.isAssignableFrom(type);
    }

    @Override
    public long getSize(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(T proxy, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        entityStream.write(getContent(proxy).getBytes(Constants.ENCODING));
    }

    public abstract String getContent(T proxy);

    protected int getFormattingIndentLevel() {
        RestConfig rg = ResourceUtil.getRestConfig(habitat);
        if (rg == null) {
            return -1;
        } else {
            return Integer.parseInt(rg.getIndentLevel());
        }

    }

    /**
     * returns true if the HTML viewer displays the hidden CLI command links
     */
    protected boolean canShowHiddenCommands() {

        RestConfig rg = ResourceUtil.getRestConfig(habitat);
        if ((rg != null) && (rg.getShowHiddenCommands().equalsIgnoreCase("true"))) {
            return true;
        }
        return false;
    }

    /**
     * returns true if the HTML viewer displays the deprecated elements or attributes of a config bean
     */

    protected boolean canShowDeprecatedItems() {

        RestConfig rg = ResourceUtil.getRestConfig(habitat);
        if ((rg != null) && (rg.getShowDeprecatedItems().equalsIgnoreCase("true"))) {
            return true;
        }
        return false;
    }

    /**
     * check for the __debug request header
     *
     */
    protected boolean isDebug() {

        RestConfig rg = ResourceUtil.getRestConfig(habitat);
        if ((rg != null) && (rg.getDebug().equalsIgnoreCase("true"))) {
            return true;
        }

        if (requestHeaders == null) {
            return true;
        }
        List header = requestHeaders.get().getRequestHeader(HEADER_DEBUG);
        return (header != null) && ("true".equals(header.get(0)));
    }

    /**
     * if a query param of name "jsoncallback" is there, returns its value or returns null otherwise.
     */
    protected String getCallBackJSONP() {
        if (uriInfo == null) {
            return null;
        }

        MultivaluedMap<String, String> l = uriInfo.get().getQueryParameters();

        if (l == null) {
            return null;
        }
        return l.getFirst(JSONP_CALLBACK);
    }

    protected String getXmlCommandLinks(String[][] commandResourcesPaths, String indent) {
        StringBuilder result = new StringBuilder();
        for (String[] commandResourcePath : commandResourcesPaths) {
            result.append("\n").append(indent).append(getStartXmlElement(KEY_COMMAND))
                    .append(getElementLink(uriInfo.get(), commandResourcePath[0])).append(getEndXmlElement(KEY_COMMAND));
        }
        return result.toString();
    }

    protected Map<String, String> getResourceLinks(Dom dom) {
        Map<String, String> links = new TreeMap<String, String>();
        Set<String> elementNames = dom.model.getElementNames();

        for (String elementName : elementNames) { //for each element
            if (elementName.equals("*")) {
                ConfigModel.Node node = (ConfigModel.Node) dom.model.getElement(elementName);
                ConfigModel childModel = node.getModel();
                List<ConfigModel> lcm = ResourceUtil.getRealChildConfigModels(childModel, dom.document);
                Collections.sort(lcm, new ConfigModelComparator());
                Collections.sort(lcm, new ConfigModelComparator());
                for (ConfigModel cmodel : lcm) {
                    links.put(cmodel.getTagName(), ProviderUtil.getElementLink(uriInfo.get(), cmodel.getTagName()));
                }
            } else {
                links.put(elementName, ProviderUtil.getElementLink(uriInfo.get(), elementName));
            }
        }

        return links;
    }

    protected Map<String, String> getResourceLinks(List<Dom> proxyList) {
        Map<String, String> links = new TreeMap<String, String>();
        Collections.sort(proxyList, new DomConfigurator());
        for (Dom proxy : proxyList) { //for each element
            try {
                links.put(proxy.getKey(), getElementLink(uriInfo.get(), proxy.getKey()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return links;
    }
}
