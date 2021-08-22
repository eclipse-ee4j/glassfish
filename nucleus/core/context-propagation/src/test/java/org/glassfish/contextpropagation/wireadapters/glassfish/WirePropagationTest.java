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

package org.glassfish.contextpropagation.wireadapters.glassfish;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.glassfish.contextpropagation.InsufficientCredentialException;
import org.glassfish.contextpropagation.PropagationMode;
//import org.glassfish.contextpropagation.adaptors.BootstrapUtils;
//import org.glassfish.contextpropagation.adaptors.MockLoggerAdapter;
import org.glassfish.contextpropagation.internal.Utils;
import org.glassfish.contextpropagation.spi.ContextMapHelper;
import org.glassfish.contextpropagation.spi.ContextMapPropagator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class WirePropagationTest {

//  @BeforeClass
//  public static void setup() throws InsufficientCredentialException {
//    BootstrapUtils.bootstrap(new DefaultWireAdapter());
//    BootstrapUtils.populateMap();
//  }
//
//  @Test
//  public void testPropagateOverWire() throws IOException {
//    ContextMapPropagator wcPropagator = ContextMapHelper.getScopeAwarePropagator();
//    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//    wcPropagator.sendRequest(baos, PropagationMode.SOAP);
//    MockLoggerAdapter.debug(Utils.toString(baos.toByteArray()));
//  }
}
