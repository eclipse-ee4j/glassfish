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

package org.glassfish.common.locator;

import javax.inject.Inject;

/**
 * @author jwells
 *
 */
public class SimpleInjectee {
    @Inject
    private SimpleService simpleService;
    
    /**
     * Used by the test code to ensure we can call a proxied service
     * from a PerLookup object
     */
    public void callIt() {
        simpleService.callMe();
    }

}
