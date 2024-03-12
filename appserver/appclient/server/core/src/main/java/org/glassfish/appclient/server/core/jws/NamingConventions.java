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

package org.glassfish.appclient.server.core.jws;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.runtime.JavaWebStartAccessDescriptor;

/**
 *
 * @author tjquinn
 */
public class NamingConventions {
    public static final String JWSAPPCLIENT_PREFIX = "/___JWSappclient";

    public static final String JWSAPPCLIENT_SYSTEM_PREFIX =
            JWSAPPCLIENT_PREFIX + "/___system";

    public static final String JWSAPPCLIENT_EXT_INTRODUCER = "___ext";

    public static final String JWSAPPCLIENT_EXT_PREFIX =
            JWSAPPCLIENT_SYSTEM_PREFIX + "/" + JWSAPPCLIENT_EXT_INTRODUCER;

    private  static final String JWSAPPCLIENT_APP_PREFIX =
            JWSAPPCLIENT_PREFIX + "/___app";

    private static final String JWSAPPCLIENT_DOMAIN_PREFIX =
            JWSAPPCLIENT_PREFIX + "/___domain";

    public static final String DYN_PREFIX = "___dyn";

    public static String contextRootForAppAdapter(final String appName) {
        /*
         * No trailing slash for the context root to use for registering
         * with Grizzly.
         */
        return NamingConventions.JWSAPPCLIENT_APP_PREFIX + "/" + appName;
    }

    public static String domainContentURIString(final String domainRelativeURIString) {
        return JWSAPPCLIENT_DOMAIN_PREFIX + "/" + domainRelativeURIString;
    }

    public static String generatedEARFacadeName(final String earName) {
        return generatedEARFacadePrefix(earName) + ".jar";
    }

    public static String generatedEARFacadePrefix(final String earName) {
        return earName + "Client";
    }

    public static String anchorSubpathForNestedClient(final String clientName) {
        /*
         * We need to add the trailing slash here because we don't want to
         * put it in the template.  Otherwise we'd have an extra slash
         * where we don't want one when we're launching a stand-alone client.
         */
        return clientName + "Client/";
    }

    public static String systemJNLPURI(final String signingAlias) {
        return DYN_PREFIX + "/___system_" + signingAlias + ".jnlp";
    }

    public static String uriToNestedClient(final ApplicationClientDescriptor descriptor) {
        String uriToClientWithinEar = descriptor.getModuleDescriptor().
                    getArchiveUri();
        uriToClientWithinEar = uriToClientWithinEar.substring(0,
                    uriToClientWithinEar.length() - ".jar".length());
        return uriToClientWithinEar;
    }

    public static String defaultUserFriendlyContextRoot(ApplicationClientDescriptor descriptor) {
        String ufContextRoot;
        /*
         * Default for stand-alone clients: appName
         * Default for nested clients: earAppName/uri-to-client-within-EAR-without-.jar
         */
        if (descriptor.getApplication().isVirtual()) {
            /*
             * Stand-alone client.
             */
            ufContextRoot = descriptor.getApplication().getAppName();
        } else {

            ufContextRoot = descriptor.getApplication().getAppName() +
                    "/" + uriToNestedClient(descriptor);
        }
        /*
         * The developer might have set the value in the sun-application-client.xml
         * descriptor.
         */
        final JavaWebStartAccessDescriptor jws =
            descriptor.getJavaWebStartAccessDescriptor();
        if (jws != null && jws.getContextRoot() != null) {
            ufContextRoot = jws.getContextRoot();
        }
        return ufContextRoot;
    }

}
