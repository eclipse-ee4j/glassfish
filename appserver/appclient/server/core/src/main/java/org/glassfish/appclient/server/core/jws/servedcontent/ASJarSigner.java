/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.server.pluggable.SecuritySupport;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PostConstruct;
import jakarta.inject.Singleton;
import jdk.security.jarsigner.JarSigner;
import jdk.security.jarsigner.JarSignerException;

import org.glassfish.appclient.server.core.jws.JavaWebStartInfo;

/**
 * Signs a specified JAR file.
 *<p>
 *This implementation searches the available keystores for the signing alias
 *indicated in the domain.xml config or, if not specified, the default alias,
 *the first time it is invoked to sign a JAR file.  After the first requested
 *signing it uses the same alias and provider to sign all JARs.
 *<p>
 *The public interface to this class is the static signJar method.
 *
 * @author tjquinn
 */
@Service
@Singleton
public class ASJarSigner implements PostConstruct {

    /** property name optionally set by the admin in domain.xml to select an alias for signing */
    public static final String USER_SPECIFIED_ALIAS_PROPERTYNAME = "com.sun.aas.jws.signing.alias";

    /** keystore type for JKS keystores */
    private static final String JKS_KEYSTORE_TYPE_VALUE = "jks";

    /** default alias for signing if the admin does not specify one */
    private static final String DEFAULT_ALIAS_VALUE = "s1as";

    private static final String DEFAULT_DIGEST_ALGORITHM = "SHA-1";
    private static final String DEFAULT_SIGNATURE_ALGORITHM = "SHA1withRSA";

    private static final SecuritySupport securitySupport = SecuritySupport.getDefaultInstance();

//    /** user-specified signing alias */
//    private final String userAlias; // = System.getProperty(USER_SPECIFIED_ALIAS_PROPERTYNAME);

    private static final StringManager localStrings = StringManager.getManager(ASJarSigner.class);

    private Logger logger;

    @Override
    public void postConstruct() {
        logger =Logger.getLogger(JavaWebStartInfo.APPCLIENT_SERVER_MAIN_LOGGER, JavaWebStartInfo.APPCLIENT_SERVER_LOGMESSAGE_RESOURCE);
    }

    /**
     *Creates a signed jar from the specified unsigned jar.
     *@param unsignedJar the unsigned JAR file
     *@param signedJar the signed JAR to be created
     *@param attrs additional attributes to be added to the JAR's manifest main section
     *@return the elapsed time to sign the JAR (in milliseconds)
     *@throws Exception getting the keystores from SSLUtils fails
     */
    public long signJar(final File unsignedJar, final File signedJar,
        String alias, Attributes attrs) throws Exception {

        final ZipOutputStream zout = new ZipOutputStream(
                    new FileOutputStream(signedJar));
        try {
            final long result = signJar(unsignedJar, zout, alias, attrs, Collections.EMPTY_MAP);
            return result;
        } catch (Exception ex) {
            /*
             *In case of any problems, make sure there is no ill-formed signed
             *jar file left behind.
             */
            if ( signedJar.exists() && ! signedJar.delete()) {
                logger.log(Level.FINE, "Could not remove generated signed JAR {0} after JarSigner reported an error",
                       signedJar.getAbsolutePath());
            }
            throw ex;
        } finally {
            zout.close();
        }
    }

    /**
     * Creates a signed ZIP output stream from an unsigned JAR and, possibly, additional content.
     * @param unsignedJar JAR file containing most of the content to sign and return
     * @param signedJar already-opened ZipOutputStream to receive the signed content
     * @param alias the alias with which to identify the cert for signing the output
     * @param attrs additional manifest attributes to add
     * @param additionalContent additional JAR entries to add
     * @return
     * @throws Exception
     */
    public long signJar(final File unsignedJar, final ZipOutputStream signedJar,
            String alias, final Attributes attrs, final Map<String,byte[]> additionalContent) throws Exception {

        if (alias == null) {
            alias = DEFAULT_ALIAS_VALUE;
        }
        long startTime = System.currentTimeMillis();
        long duration = 0;
        synchronized(this) {
            try {
                Certificate[] certificates = null;
                PrivateKey privateKey = null;
                KeyStore[] keyStores = securitySupport.getKeyStores();
                for (int i = 0; i < keyStores.length; i++) {
                    privateKey = securitySupport.getPrivateKeyForAlias(alias, i);
                    if (privateKey != null) {
                        certificates = keyStores[i].getCertificateChain(alias);
                    }
                }

                CertPath certPath = CertificateFactory.getInstance("X.509").generateCertPath(asList(certificates));

                JarSigner signer = new JarSigner.Builder(privateKey, certPath)
                        .digestAlgorithm(DEFAULT_DIGEST_ALGORITHM)
                        .signatureAlgorithm(DEFAULT_SIGNATURE_ALGORITHM)
                        .build();

                // TODO: add Attributes to Manifest and additionalContent

                signer.sign(new JarFile(unsignedJar), signedJar);

            } catch (Throwable t) {
                /*
                 *The jar signer will have written some information to System.out
                 */
                throw new Exception(localStrings.getString("jws.sign.errorSigning",
                        unsignedJar.getAbsolutePath(), alias), t);
            } finally {
                duration = System.currentTimeMillis() - startTime;
                logger.log(Level.FINE, "Signing {0} took {1} ms",
                        new Object[]{unsignedJar.getAbsolutePath(), duration});
            }
        }
        return duration;
    }

    /**
     *Wraps any underlying exception.
     *<p>
     *This is primarily used to insulate calling logic from
     *the large variety of exceptions that can occur during signing
     *from which the caller cannot really recover.
     */
    public static class ASJarSignerException extends Exception {
        public ASJarSignerException(String msg, Throwable t) {
            super(msg, t);
        }
    }
}
