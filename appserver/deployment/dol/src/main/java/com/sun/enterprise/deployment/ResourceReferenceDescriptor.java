/*
 * Copyright (c) 2022 Contributors to Eclipse Foundation. All rights reserved.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.web.ResourceReference;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.glassfish.deployment.common.Descriptor;

/**
 * This descriptor represents a dependency on a resource.
 * @author Danny Coward
 */
public class ResourceReferenceDescriptor extends EnvironmentProperty implements NamedDescriptor, ResourceReference {

    private static final long serialVersionUID = 1L;

    /**
     * For database resources, this says the application will log in.
     */
    public static final String APPLICATION_AUTHORIZATION = "Application";
    /**
     * For database resources this says the container will log in.
     */
    public static final String CONTAINER_AUTHORIZATION = "Container";

    // res-sharing-scope values
    public static final String RESOURCE_SHAREABLE = "Shareable";
    public static final String RESOURCE_UNSHAREABLE = "Unshareable";

    private static final String URL_RESOURCE_TYPE = "java.net.URL";
    private static final String CONNECTOR_RESOURCE_TYPE = "jakarta.resource.cci.ConnectionFactory";
    private static final String MAIL_RESOURCE_TYPE = "jakarta.mail.Session";
    private static final String JDBC_RESOURCE_TYPE = "javax.sql.DataSource";
    private static final String ORB_RESOURCE_TYPE = "org.omg.CORBA.ORB";
    private static final String WEBSERVICE_CONTEXT_TYPE = "jakarta.xml.ws.WebServiceContext";

    private String type;

    private ResourcePrincipalDescriptor resourcePrincipalDescriptor;

    // FIXME MailConfiguration is saved and returned, but no one ever seems
    // to use the value that's saved. Should probably just remove this.
    private MailConfiguration mailConfiguration;

    private String authorization;
    private DataSource dataSource;
    private String sharingScope;

    private List<NameValuePairDescriptor> runtimeProps;

    // for cmp-resource type
    boolean createTablesAtDeploy;
    boolean dropTablesAtUndeploy;
    private String databaseVendorName;
    private Properties schemaGeneratorProperties;

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(ResourceReferenceDescriptor.class);

    /**
     * Construct a resource reference with the given name, description and type.
     * @param name the name of the reference
     * @param description  description
     * @param type the type of the resource reference.
     */
    public ResourceReferenceDescriptor(String name, String description, String type) {
        super(name, "", description);
        this.type = type;
    }

    /**
     * Default constructor.
     */
    public ResourceReferenceDescriptor() {
    }


    /**
     * Return the JNDI name of this resource reference.
     *
     * @return the JNDI name of the resource reference.
     */
    @Override
    public String getJndiName() {
        String jndiName = super.getValue();
        if (!jndiName.isEmpty()) {
            return jndiName;
        }
        if (mappedName != null && !mappedName.isEmpty()) {
            return mappedName;
        }
        return lookupName;
    }


    /**
     * Set the JNDI name of this resource reference.
     *
     * @param jndiName JNDI name of the resource reference.
     */
    @Override
    public void setJndiName(String jndiName) {
        super.setValue(jndiName);
    }


    @Override
    public String getInjectResourceType() {
        return type;
    }


    @Override
    public void setInjectResourceType(String resourceType) {
        type = resourceType;
    }


    /**
     * Has the sharing scope been set?
     *
     * @return true if the sharing scope has been set
     */
    public boolean hasSharingScope() {
        return this.sharingScope != null;
    }


    /**
     * Return the res-sharing-scope of this resource reference.
     *
     * @return the sharing scope.
     */
    public String getSharingScope() {
        if (sharingScope == null) {
            return RESOURCE_SHAREABLE;
        }
        return sharingScope;
    }


    /**
     * Set the res-sharing-scope of this resource reference.
     *
     * @param sharingScope the sharing scope.
     */
    public void setSharingScope(String sharingScope) {
        this.sharingScope = sharingScope;
    }


    /**
     * Does this resource references have a JNDI name.
     *
     * @return true if the resource reference has a JNDI name, false otherwise
     */
    public boolean isResolved() {
        return true;
    }


    /**
     * Has the authorization type been set?
     *
     * @return true if the authorization type has been set
     */
    public boolean hasAuthorization() {
        return this.authorization != null;
    }


    /**
     * Return true of this resource reference is expecting the container
     * to authorize the resource.
     *
     * @return true if authorization is container managed.
     */
    public boolean isContainerAuthorization() {
        return this.getAuthorization().equals(CONTAINER_AUTHORIZATION);
    }


    /**
     * Return the authorization type of this resource. The default value
     * is APPLICATION_AUTHORIZATION
     *
     * @return the authorization type of the resource.
     */
    @Override
    public String getAuthorization() {
        if (this.authorization == null) {
            this.authorization = APPLICATION_AUTHORIZATION;
        }
        return this.authorization;
    }


    /**
     * Sets the authorization type of this resource.
     *
     * @param authorization the authorization type.
     */
    @Override
    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }


    /**
     * Return the type of the resource.
     *
     * @return the type of the resource.
     */
    @Override
    public String getType() {
        return type;
    }


    /**
     * Sets the type of this resource.
     *
     * @param type the type of the resource.
     */
    @Override
    public void setType(String type) {
        this.type = type;
    }


    /**
     * Lookup the datasource from the namespace based on the JNDI name.
     *
     * @return the data source
     */
    public DataSource getJDBCDataSource() {
        if (dataSource == null) {
            try {
                // Get JDBC DataSource for database
                InitialContext ctx = new InitialContext();
                // cache the datasource to avoid JNDI lookup overheads
                dataSource = (DataSource) ctx.lookup(getJndiName());
            } catch (Exception ex) {
            }
        }
        return dataSource;
    }


    public boolean isWebServiceContext() {
        return this.getType().equals(WEBSERVICE_CONTEXT_TYPE);
    }


    public boolean isORB() {
        return this.getType().equals(ORB_RESOURCE_TYPE);
    }


    /**
     * Return true if this resource is to a Jakarta Mail session object.
     *
     * @return true if the resource is a Jakarta Mail session object.
     */
    public boolean isMailResource() {
        return this.getType().equals(MAIL_RESOURCE_TYPE);
    }


    /**
     * @return true if the resource is a jdbc DataSource object.
     */
    public boolean isJDBCResource() {
        return this.getType().equals(JDBC_RESOURCE_TYPE);
    }


    /**
     * Return true if this resource is a URL object.
     *
     * @return true if the resource is a URL object, false otherwise.
     */
    public boolean isURLResource() {
        return this.getType() != null && this.getType().equals(URL_RESOURCE_TYPE);
    }


    /**
     * Return true if this resource is a JMS connection factory.
     *
     * @return true if the resource is a JMS connection factory, false otherwise.
     */
    public boolean isJMSConnectionFactory() {
        String myType = this.getType();
        return myType.equals("jakarta.jms.QueueConnectionFactory")
            || myType.equals("jakarta.jms.TopicConnectionFactory");
    }


    /**
     * Return the identity used to authorize this resource.
     *
     * @return the principal.
     */
    public ResourcePrincipalDescriptor getResourcePrincipal() {
        return this.resourcePrincipalDescriptor;
    }


    /**
     * Sets the identity used to authorize this resource.
     *
     * @param resourcePrincipalDescriptor the principal.
     */
    public void setResourcePrincipal(ResourcePrincipalDescriptor resourcePrincipalDescriptor) {
        this.resourcePrincipalDescriptor = resourcePrincipalDescriptor;
    }


    /**
     * Sets the mail configuration information for this reference.
     *
     * @param mailConfiguration the mail configuration object.
     */
    public void setMailConfiguration(MailConfiguration mailConfiguration) {
        this.mailConfiguration = mailConfiguration;
    }


    /**
     * Add a new runtime property to this cmp resource
     */
    public void addProperty(NameValuePairDescriptor newProp) {
        if (runtimeProps == null) {
            runtimeProps = new ArrayList<>();
        }
        runtimeProps.add(newProp);
    }


    /**
     * @return the runtime properties for this cmp resource
     */
    public Iterator<NameValuePairDescriptor> getProperties() {
        if (runtimeProps == null) {
            return null;
        }
        return runtimeProps.iterator();
    }


    /**
     * Return the mail configuration details of thsi resource or null.
     *
     * @return the mail configuration object.
     */
    public MailConfiguration getMailConfiguration() {
        return this.mailConfiguration;
    }


    /**
     * @return true if automatic creation of tables for the CMP Beans is
     *         done at deployment time
     */
    public boolean isCreateTablesAtDeploy() {
        return createTablesAtDeploy;
    }


    /**
     * Sets whether if automatic creation of tables for the CMP Beans is
     * done at deployment time
     */
    public void setCreateTablesAtDeploy(boolean createTablesAtDeploy) {
        this.createTablesAtDeploy = createTablesAtDeploy;
    }


    /**
     * @return true if automatic creation of tables for the CMP Beans is
     *         done at deployment time
     */
    public boolean isDropTablesAtUndeploy() {
        return dropTablesAtUndeploy;
    }


    /**
     * Sets whether if automatic creation of tables for the CMP Beans is
     * done at deployment time
     */
    public void setDropTablesAtUndeploy(boolean dropTablesAtUndeploy) {
        this.dropTablesAtUndeploy = dropTablesAtUndeploy;
    }


    /**
     * @return the database vendor name
     */
    public String getDatabaseVendorName() {
        return databaseVendorName;
    }


    /**
     * Sets the database vendor name
     */
    public void setDatabaseVendorName(String vendorName) {
        this.databaseVendorName = vendorName;
    }


    /**
     * @return the override properties for the schema generation
     */
    public Properties getSchemaGeneratorProperties() {
        return schemaGeneratorProperties;
    }


    /**
     * Sets the override properties for the schema generation
     */
    public void setSchemaGeneratorProperties(Properties props) {
        this.schemaGeneratorProperties = props;
    }


    /**
     * Equality on name.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof ResourceReference) {
            ResourceReference resourceReference = (ResourceReference) object;
            return resourceReference.getName().equals(this.getName());
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }


    /**
     * Returns a formatted string representing my state.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        StringBuffer sb = toStringBuffer;
        sb.append("Res-Ref-Env-Property: ");
        sb.append(super.getName());
        sb.append("@");
        sb.append(getType());
        sb.append("@");
        sb.append(getDescription());
        if (this.isResolved()) {
            sb.append(" resolved as: jndi: ");
            sb.append(getJndiName());
            sb.append("@res principal: ");
            sb.append(getResourcePrincipal());
            sb.append("@mail: ");
            sb.append(getMailConfiguration());
        }
        if (runtimeProps != null) {
            for (Object runtimeProp : runtimeProps) {
                sb.append("\nPropery : ");
                sb.append(runtimeProp);
            }
        } else {
            sb.append("\nNo Runtime properties");
        }
        sb.append("\nDatabase Vendor : " + databaseVendorName);
        sb.append("\nCreate Tables at Deploy : " + createTablesAtDeploy);
        sb.append("\nDelete Tables at Undeploy : " + dropTablesAtUndeploy);

        if (schemaGeneratorProperties != null) {
            sb.append("\nSchema Generator Properties : ");
            sb.append(schemaGeneratorProperties);
        }
    }


    /**
     * Return true if this resource is a CCI connection factory.
     * @return true if the resource is a CCI connection factory, false
     * otherwise.
     */
    public boolean isResourceConnectionFactory () {
        return this.getType().equals(CONNECTOR_RESOURCE_TYPE);
    }


    /**
     * Checks the given class type.
     *
     * @throws IllegalArgumentException if the class of type "type" does not exist
     */
    public void checkType() {
        if (type == null) {
            if (Descriptor.isBoundsChecking()) {
                throw new IllegalArgumentException(
                    I18N.getLocalString("enterprise.deployment.exceptiontypenotallowedpropertytype",
                        "{0} is not an allowed property value type", new Object[] {"null"}));
            }
        } else {
            // is it loadable ?
            try {
                // Bug fix 4850684: for resource-refs that are user-defined classes,
                // the classloader used to load them cannot be the one associated
                // with the application deployed, since the classloader instance
                // would have no idea about classes not included in app
                // for e.g connector module with res-ref that points to the
                // ConnectionFactory class of a resource adapter
                Class.forName(type, true, Thread.currentThread().getContextClassLoader());
            } catch (Throwable t) {
                if (Descriptor.isBoundsChecking()) {
                    throw new IllegalArgumentException(
                        I18N.getLocalString("enterprise.deployment.exceptiontypenotallowedpropertytype",
                            "{0} is not an allowed property value type", new Object[] {type}));
                }
            }
        }
    }


    public boolean isConflict(ResourceReferenceDescriptor other) {
        return getName().equals(other.getName())
            && (!DOLUtils.equals(getType(), other.getType()) || !getAuthorization().equals(other.getAuthorization())
                || !getSharingScope().equals(other.getSharingScope()) || isConflictResourceGroup(other));
    }
}
