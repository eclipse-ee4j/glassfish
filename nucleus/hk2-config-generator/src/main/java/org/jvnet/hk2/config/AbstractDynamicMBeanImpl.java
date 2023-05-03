/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

/**
 * Partial implementation of {@link DynamicMBean}.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractDynamicMBeanImpl implements DynamicMBean {

    @Override
    public final AttributeList getAttributes(String[] attributes) {
        AttributeList r = new AttributeList(attributes.length);
        for (String name : attributes) {
            Object value = null;
            try {
                value = getAttribute(name);
            } catch (AttributeNotFoundException e) {
                // error is reported as the lack of value
            } catch (MBeanException e) {
                // error is reported as the lack of value
            } catch (ReflectionException e) {
                // error is reported as the lack of value
            }
            r.add(new Attribute(name, value));
        }
        return r;
    }

    @Override
    public final AttributeList setAttributes(AttributeList attributes) {
        AttributeList r = new AttributeList(attributes.size());
        for (Object a : attributes) {
            try {
                setAttribute(Attribute.class.cast(a));
                r.add(Attribute.class.cast(a));
            } catch (AttributeNotFoundException e) {
                // error is silently ignored
            } catch (ReflectionException e) {
                // error is silently ignored
            } catch (MBeanException e) {
                // error is silently ignored
            } catch (InvalidAttributeValueException e) {
                // error is silently ignored
            }
        }
        return r;
    }
}
