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

import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WSDolSupport;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.internal.api.Globals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 * @author Kenneth Saks
 */
public class ModuleContentLinker extends DefaultDOLVisitor implements ComponentVisitor {

    // For standalone modules, this is either a directory or a jar file.
    // For .ears, this is the directory used by the j2ee classloader.
    protected ReadableArchive rootLocation_;

    // records whether URL assignments should be calculated even if the URLs
    // already have values.
    private boolean forceWSDLURLs;

    public ModuleContentLinker(ReadableArchive rootLocation, boolean forceWSDLURLs) {
        rootLocation_ = rootLocation;
        this.forceWSDLURLs = forceWSDLURLs;
    }

    public ModuleContentLinker(ReadableArchive rootLocation) {
        this(rootLocation, false);
    }

    protected ModuleContentLinker() {
    }

    public void accept (BundleDescriptor bundle) {
        for (Iterator<WebService> itr = bundle.getWebServices().getWebServices().iterator(); itr.hasNext();) {
            WebService aWebService = itr.next();
            accept(aWebService);
        }

        if (bundle instanceof JndiNameEnvironment) {
            for (Iterator<ServiceReferenceDescriptor> itr = ((JndiNameEnvironment)bundle).getServiceReferenceDescriptors().iterator(); itr.hasNext();) {
                accept(itr.next());
            }
        }

        if (bundle instanceof EjbBundleDescriptor) {
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor)bundle;
            for (EjbDescriptor anEjb : ejbBundle.getEjbs()) {
                for (Iterator<ServiceReferenceDescriptor> itr = anEjb.getServiceReferenceDescriptors().iterator(); itr.hasNext();) {
                    accept(itr.next());
                }
            }
        }
    }

    private String getModuleLocation(ModuleDescriptor module) throws IOException {
        String moduleLocation = (new File(rootLocation_.getURI())).getAbsolutePath();
        if( !module.isStandalone() ) {
            // If this is an ear, get module jar by adding the module path
            // to the root directory.
            String archiveUri = module.getArchiveUri();
            archiveUri = archiveUri.replace('.', '_');
            moduleLocation = moduleLocation + File.separator + archiveUri;
        }
        return moduleLocation;
    }

    private URL internalGetUrl(ModuleDescriptor module, String uri)
        throws Exception {
        File moduleLocation = new File(getModuleLocation(module));
        URL url = getEntryAsUrl(moduleLocation, uri);
        return url;
    }

    public static URL createJarUrl(File jarFile, String entry)
        throws MalformedURLException, IOException {
        return new URL("jar:" + jarFile.toURI().toURL() + "!/" + entry);
    }

    public static URL getEntryAsUrl(File moduleLocation, String uri)
        throws MalformedURLException, IOException {
        URL url = null;
        try {
            url = new URL(uri);
        } catch(java.net.MalformedURLException e) {
            // ignore
            url = null;
        }
        if (url!=null) {
            return url;
        }
        if( moduleLocation != null ) {
            if( moduleLocation.isFile() ) {
                url = createJarUrl(moduleLocation, uri);
            } else {
                String path = uri.replace('/', File.separatorChar);
                url = new File(moduleLocation, path).toURI().toURL();
            }
        }
        return url;
    }

    public void accept(ServiceReferenceDescriptor serviceRef) {
        try {
            ModuleDescriptor moduleDesc =
                serviceRef.getBundleDescriptor().getModuleDescriptor();

            if( serviceRef.hasWsdlFile() ) {

                String wsdlFileUri = serviceRef.getWsdlFileUri();
                File tmpFile = new File(wsdlFileUri);
                if(tmpFile.isAbsolute()) {
                    // This takes care of the case when we set wsdlFileUri from generated @WebClient
                    // and the uri is an absolute path
                    serviceRef.setWsdlFileUrl(tmpFile.toURI().toURL());
                } else {
                    // If the given WSDL is an http URL, create a URL directly from this
                    if(wsdlFileUri.startsWith("http")) {
                        serviceRef.setWsdlFileUrl(new URL(wsdlFileUri));
                    } else {
                        // Given WSDL location is a relative path - append this to the module dir
                        URL wsdlFileUrl = internalGetUrl(moduleDesc, wsdlFileUri);
                        serviceRef.setWsdlFileUrl(wsdlFileUrl);
                    }
                }
            } else {
            //Incase wsdl file is missing we can obtain it from the @WebServiceClient annotation
                  ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                  Class serviceInterfaceClass = classloader.loadClass(serviceRef.getServiceInterface());
                  WSDolSupport dolSupport = Globals.getDefaultHabitat().getService(WSDolSupport.class);
                if (dolSupport!=null) {
                    dolSupport.setServiceRef(serviceInterfaceClass, serviceRef);
                }
            }
            if( serviceRef.hasMappingFile() ) {
                String mappingFileUri = serviceRef.getMappingFileUri();
                File mappingFile = new File(getModuleLocation(moduleDesc), mappingFileUri);
                serviceRef.setMappingFile(mappingFile);

            }
        } catch (java.net.MalformedURLException mex) {
            DOLUtils.getDefaultLogger().log
                (Level.SEVERE, "enterprise.deployment.backend.invalidWsdlURL",
                new Object[] {serviceRef.getWsdlFileUri()});
        } catch(Exception e) {
            DOLUtils.getDefaultLogger().log
                (Level.SEVERE, DOLUtils.INVALID_DESC_MAPPING,
                new Object[] {serviceRef.getName() , rootLocation_});
        }
    }

    public void accept(WebService webService) {
        try {
            ModuleDescriptor moduleDesc =
                webService.getBundleDescriptor().getModuleDescriptor();
            // If the web service has a WSDL file, assign its URL if it is not
            // already assigned or if URLs are forced to be assigned.
            if( webService.hasWsdlFile() && (webService.getWsdlFileUrl()==null || forceWSDLURLs) ) {
                String wsdlFileUri = webService.getWsdlFileUri();
                URL wsdlFileURL=null;
                try {
                    URL url = new URL(wsdlFileUri);
                    if (url.getProtocol()!=null && !url.getProtocol().equalsIgnoreCase("file")) {
                        wsdlFileURL=url;
                    }
                } catch(java.net.MalformedURLException e) {
                    // ignore, it could just be a relate URI
                }
                if (wsdlFileURL==null) {
                    File wsdlFile = new File(getModuleLocation(moduleDesc), wsdlFileUri);
                    wsdlFileURL = wsdlFile.toURI().toURL();
                }
                webService.setWsdlFileUrl(wsdlFileURL);
            }
            if( webService.hasMappingFile() ) {
                String mappingFileUri = webService.getMappingFileUri();
                File mappingFile = new File(getModuleLocation(moduleDesc), mappingFileUri);
                webService.setMappingFile(mappingFile);
            }
        } catch(Exception e) {
            DOLUtils.getDefaultLogger().log
                (Level.SEVERE, DOLUtils.INVALID_DESC_MAPPING,
                new Object[] {webService.getName() , rootLocation_});
        }
    }
}
