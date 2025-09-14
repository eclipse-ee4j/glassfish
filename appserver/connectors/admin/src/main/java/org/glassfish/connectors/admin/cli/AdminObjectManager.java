/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.connectors.admin.cli;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.resource.ResourceException;

import java.beans.PropertyVetoException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.I18n;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.connectors.config.AdminObjectResource;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.admin.cli.ResourceManager;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static com.sun.enterprise.config.serverbeans.ServerTags.DESCRIPTION;
import static org.glassfish.resourcebase.resources.api.ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER;
import static org.glassfish.resources.admin.cli.ResourceConstants.ADMIN_OBJECT_CLASS_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_ADAPTER;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_TYPE;

/**
 *
 * @author Jennifer Chou
 */
@Service (name=ServerTags.ADMIN_OBJECT_RESOURCE)
@PerLookup
@I18n("create.admin.object")
public class AdminObjectManager implements ResourceManager {

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(AdminObjectManager.class);

    @Inject
    private Applications applications;

    @Inject
    private ConnectorRuntime connectorRuntime;

    @Inject
    private org.glassfish.resourcebase.resources.admin.cli.ResourceUtil resourceUtil;

    @Inject
    private org.glassfish.resourcebase.resources.util.BindableResourcesHelper resourcesHelper;

    @Inject
    private ServerEnvironment environment;

    private String resType;
    private String className;
    private String raName;
    private String enabled = Boolean.TRUE.toString();
    private String enabledValueForTarget = Boolean.TRUE.toString();
    private String jndiName;
    private String description;

    @Override
    public String getResourceType() {
        return ServerTags.ADMIN_OBJECT_RESOURCE;
    }

    @Override
    public ResourceStatus create(Resources resources, ResourceAttributes attributes, final Properties properties,
        String target) throws Exception {
        setAttributes(attributes, target);

        ResourceStatus validationStatus = isValid(resources, true, target);
        if(validationStatus.getStatus() == ResourceStatus.FAILURE){
            return validationStatus;
        }

        try {
            SingleConfigCode<Resources> configCode = param -> createResource(param, properties);
            ConfigSupport.apply(configCode, resources);
            if (!CommandTarget.TARGET_DOMAIN.equals(target)) {
                resourceUtil.createResourceRef(jndiName, enabledValueForTarget, target);
            }
        } catch (TransactionFailure tfe) {
            Logger.getLogger(AdminObjectManager.class.getName()).log(Level.SEVERE,
                    "Unabled to create administered object", tfe);
            String msg = I18N.getLocalString("create.admin.object.fail",
                    "Unable to create administered object {0}.", jndiName) +
                    " " + tfe.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        String msg = I18N.getLocalString(
                "create.admin.object.success",
                "Administered object {0} created.", jndiName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);

    }

    private ResourceStatus isValid(Resources resources, boolean validateResourceRef, String target){
        ResourceStatus status ;
        if (jndiName == null) {
            String msg = I18N.getLocalString("create.admin.object.noJndiName",
                            "No JNDI name defined for administered object.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        status = resourcesHelper.validateBindableResourceForDuplicates(resources, jndiName, validateResourceRef,
                target, AdminObjectResource.class);
        if(status.getStatus() == ResourceStatus.FAILURE){
            return status;
        }

        //no need to validate in remote instance as the validation would have happened in DAS.
        if(environment.isDas()){
            status = isValidRAName();
            if (status.getStatus() == ResourceStatus.FAILURE) {
                return status;
            }

            status = isValidAdminObject();
            if (status.getStatus() == ResourceStatus.FAILURE) {
                return status;
            }
        }
        return status;
    }

    private AdminObjectResource createResource(Resources param, Properties props) throws PropertyVetoException,
            TransactionFailure  {
        AdminObjectResource newResource = createConfigBean(param, props);
        param.getResources().add(newResource);
        return newResource;
    }

    private AdminObjectResource createConfigBean(Resources param, Properties props) throws PropertyVetoException,
            TransactionFailure {
        AdminObjectResource newResource = param.createChild(AdminObjectResource.class);
        newResource.setJndiName(jndiName);
        if (description != null) {
            newResource.setDescription(description);
        }
        newResource.setResAdapter(raName);
        newResource.setResType(resType);
        newResource.setClassName(className);
        newResource.setEnabled(enabled);
        if (props != null) {
            for (String propertyName : props.stringPropertyNames()) {
                Property prop = newResource.createChild(Property.class);
                prop.setName(propertyName);
                prop.setValue(props.getProperty(propertyName));
                newResource.getProperty().add(prop);
            }
        }
        return newResource;
    }

    private void setAttributes(ResourceAttributes attributes, String target) {
        resType = attributes.getString(RES_TYPE);
        className = attributes.getString(ADMIN_OBJECT_CLASS_NAME);
        if (target == null) {
            enabled = attributes.getString(ENABLED);
        } else {
            enabled = resourceUtil.computeEnabledValueForResourceBasedOnTarget(attributes.getString(ENABLED), target);
        }
        enabledValueForTarget = attributes.getString(ENABLED);
        jndiName = attributes.getString(JNDI_NAME);
        description = attributes.getString(DESCRIPTION);
        raName = attributes.getString(RES_ADAPTER);
    }

     //TODO Error checking taken from v2, need to refactor for v3
    private ResourceStatus isValidAdminObject() {
        // Check if the restype is valid -
        // To check this, we need to get the list of admin-object-interface
        // names and then find out if this list contains the restype.
        //boolean isValidAdminObject = true;
         boolean isValidAdminObject = false;

         //if classname is null, check whether the resType is present and only one adminobject must
         //be using that resType
         if (className == null) {

             String[] resTypes;
             try {
                 resTypes = connectorRuntime.getAdminObjectInterfaceNames(raName);
             } catch (ConnectorRuntimeException cre) {
                 Logger.getLogger(AdminObjectManager.class.getName()).log(Level.SEVERE,
                         "Could not find admin-ojbect-interface names (resTypes) from ConnectorRuntime for resource adapter.", cre);
                 String msg = I18N.getLocalString(
                         "admin.mbeans.rmb.null_ao_intf",
                         "Resource Adapter {0} does not contain any resource type for admin-object. Please specify another res-adapter.",
                         raName) + " " + cre.getLocalizedMessage();
                 return new ResourceStatus(ResourceStatus.FAILURE, msg);
             }
             if (resTypes == null || resTypes.length <= 0) {
                 String msg = I18N.getLocalString("admin.mbeans.rmb.null_ao_intf",
                         "Resource Adapter {0} does not contain any resource type for admin-object. Please specify another res-adapter.", raName);
                 return new ResourceStatus(ResourceStatus.FAILURE, msg);
             }

             int count = 0;
             for (String resType2 : resTypes) {
                 if (resType2.equals(resType)) {
                     isValidAdminObject = true;
                     count++;
                 }
             }
             if(count > 1){
                 String msg = I18N.getLocalString(
                         "admin.mbeans.rmb.multiple_admin_objects.found.for.restype",
                         "Need to specify admin-object classname parameter (--classname) as multiple admin objects " +
                                 "use this resType [ {0} ]",  resType);

                 return new ResourceStatus(ResourceStatus.FAILURE, msg);
             }
         }else{
             try{
                isValidAdminObject = connectorRuntime.hasAdminObject(raName, resType, className);
             } catch (ConnectorRuntimeException cre) {
                 Logger.getLogger(AdminObjectManager.class.getName()).log(Level.SEVERE,
                         "Could not find admin-object-interface names (resTypes) and admin-object-classnames from " +
                                 "ConnectorRuntime for resource adapter.", cre);
                 String msg = I18N.getLocalString(
                         "admin.mbeans.rmb.ao_intf_impl_check_failed",
                         "Could not determine admin object resource information of Resource Adapter [ {0} ] for " +
                                 "resType [ {1} ] and classname [ {2} ] ",
                         raName, resType, className) + " " + cre.getLocalizedMessage();
                 return new ResourceStatus(ResourceStatus.FAILURE, msg);
             }
         }

         if (!isValidAdminObject) {
            String msg = I18N.getLocalString("admin.mbeans.rmb.invalid_res_type",
                "Invalid Resource Type: {0}", resType);
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        return new ResourceStatus(ResourceStatus.SUCCESS, "");
    }

    private ResourceStatus isValidRAName() {
        //TODO turn on validation.  For now, turn validation off until connector modules ready
        //boolean retVal = false;
        ResourceStatus status = new ResourceStatus(ResourceStatus.SUCCESS, "");

        if (raName == null || raName.isEmpty()) {
            String msg = I18N.getLocalString("admin.mbeans.rmb.null_res_adapter",
                    "Resource Adapter Name is null.");
            status = new ResourceStatus(ResourceStatus.FAILURE, msg);
        } else {
            // To check for embedded connector module
            // System RA, so don't validate
            if (!ConnectorsUtil.getNonJdbcSystemRars().contains(raName)){
                // Check if the raName contains double underscore or hash.
                // If that is the case then this is the case of an embedded rar,
                // hence look for the application which embeds this rar,
                // otherwise look for the webconnector module with this raName.

                int indx = raName.indexOf(EMBEDDEDRAR_NAME_DELIMITER);
                if (indx != -1) {
                    String appName = raName.substring(0, indx);
                    Application app = applications.getModule(Application.class, appName);
                    if (app == null) {
                        String msg = I18N.getLocalString("admin.mbeans.rmb.invalid_ra_app_not_found",
                                "Invalid raname. Application with name {0} not found.", appName);
                        status = new ResourceStatus(ResourceStatus.FAILURE, msg);
                    }
                } else {
                    Application app = applications.getModule(Application.class, raName);
                    if (app == null) {
                        String msg = I18N.getLocalString("admin.mbeans.rmb.invalid_ra_cm_not_found",
                                "Invalid raname. Connector Module with name {0} not found.", raName);
                        status = new ResourceStatus(ResourceStatus.FAILURE, msg);
                    }
                }
            }
        }

        return status;
    }
    @Override
    public Resource createConfigBean(Resources resources, ResourceAttributes attributes, Properties properties,
        boolean validate) throws Exception {
        setAttributes(attributes, null);
        final ResourceStatus status;
        if (validate) {
            status = isValid(resources, false, null);
        } else {
            status = new ResourceStatus(ResourceStatus.SUCCESS, "");
        }
        if (status.getStatus() == ResourceStatus.SUCCESS) {
            return createConfigBean(resources, properties);
        }
        throw new ResourceException(status.getMessage(), status.getException());
    }
}
