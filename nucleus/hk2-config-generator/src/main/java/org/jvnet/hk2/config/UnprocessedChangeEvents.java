/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Collects change events that could not be processed.
 * Other fields might be added in the future.
 */
public final class UnprocessedChangeEvents {
    private final List<UnprocessedChangeEvent>   mUnprocessed;

    public UnprocessedChangeEvents( final List<UnprocessedChangeEvent> unprocessed ) {
        mUnprocessed = unprocessed;
    }

    public UnprocessedChangeEvents( final UnprocessedChangeEvent single ) {
        mUnprocessed = new ArrayList<UnprocessedChangeEvent>();
        mUnprocessed.add(single);
    }

    UnprocessedChangeEvents() {
        mUnprocessed = new ArrayList<UnprocessedChangeEvent>();
    }

    void addEvents(Collection<UnprocessedChangeEvent> events) {
        mUnprocessed.addAll(events);
    }

    public List<UnprocessedChangeEvent> getUnprocessed() { return mUnprocessed;}

    public int size() { return mUnprocessed == null ? 0 : mUnprocessed.size(); }

    public String toString() {
        StringBuffer result = new StringBuffer("UnprocessedChangeEvents: " + size());
        if ( size() != 0 ) {
            result.append(" {");
            for( final UnprocessedChangeEvent e : mUnprocessed )
            {
                result.append(e).append(", ");
            }
            result.append("}");
        }
        return result.toString();
    }
}
