/*
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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.UserDataConstraint;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentException;


import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * Allows validation of module content that might involve actually
 * reading the bytes themselves from the module.  Called after
 * descriptor has been loaded but before module-specific archivist
 * is closed.
 *
 * @author Kenneth Saks
 */
@Service
@PerLookup
public class ModuleContentValidator extends ModuleContentLinker implements ComponentPostVisitor {

    private ReadableArchive archive_;

    // resources...
    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(ModuleContentValidator.class);

    public ModuleContentValidator() {
    }

    public void setArchive(ReadableArchive archive) {
        archive_ = archive;
    }

    public void accept(ServiceReferenceDescriptor serviceRef) {
        if( serviceRef.hasWsdlFile() ) {
            String wsdlFileUri = serviceRef.getWsdlFileUri();
            // For JAXWS based clients, the user need not package WSDL and not refer to it
            // in @WebServiceRef; in this case we rely on @WebClient in generated files to get
            // wsdl location; this wsdl location can be URL or absolute path; in these cases
            // ensure that the file is present and return
            URL url = null;
            try {
                url = new URL(wsdlFileUri);
            } catch(java.net.MalformedURLException e) {
                // don't care, will eventuall fail below
            }
            if (url!=null) {
                if (url.getProtocol().equals("http") || url.getProtocol().equals("https"))
                    return;
            }
            File tmpFile = new File(wsdlFileUri);
            if(tmpFile.isAbsolute() && tmpFile.exists()) {
                return;
            }
            try {
                InputStream wsdlFileInputStream =
                    archive_.getEntry(wsdlFileUri);
                if( wsdlFileInputStream != null ) {
                    wsdlFileInputStream.close();
                } else {
                    String msg = localStrings.getLocalString(
                   "enterprise.deployment.util.wsdlfilenotfound",
                           "wsdl file {0} does not exist for service-ref {1}",
                           new Object[] {wsdlFileUri, serviceRef.getName()});
                    DOLUtils.getDefaultLogger().warning(msg);

                }
            } catch(IOException ioe) {
                    String msg = localStrings.getLocalString(
                   "enterprise.deployment.util.wsdlfilenotreadable",
                           "wsdl file {0}  for service-ref {1} cannot be opened : {2}",
                           new Object[] {wsdlFileUri, serviceRef.getName(), ioe.getMessage()});
                    DOLUtils.getDefaultLogger().warning(msg);

            }
        }

        if( serviceRef.hasMappingFile() ) {
            String mappingFileUri = serviceRef.getMappingFileUri();
            try {
                InputStream mappingFileInputStream =
                    archive_.getEntry(mappingFileUri);
                if( mappingFileInputStream != null ) {
                    mappingFileInputStream.close();
                } else {
                    String msg = localStrings.getLocalString(
                   "enterprise.deployment.util.mappingfilenotfound",
                           "mapping file {0} does not exist for service-ref {1}",
                           new Object[] {mappingFileUri, serviceRef.getName()});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new RuntimeException(msg);
                }
            } catch(IOException ioe) {
                    String msg = localStrings.getLocalString(
                   "enterprise.deployment.util.mappingfilenotreadable",
                           "mapping file {0}  for service-ref {1} cannot be opened : {2}",
                           new Object[] {mappingFileUri, serviceRef.getName(), ioe.getMessage()});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new RuntimeException(ioe);
            }
        }
    }

    public void accept(WebService webService) {

        try {

            String wsdlFileUri = webService.getWsdlFileUri();
            if (!webService.hasWsdlFile()) {
                // no wsdl was specified in the annotation or deployment descritor,
                //it will be generated at deployment.
                return;
            }
            try {
                URL url = new URL(wsdlFileUri);
                if (url.getProtocol()!=null && !url.getProtocol().equals("file"))
                    return;
            } catch(java.net.MalformedURLException e) {
                // ignore it could be a relative uri
            }
            InputStream wsdlFileInputStream = archive_.getEntry(wsdlFileUri);

            if( wsdlFileInputStream != null ) {

                wsdlFileInputStream.close();
                BundleDescriptor bundle = webService.getBundleDescriptor();
                if( !isWsdlContent(wsdlFileUri, bundle) ) {
                    String msg = localStrings.getLocalString(
                        "enterprise.deployment.util.wsdlpackagedinwrongservicelocation",
                        "wsdl file {0} for web service {1} must be packaged in or below {2}",
                        new Object[] {wsdlFileUri, webService.getName(), bundle.getWsdlDir()});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new RuntimeException(msg);
                }
            } else {
                // let's look in the wsdl directory
                String fullFileUri = webService.getBundleDescriptor().getWsdlDir() + "/" + wsdlFileUri;
                wsdlFileInputStream = archive_.getEntry(fullFileUri);

                if( wsdlFileInputStream != null ) {
                    // found it, let's update the DOL to not have to recalculate this again
                    wsdlFileInputStream.close();
                    webService.setWsdlFileUri(fullFileUri);
                } else {
                    // this time I give up, no idea where this WSDL is
                    String msg = localStrings.getLocalString(
                   "enterprise.deployment.util.servicewsdlfilenotfound",
                           "wsdl file {0} does not exist for web service {1}",
                           new Object[] {wsdlFileUri, webService.getName()});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new RuntimeException(msg);
                }
            }
        } catch(IOException ioe) {
            String msg = localStrings.getLocalString(
                   "enterprise.deployment.util.servicewsdlfilenotreadable",
                   "wsdl file {0}  for service-ref {1} cannot be opened : {2}",
                   new Object[] {webService.getWsdlFileUri(), webService.getName(), ioe.getMessage()});
                    DOLUtils.getDefaultLogger().severe(msg);
            throw new RuntimeException(ioe);
        }

        if(webService.getMappingFileUri() == null) {
            return;
        }

        try {
            InputStream mappingFileInputStream =
                archive_.getEntry(webService.getMappingFileUri());
            if( mappingFileInputStream != null ) {
                mappingFileInputStream.close();
            } else {
                    String msg = localStrings.getLocalString(
                   "enterprise.deployment.util.servicemappingfilenotfound",
                           "Web Service mapping file {0} for web service {1} not found",
                           new Object[] {webService.getMappingFileUri(), webService.getName()});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new RuntimeException(msg);
            }
        } catch(IOException ioe) {
                    String msg = localStrings.getLocalString(
                   "enterprise.deployment.util.servicemappingfilenotreadable",
                           "Web Service mapping file {0} for web service {1} not found {2} ",
                           new Object[] {webService.getMappingFileUri(), webService.getName(), ioe});
                    DOLUtils.getDefaultLogger().severe(msg);
                    throw new RuntimeException(ioe);
        }
    }

    /**
     * All wsdl files and wsdl imported files live under a well-known
     * wsdl directory.
     * @param uri module uri
     */
    public boolean isWsdlContent(String uri, BundleDescriptor bundle) {
        String wsdlDir = bundle.getWsdlDir();
        return (uri != null) && uri.startsWith(wsdlDir);
    }
}
