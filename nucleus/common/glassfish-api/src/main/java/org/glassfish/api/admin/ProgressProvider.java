/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;

/**
 * Interface denoting administrative commands that provide their {@code Progress} annotation. It must be considered as
 * Managed using {@code Managed} annotation or by {@code CommandModelProvider}
 *
 * @author martinmares
 */
public interface ProgressProvider {

    Progress getProgress();

    public static class Utils {
        public static Progress provide(final String name, final int totalStepCount) {
            return new Progress() {
                @Override
                public String name() {
                    return name;
                }

                @Override
                public int totalStepCount() {
                    return totalStepCount;
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return Progress.class;
                }
            };
        }

    }

}
