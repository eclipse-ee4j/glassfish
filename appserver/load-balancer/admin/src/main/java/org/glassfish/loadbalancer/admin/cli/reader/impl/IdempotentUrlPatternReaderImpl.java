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

package org.glassfish.loadbalancer.admin.cli.reader.impl;

import com.sun.enterprise.deployment.runtime.web.IdempotentUrlPattern;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;

import org.glassfish.loadbalancer.admin.cli.reader.api.IdempotentUrlPatternReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.LbReaderException;
import org.glassfish.loadbalancer.admin.cli.transform.IdempotentUrlPatternVisitor;
import org.glassfish.loadbalancer.admin.cli.transform.Visitor;

/**
 * Provides idempotent url pattern information relavant to Load balancer tier.
 *
 * @author Kshitiz Saxena
 */
public class IdempotentUrlPatternReaderImpl
        implements IdempotentUrlPatternReader {

    /**
     * Constructor for Idempotent url pattern
     */
    public IdempotentUrlPatternReaderImpl(IdempotentUrlPattern pattern) {
        _pattern = pattern;
    }

    /**
     * Returns a regular expression containing an url or url pattern.
     *
     * @return String           an url and regural expression matching multiple
     *                          urls
     */
    @Override
    public String getUrlPattern() throws LbReaderException {
        return _pattern.getAttributeValue(IdempotentUrlPattern.URL_PATTERN);
    }

    /**
     * Number of retries, when an idempotent request fails.
     *
     * @return String           value must be > 0.
     */
    @Override
    public String getNoOfRetries() throws LbReaderException {
        return _pattern.getAttributeValue(IdempotentUrlPattern.NUM_OF_RETRIES);
    }

    @Override
    public void accept(Visitor v) throws Exception {
        if (v instanceof IdempotentUrlPatternVisitor) {
            IdempotentUrlPatternVisitor iv = (IdempotentUrlPatternVisitor) v;
            iv.visit(this);
        }
    }
    //---- PRIVATE VARS ------
    int _idx = 0;
    SunWebApp _bean = null;
    IdempotentUrlPattern _pattern;
}
