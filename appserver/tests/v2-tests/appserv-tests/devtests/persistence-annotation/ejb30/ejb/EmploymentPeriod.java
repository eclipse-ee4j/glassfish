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
 * EmploymentPeriod.java
 *
 * Created on February 23, 2005, 8:22 PM
 */

package com.sun.s1asdev.ejb.ejb30.hello.session;

import java.io.*;
import java.util.*;
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
@Embeddable
public class EmploymentPeriod implements Serializable {
    private Date start;
    private Date end;
    @Basic
    public Date getStartDate() { return start; }
    public void setStartDate(Date start) { this.start = start; }
    @Basic
    public Date getEndDate() { return end; }
    public void setEndDate(Date end) { this.end = end; }
}
