/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.cert.Certificate;

import javax.xml.stream.XMLStreamException;

import com.sun.enterprise.security.perms.SMGlobalPolicyUtil;
import com.sun.enterprise.security.perms.XMLPermissionsHandler;

import sun.security.provider.PolicyFile;

public class PermissionsUtil {

    protected static final String PERMISSIONS_XML = "META-INF/permissions.xml";  
    
    protected static final String CLIENT_EE_PERMS_FILE = "javaee.client.policy";
    protected static final String CLIENT_EE_PERMS_PKG = 
        "META-INF/" + CLIENT_EE_PERMS_FILE;
    
    protected static final String CLIENT_RESTRICT_PERMS_FILE = "restrict.client.policy";
    protected static final String CLIENT_RESTRICT_PERMS_PKG = 
        "META-INF/" + CLIENT_RESTRICT_PERMS_FILE;
    
    
    //get client declared permissions which is packaged on the client's generated jar, 
    //or in the client's module jar if standalone
    //result could be null
    public static PermissionCollection getClientDeclaredPermissions(ClassLoader cl) throws IOException {
        
        URL permUrl = cl.getResource(PERMISSIONS_XML);
        
        if (permUrl == null )
            return null;
        
        InputStream declaredPermInput = permUrl.openStream(); 
        
        XMLPermissionsHandler pHdlr = null;
        
        try {
            pHdlr = new XMLPermissionsHandler(
                null, declaredPermInput, SMGlobalPolicyUtil.CommponentType.car);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        } catch (FileNotFoundException e) {
            throw new IOException(e);
        }

        return pHdlr.getAppDeclaredPermissions();
    }
    
    //get the permissions configured inside the javaee.client.policy, 
    // which might be packaged inside the client jar, 
    // or from the installed folder lib/appclient
    //result could be null if either of the above is found
    public static PermissionCollection getClientEEPolicy(ClassLoader cl)
            throws IOException {
        
        return getClientPolicy(cl, CLIENT_EE_PERMS_PKG, CLIENT_EE_PERMS_FILE);
    }

    //get the permissions configured inside the javaee.client.policy, 
    // which might be packaged inside the client jar, 
    // or from the installed folder lib/appclient
    //result could be null if either of the above is found
    public static PermissionCollection getClientRestrictPolicy(ClassLoader cl)
        throws IOException {

        return getClientPolicy(cl, CLIENT_RESTRICT_PERMS_PKG, CLIENT_RESTRICT_PERMS_FILE);
    }

    
    private static PermissionCollection getClientPolicy(ClassLoader cl, String pkgedFile, 
            String policyFileName) throws IOException {
        
        
        //1st try to find from the packaged client jar
        URL eeClientUrl = cl.getResource(pkgedFile);
        if (eeClientUrl != null)
            return getEEPolicyPermissions(eeClientUrl);
        
        
        //2nd try to find from client's installation at lib/appclient folder
        String clientPolicyClocation = getClientInstalledPath();
        if (clientPolicyClocation != null) {            
            String clietEEFile = clientPolicyClocation + policyFileName;
            return getPolicyPermissions(clietEEFile);
        }
        
        return null;
        
    }
    
    
    private static PermissionCollection getEEPolicyPermissions(URL fileUrl) throws IOException {
        
        //System.out.println("Loading policy from " + fileUrl);
        PolicyFile pf = new PolicyFile(fileUrl);
        
        CodeSource cs = 
            new CodeSource(
                    new URL(SMGlobalPolicyUtil.CLIENT_TYPE_CODESOURCE), (Certificate[])null );
        PermissionCollection pc = pf.getPermissions(cs);

        return pc;        
    }

    
    
    private static PermissionCollection  getPolicyPermissions(String policyFilename) throws IOException {

        File f = new File(policyFilename);
        if (!f.exists())
            return null;
        
        URL furl = new URL("file:" + policyFilename);
            
        return getEEPolicyPermissions(furl);
    }


    private static String getClientInstalledPath() {
        String policyPath = System.getProperty("java.security.policy");
        
        if (policyPath == null)
            return null;
        
        File pf = new File(policyPath);
        
        String  clientPath = pf.getParent() + File.separator;

        //System.out.println("clientPath  " + clientPath );
        
        return clientPath;
    }

}
