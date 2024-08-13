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

/*
 * QueryValueFetcher.java
 *
 * Created on March 15, 2002
 * @author  Daniel Tonn
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.jqlc;

import com.sun.jdo.api.persistence.support.JDOFatalInternalException;
import com.sun.jdo.spi.persistence.support.sqlstore.ValueFetcher;

import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

/**
 * Class wrapping the actual query parameters to make them
 * accessible through the ValueFetcher interface.
 */
public class QueryValueFetcher implements ValueFetcher
{
    /** I18N support */
    protected final static ResourceBundle messages =
        I18NHelper.loadBundle(QueryValueFetcher.class);

    /** The actual parameter values. */
    private Object[] parameters;

    /**
     * Constructor.
     * @param parameters the actual parameter values.
     */
    public QueryValueFetcher(Object[] parameters)
    {
        this.parameters = parameters;
    }

    /**
     * Returns the parameter value for the specified parameter index
     * @param whichOne the parameter index
     * @return the parameter value
     */
    public Object getValue(int whichOne)
    {
        if (parameters == null || whichOne >= parameters.length)
        {
            throw new JDOFatalInternalException(I18NHelper.getMessage(messages,
                "jqlc.queryvaluefetcher.getvalue.invalidparam", //NOI18N
                String.valueOf(whichOne),
                String.valueOf((parameters == null) ? 0 : parameters.length)));
        }
        return parameters[whichOne];
    }
}
