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
 * ReferenceKeyDesc.java
 *
 * Create on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.model;



/**
 * This class encapsulate the association of two KeyDescs.
 * It can be used to represent the inheritance key or 
 * the reference key between primary and secondary tables.
 */
public class ReferenceKeyDesc extends Object {
    private KeyDesc referencingKey;

    private KeyDesc referencedKey;

    private TableDesc table;

	public ReferenceKeyDesc(TableDesc table, 
							KeyDesc referencingKey,
							KeyDesc referencedKey) {
		this.referencingKey = referencingKey;
		this.referencedKey = referencedKey;
		this.table = table;
	}

	public KeyDesc getReferencingKey() {
		return referencingKey;
	}

	public KeyDesc getReferencedKey() {
		return referencedKey;
	}

	public TableDesc getTableDesc() {
		return table;
	}
}
