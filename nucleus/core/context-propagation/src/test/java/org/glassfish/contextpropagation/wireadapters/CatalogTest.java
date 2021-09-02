/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.contextpropagation.wireadapters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CatalogTest {

  @Test
  public void testUpdateMeta() {
    byte dot = '.';
    Catalog cat = new Catalog();
    cat.add((short) 5);
    cat.add((short) 0x02FF);
    byte[] bytes = "..xxxx....".getBytes();
    cat.updateCatalogMetadata(bytes);
    assertEquals(dot, bytes[0]);
    assertEquals(dot, bytes[1]);
    assertEquals((byte) 0, bytes[2]);
    assertEquals((byte) 5, bytes[3]);
    assertEquals((byte) 2, bytes[4]);
    assertEquals((byte) 0xFF, bytes[5]);
  }

  @Test
  public void testSetMeta() {
    Catalog cat = new Catalog();
    cat.setMeta(0xABCD1234);
    assertEquals((short) 0xABCD, cat.start);
    assertEquals((short) 0x1234, cat.end);
  }

}
