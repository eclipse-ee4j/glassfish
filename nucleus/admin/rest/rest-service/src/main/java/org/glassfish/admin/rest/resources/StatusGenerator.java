/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.admin.rest.resources;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.io.FileUtils;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;

import org.glassfish.admin.rest.RestLogging;
import org.glassfish.admin.rest.generator.ClassWriter;
import org.glassfish.admin.rest.generator.CommandResourceMetaData;
import org.glassfish.admin.rest.generator.CommandResourceMetaData.ParameterMetaData;
import org.glassfish.admin.rest.generator.ResourcesGenerator;
import org.glassfish.admin.rest.generator.ResourcesGeneratorBase;
import org.glassfish.admin.rest.utils.Util;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandModel.ParamModel;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;

/**
 * Generates a report containing lists of admin commands, and also a property file in user's home
 * directory named {@value #FILE}
 *
 * @author Ludovic Champenois ludo@java.net
 */
@Path("/status/")
public class StatusGenerator extends AbstractResource {

    // FIXME: Wouldn't be better to send it to the client?
    private static final File FILE = new File(FileUtils.USER_HOME, "GlassFishI18NData.properties");

    private final Set<String> commandsUsed = new TreeSet<>();
    private final Set<String> allCommands = new TreeSet<>();
    private final Set<String> restRedirectCommands = new TreeSet<>();
    private final Map<String, String> commandsToResources = new TreeMap<>();
    private final Map<String, String> resourcesToDeleteCommands = new TreeMap<>();
    private final Properties propsI18N = new SortedProperties();
    private final ServiceLocator globalLocator = Globals.getDefaultHabitat();

    static private class SortedProperties extends Properties {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Enumeration keys() {
            Enumeration<?> keysEnum = super.keys();
            Vector<String> keyList = new Vector<>();
            while (keysEnum.hasMoreElements()) {
                keyList.add((String) keysEnum.nextElement());
            }
            Collections.sort(keyList);
            return keyList.elements();
        }
    }

    @GET
    @Produces({ "text/plain" })
    public String getPlain() {
        Domain domain = locate(Domain.class);
        if (domain != null) {
            Dom dom = Dom.unwrap(domain);
            DomDocument<?> document = dom.document;
            ConfigModel rootModel = dom.document.getRoot().model;

            ResourcesGenerator resourcesGenerator = new NOOPResourcesGenerator(serviceLocator);
            resourcesGenerator.generateSingle(rootModel, document);
            resourcesGenerator.endGeneration();
        }

        final StringBuilder status = new StringBuilder();
        status.append("\n------------------------");
        status.append("All Commands used in REST Admin:\n");
        for (String command : commandsUsed) {
            status.append(command).append('\n');
        }

        detectFromCommandRunner(allCommands);
        for (String command : commandsUsed) {
            allCommands.remove(command);
        }

        status.append("\n------------------------");
        status.append("Missing Commands not used in REST Admin:\n");

        for (String command : allCommands) {
            if (hasTargetParam(command)) {
                status.append(command).append("          has a target param ").append('\n');
            } else {
                status.append(command).append('\n');
            }
        }
        status.append("\n------------------------");
        status.append("REST-REDIRECT Commands defined on ConfigBeans:\n");

        for (String ss : restRedirectCommands) {
            status.append(ss).append('\n');
        }

        status.append("\n------------------------");
        status.append("Commands to Resources Mapping Usage in REST Admin:\n");

        Iterator<Map.Entry<String, String>> entryIterator = commandsToResources.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> entry = entryIterator.next();
            if (hasTargetParam(entry.getKey())) {
                status.append(entry.getKey()).append("   :::target:::   ").append(entry.getValue()).append('\n');
            } else {
                status.append(entry.getKey()).append("      :::      ").append(entry.getValue()).append('\n');
            }
        }
        status.append("\n------------------------");
        status.append("Resources with Delete Commands in REST Admin (not counting RESTREDIRECT:\n");

        entryIterator = resourcesToDeleteCommands.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> entry = entryIterator.next();
            status.append(entry.getKey()).append("      :::      ").append(entry.getValue()).append('\n');
        }

        RestLogging.restLogger.log(Level.INFO, "Storing properties to the file {0}", FILE);
        try (FileOutputStream f = new FileOutputStream(FILE)) {
            propsI18N.store(f, "");
        } catch (Exception ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }
        return status.toString();
    }

    @GET
    @Produces({ MediaType.TEXT_HTML })
    public String getHtml() {
        Domain entity = locate(Domain.class);
        if (entity != null) {
            Dom dom = Dom.unwrap(entity);
            DomDocument<?> document = dom.document;
            ConfigModel rootModel = dom.document.getRoot().model;

            ResourcesGenerator resourcesGenerator = new NOOPResourcesGenerator(serviceLocator);
            resourcesGenerator.generateSingle(rootModel, document);
            resourcesGenerator.endGeneration();
        }

        final StringBuilder status = new StringBuilder();
        status.append("<h4>All Commands used in REST Admin</h4>\n<ul>\n");
        for (String ss : commandsUsed) {
            status.append("<li>").append(ss).append("</li>\n");
        }

        detectFromCommandRunner(allCommands);
        for (String ss : commandsUsed) {
            allCommands.remove(ss);
        }

        status.append("</ul>\n<hr/>\n").append("<h4>Missing Commands not used in REST Admin</h4>\n<ul>\n");

        for (String ss : allCommands) {
            if (hasTargetParam(ss)) {
                status.append("<li>").append(ss).append("          has a target param.</li>\n");
            } else {
                status.append("<li>").append(ss).append("</li>\n");
            }
        }

        status.append("</ul>\n<hr/>\n").append("<h4>REST-REDIRECT Commands defined on ConfigBeans</h4>\n<ul>\n");

        for (String command : restRedirectCommands) {
            status.append("<li>").append(command).append("</li>\n");
        }

        status.append("</ul>\n<hr/>\n").append("<h4>Commands to Resources Mapping Usage in REST Admin</h4>\n")
                .append("<table border=\"1\" style=\"border-collapse: collapse\">\n")
                .append("<tr><th>Command</th><th>Target</th><th>Resource</th></tr>\n");

        Iterator<Map.Entry<String, String>> entryIterator = commandsToResources.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> entry = entryIterator.next();
            status.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(hasTargetParam(entry.getKey()) ? "target" : "")
                    .append("</td><td>").append(entry.getValue()).append("</td></tr>\n");
        }
        status.append("</table>\n<hr/>\n").append("<h4>Resources with Delete Commands in REST Admin (not counting RESTREDIRECT)</h4>\n")
                .append("<table border=\"1\" style=\"border-collapse: collapse\">\n")
                .append("<tr><th>Resource</th><th>Delete Command</th></tr>\n");
        entryIterator = resourcesToDeleteCommands.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> entry = entryIterator.next();
            status.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>\n");
        }
        status.append("</table>");

        RestLogging.restLogger.log(Level.INFO, "Storing properties to the file {0}", FILE);
        try (FileOutputStream f = new FileOutputStream(FILE)) {
            propsI18N.store(f, "");
        } catch (Exception ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }
        return status.toString();
    }

    private void detectFromCommandRunner(Set<String> commands) {
        CommandRunner cr = locate(CommandRunner.class);
        RestActionReporter ar = new RestActionReporter();
        ParameterMap parameters = new ParameterMap();
        cr.getCommandInvocation("list-commands", ar, getSubject()).parameters(parameters).execute();
        List<ActionReport.MessagePart> children = ar.getTopMessagePart().getChildren();
        if (children != null) {
            for (ActionReport.MessagePart part : children) {
                commands.add(part.getMessage());
            }
        }
        ar = new RestActionReporter();
        parameters = new ParameterMap();
        parameters.add("DEFAULT", "_");
        cr.getCommandInvocation("list-commands", ar, getSubject()).parameters(parameters).execute();
        children = ar.getTopMessagePart().getChildren();
        if (children != null) {
            for (ActionReport.MessagePart part : children) {
                commands.add(part.getMessage());
            }
        }
    }

    private <T> T locate(Class<T> clazz) {
        return globalLocator.getService(clazz);
    }

    private Boolean hasTargetParam(String command) {
        try {
            if (command != null) {
                Collection<CommandModel.ParamModel> params;
                params = getParamMetaData(command);

                CommandModel.ParamModel paramModel;
                for (ParamModel param : params) {
                    paramModel = param;
                    //   Param param = paramModel.getParam();
                    if (paramModel.getName().equals("target")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            RestLogging.restLogger.log(Level.FINE, e.getMessage(), e);
        }

        return false;
    }

    private Collection<CommandModel.ParamModel> getParamMetaData(String commandName) {
        CommandRunner cr = locate(CommandRunner.class);
        CommandModel cm = cr.getModel(commandName, RestLogging.restLogger);
        Collection<CommandModel.ParamModel> params = cm.getParameters();
        return params;
    }


    class NOOPClassWriter implements ClassWriter {

        private final String className;

        public NOOPClassWriter(String className, String baseClassName, String resourcePath) {
            this.className = className;
            if (baseClassName.equals("TemplateRestResource")) {
                resourcesToDeleteCommands.put(className, ""); //init the map with empty values
            }
        }

        @Override
        public void createGetCommandResourcePaths(List<CommandResourceMetaData> commandMetaData) {
            for (CommandResourceMetaData metaData : commandMetaData) {
                commandsUsed.add(metaData.command);
                if (commandsToResources.containsKey(metaData.command)) {
                    String val = commandsToResources.get(metaData.command) + ", " + className;
                    commandsToResources.put(metaData.command, val);
                } else {
                    commandsToResources.put(metaData.command, className);
                }
            }
        }

        @Override
        public void createGetCommandResource(String commandResourceClassName, String resourcePath) {
        }

        @Override
        public void createCommandResourceConstructor(String commandResourceClassName, String commandName, String httpMethod,
                boolean linkedToParent, ParameterMetaData[] commandParams, String commandDisplayName, String commandAction) {
        }

        @Override
        public void createCustomResourceMapping(String resourceClassName, String mappingPath) {
        }

        @Override
        public void done() {
        }

        @Override
        public void createGetDeleteCommand(String commandName) {
            commandsUsed.add(commandName);
            if (commandsToResources.containsKey(commandName)) {
                String val = commandsToResources.get(commandName) + ", " + className;
                commandsToResources.put(commandName, val);

            } else {
                commandsToResources.put(commandName, className);
            }
            resourcesToDeleteCommands.put(className, commandName);
        }

        @Override
        public void createGetPostCommand(String commandName) {
            commandsUsed.add(commandName);
            if (commandsToResources.containsKey(commandName)) {
                String val = commandsToResources.get(commandName) + ", " + className;
                commandsToResources.put(commandName, val);

            } else {
                commandsToResources.put(commandName, className);
            }

        }

        @Override
        public void createGetChildResource(String path, String childResourceClassName) {
        }

        @Override
        public void createGetChildResourceForListResources(String keyAttributeName, String childResourceClassName) {
        }

        @Override
        public void createGetPostCommandForCollectionLeafResource(String commandName) {
            commandsUsed.add(commandName);
            if (commandsToResources.containsKey(commandName)) {
                String val = commandsToResources.get(commandName) + ", " + className;
                commandsToResources.put(commandName, val);
            } else {
                commandsToResources.put(commandName, className);
            }
        }

        @Override
        public void createGetDeleteCommandForCollectionLeafResource(String commandName) {
            commandsUsed.add(commandName);
            if (commandsToResources.containsKey(commandName)) {
                String val = commandsToResources.get(commandName) + ", " + className;
                commandsToResources.put(commandName, val);

            } else {
                commandsToResources.put(commandName, className);
            }
        }

        @Override
        public void createGetDisplayNameForCollectionLeafResource(String displayName) {
        }
    }

    class NOOPResourcesGenerator extends ResourcesGeneratorBase {

        public NOOPResourcesGenerator(ServiceLocator h) {
            super(h);

        }

        @Override
        public ClassWriter getClassWriter(String className, String baseClassName, String resourcePath) {
            return new NOOPClassWriter(className, baseClassName, resourcePath);
        }

        @Override
        public String endGeneration() {
            return "done";
        }

        @Override
        public void configModelVisited(ConfigModel model) {
            //I18n Calculation
            for (String attrName : model.getAttributeNames()) {
                String key = model.targetTypeName + "." + Util.eleminateHypen(attrName);
                propsI18N.setProperty(key + ".label", attrName);
                propsI18N.setProperty(key + ".help", attrName);
            }

            Class<? extends ConfigBeanProxy> cbp = null;
            try {
                cbp = (Class<? extends ConfigBeanProxy>) model.classLoaderHolder.loadClass(model.targetTypeName);
            } catch (MultiException e) {
                RestLogging.restLogger.log(Level.WARNING, "Cannot load " + model.targetTypeName, e);
                return;
            }
            RestRedirects restRedirects = cbp.getAnnotation(RestRedirects.class);
            if (restRedirects != null) {

                RestRedirect[] values = restRedirects.value();
                for (RestRedirect r : values) {
                    commandsUsed.add(r.commandName());
                    restRedirectCommands.add(r.commandName());

                }
            }
        }
    }
}
