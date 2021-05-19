/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.util.jmx;

import javax.management.MBeanAttributeInfo;

/**
 * This class contains various filters based on read/write status of
 * an Attribute.
 */
public class ReadWriteAttributeFilter implements AttributeFilter {

    protected ReadWriteAttributeFilter() {
    }


    public boolean filterAttribute(final MBeanAttributeInfo info) {
        throw new RuntimeException("Can't get here");
    }

    public static final ReadWriteAttributeFilter READ_ONLY_FILTER = new ReadWriteAttributeFilter() {

        public boolean filterAttribute(final MBeanAttributeInfo info) {
            return (info.isReadable() && !info.isWritable());
        }
    };

    public static final ReadWriteAttributeFilter READABLE_FILTER = new ReadWriteAttributeFilter() {

        public boolean filterAttribute(final MBeanAttributeInfo info) {
            return (info.isReadable());
        }
    };

    public static final ReadWriteAttributeFilter WRITE_ONLY_FILTER = new ReadWriteAttributeFilter() {

        public boolean filterAttribute(final MBeanAttributeInfo info) {
            return (info.isWritable() && !info.isReadable());
        }
    };

    public static final ReadWriteAttributeFilter WRITEABLE_FILTER = new ReadWriteAttributeFilter() {

        public boolean filterAttribute(final MBeanAttributeInfo info) {
            return (info.isWritable());
        }
    };

    public static final ReadWriteAttributeFilter READ_WRITE_FILTER = new ReadWriteAttributeFilter() {

        public boolean filterAttribute(final MBeanAttributeInfo info) {
            return (info.isWritable() && info.isReadable());
        }
    };

    public static final ReadWriteAttributeFilter ALL_FILTER = new ReadWriteAttributeFilter() {

        public boolean filterAttribute(final MBeanAttributeInfo info) {
            return (true);
        }
    };
}






