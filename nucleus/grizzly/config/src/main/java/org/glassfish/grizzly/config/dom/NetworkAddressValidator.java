/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config.dom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkAddressValidator implements ConstraintValidator<NetworkAddress, String> {
    public void initialize(final NetworkAddress networkAddress) {
    }

    public boolean isValid(final String s, final ConstraintValidatorContext constraintValidatorContext) {
        try {
            //noinspection ResultOfMethodCallIgnored
            InetAddress.getByName(s);
            return true;
        } catch (UnknownHostException e) {
            return s != null
                    && s.charAt(0) == '$'
                    && s.charAt(1) == '{'
                    && s.charAt(s.length() - 1) == '}';
        }
    }
}
