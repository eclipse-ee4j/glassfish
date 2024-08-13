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
 * CMPROTemplateFormatter.java
 *
 * Created on March 03, 2004
 */

package com.sun.jdo.spi.persistence.support.ejb.ejbc;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

/*
 * This is the helper class for JDO specific generation of
 * a read-only concrete bean implementation.
 * This class does not extend CMPTemplateFormatter but only references
 * its variables when necessary. This allows to reuse CMPTemplateFormatter's
 * properties but the corresponding templates and formatters must be accessed
 * directly.
 *
 * @author Marina Vatkina
 */
public class CMPROTemplateFormatter {

    private final static String templateFile =
        "com/sun/jdo/spi/persistence/support/ejb/ejbc/CMPROTemplates.properties"; // NOI18N

    // Strings for CMP code generation:
    public final static String updateNotAllowed_    = "updateNotAllowed"; // NOI18N
    public final static String accessNotAllowed_    = "accessNotAllowed"; // NOI18N
    public final static String loadNonDFG_          = "loadNonDFG"; // NOI18N

    public final static String jdoGetPersistenceManagerByPK_ = "jdoGetPersistenceManagerByPK"; // NOI18N

    // Code generator templates.
    public static Properties helpers = null;

    // variables
    public static MessageFormat privatetransientvformatter = null; // privateTransientVariables

    // methods
    public static MessageFormat giformatter = null; // jdoGetInstance
    public static MessageFormat jdolookuppmfformatter = null; // jdoLookupPersistenceManagerFactory
    public static MessageFormat ejb__refreshformatter = null; // ejb__refresh
    public static MessageFormat loadNonDFGformatter       = null; // loadNonDFG

    // standard templates for the corresponding keys, so that a template "xxxTemplate"
    // corresponds to a "xxx" key.
    public static String privateStaticFinalVariablesTemplate = null;
    public static String signatureTemplate = null;
    public static String updateNotAllowedTemplate = null;
    public static String accessNotAllowedTemplate = null;
    public static String jdoCleanAllRefsTemplate = null;

    // standard method bodies for the corresponding keys, so that a method body with
    // a name "xxxBody" corresponds to a "xxx" key.
    public static String[] jdoGetPersistenceManagerBody      = null;
    public static String[] jdoGetPersistenceManager0Body     = null;
    public static String[] jdoReleasePersistenceManager0Body = null;
    public static String[] jdoGetPersistenceManagerByPKBody  = null;
    public static String[] jdoClosePersistenceManagerBody    = null;

    /**
     * Constructs a new <code>CMPROTemplateFormatter</code> instance.
     */
    CMPROTemplateFormatter() {
    }

    /**
     * Initializes templates for code generation.
     */
    static synchronized void initHelpers() throws IOException {
        if (helpers == null) {
            helpers = new Properties();
            CMPTemplateFormatter.loadProperties(helpers, templateFile);

            initFormatters();
            initTemplates();
        }
    }

    /**
     * Initializes MessageFormats for code generation.
     */
    private static void initFormatters() {
        // variables
        privatetransientvformatter = new MessageFormat(helpers.getProperty(
                CMPTemplateFormatter.privateTransientVariables_));

        // methods
        giformatter = new MessageFormat(helpers.getProperty(
                CMPTemplateFormatter.getInstance_));
        jdolookuppmfformatter = new MessageFormat(helpers.getProperty(
                CMPTemplateFormatter.jdoLookupPersistenceManagerFactory_));
        ejb__refreshformatter = new MessageFormat(helpers.getProperty(
                CMPTemplateFormatter.ejb__refresh_));
        loadNonDFGformatter = new MessageFormat(helpers.getProperty(loadNonDFG_));
    }

    /**
     * Initializes standard templates for code generation.
     */
    private static void initTemplates() {
        privateStaticFinalVariablesTemplate = helpers.getProperty(
                CMPTemplateFormatter.privateStaticFinalVariables_);
        signatureTemplate = helpers.getProperty(CMPTemplateFormatter.signature_);
        updateNotAllowedTemplate = helpers.getProperty(updateNotAllowed_);
        accessNotAllowedTemplate = helpers.getProperty(accessNotAllowed_);
        jdoCleanAllRefsTemplate = helpers.getProperty(CMPTemplateFormatter.jdoCleanAllRefs_);

        jdoGetPersistenceManagerBody = CMPTemplateFormatter.getBodyAsStrings(
                helpers.getProperty(CMPTemplateFormatter.jdoGetPersistenceManager_));
        jdoGetPersistenceManager0Body = CMPTemplateFormatter.getBodyAsStrings(
                helpers.getProperty(CMPTemplateFormatter.jdoGetPersistenceManager0_));
        jdoGetPersistenceManagerByPKBody = CMPTemplateFormatter.getBodyAsStrings(
                helpers.getProperty(jdoGetPersistenceManagerByPK_));
        jdoClosePersistenceManagerBody = CMPTemplateFormatter.getBodyAsStrings(
                helpers.getProperty(CMPTemplateFormatter.jdoClosePersistenceManager_));
        jdoReleasePersistenceManager0Body = CMPTemplateFormatter.getBodyAsStrings(
                helpers.getProperty(CMPTemplateFormatter.jdoReleasePersistenceManager0_));
    }
}
