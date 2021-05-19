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

/*
 * Employee.java
 *
 * Created on February 23, 2005, 8:22 PM
 */

package com.acme;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import jakarta.persistence.*;
import static jakarta.persistence.GeneratorType.*;
import static jakarta.persistence.AccessType.*;
/**
 *
 * @author ss141213
 */
@Entity(access=FIELD)
public class Customer {
    @Id(generate=AUTO)
    protected Long id;
    @Version protected int version;
    @ManyToOne protected Address address;
    @Basic protected String description;
    @OneToMany(targetEntity="com.acme.Order", mappedBy="customer")
    protected Collection orders = new Vector();
    @ManyToMany(mappedBy="customers")
    protected Set<DeliveryService> serviceOptions = new HashSet();
}
