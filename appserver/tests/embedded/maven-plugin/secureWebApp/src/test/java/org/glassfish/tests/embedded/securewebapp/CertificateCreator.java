/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.embedded.securewebapp;

import java.io.File;

import org.glassfish.main.jdke.security.KeyTool;

import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_PASSWORD_DEFAULT;

public class CertificateCreator {

    public static void main(String... args) throws Exception {
        File keystore = new File(args[0]);
        KeyTool keyTool = new KeyTool(keystore, KEYSTORE_PASSWORD_DEFAULT.toCharArray());
        System.err.println(keyTool.generateKeyPair("s1as", "CN=localhost", "RSA", 1));
    }
}
