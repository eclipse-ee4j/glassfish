/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.cciblackbox;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.MappedRecord;

/**
 * This implementation class is used for creating IndexedRecord or MappedRecord
 * instances.
 */

public class CciRecordFactory implements jakarta.resource.cci.RecordFactory {

  public CciRecordFactory() {
  }

  public MappedRecord createMappedRecord(String recordName) throws ResourceException {
    throw new ResourceException("MappedRecord not supported.");
  }

  public IndexedRecord createIndexedRecord(String recordName) throws ResourceException {
    return new CciIndexedRecord(recordName);
  }

}
