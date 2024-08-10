/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.Map;

import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.results.GetResultList;
import org.jvnet.hk2.config.Dom;

import static org.glassfish.admin.rest.provider.ProviderUtil.KEY_CHILD_RESOURCE;
import static org.glassfish.admin.rest.provider.ProviderUtil.KEY_CHILD_RESOURCES;
import static org.glassfish.admin.rest.provider.ProviderUtil.KEY_COMMANDS;
import static org.glassfish.admin.rest.provider.ProviderUtil.KEY_ENTITY;
import static org.glassfish.admin.rest.provider.ProviderUtil.KEY_METHODS;
import static org.glassfish.admin.rest.provider.ProviderUtil.getEndXmlElement;
import static org.glassfish.admin.rest.provider.ProviderUtil.getStartXmlElement;
import static org.glassfish.admin.rest.provider.ProviderUtil.getXmlForMethodMetaData;

/**
 *
 * @author Rajeshwar Patil
 * @author Ludovic Champenois ludo@dev.java.net
 */
@Provider
@Produces(MediaType.APPLICATION_XML)
public class GetResultListXmlProvider extends BaseProvider<GetResultList> {

    public GetResultListXmlProvider() {
        super(GetResultList.class, MediaType.APPLICATION_XML_TYPE);
    }

    @Override
    public String getContent(GetResultList proxy) {
        StringBuilder result = new StringBuilder();
        String indent = Constants.INDENT;

        result.append(getStartXmlElement(KEY_ENTITY)).append("\n\n").append(indent).append(getStartXmlElement(KEY_METHODS))
                .append(getXmlForMethodMetaData(proxy.getMetaData(), indent + Constants.INDENT)).append("\n").append(indent)
                .append(getEndXmlElement(KEY_METHODS));

        //do not display empty child resources array
        if (proxy.getDomList().size() > 0) {
            result.append("\n\n").append(indent).append(getStartXmlElement(KEY_CHILD_RESOURCES))
                    .append(getResourcesLinks(proxy.getDomList(), indent + Constants.INDENT)).append("\n").append(indent)
                    .append(getEndXmlElement(KEY_CHILD_RESOURCES));
        }
        if (proxy.getCommandResourcesPaths().length > 0) {
            result.append("\n\n").append(indent).append(getStartXmlElement(KEY_COMMANDS))
                    .append(getXmlCommandLinks(proxy.getCommandResourcesPaths(), indent + Constants.INDENT)).append("\n").append(indent)
                    .append(getEndXmlElement(KEY_COMMANDS));
        }

        result.append("\n\n").append(getEndXmlElement(KEY_ENTITY));
        return result.toString();
    }

    protected String getXmlResourcesLinks(List<Dom> proxyList, String[][] commandResourcesPaths, String indent) {
        return null;
    }

    private String getResourcesLinks(List<Dom> proxyList, String indent) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> link : getResourceLinks(proxyList).entrySet()) {
            try {
                result.append("\n").append(indent).append(getStartXmlElement(KEY_CHILD_RESOURCE)).append(link.getValue())
                        .append(getEndXmlElement(KEY_CHILD_RESOURCE));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return result.toString();
    }
}
