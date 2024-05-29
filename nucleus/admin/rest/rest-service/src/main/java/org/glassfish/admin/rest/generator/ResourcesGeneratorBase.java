/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.admin.rest.generator;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.utils.ResourceUtil;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.DomDocument;

import static java.util.Map.entry;

/**
 * @author Mitesh Meswani
 * @author Ludovic Champenois
 */
public abstract class ResourcesGeneratorBase implements ResourcesGenerator {

    private static final Set<String> alreadyGenerated = new HashSet<>();
    ServiceLocator habitat;

    public ResourcesGeneratorBase(ServiceLocator habitat) {
        this.habitat = habitat;
    }

    /**
     * Generate REST resource for a single config model.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void generateSingle(ConfigModel model, DomDocument domDocument) {
        configModelVisited(model);
        //processRedirectsAnnotation(model); // TODO need to extract info from RestRedirect Annotations

        String serverConfigName = ResourceUtil.getUnqualifiedTypeName(model.targetTypeName);
        String beanName = getBeanName(serverConfigName);
        String className = getClassName(beanName);

        if (alreadyGenerated(className)) {
            return;
        }

        String baseClassName = "TemplateRestResource";
        String resourcePath = null;

        if (beanName.equals("Domain")) {
            baseClassName = "org.glassfish.admin.rest.resources.GlassFishDomainResource";
            resourcePath = "domain";
        }

        ClassWriter classWriter = getClassWriter(className, baseClassName, resourcePath);

        if (classWriter != null) {
            generateCommandResources(beanName, classWriter);

            generateGetDeleteCommandMethod(beanName, classWriter);

            generateCustomResourceMapping(beanName, classWriter);

            for (String elementName : model.getElementNames()) {
                ConfigModel.Property childElement = model.getElement(elementName);
                if (elementName.equals("*")) {
                    ConfigModel.Node node = (ConfigModel.Node) childElement;
                    ConfigModel childModel = node.getModel();
                    List<ConfigModel> subChildConfigModels = ResourceUtil.getRealChildConfigModels(childModel, domDocument);
                    for (ConfigModel subChildConfigModel : subChildConfigModels) {
                        if (ResourceUtil.isOnlyATag(childModel) || ResourceUtil.isOnlyATag(subChildConfigModel)
                                || subChildConfigModel.getAttributeNames().isEmpty() || hasSingletonAnnotation(subChildConfigModel)) {
                            String childResourceClassName = getClassName(
                                    ResourceUtil.getUnqualifiedTypeName(subChildConfigModel.targetTypeName));
                            String childPath = subChildConfigModel.getTagName();
                            classWriter.createGetChildResource(childPath, childResourceClassName);
                            generateSingle(subChildConfigModel, domDocument);
                        } else {
                            processNonLeafChildConfigModel(subChildConfigModel, childElement, domDocument, classWriter);

                        }
                    }
                } else if (childElement.isLeaf()) {
                    if (childElement.isCollection()) {
                        // handle the CollectionLeaf config objects.
                        // JVM Options is an example of CollectionLeaf object.
                        String childResourceBeanName = getBeanName(elementName);
                        String childResourceClassName = getClassName(childResourceBeanName);
                        classWriter.createGetChildResource(elementName, childResourceClassName);

                        // create resource class
                        generateCollectionLeafResource(childResourceBeanName);
                    } else {
                        String childResourceBeanName = getBeanName(elementName);
                        String childResourceClassName = getClassName(childResourceBeanName);
                        classWriter.createGetChildResource(elementName, childResourceClassName);

                        // create resource class
                        generateLeafResource(childResourceBeanName);
                    }
                } else { // => !childElement.isLeaf()
                    processNonLeafChildElement(elementName, childElement, domDocument, classWriter);
                }
            }

            classWriter.done();
        }
    }

    public void generateList(ConfigModel model, DomDocument domDocument) {
        configModelVisited(model);

        String serverConfigName = ResourceUtil.getUnqualifiedTypeName(model.targetTypeName);
        String beanName = getBeanName(serverConfigName);
        String className = "List" + getClassName(beanName);

        if (alreadyGenerated(className)) {
            return;
        }

        ClassWriter classWriter = getClassWriter(className, "TemplateListOfResource", null);

        if (classWriter != null) {
            String keyAttributeName = getKeyAttributeName(model);
            String childResourceClassName = getClassName(beanName);
            classWriter.createGetChildResourceForListResources(keyAttributeName, childResourceClassName);
            generateCommandResources("List" + beanName, classWriter);

            generateGetPostCommandMethod("List" + beanName, classWriter);

            classWriter.done();

            generateSingle(model, domDocument);
        }
    }

    /*
     * Empty method to be overwritten to get a callback when a model is visited.
     */
    public void configModelVisited(ConfigModel model) {
    }

    private void generateCollectionLeafResource(String beanName) {
        String className = getClassName(beanName);

        if (alreadyGenerated(className)) {
            return;
        }

        ClassWriter classWriter = getClassWriter(className, "CollectionLeafResource", null);

        if (classWriter != null) {
            CollectionLeafMetaData metaData = configBeanToCollectionLeafMetaData.get(beanName);

            if (metaData != null) {
                if (metaData.postCommandName != null) {
                    if (ResourceUtil.commandIsPresent(habitat, metaData.postCommandName)) { // and the command exits
                        classWriter.createGetPostCommandForCollectionLeafResource(metaData.postCommandName);
                    }
                }

                if (metaData.deleteCommandName != null) {
                    if (ResourceUtil.commandIsPresent(habitat, metaData.deleteCommandName)) { // and the command exits
                        classWriter.createGetDeleteCommandForCollectionLeafResource(metaData.deleteCommandName);
                    }
                }

                // display name method
                classWriter.createGetDisplayNameForCollectionLeafResource(metaData.displayName);
            }

            classWriter.done();
        }
    }

    private void generateLeafResource(String beanName) {
        String className = getClassName(beanName);

        if (alreadyGenerated(className)) {
            return;
        }

        ClassWriter classWriter = getClassWriter(className, "LeafResource", null);

        if (classWriter != null) {
            classWriter.done();
        }
    }

    @SuppressWarnings("rawtypes")
    private void processNonLeafChildElement(String elementName, ConfigModel.Property childElement,
            DomDocument domDocument, ClassWriter classWriter) {
        ConfigModel.Node node = (ConfigModel.Node) childElement;
        ConfigModel childModel = node.getModel();
        String beanName = ResourceUtil.getUnqualifiedTypeName(childModel.targetTypeName);

        if (beanName.equals("Property")) {
            classWriter.createGetChildResource("property", "PropertiesBagResource");
        } else {
            String childResourceClassName = getClassName(beanName);
            if (childElement.isCollection()) {
                childResourceClassName = "List" + childResourceClassName;
            }
            classWriter.createGetChildResource(/*childModel.getTagName()*/ elementName, childResourceClassName);
        }

        if (childElement.isCollection()) {
            generateList(childModel, domDocument);
        } else {
            generateSingle(childModel, domDocument);
        }
    }

    /**
     * Process given childConfigModel.
     *
     * @param childConfigModel the child config model
     * @param childElement the child element
     * @param domDocument the DOM document
     * @param classWriter the class writer
     */
    @SuppressWarnings("rawtypes")
    private void processNonLeafChildConfigModel(ConfigModel childConfigModel, ConfigModel.Property childElement,
            DomDocument domDocument, ClassWriter classWriter) {
        String childResourceClassName = getClassName("List" + ResourceUtil.getUnqualifiedTypeName(childConfigModel.targetTypeName));
        String childPath = childConfigModel.getTagName();
        classWriter.createGetChildResource(childPath, childResourceClassName);
        if (childElement.isCollection()) {
            generateList(childConfigModel, domDocument);
        } else {
            // The code flow should never reach here. NonLeaf ChildElements are assumed to be collection typed
            // that is why we generate childResource as generateSingle(childConfigModel, domDocument);
        }
    }

    private void generateGetDeleteCommandMethod(String beanName, ClassWriter classWriter) {
        String commandName = configBeanToDELETECommand.get(beanName);
        if (commandName != null) {
            if (ResourceUtil.commandIsPresent(habitat, commandName)) { // and the command exits
                classWriter.createGetDeleteCommand(commandName);
            }
        }
    }

    private void generateCustomResourceMapping(String beanName, ClassWriter classWriter) {
        for (CommandResourceMetaData cmd : CommandResourceMetaData.getCustomResourceMapping(beanName)) {
            classWriter.createCustomResourceMapping(cmd.customClassName, cmd.resourcePath);
        }
    }

    void generateGetPostCommandMethod(String resourceName, ClassWriter classWriter) {
        String commandName = configBeanToPOSTCommand.get(resourceName);
        if (commandName != null) {
            if (ResourceUtil.commandIsPresent(habitat, commandName)) { // and the command exits
                classWriter.createGetPostCommand(commandName);
            }
        }
    }

    /**
     * Generate resources for commands mapped under given {@code parentBeanName}.
     *
     * @param parentBeanName the parent bean name
     * @param parentWriter - the parent class writer
     */
    private void generateCommandResources(String parentBeanName, ClassWriter parentWriter) {
        List<CommandResourceMetaData> commandMetaData = CommandResourceMetaData.getMetaData(parentBeanName);
        if (commandMetaData.size() > 0) {
            for (CommandResourceMetaData metaData : commandMetaData) {
                if (ResourceUtil.commandIsPresent(habitat, metaData.command)) { // only if the command really exists
                    String commandResourceName = parentBeanName + getBeanName(metaData.resourcePath);
                    String commandResourceClassName = getClassName(commandResourceName);

                    // Generate command resource class
                    generateCommandResourceClass(parentBeanName, metaData);

                    // Generate getCommandResource() method in parent
                    parentWriter.createGetCommandResource(commandResourceClassName, metaData.resourcePath);
                }

            }
            // Generate GetCommandResourcePaths() method in parent
            parentWriter.createGetCommandResourcePaths(commandMetaData);
        }
    }

    /**
     * Generate code for Resource class corresponding to given {@code parentBeanName} and command.
     *
     * @param parentBeanName the parent bean name
     * @param metaData the command metadata
     */
    private void generateCommandResourceClass(String parentBeanName, CommandResourceMetaData metaData) {

        String commandResourceClassName = getClassName(parentBeanName + getBeanName(metaData.resourcePath));

        if (alreadyGenerated(commandResourceClassName)) {
            return;
        }

        String commandName = metaData.command;
        String commandDisplayName = metaData.resourcePath;
        String httpMethod = metaData.httpMethod;
        String commandAction = metaData.displayName;
        String baseClassName;

        if ("GET".equals(httpMethod)) {
            baseClassName = "org.glassfish.admin.rest.resources.TemplateCommandGetResource";
        } else if ("DELETE".equals(httpMethod)) {
            baseClassName = "org.glassfish.admin.rest.resources.TemplateCommandDeleteResource";
        } else if ("POST".equals(httpMethod)) {
            baseClassName = "org.glassfish.admin.rest.resources.TemplateCommandPostResource";
        } else {
            throw new GeneratorException("Invalid httpMethod specified: " + httpMethod);
        }

        ClassWriter classWriter = getClassWriter(commandResourceClassName, baseClassName, null);

        if (classWriter != null) {
            boolean isLinkedToParent = false;
            if (metaData.commandParams != null) {
                for (CommandResourceMetaData.ParameterMetaData parameterMetaData : metaData.commandParams) {
                    if (Constants.VAR_PARENT.equals(parameterMetaData.value)) {
                        isLinkedToParent = true;
                        break;
                    }
                }
            }

            classWriter.createCommandResourceConstructor(commandResourceClassName, commandName, httpMethod,
                    isLinkedToParent, metaData.commandParams, commandDisplayName, commandAction);
            classWriter.done();
        }
    }

    /**
     * @param className the class name
     * @return true if the given {@code className} is already generated, false otherwise.
     */
    protected boolean alreadyGenerated(String className) {
        boolean retVal = true;
        if (!alreadyGenerated.contains(className)) {
            alreadyGenerated.add(className);
            retVal = false;
        }
        return retVal;
    }

    /**
     * @param beanName the bean name
     * @return generated class name for given {@code beanName}
     */
    private String getClassName(String beanName) {
        return beanName + "Resource";
    }

    /**
     * Gets the bean name corresponding given {@code elementName}.
     *
     * <p>The name is derived by uppercasing first letter of {@code elementName},
     * eliminating hyphens from {@code elementName} and uppercasing letter followed by hyphen.
     *
     * @param elementName the element name
     * @return the bean name for the given element name.
     */
    public static String getBeanName(String elementName) {
        StringBuilder ret = new StringBuilder();
        boolean nextIsUpper = true;
        for (int i = 0; i < elementName.length(); i++) {
            if (nextIsUpper) {
                ret.append(elementName.substring(i, i + 1).toUpperCase(Locale.US));
                nextIsUpper = false;
            } else {
                if (elementName.charAt(i) == '-') {
                    nextIsUpper = true;
                } else if (elementName.charAt(i) == '/') {
                    nextIsUpper = true;
                } else {
                    ret.append(elementName.charAt(i));
                }
            }
        }
        return ret.toString();
    }

    /**
     * @param model the config model
     * @return name of the key attribute for the given {@code model}.
     */
    private String getKeyAttributeName(ConfigModel model) {
        String keyAttributeName = null;
        if (model.key == null) {
            for (String s : model.getAttributeNames()) { // no key, by default use the name attr
                if (s.equals("name")) {
                    keyAttributeName = getBeanName(s);
                }
            }
            if (keyAttributeName == null) // nothing, so pick the first one
            {
                Set<String> attributeNames = model.getAttributeNames();
                if (!attributeNames.isEmpty()) {
                    keyAttributeName = getBeanName(attributeNames.iterator().next());
                } else {
                    // TODO carried forward from old generator. Should never reach here. But we do for ConfigExtension and WebModuleConfig
                    keyAttributeName = "ThisIsAModelBug:NoKeyAttr"; // no attr choice fo a key!!! Error!!!
                }

            }
        } else {
            final int keyLength = model.key.length();
            String key = model.key.substring(1, model.key.endsWith(">") ? keyLength - 1 : keyLength);
            keyAttributeName = getBeanName(key);
        }
        return keyAttributeName;
    }

    @SuppressWarnings("unchecked")
    private boolean hasSingletonAnnotation(ConfigModel model) {
        Class<? extends ConfigBeanProxy> cbp;
        try {
            cbp = (Class<? extends ConfigBeanProxy>) model.classLoaderHolder.loadClass(model.targetTypeName);
            if (cbp != null) {
                org.glassfish.config.support.Singleton sing = cbp.getAnnotation(org.glassfish.config.support.Singleton.class);
                return (sing != null);
            }
        } catch (MultiException e) {
            e.printStackTrace();
        }
        return false;
    }

    // TODO - fetch command name from config bean(RestRedirect annotation).
    // RESTREdirect currently only support automatically these deletes:
    /*
     * delete-admin-object delete-audit-module delete-auth-realm delete-connector-connection-pool delete-connector-resource
     * delete-custom-resource delete-http-listener delete-iiop-listener delete-mail-resource delete-jdbc-connection-pool
     * delete-jdbc-resource delete-jms-host delete-message-security-provider delete-profiler delete-resource-adapter-config
     * delete-resource-ref delete-system-property delete-virtual-server What is missing is: delete-jms-resource delete-jmsdest
     * delete-jndi-resource delete-lifecycle-module delete-message-security-provider delete-connector-security-map
     * delete-connector-work-security-map delete-node-config delete-node-ssh delete-file-user delete-password-alias
     * delete-http-health-checker delete-http-lb-ref delete-http-redirect delete-instance
     */
    private static final Map<String, String> configBeanToDELETECommand = Map.ofEntries(
            entry("AdminObjectResource", "delete-admin-object"),
            entry("AuditModule", "delete-audit-module"),
            entry("AuthRealm", "delete-auth-realm"),
            entry("ApplicationRef", "delete-application-ref"),
            entry("Cluster", "delete-cluster"),
            entry("Config", "delete-config"),
            entry("ConnectorConnectionPool", "delete-connector-connection-pool"),
            entry("ConnectorResource", "delete-connector-resource"),
            entry("CustomResource", "delete-custom-resource"),
            entry("ExternalJndiResource", "delete-jndi-resource"),
            entry("HttpListener", "delete-http-listener"),
            entry("Http", "delete-http"),
            entry("IiopListener", "delete-iiop-listener"),
            entry("JdbcResource", "delete-jdbc-resource"),
            entry("JaccProvider", "delete-jacc-provider"),
            // entry("JmsHost", "delete-jms-host"),
            entry("LbConfig", "delete-http-lb-config"),
            entry("LoadBalancer", "delete-http-lb"),
            entry("NetworkListener", "delete-network-listener"),
            entry("Profiler", "delete-profiler"),
            entry("Protocol", "delete-protocol"),
            entry("ProtocolFilter", "delete-protocol-filter"),
            entry("ProtocolFinder", "delete-protocol-finder"),
            entry("ProviderConfig", "delete-message-security-provider"),
            entry("ResourceAdapterConfig", "delete-resource-adapter-config"),
            entry("SecurityMap", "delete-connector-security-map"),
            entry("Ssl", "delete-ssl"),
            entry("Transport", "delete-transport"),
            entry("ThreadPool", "delete-threadpool"),
            entry("VirtualServer", "delete-virtual-server"),
            entry("WorkSecurityMap", "delete-connector-work-security-map")
    );

    // TODO - fetch command name from config bean(RestRedirect annotation).
    public static final Map<String, String> configBeanToPOSTCommand = Map.ofEntries(
            entry("Application", "redeploy"), // TODO check : This row is not used
            entry("JavaConfig", "create-profiler"), // TODO check: This row is not used
            entry("ListAdminObjectResource", "create-admin-object"),
            entry("ListApplication", "deploy"),
            entry("ListApplicationRef", "create-application-ref"),
            entry("ListAuditModule", "create-audit-module"),
            entry("ListAuthRealm", "create-auth-realm"),
            entry("ListCluster", "create-cluster"),
            entry("ListConfig", "_create-config"),
            entry("ListConnectorConnectionPool", "create-connector-connection-pool"),
            entry("ListConnectorResource", "create-connector-resource"),
            entry("ListCustomResource", "create-custom-resource"),
            entry("ListExternalJndiResource", "create-jndi-resource"),
            entry("ListHttpListener", "create-http-listener"),
            entry("ListIiopListener", "create-iiop-listener"),
            entry("ListJaccProvider", "create-jacc-provider"),
            entry("ListJdbcConnectionPool", "create-jdbc-connection-pool"),
            entry("ListJdbcResource", "create-jdbc-resource"),
            entry("ListJmsHost", "create-jms-host"),
            entry("ListLbConfig", "create-http-lb-config"),
            entry("ListLoadBalancer", "create-http-lb"),
            entry("ListMailResource", "create-mail-resource"),
            entry("ListMessageSecurityConfig", "create-message-security-provider"),
            entry("ListNetworkListener", "create-network-listener"),
            entry("ListProtocol", "create-protocol"),
            entry("ListResourceAdapterConfig", "create-resource-adapter-config"),
            entry("ListResourceRef", "create-resource-ref"),
            entry("ListSystemProperty", "create-system-properties"),
            entry("ListThreadPool", "create-threadpool"),
            entry("ListTransport", "create-transport"),
            entry("ListVirtualServer", "create-virtual-server"),
            entry("ListWorkSecurityMap", "create-connector-work-security-map"),
            entry("ProtocolFilter", "create-protocol-filter"),
            entry("ProtocolFinder", "create-protocol-finder"),
            entry("ListSecurityMap", "create-connector-security-map")
    );

    // This map is used to generate CollectionLeaf resources.
    // Example: JVM Options. This information will eventually move to config bean -
    // JavaConfig or JvmOptionBag
    public static final Map<String, CollectionLeafMetaData> configBeanToCollectionLeafMetaData = Map.ofEntries(
            entry("JvmOptions", new CollectionLeafMetaData("create-jvm-options", "delete-jvm-options", "JvmOption"))
            // entry("Principal", new CollectionLeafMetaData("__create-principal", "__delete-principal", "Principal"));
            // entry("UserGroup", new CollectionLeafMetaData("__create-user-group", "__delete-user-group", "User Group"));
    );
}
