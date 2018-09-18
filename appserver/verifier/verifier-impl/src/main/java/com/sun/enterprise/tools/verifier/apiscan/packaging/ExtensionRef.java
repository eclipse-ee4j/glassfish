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

/*
 * OptPkgRef.java
 *
 * Created on August 13, 2004, 4:26 PM
 */

package com.sun.enterprise.tools.verifier.apiscan.packaging;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class holds the information for each installed optional package
 * reference. It is used to select the optional package actually referenced.
 * Refer to http://java.sun.com/j2se/1.4.2/docs/guide/extensions/versioning.html#packages
 * for more info.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @see Archive
 */
public class ExtensionRef {
    private static String resourceBundleName = "com.sun.enterprise.tools.verifier.apiscan.LocalStrings";
    public static final Logger logger = Logger.getLogger("apiscan.packaging", resourceBundleName); // NOI18N
    private static final String myClassName = "ExtensionRef"; // NOI18N
    // a client can specify dependency using atleast the name or name and any other attr
    private String name, implVendorId = "";
    private String implVer;//See javadocs of java.lang.Package. implVer is a string and not DeweyDecimal
    private DeweyDecimal specVer;

    /**
     * @param manifest Manifest file to be read.
     * @param extName  Name of the extension reference. It is the string that is
     *                 mentioned in the Extension-List manifest attribute.
     */
    public ExtensionRef(Manifest manifest, String extName) {
        Attributes attrs = manifest.getMainAttributes();
        name = attrs.getValue(extName + "-" + Attributes.Name.EXTENSION_NAME); // NOI18N
        String s = attrs.getValue(
                extName + "-" + Attributes.Name.SPECIFICATION_VERSION); // NOI18N
        if (s != null) {
            try {
                specVer = new DeweyDecimal(s);
            } catch (NumberFormatException e) {
                logger.log(Level.SEVERE, getClass().getName() + ".exception1", new Object[]{e.getMessage()});
                logger.log(Level.SEVERE, "", e);
                throw e;
            }
        }
        implVendorId =
                attrs.getValue(
                        extName + "-" + Attributes.Name.IMPLEMENTATION_VENDOR_ID); // NOI18N
        implVer =
                attrs.getValue(
                        extName + "-" + Attributes.Name.IMPLEMENTATION_VERSION); // NOI18N
        validate();
    }

    private void validate() {
        if (name == null || name.length() <= 0) {
            throw new IllegalArgumentException("Extension-Name can not be empty.");
        }
    }

    /**
     * @param another Archive whose specifications will be used for matching.
     * @return true if the other archive meets the specifications of this
     *         extensionRef, else returns false.
     */
    public boolean isSatisfiedBy(Archive another) throws IOException {
        logger.entering(myClassName, "isSatisfiedBy", another); // NOI18N
        Attributes attrs = another.getManifest().getMainAttributes();
        String name = attrs.getValue(Attributes.Name.EXTENSION_NAME);
        String s = attrs.getValue(Attributes.Name.SPECIFICATION_VERSION);
        DeweyDecimal specVer = null;
        try {
            specVer = s != null ? new DeweyDecimal(s) : null;
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, getClass().getName() + ".warning1", new Object[]{e.getMessage(), another.toString()});
            return false;
        }
        String implVendorId = attrs.getValue(
                Attributes.Name.IMPLEMENTATION_VENDOR_ID);
        String implVer = attrs.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        //implVendor is not used for comparision because it is not supposed to be used.
        //See optional package versioning.
        //The order of comparision is very well defined in 
        //http://java.sun.com/j2se/1.4.2/docs/guide/extensions/versioning.html
        //Although that is specified for Java plugins for applets, it is equally
        //applicable for J2EE.
        //See J2EE 1.4 section#8.2
        return this.name.equals(name) &&
                (this.specVer == null || this.specVer.isCompatible(specVer)) &&
                (this.implVendorId == null ||
                this.implVendorId.equals(implVendorId)) &&
                (this.implVer == null || this.implVer.equals(implVer));
    }

    /**
     * Used for pretty printing.
     */
    public String toString() {
        return "Extension-Name: " + name + "\n" + // NOI18N
                (specVer != null ? "Specification-Version: " + specVer + "\n" : "") + // NOI18N
                (implVendorId != null ?
                "Implementation-Vendor-Id: " + implVendorId + "\n" : "") + // NOI18N
                (implVer != null ? "Implementation-Version: " + implVer + "\n" : ""); // NOI18N
    }
}
