/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.common;

import jakarta.inject.Singleton;

import java.util.logging.Logger;

import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.ValidationService;
import org.glassfish.hk2.api.Validator;
import org.jvnet.hk2.annotations.Service;


@Service
@Singleton
public class SecurityAccessValidationService implements ValidationService {


    private SecurityAccessValidator commValidator = new SecurityAccessValidator();

    private SecurityAccessFilter flt = new SecurityAccessFilter();

    static final Logger _theLog = Logger.getLogger("org.glassfish.security.services");


    @Override
    public Filter getLookupFilter() {
        return flt;
    }

    @Override
    public Validator getValidator() {
        return commValidator;
    }

}
