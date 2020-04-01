/*
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

package com.sun.enterprise.connectors.deployment.annotation.handlers;

import com.sun.enterprise.deployment.annotation.context.RarBundleContext;
import com.sun.enterprise.deployment.annotation.handlers.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.LicenseDescriptor;
import com.sun.enterprise.deployment.AuthMechanism;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.resource.spi.Connector;
import jakarta.resource.spi.SecurityPermission;
import jakarta.resource.spi.AuthenticationMechanism;
import jakarta.resource.spi.TransactionSupport;
import jakarta.resource.spi.work.WorkContext;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;

import org.glassfish.apf.*;
import org.glassfish.apf.impl.HandlerProcessingResultImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Jagadish Ramu
 */
@Service
@AnnotationHandlerFor(Connector.class)
public class ConnectorAnnotationHandler extends AbstractHandler {

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ConnectorAnnotationHandler.class);

    public HandlerProcessingResult processAnnotation(AnnotationInfo element) throws AnnotationProcessorException {
        AnnotatedElementHandler aeHandler = element.getProcessingContext().getHandler();
        Connector connector = (Connector) element.getAnnotation();

        List<Class<? extends Annotation>> list = new ArrayList<Class<? extends Annotation>>();
        list.add(getAnnotationType());
/*
        list.add(SecurityPermission.class);
        list.add(AuthenticationMechanism.class);
*/

        if (aeHandler instanceof RarBundleContext) {
            RarBundleContext rarContext = (RarBundleContext) aeHandler;
            ConnectorDescriptor desc = rarContext.getDescriptor();
            Class annotatedClass = (Class)element.getAnnotatedElement();
            if(desc.getResourceAdapterClass().equals("")){
                desc.addConnectorAnnotation(element);
                return getSuccessfulProcessedResult(list);
            }else if(!isResourceAdapterClass(annotatedClass)){
                desc.addConnectorAnnotation(element);
                return getSuccessfulProcessedResult(list);
            }else if(!desc.getResourceAdapterClass().equals(annotatedClass.getName())){
                desc.addConnectorAnnotation(element);
                return getSuccessfulProcessedResult(list);
            }else{
                processDescriptor(annotatedClass, connector, desc);
                desc.setValidConnectorAnnotationProcessed(true);
            }
        } else {
            String logMessage = "Not a rar bundle context";
            return getFailureResult(element, logMessage, true);
        }
        return getSuccessfulProcessedResult(list);
    }

    public static void processDescriptor(Class annotatedClass, Connector connector, ConnectorDescriptor desc) {
        //TODO don't use deprecated methods

        //TODO For *all annotations* need to ignore "default" or unspecified attributes
        //TODO make sure that the annotation defined defaults are the defaults of DD and DOL


        if (desc.getDescription().equals("") && connector.description().length > 0) {
            desc.setDescription(convertStringArrayToStringBuffer(connector.description()));
        }

        if (desc.getDisplayName().equals("") && connector.displayName().length > 0) {
            desc.setDisplayName(convertStringArrayToStringBuffer(connector.displayName()));
        }

        if ((desc.getSmallIconUri() == null || desc.getSmallIconUri().equals(""))
                && connector.smallIcon().length > 0) {
            desc.setSmallIconUri(convertStringArrayToStringBuffer(connector.smallIcon()));
        }

        if ((desc.getLargeIconUri() == null || desc.getLargeIconUri().equals(""))
                && connector.largeIcon().length > 0) {
            desc.setLargeIconUri(convertStringArrayToStringBuffer(connector.largeIcon()));
        }

        if (desc.getVendorName().equals("") && !connector.vendorName().equals("")) {
            desc.setVendorName(connector.vendorName());
        }

        if (desc.getEisType().equals("") && !connector.eisType().equals("")) {
            desc.setEisType(connector.eisType());
        }

        if (desc.getVersion().equals("") && !connector.version().equals("")) {
            desc.setVersion(connector.version());
        }

/*
        if(!desc.isModuleNameSet() && !connector.moduleName().equals("")){
            desc.getModuleDescriptor().setModuleName(connector.moduleName());
        }
*/

        if (desc.getLicenseDescriptor() == null) {
            // We will be able to detect whether license description is specified in annotation
            // or not, but "license required" can't be detected. Hence taking the annotated values *always*
            // if DD does not have an equivalent
            String[] licenseDescriptor = connector.licenseDescription();
            boolean licenseRequired = connector.licenseRequired();
            LicenseDescriptor ld = new LicenseDescriptor();
            ld.setDescription(convertStringArrayToStringBuffer(licenseDescriptor));
            ld.setLicenseRequired(licenseRequired);
            desc.setLicenseDescriptor(ld);
        }

        AuthenticationMechanism[] auths = connector.authMechanisms();
        if (auths != null && auths.length > 0) {
            for (AuthenticationMechanism auth : auths) {
                String authMechString = auth.authMechanism();
                int authMechInt = AuthMechanism.getAuthMechInt(authMechString);

                // check whether the same auth-mechanism is defined in DD also,
                // possible change could be with auth-mechanism's credential-interface for a particular
                // auth-mechanism-type
                boolean ignore = false;
                OutboundResourceAdapter ora = getOutbound(desc);
                Set ddAuthMechanisms = ora.getAuthMechanisms();

                for (Object o : ddAuthMechanisms) {
                    AuthMechanism ddAuthMechanism = (AuthMechanism) o;
                    if (ddAuthMechanism.getAuthMechType().equals(auth.authMechanism())) {
                        ignore = true;
                        break;
                    }
                }

                // if it was not specified in DD, add it to connector-descriptor
                if (!ignore) {
                    String credentialInterfaceName = ora.getCredentialInterfaceName(auth.credentialInterface());
                    //XXX: Siva: For now use the first provided description
                    String description = "";
                    if(auth.description().length > 0){
                        description = auth.description()[0];
                    }
                    AuthMechanism authM = new AuthMechanism(description, authMechInt, credentialInterfaceName);
                    ora.addAuthMechanism(authM);
                }
            }
        }

        // merge DD and annotation entries of security-permission
        SecurityPermission[] perms = connector.securityPermissions();
        if (perms != null && perms.length > 0) {
            for (SecurityPermission perm : perms) {
                boolean ignore = false;
                // check whether the same permission is defined in DD also,
                // though it does not make any functionality difference except possible
                // "Description" change
                Set ddSecurityPermissions = desc.getSecurityPermissions();
                for (Object o : ddSecurityPermissions) {
                    com.sun.enterprise.deployment.SecurityPermission ddSecurityPermission =
                            (com.sun.enterprise.deployment.SecurityPermission) o;
                    if (ddSecurityPermission.getPermission().equals(perm.permissionSpec())) {
                        ignore = true;
                        break;
                    }
                }

                // if it was not specified in DD, add it to connector-descriptor
                if (!ignore) {
                    com.sun.enterprise.deployment.SecurityPermission sp =
                            new com.sun.enterprise.deployment.SecurityPermission();
                    sp.setPermission(perm.permissionSpec());
                    //XXX: Siva for now use the first provided Description
                    String firstDesc = "";
                    if(perm.description().length > 0) firstDesc = perm.description()[0];
                    sp.setDescription(firstDesc);
                    desc.addSecurityPermission(sp);
                }
            }
        }

        //we should not create outbound resource adapter unless it is required.
        //this is necessary as the default value processing in the annotation may
        //result in outbound to be defined without any connection-definition which is an issue.

        //if reauth is false, we can ignore it as default value in dol is also false.
        if(connector.reauthenticationSupport()){
            OutboundResourceAdapter ora = getOutbound(desc);
            if(!ora.isReauthenticationSupportSet()){
                ora.setReauthenticationSupport(connector.reauthenticationSupport());
            }
        }
        //if transaction-support is no-transaction, we can ignore it as default value in dol is also no-transaction.
        if(!connector.transactionSupport().equals(TransactionSupport.TransactionSupportLevel.NoTransaction)){
            OutboundResourceAdapter ora = getOutbound(desc);
            if(!ora.isTransactionSupportSet()){
                ora.setTransactionSupport(connector.transactionSupport().toString());
            }
        }

        //merge the DD & annotation specified values of required-inflow-contexts
        //merge involves simple union of class-names of inflow-contexts of DD and annotation

        //due to the above approach, its not possible to switch off one of the required-inflow-contexts ?

        //TODO need to check support and throw exception ?

        Class<? extends WorkContext>[] requiredInflowContexts = connector.requiredWorkContexts();
        if (requiredInflowContexts != null) {
            for (Class<? extends WorkContext> ic : requiredInflowContexts) {
                desc.addRequiredWorkContext(ic.getName());
            }
        }

        if (desc.getResourceAdapterClass().equals("")) {
            if (isResourceAdapterClass(annotatedClass)) {
                desc.setResourceAdapterClass(annotatedClass.getName());
            }
        }
    }

    public static boolean isResourceAdapterClass(Class claz){
        return jakarta.resource.spi.ResourceAdapter.class.isAssignableFrom(claz);
    }

    public static String convertStringArrayToStringBuffer(String[] stringArray) {
        StringBuffer result = new StringBuffer();
        if (stringArray != null) {
            for (String string : stringArray) {
                result.append(string);
            }
        }
        return result.toString();
    }

    /**
     * @return a default processed result
     */
    protected HandlerProcessingResult getSuccessfulProcessedResult(List<Class<? extends Annotation>> annotationTypes) {

        HandlerProcessingResultImpl result = new HandlerProcessingResultImpl();

        for(Class<? extends Annotation> annotation : annotationTypes){
            result.addResult(annotation, ResultType.PROCESSED);
        }
        return result;
    }


    public Class<? extends Annotation>[] getTypeDependencies() {
        return null;
    }

    public static OutboundResourceAdapter getOutbound(ConnectorDescriptor desc) {
        if (!desc.getOutBoundDefined()) {
            desc.setOutboundResourceAdapter(new OutboundResourceAdapter());
        }
        return desc.getOutboundResourceAdapter();
    }

    private HandlerProcessingResultImpl getFailureResult(AnnotationInfo element, String message, boolean doLog) {
        HandlerProcessingResultImpl result = new HandlerProcessingResultImpl();
        result.addResult(getAnnotationType(), ResultType.FAILED);
        if (doLog) {
            Class c = (Class) element.getAnnotatedElement();
            String className = c.getName();
            Object args[] = new Object[]{
                element.getAnnotation(),
                className,
                message,
            };
            String localString = localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.connectorannotationfailure",
                    "failed to handle annotation [ {0} ] on class [ {1} ], reason : {2}", args);
            logger.log(Level.WARNING, localString);
        }
        return result;
    }
}
