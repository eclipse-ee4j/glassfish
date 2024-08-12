/*
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

import jakarta.inject.Singleton;

import java.util.StringTokenizer;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.ValidationInformation;
import org.glassfish.hk2.api.ValidationService;
import org.glassfish.hk2.api.Validator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Service;

/**
 * This file is used to turn on HK2 bind and unbind tracing
 *
 * @author jwells
 *
 */
@Service
@Singleton
public class HK2BindTracingService implements ValidationService {
    private final static Filter ALL_FILTER = BuilderHelper.allFilter();
    private final static Filter NONE_FILTER = new Filter() {

        @Override
        public boolean matches(Descriptor d) {
            return false;
        }

    };
    private final static boolean TRACE_BINDS = Boolean.parseBoolean(
            System.getProperty("org.glassfish.hk2.tracing.binds", "false"));
    private final static String TRACE_BINDS_PATTERN =
            System.getProperty("org.glassfish.hk2.tracing.bindsPattern");
    private final static boolean TRACE_LOOKUPS = Boolean.parseBoolean(
            System.getProperty("org.glassfish.hk2.tracing.lookups", "false"));
    private final static String TRACE_LOOKUPS_PATTERN =
            System.getProperty("org.glassfish.hk2.tracing.lookupsPattern");

    private final static String STACK_PATTERN =
            System.getProperty("org.glassfish.hk2.tracing.binds.stackPattern");

    private final static Validator VALIDATOR = new ValidatorImpl();

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ValidationService#getLookupFilter()
     */
    @Override
    public Filter getLookupFilter() {
        if (TRACE_LOOKUPS == true) return ALL_FILTER;

        return NONE_FILTER;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ValidationService#getValidator()
     */
    @Override
    public Validator getValidator() {
        return VALIDATOR;
    }

    private static boolean matchesPattern(String pattern, ActiveDescriptor<?> descriptor) {
        if (pattern == null) return true;

        StringTokenizer st = new StringTokenizer(pattern, "|");

        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            if (descriptor.getImplementation().contains(token)) {
                return true;
            }

            for (String contract : descriptor.getAdvertisedContracts()) {
                if (contract.contains(token)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static class ValidatorImpl implements Validator {

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.Validator#validate(org.glassfish.hk2.api.ValidationInformation)
         */
        @Override
        public boolean validate(ValidationInformation info) {
            if (!TRACE_BINDS && !TRACE_LOOKUPS) return true;

            switch (info.getOperation()) {
            case BIND:
                if (TRACE_BINDS && matchesPattern(TRACE_BINDS_PATTERN, info.getCandidate())) {
                    System.out.println("HK2 Tracing (BIND): " + info.getCandidate());
                }
                break;
            case UNBIND:
                if (TRACE_BINDS && matchesPattern(TRACE_BINDS_PATTERN, info.getCandidate())) {
                    System.out.println("HK2 Tracing (UNBIND): " + info.getCandidate());
                }
                break;
            case LOOKUP:
                if (TRACE_LOOKUPS && matchesPattern(TRACE_LOOKUPS_PATTERN, info.getCandidate())) {
                    System.out.println("HK2 Tracing (LOOKUP) Candidate: " + info.getCandidate());
                    if (info.getInjectee() != null) {
                        System.out.println("HK2 Tracing (LOOKUP) Injectee: " + info.getInjectee());
                    }
                }
                break;
            default:
                // Do nothing

            }

            if ((STACK_PATTERN != null) && matchesPattern(STACK_PATTERN, info.getCandidate())) {
                Thread.dumpStack();
            }

            return true;
        }

    }

}
