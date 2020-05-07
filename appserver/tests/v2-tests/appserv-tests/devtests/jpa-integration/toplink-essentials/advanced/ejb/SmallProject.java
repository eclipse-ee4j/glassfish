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



package oracle.toplink.essentials.testing.models.cmp3.advanced;

import jakarta.persistence.*;

/**
 * Local interface for the small project bean.
 * This is the bean's public/local interface for the clients usage.
 * All locals must extend the jakarta.ejb.EJBLocalObject.
 * The bean itself does not have to implement the local interface, but must implement all of the methods.
 */
@Entity
@Table(name="CMP3_PROJECT")
@DiscriminatorValue("S")
public class SmallProject extends Project {
}
