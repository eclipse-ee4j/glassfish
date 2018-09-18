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

package com.sun.enterprise.tools.verifier.tests.persistence;

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.persistence.AVKPersistenceUnitInfoImpl;
import com.sun.enterprise.tools.verifier.tests.VerifierCheck;
import com.sun.enterprise.tools.verifier.tests.VerifierTest;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import java.util.logging.Level;
import java.util.Properties;

import org.eclipse.persistence.exceptions.IntegrityException;
import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.glassfish.api.deployment.InstrumentableClassLoader;

/**
 * This test uses TopLink Essential to do the validation.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class DefaultProviderVerification extends VerifierTest
        implements VerifierCheck {
    public Result check(Descriptor descriptor) {
        PersistenceUnitDescriptor pu =
                PersistenceUnitDescriptor.class.cast(descriptor);
        Result result = getInitializedResult();
        result.setStatus(Result.PASSED);
        PersistenceProvider provider;
        final String appLocation =
                getVerifierContext().getAbstractArchive().getURI().getPath();
        final InstrumentableClassLoader cl =
                InstrumentableClassLoader.class.cast(pu.getParent().getClassLoader());
        PersistenceUnitInfo pi = new AVKPersistenceUnitInfoImpl(pu, appLocation, cl);
        logger.fine("PersistenceInfo for PU is :\n" + pi);
        Properties props = new Properties();
        // This property is set to indicate that TopLink should only
        // validate the descriptors etc. and not try to login to database.
        props.put(PersistenceUnitProperties.VALIDATION_ONLY_PROPERTY,
                "TRUE"); // NOI18N
        // This property is used so that TopLink throws validation exceptions
        // as opposed to printing CONFIG level messages to console.
        // e.g. if mapping file does not exist, we will get an exception.
        props.put(PersistenceUnitProperties.THROW_EXCEPTIONS,
                "TRUE"); // NOI18N

        // the following property is needed as it initializes the logger in TL
        props.put(PersistenceUnitProperties.TARGET_SERVER,
                      "SunAS9"); // NOI18N

        // Turn off enhancement during verification. For details,
        // refer to http://glassfish.dev.java.net/issues/show_bug.cgi?id=3295
        props.put(PersistenceUnitProperties.WEAVING, "FALSE");

        provider = new org.eclipse.persistence.jpa.PersistenceProvider();
        EntityManagerFactory emf = null;
        try {
            emf = provider.createContainerEntityManagerFactory(pi, props);
            logger.logp(Level.FINE, "DefaultProviderVerification", "check",
                    "emf = {0}", emf);
        } catch(IntegrityException ie){
            result.setStatus(Result.FAILED);
            addErrorDetails(result, getVerifierContext().getComponentNameConstructor());
            for(Object o: ie.getIntegrityChecker().getCaughtExceptions()){
                Exception e = Exception.class.cast(o);
                result.addErrorDetails(e.getMessage());
            }
        } catch (ValidationException ve) {
            addErrorDetails(result, getVerifierContext().getComponentNameConstructor());
            result.failed(ve.getMessage());
            logger.logp(Level.FINE, "DefaultProviderVerification", "check", "Following exception occurred", ve);
        } catch(DatabaseException de) {
            addErrorDetails(result, getVerifierContext().getComponentNameConstructor());
            result.failed(de.getMessage());
            logger.logp(Level.FINE, "DefaultProviderVerification", "check", "Following exception occurred", de);
        } catch(PersistenceException pe) {
            addErrorDetails(result, getVerifierContext().getComponentNameConstructor());
            result.failed(pe.getMessage());
            logger.logp(Level.FINE, "DefaultProviderVerification", "check", "Following exception occurred", pe);
        } finally {
            if(emf != null) {
                emf.close();
            }
        }
        return result;
    }

}
