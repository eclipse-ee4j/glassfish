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

package org.glassfish.security.services.api.authorization;


/**
 * The AzResult class represents the result of an authorization decision.  The result includes three distinct results:
 * A result status, which indicates whether the call was successful or not, and may indicate a reason for a failed call;
 * an authorization decision, which is valid only if the status is "OK", and obligations, which represent actions the
 * calling code must take if it proceeds to access the resource in question.  Obligations are expressed as attributes;
 * the meaning of any specific obligation is established by an out-of-band agreement between the PDP and the PEP.
 */
public interface AzResult {

    /**
     * The possible authorization decision values.  These carry the same meaning as the corresponding
     * decision values defined by XACML 2.0 and later.
     */
    public enum Decision { PERMIT, DENY, INDETERMINATE, NOT_APPLICABLE }
    /**
     * The possible authorization status values.  These carry the same meaning as the corresponding
     * status values defined by XACML 2.0 and later.
     */
    public enum Status { OK, MISSING_ATTRIBUTE, PROCESSING_ERROR, SYNTAX_ERROR }

    /**
     * Get the authorization decision value for this AzResult.
     *
     * @return The Decision value.
     */
    Decision getDecision();

    /**
     * Get the authorization status value for this AzResult.
     *
     * @return The Status value.
     */
    Status getStatus();

    /**
     * Return the obligations that apply to this result.
     *
     * @return The AzObligations.
     */
    AzObligations getObligations();

}
