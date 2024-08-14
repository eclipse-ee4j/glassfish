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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.appclient.server.core.jws.Util;

/**
 * Abstracts the content of several server-side config files so the current
 * values can be served to the Java Web Start client.
 *
 * @author tjquinn
 */
public class ACCConfigContent {

    private final SunACCPairedFiles sunACC;
    private final PairedFiles appClientLogin;

    /* match the security.config property and capture the value */
    private final static Pattern SECURITY_CONFIG_VALUE_PATTERN = Pattern.compile(
            "<property name=\"security.config\"\\s*value=\"([^\"]*)\"\\s*/\\s*>");

    private final static String SECURITY_CONFIG_REPLACEMENT =
            "<property name=\"security.config\" value=\"\\${security.config.path}\"/>";

    public ACCConfigContent(File domainConfig, File installLibAppclient) throws FileNotFoundException, IOException {

        sunACC = SunACCPairedFiles.newSunACCPairedFiles(
                new File(domainConfig, "glassfish-acc.xml"),
                new File(domainConfig, "glassfish-acc.jws.xml"));

        appClientLogin = PairedFiles.newPairedFiles(
                new File(installLibAppclient, "appclientlogin.conf"),
                new File(installLibAppclient, "appclientlogin.jws.conf"));

    }

    public String sunACC() throws FileNotFoundException, IOException {
        return sunACC.content();
    }

    public String appClientLogin() throws FileNotFoundException, IOException {
        return appClientLogin.content();
    }

    public String securityConfig() throws FileNotFoundException, IOException {
        return sunACC.securityConfigContent();
    }


    private static class PairedFiles {
        private final File normalFile;
        private final File jwsFile;

        private String currentContent;

        private long lastModified = 0;

        private static PairedFiles newPairedFiles(final File normalFile, final File jwsFile) throws FileNotFoundException, IOException {
            final PairedFiles result = new PairedFiles(normalFile, jwsFile);
            result.setCurrentContent();
            return result;
        }

        private PairedFiles(final File normalFile, final File jwsFile)
                throws FileNotFoundException, IOException {
            this.normalFile = normalFile;
            this.jwsFile = jwsFile;
        }

        protected long lastModified() {
            return lastModified;
        }

        protected void setCurrentContent() throws FileNotFoundException, IOException {
            setCurrentContent(loadContent(fileToCheck()));
        }

        protected void setCurrentContent(final String content) {
            currentContent = content;
            lastModified = fileToCheck().lastModified();
        }

        protected boolean isContentCurrent() {
            return lastModified >= fileToCheck().lastModified();
        }

        protected File fileToCheck() {
            return (jwsFile.exists() ? jwsFile : normalFile);
        }

        protected String loadContent(final File f) throws FileNotFoundException, IOException {
            FileReader fr = new FileReader(f);
            StringBuilder sb = new StringBuilder();
            int charsRead;

            final char[] buffer = new char[1024];
            try {
                while ( (charsRead = fr.read(buffer)) != -1) {
                    sb.append(buffer, 0, charsRead);
                }
                return Util.replaceTokens(sb.toString(), System.getProperties());
            } finally {
                fr.close();
            }
        }

        String content() throws FileNotFoundException, IOException {
            if ( ! isContentCurrent()) {
                loadContent(fileToCheck());
            }
            return currentContent;
        }

    }

    private static class SunACCPairedFiles extends PairedFiles {

        private String configFilePath = null;

        private File securityConfigFile = null;

        private String securityConfigContent = null;

        private static SunACCPairedFiles newSunACCPairedFiles(
                final File normalFile, final File jwsFile) throws FileNotFoundException, IOException {
            final SunACCPairedFiles result = new SunACCPairedFiles(normalFile, jwsFile);
            result.setCurrentContent();
            return result;
        }

        public SunACCPairedFiles(File normalFile, File jwsFile) throws FileNotFoundException, IOException {
            super(normalFile, jwsFile);
        }

        @Override
        protected boolean isContentCurrent() {
            return super.isContentCurrent() &&
                    (securityConfigFile.lastModified() <= lastModified());
        }

        @Override
        protected String loadContent(File f) throws FileNotFoundException, IOException {
            String origContent = super.loadContent(f);
            /*
             * Replace the value in the glassfish-acc.xml content for the
             * security.config property with a placeholder that the client
             * will recognize and replace with a temp file constructed on
             * the client.
             */
            final Matcher m = SECURITY_CONFIG_VALUE_PATTERN.matcher(origContent);
            final StringBuffer sb = new StringBuffer();
            final String origConfigFilePath = configFilePath;
            while (m.find()) {
                /*
                 * This should match only once.
                 */
                configFilePath = m.group(1);
                m.appendReplacement(sb, SECURITY_CONFIG_REPLACEMENT);
            }
            m.appendTail(sb);

            if ( ! configFilePath.equals(origConfigFilePath)) {
                securityConfigFile = new File(configFilePath);
                securityConfigContent = super.loadContent(securityConfigFile);
            }
            return sb.toString();
        }

        String securityConfigContent() throws FileNotFoundException, IOException {
            if ( ! isContentCurrent()) {
                setCurrentContent(loadContent(fileToCheck()));
            }
            return securityConfigContent;
        }
    }

}
