/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate what type of lock to acquire before executing an admin command. By default (witout this
 * annotation), admin commands acquire a shared lock, allowing multiple admin commands to execute in parallel. Some
 * commands, such as the synchronization command and the quiesce command require the exclusive lock to prevent any other
 * admin commands from executing. Admin commands that are "read-only" and don't change any configuration state don't
 * need any lock.
 *
 * @author Bill Shannon
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommandLock {

    /**
     * The type of command lock.
     */
    public enum LockType {
        NONE, SHARED, EXCLUSIVE
    }

    /**
     * Returns the type of lock to acquire.
     *
     * @return the lock type
     */
    LockType value();
}
