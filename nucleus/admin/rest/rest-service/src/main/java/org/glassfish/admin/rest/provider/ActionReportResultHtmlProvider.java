/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.v3.common.ActionReporter;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.utils.ResourceUtil;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.jvnet.hk2.config.ConfigBean;

import static org.glassfish.admin.rest.provider.ProviderUtil.getHint;
import static org.glassfish.admin.rest.provider.ProviderUtil.getHtmlForComponent;
import static org.glassfish.admin.rest.provider.ProviderUtil.getHtmlRespresentationsForCommand;

/**
 * @author Ludovic Champenois
 */
@Provider
@Produces(MediaType.TEXT_HTML)
public class ActionReportResultHtmlProvider extends BaseProvider<ActionReportResult> {
    public ActionReportResultHtmlProvider() {
        super(ActionReportResult.class, MediaType.TEXT_HTML_TYPE);
    }

    @Override
    public String getContent(ActionReportResult proxy) {
        RestActionReporter ar = (RestActionReporter) proxy.getActionReport();
        StringBuilder result = new StringBuilder(ProviderUtil.getHtmlHeader(getBaseUri()));
        final String message = ResourceUtil.encodeString(ar.getCombinedMessage());

        if (!message.isEmpty()) {
            result.append("<h3>").append(message).append("</h3>");
        }

        if (proxy.isError()) {
            result.append("<h2>").append(ar.getActionDescription()).append(" Error:</h2>").append(proxy.getErrorMessage());
        } else {
            final Map<String, String> childResources = (Map<String, String>) ar.getExtraProperties().get("childResources");
            final List<Map<String, String>> commands = (List<Map<String, String>>) ar.getExtraProperties().get("commands");
            final MethodMetaData postMetaData = proxy.getMetaData().getMethodMetaData("POST");
            final MethodMetaData deleteMetaData = proxy.getMetaData().getMethodMetaData("DELETE");
            final MethodMetaData getMetaData = proxy.getMetaData().getMethodMetaData("GET");
            final ConfigBean entity = proxy.getEntity();

            if ((proxy.getCommandDisplayName() != null) && (getMetaData != null)) {//for commands, we want the output of the command before the form
                if (entity == null) {//show extra properties only for non entity pages
                    result.append(processReport(ar));
                }
            }

            if ((postMetaData != null) && (entity == null)) {
                String postCommand = getHtmlRespresentationsForCommand(postMetaData, "POST",
                        (proxy.getCommandDisplayName() == null) ? "Create" : proxy.getCommandDisplayName(), uriInfo);
                result.append(getHtmlForComponent(postCommand, "Create " + ar.getActionDescription(), ""));
            }
            if ((deleteMetaData != null) && (entity == null)) {
                String deleteCommand = getHtmlRespresentationsForCommand(deleteMetaData, "DELETE",
                        (proxy.getCommandDisplayName() == null) ? "Delete" : proxy.getCommandDisplayName(), uriInfo);
                result.append(getHtmlForComponent(deleteCommand, "Delete " + ar.getActionDescription(), ""));
            }
            if ((getMetaData != null) && (entity == null) && (proxy.getCommandDisplayName() != null)) {
                String getCommand = getHtmlRespresentationsForCommand(getMetaData, "GET",
                        (proxy.getCommandDisplayName() == null) ? "Get" : proxy.getCommandDisplayName(), uriInfo);
                result.append(getHtmlForComponent(getCommand, "Get " + ar.getActionDescription(), ""));
            }
            if (entity != null) {
                String attributes = ProviderUtil.getHtmlRepresentationForAttributes(proxy.getEntity(), uriInfo);
                result.append(ProviderUtil.getHtmlForComponent(attributes, ar.getActionDescription() + " Attributes", ""));

                String deleteCommand = ProviderUtil.getHtmlRespresentationsForCommand(proxy.getMetaData().getMethodMetaData("DELETE"),
                        "DELETE", (proxy.getCommandDisplayName() == null) ? "Delete" : proxy.getCommandDisplayName(), uriInfo);
                result.append(ProviderUtil.getHtmlForComponent(deleteCommand, "Delete " + entity.model.getTagName(), ""));

            } else if (proxy.getLeafContent() != null) { //it is a single leaf @Element
                String content = "<form action=\"" + uriInfo.getAbsolutePath().toString() + "\" method=\"post\">" + "<dl><dt>"
                        + "<label for=\"" + proxy.getLeafContent().name + "\">" + proxy.getLeafContent().name + ":&nbsp;</label>"
                        + "</dt><dd>" + "<input name=\"" + proxy.getLeafContent().name + "\" value =\"" + proxy.getLeafContent().value
                        + "\" type=\"text\" >"
                        + "</dd><dt class=\"button\"></dt><dd class=\"button\"><input value=\"Update\" type=\"submit\"></dd></dl>"
                        + "</form><br><hr class=\"separator\"/";
                result.append(content);

            } else { //This is a monitoring result!!!

                final Map vals = (Map) ar.getExtraProperties().get("entity");

                if ((vals != null) && (!vals.isEmpty())) {
                    result.append("<ul>");

                    for (Map.Entry entry : (Set<Map.Entry>) vals.entrySet()) {

                        Object object = entry.getValue();
                        if (object == null) {
                            //do nothing
                        } else if (object instanceof Collection) {
                            if (!((Collection) object).isEmpty()) {
                                Collection c = ((Collection) object);
                                Iterator i = c.iterator();
                                result.append("<li>").append(entry.getKey());
                                result.append("<ul>");

                                while (i.hasNext()) {
                                    result.append("<li>").append(getHtmlRepresentation(i.next())).append("</li>");
                                }
                                result.append("</ul>");
                                result.append("</li>");

                            }
                        } else if (object instanceof Map) {
                            if (!((Map) object).isEmpty()) {
                                Map m = (Map) object;
                                if (vals.size() != 1) {//add a link if more than 1 child
                                    result.append("<li>").append("<a href=\"" + uriInfo.getAbsolutePath().toString() + "/"
                                            + entry.getKey() + "\">" + entry.getKey() + "</a>");
                                } else {
                                    result.append("<li>").append(entry.getKey());
                                }
                                result.append("<ul>");

                                for (Map.Entry anEntry : (Set<Map.Entry>) m.entrySet()) {
                                    final String htmlRepresentation = getHtmlRepresentation(anEntry.getValue());
                                    if (htmlRepresentation != null) {
                                        result.append("<li>").append(anEntry.getKey()).append(" : ").append(htmlRepresentation)
                                                .append("</li>");
                                    }
                                }
                                result.append("</ul>");
                                result.append("</li>");

                            }
                        } else {
                            result.append("<li>").append(entry.getKey()).append(" : ").append(object.toString()).append("</li>");
                        }
                    }
                    result.append("</ul>");

                } else {//no values to show... give an hint
                    if ((childResources == null) || (childResources.isEmpty())) {
                        if ((uriInfo != null) && (uriInfo.getPath().equalsIgnoreCase("domain"))) {
                            result.append(getHint(uriInfo, MediaType.TEXT_HTML));
                        }
                    }

                }
            }

            if ((childResources != null) && (!childResources.isEmpty())) {
                String childResourceLinks = getResourcesLinks(childResources);
                result.append(ProviderUtil.getHtmlForComponent(childResourceLinks, "Child Resources", ""));
            }

            if ((commands != null) && (!commands.isEmpty())) {
                String commandLinks = getCommandLinks(commands);
                result.append(ProviderUtil.getHtmlForComponent(commandLinks, "Commands", ""));
            }

        }
        return result.append("</div></body></html>").toString();
    }

    protected String getBaseUri() {
        if (uriInfo != null) {
            return uriInfo.getBaseUri().toASCIIString();
        }
        return "";
    }

    protected String getResourcesLinks(Map<String, String> childResources) {
        StringBuilder links = new StringBuilder("<div>");
        for (Map.Entry<String, String> link : childResources.entrySet()) {
            links.append("<a href=\"").append(link.getValue()).append("\">").append(link.getKey()).append("</a><br>");

        }

        return links.append("</div><br/>").toString();
    }

    protected String getCommandLinks(List<Map<String, String>> commands) {
        StringBuilder result = new StringBuilder("<div>");
        boolean showHiddenCommands = canShowHiddenCommands();
        for (Map<String, String> commandList : commands) {
            String command = commandList.get("command");
            String path = commandList.get("path");
            if (path.startsWith("_") && (showHiddenCommands == false)) {//hidden cli command name
                result.append("<!--");//hide the link in a comment
            }
            result.append("<a href=\"").append(ProviderUtil.getElementLink(uriInfo, command)).append("\">").append(command)
                    .append("</a><br>");
            if (path.startsWith("_") && (showHiddenCommands == false)) {//hidden cli
                result.append("-->");
            }
        }

        return result.append("</div><br>").toString();
    }

    protected String processReport(ActionReporter ar) {

        StringBuilder result = new StringBuilder();
        String des = ar.getActionDescription();
        //check for no description, make it blank
        if (des == null) {
            des = "";
        }
        final String message = ResourceUtil
                .encodeString((ar instanceof RestActionReporter) ? ((RestActionReporter) ar).getCombinedMessage() : ar.getMessage());
        result.append("<h2>").append(des).append(" output:</h2><h3>").append("<pre>").append(message).append("</pre>").append("</h3>");
        if (ar.getActionExitCode() != ExitCode.SUCCESS) {
            result.append("<h3>Exit Code: ").append(ar.getActionExitCode().toString()).append("</h3>");

        }

        Properties properties = ar.getTopMessagePart().getProps();
        if (!properties.isEmpty()) {
            result.append(processProperties(properties));
        }

        Properties extraProperties = ar.getExtraProperties();
        if ((extraProperties != null) && (!extraProperties.isEmpty())) {
            if ((extraProperties.size() == 1) && (extraProperties.get("methods") != null)) {
                //do not show only methods metadata in html, not really needed
            } else {
                result.append(getExtraProperties(extraProperties));
            }
        }

        List<ActionReport.MessagePart> children = ar.getTopMessagePart().getChildren();
        if (children.size() > 0) {
            result.append(processChildren(children));
        }

        List<ActionReporter> subReports = ar.getSubActionsReport();
        if (subReports.size() > 0) {
            result.append(processSubReports(subReports));
        }

        return result.toString();
    }

    protected String processProperties(Properties props) {
        StringBuilder result = new StringBuilder("<h3>Properties</h3>");
        result.append(getHtml(props));

        return result.append("</table>").toString();
    }

    protected String getExtraProperties(Properties props) {
        StringBuilder result = new StringBuilder("<h3>Extra Properties</h3>");
        result.append(getHtml(props));

        return result.toString();
    }

    protected String processChildren(List<ActionReport.MessagePart> parts) {
        StringBuilder result = new StringBuilder("<h3>Children</h3><ul>");

        for (ActionReport.MessagePart part : parts) {
            result.append("<li><table border=\"1\" style=\"border-collapse: collapse\">").append("<tr><td>Message</td>").append("<td>")
                    .append(part.getMessage()).append("</td></tr><td>Properties</td><td>").append(getHtml(part.getProps()))
                    .append("</td></tr>");
            List<ActionReport.MessagePart> children = part.getChildren();
            if (children.size() > 0) {
                result.append("<tr><td>Children</td><td>").append(processChildren(part.getChildren())).append("</td></tr>");
            }
            result.append("</table></li>");
        }

        return result.append("</ul>").toString();
    }

    protected String processSubReports(List<ActionReporter> subReports) {
        StringBuilder result = new StringBuilder("<h3>Sub Reports</h3><ul>");

        for (ActionReporter subReport : subReports) {
            result.append(processReport(subReport));
        }

        return result.append("</ul>").toString();
    }

    protected String getHtmlRepresentation(Object object) {
        String result = null;
        if (object == null) {
            return "";
        } else if (object instanceof Collection) {
            if (!((Collection) object).isEmpty()) {
                result = getHtml((Collection) object);
            }
        } else if (object instanceof Map) {
            if (!((Map) object).isEmpty()) {
                result = getHtml((Map) object);
            }
        } else {
            result = object.toString();
        }

        return result;
    }

    protected String getHtml(Collection c) {
        StringBuilder result = new StringBuilder("<ul>");
        Iterator i = c.iterator();
        while (i.hasNext()) {
            result.append("<li>").append(getHtmlRepresentation(i.next())).append("</li>");
        }

        return result.append("</li></ul>").toString();
    }

    protected String getHtml(Map map) {
        StringBuilder result = new StringBuilder();
        if (!map.isEmpty()) {
            result.append("<table border=\"1\" style=\"border-collapse: collapse\"><tr><th>key</th><th>value</th></tr>");

            for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
                final String htmlRepresentation = getHtmlRepresentation(entry.getValue());
                if (htmlRepresentation != null) {
                    result.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(htmlRepresentation).append("</td></tr>");
                }
            }

            result.append("</table>");
        }

        return result.toString();
    }
}
