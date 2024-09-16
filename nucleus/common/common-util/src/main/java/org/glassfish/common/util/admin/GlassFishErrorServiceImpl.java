/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.common.util.admin;

import com.sun.enterprise.util.FelixPrettyPrinter;

import jakarta.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

import org.glassfish.hk2.api.ErrorInformation;
import org.glassfish.hk2.api.ErrorService;
import org.glassfish.hk2.api.MultiException;
import org.jvnet.hk2.annotations.Service;
import org.osgi.framework.BundleException;

/**
 * @author jwells
 *
 */
@Service
@Singleton
public class GlassFishErrorServiceImpl implements ErrorService {
    /*
     * (non-Javadoc)
     *
     * @see org.glassfish.hk2.api.ErrorService#onFailure(org.glassfish.hk2.api.ErrorInformation)
     */
    @Override
    public void onFailure(ErrorInformation errorInformation) throws MultiException {
        if (errorInformation.getAssociatedException() != null) {
            MultiException multiException = errorInformation.getAssociatedException();

            Set<String> bundleMessages = new HashSet<>();
            Set<String> stateMessages = new HashSet<>();

            for (Throwable throwable : multiException.getErrors()) { // throwable, error, exception, what's in a name?
                String bundleText = findBundleExceptionText(throwable);
                if (bundleText != null) {
                    bundleMessages.add(bundleText);
                }

                if (throwable instanceof IllegalStateException) {
                    stateMessages.add(throwable.getMessage());
                }
            }

            for (String stateMessage : stateMessages) {
                bundleMessages.remove(stateMessage);
            }

            for (String bundleMessage : bundleMessages) {
                multiException.addError(new IllegalStateException(bundleMessage));
            }

            throw errorInformation.getAssociatedException();
        }
    }

    String findBundleExceptionText(Throwable throwable) {
        while (throwable != null) {
            boolean isBundleException = false;
            try {
                isBundleException = throwable instanceof BundleException;
            } catch (NoClassDefFoundError | Exception e) {
                // BundleException not found - ignore, we don't run in OSGi, e.g. GlassFish Embedded
            }
            if (isBundleException) {
                BundleException bundleException = (BundleException) throwable;
                return FelixPrettyPrinter.prettyPrintExceptionMessage(bundleException.getMessage());
            }
            throwable = throwable.getCause();
        }

        return null;
    }

}
