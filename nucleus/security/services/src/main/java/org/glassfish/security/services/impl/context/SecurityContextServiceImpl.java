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

package org.glassfish.security.services.impl.context;


import jakarta.inject.Singleton;

import org.glassfish.security.services.api.common.Attributes;
import org.glassfish.security.services.api.context.SecurityContextService;
import org.glassfish.security.services.impl.common.AttributesImpl;
import org.jvnet.hk2.annotations.Service;

@Service
@Singleton
public class SecurityContextServiceImpl implements SecurityContextService {

        private Attributes envAttributes = new AttributesImpl();

        public Attributes getEnvironmentAttributes() {
                return envAttributes;
        }

}
