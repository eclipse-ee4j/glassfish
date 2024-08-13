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

package org.glassfish.appclient.server.core.jws.servedcontent;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import java.net.URI;
import java.util.Properties;

import org.glassfish.appclient.server.core.AppClientDeployerHelper;
import org.glassfish.appclient.server.core.NestedAppClientDeployerHelper;
import org.glassfish.appclient.server.core.StandaloneAppClientDeployerHelper;
import org.glassfish.appclient.server.core.jws.JWSAdapterManager;
import org.glassfish.appclient.server.core.jws.JavaWebStartInfo;
import org.glassfish.appclient.server.core.jws.JavaWebStartInfo.VendorInfo;
import org.glassfish.appclient.server.core.jws.NamingConventions;

/**
 *
 * @author tjquinn
 */
public abstract class TokenHelper {

    private static final String AGENT_JAR = "gf-client.jar";
    private static final String DYN_PREFIX = "___dyn/";
    private static final String GROUP_JAR_ELEMENT_PROPERTY_NAME = "group.facade.jar.element";

    private Properties tokens;
    protected final AppClientDeployerHelper dHelper;

    private final LocalStringsImpl localStrings = new LocalStringsImpl(TokenHelper.class);

    private VendorInfo vendorInfo = null;

    private String signingAlias;

    public static TokenHelper newInstance(final AppClientDeployerHelper dHelper,
            final VendorInfo vendorInfo) {
        TokenHelper tHelper;
        if (dHelper instanceof StandaloneAppClientDeployerHelper) {
            tHelper = new StandAloneClientTokenHelper(dHelper);
        } else {
            if (dHelper instanceof NestedAppClientDeployerHelper) {
                tHelper = new NestedClientTokenHelper((NestedAppClientDeployerHelper)dHelper);
            } else {
                throw new RuntimeException("dHelper.getClass() = " + dHelper.getClass().getName() + " != NestedAppClientDeployerHelper");
            }
        }
        tHelper.vendorInfo = vendorInfo;

        tHelper.signingAlias = JWSAdapterManager.signingAlias(dHelper.dc());
        tHelper.tokens = tHelper.buildTokens();
        return tHelper;
    }

    public Properties tokens() {
        return tokens;
    }

    public Object setProperty(final String propName, final String propValue) {
        return tokens.setProperty(propName, propValue);
    }

    public String imageURIFromDescriptor() {
        return vendorInfo.getImageURI();
    }

    public String splashScreenURIFromDescriptor() {
        return vendorInfo.getSplashImageURI();
    }

    protected TokenHelper(final AppClientDeployerHelper dHelper) {
        this.dHelper = dHelper;
    }

    public String appCodebasePath() {
        return NamingConventions.contextRootForAppAdapter(dHelper.appName());
    }

    public String systemContextRoot() {
        return NamingConventions.JWSAPPCLIENT_SYSTEM_PREFIX;
    }

    public String agentJar() {
        return AGENT_JAR;
    }

    public String systemJNLP() {
        return NamingConventions.systemJNLPURI(signingAlias);
    }

    public abstract String appLibraryExtensions();

    /**
     * Returns the relative path from the app's context root to its
     * anchor.  For example, for a stand-alone client the anchor is
     * the same place as the context root; that is where its facade and
     * client JAR reside.  For a nested app client, the
     * anchor is the subdirectory ${clientName}Client.
     *
     * @return
     */
    protected abstract String anchorSubpath();

    public String mainJNLP() {
        return dyn() + anchorSubpath() + "___main.jnlp";
    }

    public String clientJNLP() {
        return dyn() + anchorSubpath() + "___client.jnlp";
    }

    public String clientFacadeJNLP() {
        return dyn() + anchorSubpath() + "___clientFacade.jnlp";
    }

    public String dyn() {
        return DYN_PREFIX;
    }

    protected AppClientDeployerHelper dHelper() {
        return dHelper;
    }

    public String clientFacadeJARPath() {
        return anchorSubpath() + dHelper.clientName();
    }

    private Properties buildTokens() {
        final Properties t = new Properties();

        t.setProperty("app.codebase.path", appCodebasePath());
        t.setProperty("main.jnlp.path", mainJNLP());
        t.setProperty("system.context.root", systemContextRoot());
        t.setProperty("agent.jar", agentJar());
        t.setProperty("system.jnlp", systemJNLP());
//        t.setProperty("client.facade.jnlp.path", clientFacadeJNLP());
        t.setProperty("client.jnlp.path", clientJNLP());
        t.setProperty(JavaWebStartInfo.APP_LIBRARY_EXTENSION_PROPERTY_NAME, appLibraryExtensions());
        t.setProperty("anchor.subpath", anchorSubpath());
        t.setProperty("dyn", dyn());

        t.setProperty("client.facade.jar.path", clientFacadeJARPath());

        t.setProperty("client.security", "<all-permissions/>");

        final ApplicationClientDescriptor acDesc = dHelper.appClientDesc();
        /*
         * Set the JNLP information title to the app client module's display name,
         * if one is present.
         */
        String displayName = acDesc.getDisplayName();
        String jnlpInformationTitle =
                (displayName != null && displayName.length() > 0) ?
                    displayName : localStrings.get("jws.information.title.prefix") + " " + dHelper.appName();
        t.setProperty("appclient.main.information.title", jnlpInformationTitle);
        t.setProperty("appclient.client.information.title", jnlpInformationTitle);

        /*
         * Set the one-line description the same as the title for now.
         */
        t.setProperty("appclient.main.information.description.one-line", jnlpInformationTitle);
        t.setProperty("appclient.client.information.description.one-line", jnlpInformationTitle);

        /*
         *Set the short description to the description from the descriptor, if any.
         */
        String description = acDesc.getDescription();
        String jnlpInformationShortDescription =
                (description != null && description.length() > 0) ?
                    description : jnlpInformationTitle;
        t.setProperty("appclient.main.information.description.short", jnlpInformationShortDescription);
        t.setProperty("appclient.client.information.description.short", jnlpInformationShortDescription);

        t.setProperty("appclient.vendor", vendorInfo.getVendor());

        /*
         * Construct the icon elements, if the user specified any in the
         * optional descriptor element.
         */
        t.setProperty("appclient.main.information.images", iconElements(vendorInfo));

        /*
         * For clients in an EAR there will be an EAR-level generated group facade
         * JAR to include in the downloaded files.
         */
        final URI groupFacadeUserURI = dHelper.groupFacadeUserURI(dHelper.dc());
        t.setProperty(GROUP_JAR_ELEMENT_PROPERTY_NAME,
                (groupFacadeUserURI == null ? "" : "<jar href=\"" + groupFacadeUserURI.toASCIIString() + "\"/>"));
        setSystemJNLPTokens(t);
        return t;

    }

    private String iconElements(final VendorInfo vendorInfo) {

        StringBuilder result = new StringBuilder();
        String imageURI = vendorInfo.JNLPImageURI();
        if (imageURI.length() > 0) {
            result.append("<icon href=\"" + imageURI + "\"/>");
//            addImageContent(origin, location, imageURI);
        }
        String splashImageURI = vendorInfo.JNLPSplashImageURI();
        if (splashImageURI.length() > 0) {
            result.append("<icon kind=\"splash\" href=\"" + splashImageURI + "\"/>");
//            addImageContent(origin, location, splashImageURI);
        }
        return result.toString();
    }

    private void setSystemJNLPTokens(final Properties props) {
        final String[] tokenNames = new String[] {
            "jws.appserver.information.title",
            "jws.appserver.information.vendor",
            "jws.appserver.information.description.one-line",
            "jws.appserver.information.description.short"
        };

        for (String tokenName : tokenNames) {
            final String value = localStrings.get(tokenName);
            props.setProperty(tokenName, value);
        }
    }
}
