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

package com.sun.appserv.web.cache.mapping;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.web.LogFacade;

import static com.sun.logging.LogCleanerUtil.neutralizeForLog;


/** ConstraintField class represents a single Field and constraints on its
 *  values; Field name and its scope are inherited from the Field class.
 */
public class ConstraintField extends Field {

    private static final String[] SCOPE_NAMES = {
        "", "context.attribute", "request.header", "request.parameter",
        "request.cookie", "request.attribute", "session.attribute",
        "session.id"
    };

    private static final Logger _logger = LogFacade.getLogger();

    // whether to cache if there was a match
    boolean cacheOnMatch = true;
    // whether to cache if there was a failure to match
    boolean cacheOnMatchFailure = false;

    // field value constraints
    ValueConstraint constraints[] = new ValueConstraint[0];

    /**
     * create a new cache field, given a string representation of the scope
     * @param name name of this field
     * @param scope scope of this field
     */
    public ConstraintField(String name, String scope)
            throws IllegalArgumentException {
        super(name, scope);
    }

    /** set whether to cache should the constraints check pass
     * @param cacheOnMatch should the constraint check pass, should we cache?
     */
    public void setCacheOnMatch(boolean cacheOnMatch) {
        this.cacheOnMatch = cacheOnMatch;
    }

    /**
     * @return cache-on-match setting
     */
    public boolean getCacheOnMatch() {
        return cacheOnMatch;
    }

    /** set whether to cache should there be a failure forcing the constraint
     * @param cacheOnMatchFailure should there be a constraint check failure,
     *  enable cache?
     */
    public void setCacheOnMatchFailure(boolean cacheOnMatchFailure) {
        this.cacheOnMatchFailure = cacheOnMatchFailure;
    }

    /**
     * @return cache-on-match-failure setting
     */
    public boolean getCacheOnMatchFailure() {
        return cacheOnMatchFailure;
    }

    /**
     * add a constraint for this field
     * @param constraint one constraint associated with this field
     */
    public void addConstraint(ValueConstraint constraint) {
        if (constraint == null)
            return;

        ValueConstraint results[] =
            new ValueConstraint[constraints.length + 1];
        for (int i = 0; i < constraints.length; i++)
            results[i] = constraints[i];

        results[constraints.length] = constraint;
        constraints = results;
    }

    /**
     * add an array of constraints for this field
     * @param vcs constraints associated with this field
     */
    public void setValueConstraints(ValueConstraint[] vcs) {
        if (vcs == null)
            return;

        constraints = vcs;
    }

    /** apply the constraints on the value of the field in the given request.
     *  return a true if all the constraints pass; false when the
     *  field is not found or the field value doesn't pass the caching
     *  constraints.
     */
    public boolean applyConstraints(ServletContext context,
                                    HttpServletRequest request) {

        Object value = getValue(context, request);
        boolean isFine = _logger.isLoggable(Level.FINE);
        if (value == null) {
            // the field is not present in the request
            if (isFine) {
                _logger.log(Level.FINE, LogFacade.CONSTRAINT_FIELD_NOT_FOUND,
                        new Object[] {neutralizeForLog(name), SCOPE_NAMES[scope], cacheOnMatchFailure});
            }
            return cacheOnMatchFailure;
        } else if (constraints.length == 0) {
            // the field is present but has no value constraints
            if (isFine) {
                _logger.log(Level.FINE, LogFacade.CONSTRAINT_FIELD_FOUND,
                        new Object[] {neutralizeForLog(name), neutralizeForLog(value.toString()), SCOPE_NAMES[scope], cacheOnMatch});
            }
            return cacheOnMatch;
        }

        // apply all the value constraints
        for (int i = 0; i < constraints.length; i++) {
            ValueConstraint c = constraints[i];

            // one of the values matched
            if (c.matches(value)) {
                if (isFine) {
                    _logger.log(Level.FINE, LogFacade.CONSTRAINT_FIELD_MATCH,
                            new Object[] {neutralizeForLog(name), neutralizeForLog(value.toString()), SCOPE_NAMES[scope], cacheOnMatch});
                }
                return cacheOnMatch;
            }
        }

        // none of the values matched; should we cache?
        if (isFine) {
            _logger.log(Level.FINE, LogFacade.CONSTRAINT_FIELD_NOT_MATCH,
                    new Object[] {neutralizeForLog(name), neutralizeForLog(value.toString()), SCOPE_NAMES[scope], cacheOnMatchFailure});
        }
        return cacheOnMatchFailure;
    }
}
