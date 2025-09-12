/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.admin.cli;

import com.sun.enterprise.util.i18n.StringManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.glassfish.api.I18n;
import org.glassfish.resources.api.Resource;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_GLOBAL;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_MODULE;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;
import static org.glassfish.resources.admin.cli.ResourceConstants.ADMIN_OBJECT_CLASS_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.ALLOW_NON_COMPONENT_CALLERS;
import static org.glassfish.resources.admin.cli.ResourceConstants.ASSOCIATE_WITH_THREAD;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_CREATION_RETRY_ATTEMPTS;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_LEAK_RECLAIM;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_LEAK_TIMEOUT_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_POOL_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_VALIDATION_METHOD;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTOR_CONNECTION_POOL_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONN_DEF_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONN_FAIL_ALL_CONNECTIONS;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONN_IDLE_TIME_OUT;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONN_MAX_POOL_SIZE;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONN_POOL_RESIZE_QUANTITY;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONN_STEADY_POOL_SIZE;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONN_TRANSACTION_SUPPORT;
import static org.glassfish.resources.admin.cli.ResourceConstants.CUSTOM_VALIDATION;
import static org.glassfish.resources.admin.cli.ResourceConstants.DATASOURCE_CLASS;
import static org.glassfish.resources.admin.cli.ResourceConstants.DRIVER_CLASSNAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.FACTORY_CLASS;
import static org.glassfish.resources.admin.cli.ResourceConstants.FAIL_ALL_CONNECTIONS;
import static org.glassfish.resources.admin.cli.ResourceConstants.IDLE_TIME_OUT_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.INIT_SQL;
import static org.glassfish.resources.admin.cli.ResourceConstants.IS_CONNECTION_VALIDATION_REQUIRED;
import static org.glassfish.resources.admin.cli.ResourceConstants.IS_ISOLATION_LEVEL_GUARANTEED;
import static org.glassfish.resources.admin.cli.ResourceConstants.JDBC_RESOURCE_JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_LOOKUP;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.LAZY_CONNECTION_ASSOCIATION;
import static org.glassfish.resources.admin.cli.ResourceConstants.LAZY_CONNECTION_ENLISTMENT;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_DEBUG;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_FROM_ADDRESS;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_HOST;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_STORE_PROTO;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_STORE_PROTO_CLASS;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_TRANS_PROTO;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_TRANS_PROTO_CLASS;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAIL_USER;
import static org.glassfish.resources.admin.cli.ResourceConstants.MATCH_CONNECTIONS;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAX_CONNECTION_USAGE_COUNT;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAX_POOL_SIZE;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAX_WAIT_TIME_IN_MILLIS;
import static org.glassfish.resources.admin.cli.ResourceConstants.NON_TRANSACTIONAL_CONNECTIONS;
import static org.glassfish.resources.admin.cli.ResourceConstants.PASSWORD;
import static org.glassfish.resources.admin.cli.ResourceConstants.PING;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOLING;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOL_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOL_SIZE_QUANTITY;
import static org.glassfish.resources.admin.cli.ResourceConstants.RESOURCE_ADAPTER_CONFIG_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.RESOURCE_TYPE;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_ADAPTER;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_ADAPTER_CONFIG;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_ADAPTER_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_TYPE;
import static org.glassfish.resources.admin.cli.ResourceConstants.SECURITY_MAP;
import static org.glassfish.resources.admin.cli.ResourceConstants.SECURITY_MAP_BACKEND_PRINCIPAL;
import static org.glassfish.resources.admin.cli.ResourceConstants.SECURITY_MAP_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.SECURITY_MAP_PRINCIPAL;
import static org.glassfish.resources.admin.cli.ResourceConstants.SECURITY_MAP_USER_GROUP;
import static org.glassfish.resources.admin.cli.ResourceConstants.SQL_TRACE_LISTENERS;
import static org.glassfish.resources.admin.cli.ResourceConstants.STATEMENT_CACHE_SIZE;
import static org.glassfish.resources.admin.cli.ResourceConstants.STATEMENT_LEAK_RECLAIM;
import static org.glassfish.resources.admin.cli.ResourceConstants.STATEMENT_LEAK_TIMEOUT_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.STATEMENT_TIMEOUT_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.STEADY_POOL_SIZE;
import static org.glassfish.resources.admin.cli.ResourceConstants.THREAD_POOL_IDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.TRANS_ISOLATION_LEVEL;
import static org.glassfish.resources.admin.cli.ResourceConstants.USER_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.VALIDATION_TABLE_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.WORK_SECURITY_MAP_EIS_GROUP;
import static org.glassfish.resources.admin.cli.ResourceConstants.WORK_SECURITY_MAP_EIS_PRINCIPAL;
import static org.glassfish.resources.admin.cli.ResourceConstants.WORK_SECURITY_MAP_GROUP_MAP;
import static org.glassfish.resources.admin.cli.ResourceConstants.WORK_SECURITY_MAP_MAPPED_GROUP;
import static org.glassfish.resources.admin.cli.ResourceConstants.WORK_SECURITY_MAP_MAPPED_PRINCIPAL;
import static org.glassfish.resources.admin.cli.ResourceConstants.WORK_SECURITY_MAP_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.WORK_SECURITY_MAP_PRINCIPAL_MAP;
import static org.glassfish.resources.admin.cli.ResourceConstants.WORK_SECURITY_MAP_RA_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.WRAP_JDBC_OBJECTS;
import static org.glassfish.resources.api.Resource.JDBC_RESOURCE;

//i18n import

/**
 * This Class reads the Properties (resources) from the xml file supplied
 * to constructor
 */
@I18n("resources.parser")
public class ResourcesXMLParser implements EntityResolver {

    private static final Logger LOG = System.getLogger(ResourcesXMLParser.class.getName());

    private File resourceFile = null;
    private Document document;
    private final List<org.glassfish.resources.api.Resource> vResources;
    private final Map<Resource, Node> resourceMap = new HashMap<>();

    private boolean isDoctypePresent = false;
    /* list of resources that needs to be created prior to module deployment. This
     * includes all non-Connector resources and resource-adapter-config
     */
//    private List<Resource> connectorResources;

    /* Includes all connector resources in the order in which the resources needs
      to be created */
//    private List<Resource> nonConnectorResources;

    // i18n StringManager
    private static StringManager localStrings = StringManager.getManager(ResourcesXMLParser.class);

    private static final int NONCONNECTOR = 2;
    private static final int CONNECTOR = 1;

    private static final String SUN_RESOURCES = "sun-resources";

    /** List of naming scopes */
    private static final List<String> JNDI_SCOPES = List.of(JNDI_CTX_JAVA_APP, JNDI_CTX_JAVA_COMPONENT,
        JNDI_CTX_JAVA_MODULE, JNDI_CTX_JAVA_GLOBAL);

    private static final String publicID_sjsas90 = "Sun Microsystems, Inc.//DTD Application Server 9.0 Resource Definitions";
    private static final String publicID_ges30 = "Sun Microsystems, Inc.//DTD GlassFish Application Server 3.0 Resource Definitions";
    private static final String publicID_ges31 = "GlassFish.org//DTD GlassFish Application Server 3.1 Resource Definitions";

    private static final String DTD_1_5 = "glassfish-resources_1_5.dtd";
    private static final String DTD_1_4 = "sun-resources_1_4.dtd";
    private static final String DTD_1_3 = "sun-resources_1_3.dtd";
    private static final String DTD_1_2 = "sun-resources_1_2.dtd";
    private static final String DTD_1_1 = "sun-resources_1_1.dtd";
    private static final String DTD_1_0 = "sun-resources_1_0.dtd";

    private static final List<String> systemIDs = Collections.unmodifiableList(
            Arrays.asList(
                    DTD_1_5,
                    DTD_1_4,
                    DTD_1_3,
                    DTD_1_2,
                    DTD_1_1,
                    DTD_1_0
            ));


    /** Creates new ResourcesXMLParser */
    public ResourcesXMLParser(File resourceFile) throws Exception {
        this.resourceFile = resourceFile;
        initProperties();
        vResources = new ArrayList<>();
        String scope = "";
        generateResourceObjects(scope);
    }

    /** Creates new ResourcesXMLParser */
    public ResourcesXMLParser(File resourceFile, String scope) throws Exception {
        this.resourceFile = resourceFile;
        initProperties();
        vResources = new ArrayList<>();
        generateResourceObjects(scope);
    }

    /**
     * Persist the XML file.
     * @param to target location
     */
    public void persist(File to) {
        // now serialize to a file.
        FileOutputStream out = null;
        try {
            String systemValue = (new File(document.getDoctype().getSystemId())).getName();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);
            out = new FileOutputStream(to);
            transformer.transform(new DOMSource(document), new StreamResult(out));
        } catch (Exception ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                    LOG.log(WARNING, ex.getMessage());
                }
            }
        }
    }

    // update the document node with modified resource
    public void updateDocumentNode(Resource originalResource, Resource modifiedResource) {
        Node resourceNode = resourceMap.remove(originalResource);

        // Remove all the existing property nodes.
        while (resourceNode.hasChildNodes()) {
            resourceNode.removeChild(resourceNode.getFirstChild());
        }

        Map<String, String> attrs = modifiedResource.getAttributes().getStrings();
        for (Entry<String, String> thisEntry : attrs.entrySet()) {
            ((Element) resourceNode).setAttribute(thisEntry.getKey(), thisEntry.getValue());
        }

        // Put the new/modified property nodes.
        Properties props = modifiedResource.getProperties();
        for (String key : props.stringPropertyNames()) {
            String val = props.getProperty(key);
            org.w3c.dom.Element prop = document.createElement("property");
            prop.setAttribute("name", key);
            prop.setAttribute("value", val);
            resourceNode.appendChild(prop);
        }

        // update the map
        resourceMap.put(modifiedResource, resourceNode);
    }

    public File getResourceFile() {
        return resourceFile;
    }

    /**
     *Parse the XML Properties file and populate it into document object
     */
    private void initProperties() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        FileInputStream fis = null;
        try {
            AddResourcesErrorHandler  errorHandler = new AddResourcesErrorHandler();
            factory.setValidating(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(this);
            builder.setErrorHandler(errorHandler);
            Object args[] = new Object[]{resourceFile};
            if (resourceFile == null) {
                String msg = localStrings.getStringWithDefault("resources.parser.no_resource_file",
                    "Resource file ({0} does not exist", args);
                throw new Exception(msg);
            }
            fis = new FileInputStream(resourceFile);
            InputSource is = new InputSource(fis);
            document = builder.parse(is);
            detectDeprecatedDescriptor();
        }/*catch(SAXParseException saxpe){
            throw new Exception(saxpe.getLocalizedMessage());
        }*/catch (SAXException sxe) {
            //This check is introduced to check if DOCTYPE is present in sun-resources.xml
            //And throw proper error message if DOCTYPE is missing
            try {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();
                sp.setProperty("http://xml.org/sax/properties/lexical-handler", new MyLexicalHandler());
                //we need to open the same file. Close it to be safe.
                if(fis != null){
                    try{
                        fis.close();
                        fis = null;
                    }catch(Exception e){
                        //ignore
                    }
                }
                fis = new FileInputStream(resourceFile);
                sp.getXMLReader().parse(new InputSource(fis));
            } catch (ParserConfigurationException ex) {
            } catch (SAXException ex) {
            } catch (IOException ex) {
            }
            if (!isDoctypePresent) {
                Object args[] = new Object[]{resourceFile.toString()};
                throw new Exception(
                    localStrings.getStringWithDefault("resources.parser.doctype_not_present_in_xml",
                                            "Error Parsing the xml ({0}), doctype is not present",
                                            args));
            }
            Exception  x = sxe;
            if (sxe.getException() != null) {
                x = sxe.getException();
            }
            throw new Exception(x.getLocalizedMessage());
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            throw new Exception(pce.getLocalizedMessage());
        } catch (IOException ioe) {
            throw new Exception(ioe.getLocalizedMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    /**
     * detects and logs a warning if any of the deprecated descriptor (sun-resources*.dtd) is specified
     */
    private void detectDeprecatedDescriptor() {
        String publicId = document.getDoctype().getPublicId();
        String systemId = document.getDoctype().getSystemId();
        if( (publicId != null && publicId.contains(SUN_RESOURCES)) ||
                (systemId != null && systemId.contains(SUN_RESOURCES))){
            String msg = localStrings.getString(
                    "deprecated.resources.dtd", resourceFile.getAbsolutePath() );
            LOG.log(DEBUG, msg);
        }
    }

    /**
     * Get All the resources from the document object.
     *
     */
    private void generateResourceObjects(String scope) throws Exception {
        if (document == null) {
            return;
        }
        for (Node nextKid = document.getDocumentElement().getFirstChild(); nextKid != null; nextKid = nextKid
            .getNextSibling()) {
            String nodeName = nextKid.getNodeName();
            if (nodeName.equalsIgnoreCase(Resource.CUSTOM_RESOURCE)) {
                generateCustomResource(nextKid, scope);
            } else if (nodeName.equalsIgnoreCase(org.glassfish.resources.api.Resource.EXTERNAL_JNDI_RESOURCE)) {
                generateJNDIResource(nextKid, scope);
            } else if (nodeName.equalsIgnoreCase(org.glassfish.resources.api.Resource.JDBC_RESOURCE)) {
                generateJDBCResource(nextKid, scope);
            } else if (nodeName.equalsIgnoreCase(org.glassfish.resources.api.Resource.JDBC_CONNECTION_POOL)) {
                generateJDBCConnectionPoolResource(nextKid, scope);
            } else if (nodeName.equalsIgnoreCase(Resource.MAIL_RESOURCE)) {
                generateMailResource(nextKid, scope);
            } else if (nodeName
                .equalsIgnoreCase(org.glassfish.resources.api.Resource.PERSISTENCE_MANAGER_FACTORY_RESOURCE)) {
                // PMF resource is no more supported and hence removing support form sun-resources.xml
                generatePersistenceResource(nextKid);
                LOG.log(DEBUG, "persistence-manager-factory-resource is no more supported "
                    + ", ignoring the resource description");
            } else if (nodeName.equalsIgnoreCase(Resource.ADMIN_OBJECT_RESOURCE)) {
                generateAdminObjectResource(nextKid, scope);
            } else if (nodeName.equalsIgnoreCase(Resource.CONNECTOR_RESOURCE)) {
                generateConnectorResource(nextKid, scope);
            } else if (nodeName.equalsIgnoreCase(org.glassfish.resources.api.Resource.CONNECTOR_CONNECTION_POOL)) {
                generateConnectorConnectionPoolResource(nextKid, scope);
            } else if (nodeName.equalsIgnoreCase(org.glassfish.resources.api.Resource.RESOURCE_ADAPTER_CONFIG)) {
                generateResourceAdapterConfig(nextKid, scope);
            } else if (nodeName.equalsIgnoreCase(org.glassfish.resources.api.Resource.CONNECTOR_WORK_SECURITY_MAP)) {
                generateWorkSecurityMap(nextKid, scope);
            }
        }
    }

    /**
     * Sorts the resources defined in the resources configuration xml file into
     * two buckets
     *  a) list of resources that needs to be created prior to
     *  module deployment. This includes all non-Connector resources
     *  and resource-adapter-config
     *  b) includes all connector resources in the order in which the resources needs
     *  to be created
     *  and returns the requested resources bucker to the caller.
     *
     *  @param resources Resources list as defined in sun-resources.xml
     *  @param type Specified either ResourcesXMLParser.CONNECTOR or
     *  ResourcesXMLParser.NONCONNECTOR to indicate the type of
     *  resources are needed by the client of the ResourcesXMLParser
     *  @param isResourceCreation During the resource Creation Phase, RA configs are
     *  added to the nonConnector resources list so that they can be created in the
     *  <code>PreResCreationPhase</code>. When the isResourceCreation is false, the
     *  RA config resuorces are added to the Connector Resources list, so that they
     *  can be deleted as all other connector resources in the
     *  <code>PreResDeletionPhase</code>
     */
    private static List<org.glassfish.resources.api.Resource> getResourcesOfType(List<Resource> resources,
                                            int type, boolean isResourceCreation, boolean ignoreDuplicates) {
        List<Resource> nonConnectorResources = new ArrayList<>();
        List<org.glassfish.resources.api.Resource> connectorResources = new ArrayList<>();

        for (Resource res : resources) {
            if (isConnectorResource(res)) {
                if (res.getType().equals(Resource.RESOURCE_ADAPTER_CONFIG)) {
                    if(isResourceCreation) {
                        //RA config is considered as a nonConnector Resource,
                        //during sun-resources.xml resource-creation phase, so that
                        //it could be created before the RAR is deployed.
                        addToList(nonConnectorResources, res, ignoreDuplicates);
                    } else {
                        addToList(connectorResources, res, ignoreDuplicates);
                    }
                } else {
                    addToList(connectorResources, res, ignoreDuplicates);
                }
            } else {
                addToList(nonConnectorResources, res, ignoreDuplicates);
            }
        }

        List<org.glassfish.resources.api.Resource> finalSortedConnectorList = sortConnectorResources(connectorResources);
        List<org.glassfish.resources.api.Resource> finalSortedNonConnectorList = sortNonConnectorResources(nonConnectorResources);
        return type == CONNECTOR ? finalSortedConnectorList : finalSortedNonConnectorList;
    }


    private static void addToList(List<Resource> resources, Resource res, boolean ignoreDuplicates) {
        if (ignoreDuplicates) {
            if (!resources.contains(res)) {
                resources.add(res);
            }
        } else {
            resources.add(res);
        }
    }

    /**
     * Sort connector resources
     * Resource Adapter configs are added first.
     * Pools are then added to the list, so that the connection
     * pools can be created prior to any other connector resource defined
     * in the resources configuration xml file.
     * @param connectorResources List of Resources to be sorted.
     * @return The sorted list.
     */
    private static List<Resource> sortConnectorResources(List<org.glassfish.resources.api.Resource> connectorResources) {
        List<org.glassfish.resources.api.Resource> raconfigs = new ArrayList<>();
        List<Resource> ccps = new ArrayList<>();
        List<org.glassfish.resources.api.Resource> others = new ArrayList<>();

        List<Resource> finalSortedConnectorList = new ArrayList<>();

        for (Resource resource : connectorResources) {
            if (resource.getType().equals(org.glassfish.resources.api.Resource.RESOURCE_ADAPTER_CONFIG)){
                raconfigs.add(resource);
            } else if (resource.getType().equals(org.glassfish.resources.api.Resource.CONNECTOR_CONNECTION_POOL)) {
                ccps.add(resource);
            } else {
                others.add(resource);
            }
        }

        finalSortedConnectorList.addAll(raconfigs);
        finalSortedConnectorList.addAll(ccps);
        finalSortedConnectorList.addAll(others);
        return finalSortedConnectorList;
    }

    /**
     * Sort non connector resources
     * JDBC Pools are added first to the list, so that the conncetion
     * pools can be created prior to any other jdbc resource defined
     * in the resources configuration xml file.
     * @param nonConnectorResources List of Resources to be sorted.
     * @return The sorted list.
     */
    private static List<org.glassfish.resources.api.Resource> sortNonConnectorResources(List<org.glassfish.resources.api.Resource> nonConnectorResources) {
        List<org.glassfish.resources.api.Resource> jdbccps = new ArrayList<>();
        List<org.glassfish.resources.api.Resource> pmfs = new ArrayList<>();
        List<org.glassfish.resources.api.Resource> others = new ArrayList<>();

        List<org.glassfish.resources.api.Resource> finalSortedNonConnectorList = new ArrayList<>();

        for (Resource resource : nonConnectorResources) {
            if(resource.getType().equals(org.glassfish.resources.api.Resource.JDBC_CONNECTION_POOL)) {
                jdbccps.add(resource);
            } else if(resource.getType().equals(org.glassfish.resources.api.Resource.PERSISTENCE_MANAGER_FACTORY_RESOURCE)) {
                //TODO throw exception as pmf resource is not supported anymore
                pmfs.add(resource);
            } else {
                others.add(resource);
            }
        }

        finalSortedNonConnectorList.addAll(jdbccps);
        finalSortedNonConnectorList.addAll(others);
        finalSortedNonConnectorList.addAll(pmfs);
        return finalSortedNonConnectorList;
    }

    /**
     * Determines if the passed in <code>Resource</code> is a connector
     * resource. A connector resource is either a connector connection pool or a
     * connector resource, security map, ra config or an admin object
     *
     * @param res Resource that needs to be tested
     * @return
     */
    private static boolean isConnectorResource(Resource res) {
        String type = res.getType();
        return (
                    (type.equals(Resource.ADMIN_OBJECT_RESOURCE)) ||
                    (type.equals(org.glassfish.resources.api.Resource.CONNECTOR_CONNECTION_POOL)) ||
                    (type.equals(org.glassfish.resources.api.Resource.CONNECTOR_RESOURCE)) ||
                    (type.equals(org.glassfish.resources.api.Resource.CONNECTOR_SECURITY_MAP)) ||
                    (type.equals(org.glassfish.resources.api.Resource.RESOURCE_ADAPTER_CONFIG))
            );
    }

    private String getScopedName(String name, String scope) throws Exception{
        if (JNDI_SCOPES.contains(scope)) {
            if (name != null) {
                for (String namingScope : JNDI_SCOPES) {
                    if (name.startsWith(namingScope) && !namingScope.equals(scope)) {
                        Object args[] = new Object[] {name, namingScope, scope};
                        String msg = localStrings.getStringWithDefault( "invalid.scope.defined.for.resource",
                                                                "Resource [ {0} ] is not allowed to specify the scope " +
                                                                        "[ {1} ]. Acceptable scope for this resource" +
                                                                        " is [ {2} ]", args
                                                                 );
                        throw new IllegalStateException(msg);
                    }
                }
                if (!name.startsWith(scope)) {
                    name = scope + name;
                }
            }
        }
        return name;
    }


    private void generatePersistenceResource(Node nextKid) throws Exception {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null) {
            return;
        }

        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = jndiNameNode.getNodeValue();
        Node factoryClassNode = attributes.getNamedItem(FACTORY_CLASS);
        Node poolNameNode = attributes.getNamedItem(JDBC_RESOURCE_JNDI_NAME);
        Node enabledNode = attributes.getNamedItem(ENABLED);

        Resource persistenceResource =
                    new org.glassfish.resources.api.Resource(org.glassfish.resources.api.Resource.PERSISTENCE_MANAGER_FACTORY_RESOURCE);
        persistenceResource.setAttribute(JNDI_NAME, jndiName);
        if (factoryClassNode != null) {
           String factoryClass = factoryClassNode.getNodeValue();
           persistenceResource.setAttribute(FACTORY_CLASS, factoryClass);
        }
        if (poolNameNode != null) {
           String poolName = poolNameNode.getNodeValue();
           persistenceResource.setAttribute(JDBC_RESOURCE_JNDI_NAME, poolName);
        }
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           persistenceResource.setAttribute(ENABLED, sEnabled);
        }

        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(persistenceResource, children);
        vResources.add(persistenceResource);
        resourceMap.put(persistenceResource, nextKid);

        LOG.log(DEBUG, "Persistence resource jndiName {0} attributes: {1}", jndiName,
            persistenceResource.getAttributes());
    }

    /**
     * Generate the Custom resource
     */
    private void generateCustomResource(Node nextKid, String scope) throws Exception {
        NamedNodeMap attributes = nextKid.getAttributes();

        if (attributes == null) {
            return;
        }

        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = getScopedName(jndiNameNode.getNodeValue(), scope);

        Node resTypeNode = attributes.getNamedItem(RES_TYPE);
        String resType = resTypeNode.getNodeValue();

        Node factoryClassNode = attributes.getNamedItem(FACTORY_CLASS);
        String factoryClass = factoryClassNode.getNodeValue();

        Node enabledNode = attributes.getNamedItem(ENABLED);

        Resource customResource = new Resource(Resource.CUSTOM_RESOURCE);
        customResource.setAttribute(JNDI_NAME, jndiName);
        customResource.setAttribute(RES_TYPE, resType);
        customResource.setAttribute(FACTORY_CLASS, factoryClass);
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           customResource.setAttribute(ENABLED, sEnabled);
        }

        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(customResource, children);
        vResources.add(customResource);
        resourceMap.put(customResource, nextKid);

        LOG.log(DEBUG, "Custom resource jndiName {0} attributes: {1}", jndiName, customResource.getAttributes());
    }

    /**
     * Generate the JNDI resource
     */
    private void generateJNDIResource(Node nextKid, String scope) throws Exception {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null) {
            return;
        }

        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = getScopedName(jndiNameNode.getNodeValue(), scope);
        Node jndiLookupNode = attributes.getNamedItem(JNDI_LOOKUP);
        String jndiLookup = jndiLookupNode.getNodeValue();
        Node resTypeNode = attributes.getNamedItem(RES_TYPE);
        String resType = resTypeNode.getNodeValue();
        Node factoryClassNode = attributes.getNamedItem(FACTORY_CLASS);
        String factoryClass = factoryClassNode.getNodeValue();
        Node enabledNode = attributes.getNamedItem(ENABLED);

        org.glassfish.resources.api.Resource jndiResource = new Resource(Resource.EXTERNAL_JNDI_RESOURCE);
        jndiResource.setAttribute(JNDI_NAME, jndiName);
        jndiResource.setAttribute(JNDI_LOOKUP, jndiLookup);
        jndiResource.setAttribute(RES_TYPE, resType);
        jndiResource.setAttribute(FACTORY_CLASS, factoryClass);
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           jndiResource.setAttribute(ENABLED, sEnabled);
        }

        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(jndiResource, children);
        vResources.add(jndiResource);
        resourceMap.put(jndiResource, nextKid);

        LOG.log(DEBUG, "JNDI resource jndiName {0} attributes: {1}", jndiName, jndiResource.getAttributes());
    }

    /**
     * Generate the JDBC resource
     */
    private void generateJDBCResource(Node nextKid, String scope) throws Exception {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null) {
            return;
        }

        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = getScopedName(jndiNameNode.getNodeValue(), scope);
        Node poolNameNode = attributes.getNamedItem(POOL_NAME);
        String poolName = getScopedName(poolNameNode.getNodeValue(), scope);
        Node enabledNode = attributes.getNamedItem(ENABLED);

        Resource jdbcResource = new org.glassfish.resources.api.Resource(JDBC_RESOURCE);
        jdbcResource.setAttribute(JNDI_NAME, jndiName);
        jdbcResource.setAttribute(POOL_NAME, poolName);
        if (enabledNode != null) {
           String enabledName = enabledNode.getNodeValue();
           jdbcResource.setAttribute(ENABLED, enabledName);
        }

        NodeList children = nextKid.getChildNodes();
        //get description
        if (children != null) {
            for (int ii = 0; ii < children.getLength(); ii++) {
                if (children.item(ii).getNodeName().equals("description")) {
                    if (children.item(ii).getFirstChild() != null) {
                        jdbcResource.setDescription(children.item(ii).getFirstChild().getNodeValue());
                    }
                }
            }
        }

        vResources.add(jdbcResource);
        resourceMap.put(jdbcResource, nextKid);

        LOG.log(DEBUG, "JDBC resource jndiName {0} attributes: {1}", jndiName, jdbcResource.getAttributes());
    }

    /**
     * Generate the JDBC Connection pool Resource
     */
    private void generateJDBCConnectionPoolResource(Node nextKid, String scope) throws Exception {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null) {
            return;
        }

        Node nameNode = attributes.getNamedItem(CONNECTION_POOL_NAME);
        String name = getScopedName(nameNode.getNodeValue(), scope);
        Node nSteadyPoolSizeNode = attributes.getNamedItem(STEADY_POOL_SIZE);
        Node nMaxPoolSizeNode = attributes.getNamedItem(MAX_POOL_SIZE);
        Node nMaxWaitTimeInMillisNode = attributes.getNamedItem(MAX_WAIT_TIME_IN_MILLIS);
        Node nPoolSizeQuantityNode = attributes.getNamedItem(POOL_SIZE_QUANTITY);
        Node nIdleTimeoutInSecNode = attributes.getNamedItem(IDLE_TIME_OUT_IN_SECONDS);
        Node nIsConnectionValidationRequiredNode = attributes.getNamedItem(IS_CONNECTION_VALIDATION_REQUIRED);
        Node nConnectionValidationMethodNode = attributes.getNamedItem(CONNECTION_VALIDATION_METHOD);
        Node nFailAllConnectionsNode = attributes.getNamedItem(FAIL_ALL_CONNECTIONS);
        Node nValidationTableNameNode = attributes.getNamedItem(VALIDATION_TABLE_NAME);
        Node nResType = attributes.getNamedItem(RES_TYPE);
        Node nTransIsolationLevel = attributes.getNamedItem(TRANS_ISOLATION_LEVEL);
        Node nIsIsolationLevelQuaranteed = attributes.getNamedItem(IS_ISOLATION_LEVEL_GUARANTEED);
        Node datasourceNode = attributes.getNamedItem(DATASOURCE_CLASS);
        Node nonTransactionalConnectionsNode = attributes.getNamedItem(NON_TRANSACTIONAL_CONNECTIONS);
        Node allowNonComponentCallersNode = attributes.getNamedItem(ALLOW_NON_COMPONENT_CALLERS);
        Node validateAtmostOncePeriodNode = attributes.getNamedItem(VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS);
        Node connectionLeakTimeoutNode = attributes.getNamedItem(CONNECTION_LEAK_TIMEOUT_IN_SECONDS);
        Node connectionLeakReclaimNode = attributes.getNamedItem(CONNECTION_LEAK_RECLAIM);
        Node connectionCreationRetryAttemptsNode = attributes.getNamedItem(CONNECTION_CREATION_RETRY_ATTEMPTS);
        Node connectionCreationRetryIntervalNode = attributes.getNamedItem(CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS);
        Node statementTimeoutNode = attributes.getNamedItem(STATEMENT_TIMEOUT_IN_SECONDS);
        Node lazyConnectionEnlistmentNode = attributes.getNamedItem(LAZY_CONNECTION_ENLISTMENT);
        Node lazyConnectionAssociationNode = attributes.getNamedItem(LAZY_CONNECTION_ASSOCIATION);
        Node associateWithThreadNode = attributes.getNamedItem(ASSOCIATE_WITH_THREAD);
        Node matchConnectionsNode = attributes.getNamedItem(MATCH_CONNECTIONS);
        Node maxConnectionUsageCountNode = attributes.getNamedItem(MAX_CONNECTION_USAGE_COUNT);
        Node wrapJDBCObjectsNode = attributes.getNamedItem(WRAP_JDBC_OBJECTS);
        Node poolingNode = attributes.getNamedItem(POOLING);
        Node pingNode = attributes.getNamedItem(PING);
        Node customValidationNode = attributes.getNamedItem(CUSTOM_VALIDATION);
        Node driverClassNameNode = attributes.getNamedItem(DRIVER_CLASSNAME);
        Node initSqlNode = attributes.getNamedItem(INIT_SQL);
        Node sqlTraceListenersNode = attributes.getNamedItem(SQL_TRACE_LISTENERS);
        Node statementCacheSizeNode = attributes.getNamedItem(STATEMENT_CACHE_SIZE);
        Node statementLeakTimeoutNode = attributes.getNamedItem(STATEMENT_LEAK_TIMEOUT_IN_SECONDS);
        Node statementLeakReclaimNode = attributes.getNamedItem(STATEMENT_LEAK_RECLAIM);

        org.glassfish.resources.api.Resource jdbcConnPool = new Resource(org.glassfish.resources.api.Resource.JDBC_CONNECTION_POOL);
        jdbcConnPool.setAttribute(CONNECTION_POOL_NAME, name);

        if (datasourceNode != null) {
            String datasource = datasourceNode.getNodeValue();
            jdbcConnPool.setAttribute(DATASOURCE_CLASS, datasource);
        }

        if (nSteadyPoolSizeNode != null) {
            String sSteadyPoolSize = nSteadyPoolSizeNode.getNodeValue();
            jdbcConnPool.setAttribute(STEADY_POOL_SIZE, sSteadyPoolSize);
        }
        if (nMaxPoolSizeNode != null) {
            String sMaxPoolSize = nMaxPoolSizeNode.getNodeValue();
            jdbcConnPool.setAttribute(MAX_POOL_SIZE, sMaxPoolSize);
        }
        if (nMaxWaitTimeInMillisNode != null) {
            String sMaxWaitTimeInMillis = nMaxWaitTimeInMillisNode.getNodeValue();
            jdbcConnPool.setAttribute(MAX_WAIT_TIME_IN_MILLIS, sMaxWaitTimeInMillis);
        }
        if (nPoolSizeQuantityNode != null) {
            String sPoolSizeQuantity = nPoolSizeQuantityNode.getNodeValue();
            jdbcConnPool.setAttribute(POOL_SIZE_QUANTITY, sPoolSizeQuantity);
        }
        if (nIdleTimeoutInSecNode != null) {
            String sIdleTimeoutInSec = nIdleTimeoutInSecNode.getNodeValue();
            jdbcConnPool.setAttribute(IDLE_TIME_OUT_IN_SECONDS, sIdleTimeoutInSec);
        }
        if (nIsConnectionValidationRequiredNode != null) {
            String sIsConnectionValidationRequired = nIsConnectionValidationRequiredNode.getNodeValue();
            jdbcConnPool.setAttribute(IS_CONNECTION_VALIDATION_REQUIRED, sIsConnectionValidationRequired);
        }
        if (nConnectionValidationMethodNode != null) {
            String sConnectionValidationMethod = nConnectionValidationMethodNode.getNodeValue();
            jdbcConnPool.setAttribute(CONNECTION_VALIDATION_METHOD, sConnectionValidationMethod);
        }
        if (nFailAllConnectionsNode != null) {
            String sFailAllConnection = nFailAllConnectionsNode.getNodeValue();
            jdbcConnPool.setAttribute(FAIL_ALL_CONNECTIONS, sFailAllConnection);
        }
        if (nValidationTableNameNode != null) {
            String sValidationTableName = nValidationTableNameNode.getNodeValue();
            jdbcConnPool.setAttribute(VALIDATION_TABLE_NAME, sValidationTableName);
        }
        if (nResType != null) {
            String sResType = nResType.getNodeValue();
            jdbcConnPool.setAttribute(RES_TYPE, sResType);
        }
        if (nTransIsolationLevel != null) {
            String sTransIsolationLevel = nTransIsolationLevel.getNodeValue();
            jdbcConnPool.setAttribute(TRANS_ISOLATION_LEVEL, sTransIsolationLevel);
        }
        if (nIsIsolationLevelQuaranteed != null) {
            String sIsIsolationLevelQuaranteed = nIsIsolationLevelQuaranteed.getNodeValue();
            jdbcConnPool.setAttribute(IS_ISOLATION_LEVEL_GUARANTEED, sIsIsolationLevelQuaranteed);
        }
        if (nonTransactionalConnectionsNode != null) {
            jdbcConnPool.setAttribute(NON_TRANSACTIONAL_CONNECTIONS, nonTransactionalConnectionsNode.getNodeValue());
        }
        if (allowNonComponentCallersNode != null) {
            jdbcConnPool.setAttribute(ALLOW_NON_COMPONENT_CALLERS, allowNonComponentCallersNode.getNodeValue());
        }
        if (validateAtmostOncePeriodNode != null) {
            jdbcConnPool.setAttribute(VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS,
                validateAtmostOncePeriodNode.getNodeValue());
        }
        if (connectionLeakTimeoutNode != null) {
            jdbcConnPool.setAttribute(CONNECTION_LEAK_TIMEOUT_IN_SECONDS, connectionLeakTimeoutNode.getNodeValue());
        }
        if (connectionLeakReclaimNode != null) {
            jdbcConnPool.setAttribute(CONNECTION_LEAK_RECLAIM, connectionLeakReclaimNode.getNodeValue());
        }
        if (connectionCreationRetryAttemptsNode != null) {
            jdbcConnPool.setAttribute(CONNECTION_CREATION_RETRY_ATTEMPTS,
                connectionCreationRetryAttemptsNode.getNodeValue());
        }
        if (connectionCreationRetryIntervalNode != null) {
            jdbcConnPool.setAttribute(CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS,
                connectionCreationRetryIntervalNode.getNodeValue());
        }
        if (statementTimeoutNode != null) {
            jdbcConnPool.setAttribute(STATEMENT_TIMEOUT_IN_SECONDS, statementTimeoutNode.getNodeValue());
        }
        if (lazyConnectionEnlistmentNode != null) {
            jdbcConnPool.setAttribute(LAZY_CONNECTION_ENLISTMENT, lazyConnectionEnlistmentNode.getNodeValue());
        }
        if (lazyConnectionAssociationNode != null) {
            jdbcConnPool.setAttribute(LAZY_CONNECTION_ASSOCIATION, lazyConnectionAssociationNode.getNodeValue());
        }
        if (associateWithThreadNode != null) {
            jdbcConnPool.setAttribute(ASSOCIATE_WITH_THREAD, associateWithThreadNode.getNodeValue());
        }
        if (matchConnectionsNode != null) {
            jdbcConnPool.setAttribute(MATCH_CONNECTIONS, matchConnectionsNode.getNodeValue());
        }
        if (maxConnectionUsageCountNode != null) {
            jdbcConnPool.setAttribute(MAX_CONNECTION_USAGE_COUNT, maxConnectionUsageCountNode.getNodeValue());
        }
        if (wrapJDBCObjectsNode != null) {
            jdbcConnPool.setAttribute(WRAP_JDBC_OBJECTS, wrapJDBCObjectsNode.getNodeValue());
        }
        if (poolingNode != null) {
            String pooling = poolingNode.getNodeValue();
            jdbcConnPool.setAttribute(POOLING, pooling);
        }
        if (pingNode != null) {
            String ping = pingNode.getNodeValue();
            jdbcConnPool.setAttribute(PING, ping);
        }
        if (initSqlNode != null) {
            String initSQL = initSqlNode.getNodeValue();
            jdbcConnPool.setAttribute(INIT_SQL, initSQL);
        }
        if (sqlTraceListenersNode != null) {
            String sqlTraceListeners = sqlTraceListenersNode.getNodeValue();
            jdbcConnPool.setAttribute(SQL_TRACE_LISTENERS, sqlTraceListeners);
        }
        if (customValidationNode != null) {
            String customValidation = customValidationNode.getNodeValue();
            jdbcConnPool.setAttribute(CUSTOM_VALIDATION, customValidation);
        }
        if (driverClassNameNode != null) {
            String driverClassName = driverClassNameNode.getNodeValue();
            jdbcConnPool.setAttribute(DRIVER_CLASSNAME, driverClassName);
        }
        if (statementCacheSizeNode != null) {
            String statementCacheSize = statementCacheSizeNode.getNodeValue();
            jdbcConnPool.setAttribute(STATEMENT_CACHE_SIZE, statementCacheSize);
        }
        if (statementLeakTimeoutNode != null) {
            jdbcConnPool.setAttribute(STATEMENT_LEAK_TIMEOUT_IN_SECONDS, statementLeakTimeoutNode.getNodeValue());
        }
        if (statementLeakReclaimNode != null) {
            jdbcConnPool.setAttribute(STATEMENT_LEAK_RECLAIM, statementLeakReclaimNode.getNodeValue());
        }

        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(jdbcConnPool, children);
        vResources.add(jdbcConnPool);
        resourceMap.put(jdbcConnPool, nextKid);

        LOG.log(DEBUG, "JDBC connection pool name {0} attributes: {1}", name, jdbcConnPool.getAttributes());
    }

    /**
     * Generate the Mail resource
     */
    private void generateMailResource(Node nextKid, String scope) throws Exception {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null) {
            return;
        }

        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        Node hostNode = attributes.getNamedItem(MAIL_HOST);
        Node userNode = attributes.getNamedItem(MAIL_USER);
        Node fromAddressNode = attributes.getNamedItem(MAIL_FROM_ADDRESS);
        Node storeProtoNode = attributes.getNamedItem(MAIL_STORE_PROTO);
        Node storeProtoClassNode = attributes.getNamedItem(MAIL_STORE_PROTO_CLASS);
        Node transProtoNode = attributes.getNamedItem(MAIL_TRANS_PROTO);
        Node transProtoClassNode = attributes.getNamedItem(MAIL_TRANS_PROTO_CLASS);
        Node debugNode = attributes.getNamedItem(MAIL_DEBUG);
        Node enabledNode = attributes.getNamedItem(ENABLED);

        String jndiName = getScopedName(jndiNameNode.getNodeValue(), scope);
        String host = hostNode.getNodeValue();
        String user = userNode.getNodeValue();
        String fromAddress = fromAddressNode.getNodeValue();

        org.glassfish.resources.api.Resource mailResource = new org.glassfish.resources.api.Resource(
            org.glassfish.resources.api.Resource.MAIL_RESOURCE);

        mailResource.setAttribute(JNDI_NAME, jndiName);
        mailResource.setAttribute(MAIL_HOST, host);
        mailResource.setAttribute(MAIL_USER, user);
        mailResource.setAttribute(MAIL_FROM_ADDRESS, fromAddress);
        if (storeProtoNode != null) {
            String sStoreProto = storeProtoNode.getNodeValue();
            mailResource.setAttribute(MAIL_STORE_PROTO, sStoreProto);
        }
        if (storeProtoClassNode != null) {
            String sStoreProtoClass = storeProtoClassNode.getNodeValue();
            mailResource.setAttribute(MAIL_STORE_PROTO_CLASS, sStoreProtoClass);
        }
        if (transProtoNode != null) {
            String sTransProto = transProtoNode.getNodeValue();
            mailResource.setAttribute(MAIL_TRANS_PROTO, sTransProto);
        }
        if (transProtoClassNode != null) {
            String sTransProtoClass = transProtoClassNode.getNodeValue();
            mailResource.setAttribute(MAIL_TRANS_PROTO_CLASS, sTransProtoClass);
        }
        if (debugNode != null) {
            String sDebug = debugNode.getNodeValue();
            mailResource.setAttribute(MAIL_DEBUG, sDebug);
        }
        if (enabledNode != null) {
            String sEnabled = enabledNode.getNodeValue();
            mailResource.setAttribute(ENABLED, sEnabled);
        }

        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(mailResource, children);
        vResources.add(mailResource);
        resourceMap.put(mailResource, nextKid);

        LOG.log(DEBUG, "Mail resource jndiName {0} attributes: {1}", jndiName, mailResource.getAttributes());
    }

    /**
     * Generate the Admin Object resource
     */
    private void generateAdminObjectResource(Node nextKid, String scope) throws Exception {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null) {
            return;
        }

        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = getScopedName(jndiNameNode.getNodeValue(), scope);
        Node resTypeNode = attributes.getNamedItem(RES_TYPE);
        String resType = resTypeNode.getNodeValue();
        Node classNameNode = attributes.getNamedItem(ADMIN_OBJECT_CLASS_NAME);
        String className = null;
        if (classNameNode != null) {
            className = classNameNode.getNodeValue();
        }
        Node resAdapterNode = attributes.getNamedItem(RES_ADAPTER);
        String resAdapter = resAdapterNode.getNodeValue();
        Node enabledNode = attributes.getNamedItem(ENABLED);

        org.glassfish.resources.api.Resource adminObjectResource
            = new org.glassfish.resources.api.Resource(Resource.ADMIN_OBJECT_RESOURCE);
        adminObjectResource.setAttribute(JNDI_NAME, jndiName);
        adminObjectResource.setAttribute(RES_TYPE, resType);
        if (classNameNode != null) {
            adminObjectResource.setAttribute(ADMIN_OBJECT_CLASS_NAME, className);
        }
        adminObjectResource.setAttribute(RES_ADAPTER, resAdapter);

        if (enabledNode != null) {
            String sEnabled = enabledNode.getNodeValue();
            adminObjectResource.setAttribute(ENABLED, sEnabled);
        }

        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(adminObjectResource, children);
        vResources.add(adminObjectResource);
        resourceMap.put(adminObjectResource, nextKid);

        LOG.log(DEBUG, "Admin Object resource jndiName {0} attributes: {1}", jndiName,
            adminObjectResource.getAttributes());
    }

    /**
     * Generate the Connector resource
     */
    private void generateConnectorResource(Node nextKid, String scope) throws Exception {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null) {
            return;
        }

        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = getScopedName(jndiNameNode.getNodeValue(), scope);
        Node poolNameNode = attributes.getNamedItem(POOL_NAME);
        String poolName = getScopedName(poolNameNode.getNodeValue(), scope);
        Node resTypeNode = attributes.getNamedItem(RESOURCE_TYPE);
        Node enabledNode = attributes.getNamedItem(ENABLED);

        org.glassfish.resources.api.Resource connectorResource =
                    new Resource(org.glassfish.resources.api.Resource.CONNECTOR_RESOURCE);
        connectorResource.setAttribute(JNDI_NAME, jndiName);
        connectorResource.setAttribute(POOL_NAME, poolName);
        if (resTypeNode != null) {
           String resType = resTypeNode.getNodeValue();
           connectorResource.setAttribute(RESOURCE_TYPE, resType);
        }
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           connectorResource.setAttribute(ENABLED, sEnabled);
        }

        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(connectorResource, children);
        vResources.add(connectorResource);
        resourceMap.put(connectorResource, nextKid);

        LOG.log(DEBUG, "Connector resource jndiName {0} attributes: {1}", jndiName, connectorResource.getAttributes());
    }


    private void generatePropertyElement(Resource rs, NodeList children) throws Exception {
        if (children != null) {
            for (int i=0; i<children.getLength(); i++) {
                if (children.item(i).getNodeName().equals("property")) {
                    NamedNodeMap attNodeMap = children.item(i).getAttributes();
                    Node nameNode = attNodeMap.getNamedItem("name");
                    Node valueNode = attNodeMap.getNamedItem("value");
                    if (nameNode != null && valueNode != null) {
                        boolean bDescFound = false;
                        String sName = nameNode.getNodeValue();
                        String sValue = valueNode.getNodeValue();
                        //get property description
                        // FIX ME: Ignoring the value for description for the
                        // time-being as there is no method available in
                        // configMBean to set description for a property
                        Node descNode = children.item(i).getFirstChild();
                        while (descNode != null && !bDescFound) {
                            if (descNode.getNodeName().equalsIgnoreCase("description")) {
                                try {
                                    //rs.setElementProperty(sName, sValue, descNode.getFirstChild().getNodeValue());
                                    rs.setProperty(sName, sValue);
                                    bDescFound = true;
                                }
                                catch (DOMException dome) {
                                    // DOM Error
                                    throw new Exception(dome.getLocalizedMessage());
                                }
                            }
                            descNode = descNode.getNextSibling();
                        }
                        if (!bDescFound) {
                            rs.setProperty(sName, sValue);
                        }
                    }
                }
                if (children.item(i).getNodeName().equals("description")) {
                    Node descNode = children.item(i).getFirstChild();
                    if(descNode != null){
                        rs.setDescription(descNode.getNodeValue());
                    }
                }
            }
        }
    }

    /**
     * Generate the Connector Connection pool Resource
     */
    private void generateConnectorConnectionPoolResource(Node nextKid, String scope) throws Exception {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null) {
            return ;
        }

        Node nameNode = attributes.getNamedItem(CONNECTOR_CONNECTION_POOL_NAME);
        Node raConfigNode = attributes.getNamedItem(RESOURCE_ADAPTER_CONFIG_NAME);
        Node conDefNode = attributes.getNamedItem(CONN_DEF_NAME);
        Node steadyPoolSizeNode = attributes.getNamedItem(CONN_STEADY_POOL_SIZE);
        Node maxPoolSizeNode = attributes.getNamedItem(CONN_MAX_POOL_SIZE);
        Node poolResizeNode = attributes.getNamedItem(CONN_POOL_RESIZE_QUANTITY);
        Node idleTimeOutNode = attributes.getNamedItem(CONN_IDLE_TIME_OUT);
        Node failAllConnNode = attributes.getNamedItem(CONN_FAIL_ALL_CONNECTIONS);
        Node maxWaitTimeMillisNode = attributes.getNamedItem(MAX_WAIT_TIME_IN_MILLIS);
        Node transactionSupportNode = attributes.getNamedItem(CONN_TRANSACTION_SUPPORT);
        Node connValidationReqdNode = attributes.getNamedItem(IS_CONNECTION_VALIDATION_REQUIRED);
        Node validateAtmostOncePeriodNode = attributes.getNamedItem(VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS);
        Node connLeakTimeoutNode = attributes.getNamedItem(CONNECTION_LEAK_TIMEOUT_IN_SECONDS);
        Node connLeakReclaimNode = attributes.getNamedItem(CONNECTION_LEAK_RECLAIM);
        Node connCreationRetryAttemptsNode = attributes.getNamedItem(CONNECTION_CREATION_RETRY_ATTEMPTS);
        Node connCreationRetryIntervalNode = attributes.getNamedItem(CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS);
        Node lazyConnEnlistmentNode = attributes.getNamedItem(LAZY_CONNECTION_ENLISTMENT);
        Node lazyConnAssociationNode = attributes.getNamedItem(LAZY_CONNECTION_ASSOCIATION);
        Node associateWithThreadNode = attributes.getNamedItem(ASSOCIATE_WITH_THREAD);
        Node matchConnectionsNode = attributes.getNamedItem(MATCH_CONNECTIONS);
        Node maxConnUsageCountNode = attributes.getNamedItem(MAX_CONNECTION_USAGE_COUNT);
        Node poolingNode = attributes.getNamedItem(POOLING);
        Node pingNode = attributes.getNamedItem(PING);

        String poolName = null;

        org.glassfish.resources.api.Resource connectorConnPoolResource
            = new Resource(org.glassfish.resources.api.Resource.CONNECTOR_CONNECTION_POOL);
        if (nameNode != null) {
            poolName = getScopedName(nameNode.getNodeValue(), scope);
            connectorConnPoolResource.setAttribute(CONNECTION_POOL_NAME, poolName);
        }
        if (raConfigNode != null) {
            String raConfig = raConfigNode.getNodeValue();
            connectorConnPoolResource.setAttribute(RESOURCE_ADAPTER_CONFIG_NAME, raConfig);
        }
        if (conDefNode != null) {
            String conDef = conDefNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONN_DEF_NAME, conDef);
        }
        if (steadyPoolSizeNode != null) {
            String steadyPoolSize = steadyPoolSizeNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONN_STEADY_POOL_SIZE, steadyPoolSize);
        }
        if (maxPoolSizeNode != null) {
            String maxPoolSize = maxPoolSizeNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONN_MAX_POOL_SIZE, maxPoolSize);
        }
        if (poolResizeNode != null) {
            String poolResize = poolResizeNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONN_POOL_RESIZE_QUANTITY, poolResize);
        }
        if (idleTimeOutNode != null) {
            String idleTimeOut = idleTimeOutNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONN_IDLE_TIME_OUT, idleTimeOut);
        }
        if (failAllConnNode != null) {
            String failAllConn = failAllConnNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONN_FAIL_ALL_CONNECTIONS, failAllConn);
        }
        if (maxWaitTimeMillisNode != null) {
            String maxWaitTimeMillis = maxWaitTimeMillisNode.getNodeValue();
            connectorConnPoolResource.setAttribute(MAX_WAIT_TIME_IN_MILLIS, maxWaitTimeMillis);
        }
        if (transactionSupportNode != null) {
            String transactionSupport = transactionSupportNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONN_TRANSACTION_SUPPORT, transactionSupport);
        }
        if (connValidationReqdNode != null) {
            String connValidationReqd = connValidationReqdNode.getNodeValue();
            connectorConnPoolResource.setAttribute(IS_CONNECTION_VALIDATION_REQUIRED, connValidationReqd);
        }
        if (validateAtmostOncePeriodNode != null) {
            String validateAtmostOncePeriod = validateAtmostOncePeriodNode.getNodeValue();
            connectorConnPoolResource.setAttribute(VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS, validateAtmostOncePeriod);
        }
        if (connLeakTimeoutNode != null) {
            String connLeakTimeout = connLeakTimeoutNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONNECTION_LEAK_TIMEOUT_IN_SECONDS, connLeakTimeout);
        }
        if (connLeakReclaimNode != null) {
            String connLeakReclaim = connLeakReclaimNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONNECTION_LEAK_RECLAIM, connLeakReclaim);
        }
        if (connCreationRetryAttemptsNode != null) {
            String connCreationRetryAttempts = connCreationRetryAttemptsNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONNECTION_CREATION_RETRY_ATTEMPTS, connCreationRetryAttempts);
        }
        if (connCreationRetryIntervalNode != null) {
            String connCreationRetryInterval = connCreationRetryIntervalNode.getNodeValue();
            connectorConnPoolResource.setAttribute(CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS,
                connCreationRetryInterval);
        }
        if (lazyConnEnlistmentNode != null) {
            String lazyConnEnlistment = lazyConnEnlistmentNode.getNodeValue();
            connectorConnPoolResource.setAttribute(LAZY_CONNECTION_ENLISTMENT, lazyConnEnlistment);
        }
        if (lazyConnAssociationNode != null) {
            String lazyConnAssociation = lazyConnAssociationNode.getNodeValue();
            connectorConnPoolResource.setAttribute(LAZY_CONNECTION_ASSOCIATION, lazyConnAssociation);
        }
        if (associateWithThreadNode != null) {
            String associateWithThread = associateWithThreadNode.getNodeValue();
            connectorConnPoolResource.setAttribute(ASSOCIATE_WITH_THREAD, associateWithThread);
        }
        if (matchConnectionsNode != null) {
            String matchConnections = matchConnectionsNode.getNodeValue();
            connectorConnPoolResource.setAttribute(MATCH_CONNECTIONS, matchConnections);
        }
        if (maxConnUsageCountNode != null) {
            String maxConnUsageCount = maxConnUsageCountNode.getNodeValue();
            connectorConnPoolResource.setAttribute(MAX_CONNECTION_USAGE_COUNT, maxConnUsageCount);
        }
        if (poolingNode != null) {
            String pooling = poolingNode.getNodeValue();
            connectorConnPoolResource.setAttribute(POOLING, pooling);
        }
        if (pingNode != null) {
            String ping = pingNode.getNodeValue();
            connectorConnPoolResource.setAttribute(PING, ping);
        }

        NodeList children = nextKid.getChildNodes();
        // get description
        generatePropertyElement(connectorConnPoolResource, children);

        vResources.add(connectorConnPoolResource);
        resourceMap.put(connectorConnPoolResource, nextKid);
        // with the given poolname ..create security-map
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                if ((children.item(i).getNodeName().equals(SECURITY_MAP))) {
                    generateSecurityMap(poolName, children.item(i), scope);
                }
            }
        }
        LOG.log(DEBUG, "Connector pool resource jndiName {0} attributes: {1}", poolName,
            connectorConnPoolResource.getAttributes());
    }

    private void generateWorkSecurityMap(Node node, String scope) throws Exception {

        //ignore "scope" as work-security-map is not a bindable resource

        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null) {
            return;
        }

        Node nameNode = attributes.getNamedItem(WORK_SECURITY_MAP_NAME);
        Resource workSecurityMapResource = new Resource(org.glassfish.resources.api.Resource.CONNECTOR_WORK_SECURITY_MAP);
        final String name;
        if (nameNode == null) {
            name = null;
        } else {
            name = nameNode.getNodeValue();
            workSecurityMapResource.setAttribute(WORK_SECURITY_MAP_NAME, name);
        }

        Node raNameNode = attributes.getNamedItem(WORK_SECURITY_MAP_RA_NAME);
        if (raNameNode != null) {
            workSecurityMapResource.setAttribute(WORK_SECURITY_MAP_RA_NAME, raNameNode.getNodeValue());
        }

        NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String nodeName = child.getNodeName();
                if (nodeName.equals(WORK_SECURITY_MAP_GROUP_MAP)) {
                    Properties groupMaps = new Properties();
                    NamedNodeMap childAttributes = child.getAttributes();
                    if (childAttributes != null) {
                        Node eisGroup = childAttributes.getNamedItem(WORK_SECURITY_MAP_EIS_GROUP);
                        Node mappedGroup = childAttributes.getNamedItem(WORK_SECURITY_MAP_MAPPED_GROUP);
                        if (eisGroup != null && mappedGroup != null) {
                            String eisGroupValue = eisGroup.getNodeValue();
                            String serverGroupValue = mappedGroup.getNodeValue();
                            if (eisGroupValue != null && serverGroupValue != null) {
                                groupMaps.put(eisGroupValue, serverGroupValue);
                            }
                        }
                        workSecurityMapResource.setAttribute(WORK_SECURITY_MAP_GROUP_MAP, groupMaps);
                    }
                } else if (nodeName.equals(WORK_SECURITY_MAP_PRINCIPAL_MAP)) {
                    Properties principalMaps = new Properties();
                    NamedNodeMap childAttributes = child.getAttributes();
                    if (childAttributes != null) {
                        Node eisPrincipal = childAttributes.getNamedItem(WORK_SECURITY_MAP_EIS_PRINCIPAL);
                        Node mappedPrincipal = childAttributes.getNamedItem(WORK_SECURITY_MAP_MAPPED_PRINCIPAL);
                        if (eisPrincipal != null && mappedPrincipal != null) {
                            String eisPrincipalValue = eisPrincipal.getNodeValue();
                            String serverPrincipalValue = mappedPrincipal.getNodeValue();
                            if (eisPrincipalValue != null && serverPrincipalValue != null) {
                                principalMaps.put(eisPrincipalValue, serverPrincipalValue);
                            }
                        }
                        workSecurityMapResource.setAttribute(WORK_SECURITY_MAP_PRINCIPAL_MAP, principalMaps);
                    }
                }
            }
        }
        vResources.add(workSecurityMapResource);
        resourceMap.put(workSecurityMapResource, node);

        LOG.log(DEBUG, "Work Security resource name {0} attributes: {1}", name,
            workSecurityMapResource.getAttributes());
    }


    private void generateSecurityMap(String poolName, Node mapNode, String scope) throws Exception {
        //scope is not needed for security map.

        NamedNodeMap attributes = mapNode.getAttributes();
        if (attributes == null) {
            return;
        }
        Node nameNode = attributes.getNamedItem(SECURITY_MAP_NAME);

        Resource map = new Resource(org.glassfish.resources.api.Resource.CONNECTOR_SECURITY_MAP);
        if (nameNode != null) {
            String name = nameNode.getNodeValue();
            map.setAttribute(SECURITY_MAP_NAME, name);
        }
        if (poolName != null) {
            map.setAttribute(POOL_NAME, poolName);
        }

        StringBuilder principal = new StringBuilder();
        StringBuilder usergroup = new StringBuilder();

        NodeList children = mapNode.getChildNodes();

        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                Node gChild = children.item(i);
                String strNodeName = gChild.getNodeName();
                if (strNodeName.equals(SECURITY_MAP_PRINCIPAL)) {
                    String p = (gChild.getFirstChild()).getNodeValue();
                    principal.append(p).append(",");
                }
                if (strNodeName.equals(SECURITY_MAP_USER_GROUP)) {
                    String u = (gChild.getFirstChild()).getNodeValue();
                    usergroup.append(u).append(",");
                }
                if ((strNodeName.equals(SECURITY_MAP_BACKEND_PRINCIPAL))) {
                    NamedNodeMap attributes1 = (children.item(i)).getAttributes();
                    if (attributes1 != null) {
                        Node userNode = attributes1.getNamedItem(USER_NAME);
                        if (userNode != null) {
                            String userName = userNode.getNodeValue();
                            map.setAttribute(USER_NAME, userName);
                        }
                        Node passwordNode = attributes1.getNamedItem(PASSWORD);
                        if (passwordNode != null) {
                            String pwd = passwordNode.getNodeValue();
                            map.setAttribute(PASSWORD, pwd);
                        }
                    }
                }
            }
        }
        map.setAttribute(SECURITY_MAP_PRINCIPAL, convertToStringArray(principal.toString()));
        vResources.add(map);
        resourceMap.put(map, mapNode);
    }// end of generateSecurityMap....


    /**
     * Generate the Resource Adapter Config
     */
    private void generateResourceAdapterConfig(Node nextKid, String scope) throws Exception {
        // ignore "scope" as resource-adapter-config is not a bindable resource

        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null) {
            return;
        }

        Resource resAdapterConfigResource = new Resource(Resource.RESOURCE_ADAPTER_CONFIG);
        Node resAdapConfigNode = attributes.getNamedItem(RES_ADAPTER_CONFIG);
        if (resAdapConfigNode != null) {
            String resAdapConfig = resAdapConfigNode.getNodeValue();
            resAdapterConfigResource.setAttribute(RES_ADAPTER_CONFIG, resAdapConfig);
        }
        Node poolIdNode = attributes.getNamedItem(THREAD_POOL_IDS);
        if (poolIdNode != null) {
            String threadPoolId = poolIdNode.getNodeValue();
            resAdapterConfigResource.setAttribute(THREAD_POOL_IDS, threadPoolId);
        }
        Node resAdapNameNode = attributes.getNamedItem(RES_ADAPTER_NAME);
        if (resAdapNameNode != null) {
            String resAdapName = resAdapNameNode.getNodeValue();
            resAdapterConfigResource.setAttribute(RES_ADAPTER_NAME, resAdapName);
        }

        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(resAdapterConfigResource, children);
        vResources.add(resAdapterConfigResource);
        resourceMap.put(resAdapterConfigResource, nextKid);

        LOG.log(DEBUG, "Resource Adapter attributes: {0}", resAdapterConfigResource.getAttributes());
    }

    /**
     * Returns an Iterator of <code>Resource</code>objects in the order as defined
     * in the resources XML configuration file. Maintained for backward compat
     * purposes only.
     */
    public Iterator<org.glassfish.resources.api.Resource> getResources() {
        return vResources.iterator();
    }

    public List<org.glassfish.resources.api.Resource> getResourcesList() {
        return vResources;
    }

    /**
     * Returns an List of <code>Resource</code>objects that needs to be
     * created prior to module deployment. This includes all non-Connector
     * resources and resource-adapter-config
     * @param resources List of resources, from which the non connector
     * resources need to be obtained.
     * @param isResourceCreation indicates if this determination needs to be
     * done during the <code>PreResCreationPhase</code>. In the
     * <code>PreResCreationPhase</code>, RA config is added to the
     * non connector list, so that the RA config is created prior to the
     * RA deployment. For all other purpose, this flag needs to be set to false.
     */
    public static List<Resource> getNonConnectorResourcesList(List<org.glassfish.resources.api.Resource> resources,
        boolean isResourceCreation, boolean ignoreDuplicates) {
        return getResourcesOfType(resources, NONCONNECTOR, isResourceCreation, ignoreDuplicates);
    }


    /**
     *  Returns an Iterator of <code>Resource</code> objects that correspond to
     *  connector resources that needs to be created post module deployment. They
     *  are arranged in the order in which the resources needs to be created
     * @param resources List of resources, from which the non connector
     * resources need to be obtained.
     * @param isResourceCreation indicates if this determination needs to be
     * done during the <code>PreResCreationPhase</code>. In the
     * <code>PreResCreationPhase</code>, RA config is added to the
     * non connector list, so that the RA config is created prior to the
     * RA deployment. For all other purpose, this flag needs to be set to false.
     */

    public static List<Resource> getConnectorResourcesList(List<org.glassfish.resources.api.Resource> resources,
        boolean isResourceCreation, boolean ignoreDuplicates) {
        return getResourcesOfType(resources, CONNECTOR, isResourceCreation, ignoreDuplicates);
    }

    // Helper Method to convert a String type to a String[]
    private String[] convertToStringArray(Object sOptions){
        StringTokenizer optionTokenizer = new StringTokenizer((String) sOptions, ",");
        int size = optionTokenizer.countTokens();
        String[] sOptionsList = new String[size];
        for (int ii = 0; ii < size; ii++) {
            sOptionsList[ii] = optionTokenizer.nextToken();
        }
        return sOptionsList;
    }


    static final class AddResourcesErrorHandler implements ErrorHandler {

        @Override
        public void error(SAXParseException e) throws org.xml.sax.SAXException {
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws org.xml.sax.SAXException {
            throw e;
        }

        @Override
        public void warning(SAXParseException e) throws org.xml.sax.SAXException {
            throw e;
        }
    }


    private String getDtdFileName(String publicId, String systemId) {
        if (systemId != null) {
            for (String systemID2 : systemIDs) {
                if (systemId.contains(systemID2)) {
                    return systemID2;
                }
            }
        }
        if (publicId != null) {
            if (publicId.contains(publicID_sjsas90)) {
                return DTD_1_3;
            } else if (publicId.contains(publicID_ges30)) {
                return DTD_1_4;
            }
        }
        return DTD_1_5;
    }


    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        final String dtdFileName = getDtdFileName(publicId, systemId);
        try {
            String dtd = System.getProperty(INSTALL_ROOT.getSystemPropertyName()) +
                File.separator + "lib" + File.separator + "dtds" + File.separator +
                dtdFileName;
            LOG.log(TRACE, () -> "using DTD [ " + dtd + " ]");
            File f = new File(dtd);
            if (f.exists()) {
                LOG.log(TRACE, () -> "DTD [" + dtd + "] exists");
                return new InputSource(new java.io.FileInputStream(dtd));
            }
            LOG.log(TRACE, () -> "No DTD [" + dtd + "] found, trying the enclosing jar (uber jar)");
            //In case of embedded Uber jar, it is part of "/dtds" and all modules are in single jar.
            //TODO refactor and move this logic to embedded module.
            final URL url = getClass().getResource("/dtds/" + dtdFileName);
            final InputStream stream = url == null ? null : url.openStream();
            if (url == null || stream == null) {
                LOG.log(TRACE,
                    () -> "DTD [" + dtdFileName + " ] not found in installation, public URL might resolve it");
                return null;
            }
            LOG.log(TRACE, () -> "DTD [" + dtdFileName + "] found in enclosing jar");
            final InputSource is = new InputSource(stream);
            is.setSystemId(url.toString());
            return is;
        } catch (Exception e) {
            throw new SAXException("cannot resolve dtd", e);
        }
    }

    class MyLexicalHandler implements LexicalHandler{
        @Override
        public void startDTD(String name, String publicId, String systemId) throws SAXException {
            isDoctypePresent = true;
        }

        @Override
        public void endDTD() throws SAXException {
        }

        @Override
        public void startEntity(String name) throws SAXException {
        }

        @Override
        public void endEntity(String name) throws SAXException {
        }

        @Override
        public void startCDATA() throws SAXException {
        }

        @Override
        public void endCDATA() throws SAXException {
        }

        @Override
        public void comment(char[] ch, int start, int length) throws SAXException {
        }

    }
}
