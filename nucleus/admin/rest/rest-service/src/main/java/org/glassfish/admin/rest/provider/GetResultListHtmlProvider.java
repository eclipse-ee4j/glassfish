/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.glassfish.admin.rest.results.GetResultList;
import org.glassfish.admin.rest.utils.DomConfigurator;
import org.jvnet.hk2.config.Dom;

import static org.glassfish.admin.rest.provider.ProviderUtil.getElementLink;
import static org.glassfish.admin.rest.provider.ProviderUtil.getHtmlForComponent;
import static org.glassfish.admin.rest.provider.ProviderUtil.getHtmlHeader;
import static org.glassfish.admin.rest.provider.ProviderUtil.getHtmlRespresentationsForCommand;
import static org.glassfish.admin.rest.utils.Util.decode;
import static org.glassfish.admin.rest.utils.Util.getName;
import static org.glassfish.admin.rest.utils.Util.upperCaseFirstLetter;

/**
 * @author Rajeshwar Patil
 * @author Ludovic Champenois ludo@dev.java.net
 */
@Provider
@Produces(MediaType.TEXT_HTML)
public class GetResultListHtmlProvider extends BaseProvider<GetResultList> {

    public GetResultListHtmlProvider() {
        super(GetResultList.class, MediaType.TEXT_HTML_TYPE);
    }

    @Override
    public String getContent(GetResultList proxy) {
        String result = getHtmlHeader(uriInfo.getBaseUri().toASCIIString());
        final String typeKey = upperCaseFirstLetter((decode(getName(uriInfo.getPath(), '/'))));
        result = result + "<h1>" + typeKey + "</h1>";

        String postCommand = getHtmlRespresentationsForCommand(proxy.getMetaData().getMethodMetaData("POST"), "POST",
            "Create", uriInfo);
        result = getHtmlForComponent(postCommand, "Create " + typeKey, result);

        String childResourceLinks = getResourcesLinks(proxy.getDomList());
        result = getHtmlForComponent(childResourceLinks, "Child Resources", result);

        String commandLinks = getCommandLinks(proxy.getCommandResourcesPaths());
        result = getHtmlForComponent(commandLinks, "Commands", result);

        result = result + "</html></body>";
        return result;
    }

    private String getResourcesLinks(List<Dom> proxyList) {
        StringBuilder result = new StringBuilder("<div>");
        Collections.sort(proxyList, new DomConfigurator());
        for (Map.Entry<String, String> link : getResourceLinks(proxyList).entrySet()) {
            result.append("<a href=\"").append(link.getValue()).append("\">").append(link.getKey()).append("</a><br>");
        }

        result.append("</div><br/>");
        return result.toString();
    }

    private String getCommandLinks(String[][] commandResourcesPaths) {
        StringBuilder result = new StringBuilder("<div>");
        for (String[] commandResourcePath : commandResourcesPaths) {
            try {
                result.append("<a href=\"").append(getElementLink(uriInfo, commandResourcePath[0])).append("\">")
                        .append(commandResourcePath[0]).append("</a><br/>");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        result.append("</div><br/>");
        return result.toString();
    }
}
