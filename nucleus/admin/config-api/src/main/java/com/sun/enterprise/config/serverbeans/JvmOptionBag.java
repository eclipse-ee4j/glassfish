/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.config.serverbeans;

import java.beans.PropertyVetoException;
import java.util.List;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Element;

/**
 * Factored out the list of jvm-options from at least two other interfaces that have them:
 * {@code java-config} and {@code profiler}.
 *
 * <p>Similar to {@link org.jvnet.hk2.config.types.PropertyBag}.
 */
public interface JvmOptionBag extends ConfigBeanProxy {

    @Element
    List<String> getJvmOptions();

    void setJvmOptions(List<String> options) throws PropertyVetoException;

    /**
     * It's observed that many a time we need the value of max heap size. This is useful
     * when deciding if a user is misconfiguring the JVM by specifying {@code -Xmx} that
     * is smaller than {@code -Xms}. Sun's JVM for example, bails out with
     * {@code Incompatible minimum and maximum heap sizes specified} when that happens.
     * It's generally better to do some basic validations in those cases and that's when
     * this method may be useful.
     *
     * <p><strong>Note:</strong> It does not take defaults into account. Also,
     * I have tested that server does not start with a heap that is less than 1m, so I think
     * I don't have to worry about -Xmx that is specified to be less than 1 MB. Again, there
     * is lots and lots of platform dependent code here, so this check should be minimal.
     * Again, I am doing this kind of check here because while testing, I was able to get into
     * a situation where -Xmx is configured smaller than -Xms and the server won't start. The user
     * then must edit the domain.xml by hand!
     *
     * @return an integer specifying the actual max heap memory ({@code -Xmx}) configured.
     * If it's specified as {@code -Xmx2g}, then {@code 2*1024} i.e. {@code 2048} is returned.
     * Returns {@code -1} if no {@code -Xmx} is specified.
     */
    default int getXmxMegs() {
        return getMemory("-Xmx");
    }

    /**
     * See #getXmxMegs.
     *
     * @return integer specifying {@code -Xms} in megabytes, or {@code -1}
     */
    default int getXmsMegs() {
        return getMemory("-Xms");
    }

    default boolean contains(String option) {
        return getJvmOptions().contains(option);
    }

    default String getStartingWith(String start) {
        for (String option : getJvmOptions()) {
            if (option.startsWith(start))
                return option;
        }
        return null;
    }

    private int getMemory(String which) {
        List<String> options = getJvmOptions();
        for (String option : options) {
            if (option.contains(which)) {
                return toMeg(option, which);
            }
        }
        return -1;
    }

    static int toMeg(String whole, String which) {
        String size = whole.substring(which.length());
        char unit = size.charAt(size.length() - 1);
        try {
            int sizeInMegabytes = Integer.parseInt(size.substring(0, size.length() - 1));
            if (unit == 'g' || unit == 'G') {
                return sizeInMegabytes * 1024; // I don't think we'll have an overflow
            } else if (unit == 'm' || unit == 'M') {
                return sizeInMegabytes;
            } else if (unit == 'k' || unit == 'K') {
                return sizeInMegabytes / 1024; // beware, integer division
            } else {
                return Integer.parseInt(size) / (1024 * 1024); // bytes, this is a rare case, hopefully -- who does -Xmx1073741824 to specify a meg?
            }
        } catch (RuntimeException e) {
            // squelch all exceptions
            return -1;
        }
    }
}
