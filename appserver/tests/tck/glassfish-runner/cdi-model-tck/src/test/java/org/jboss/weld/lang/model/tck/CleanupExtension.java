/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation. All rights reserved.
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
package org.jboss.weld.lang.model.tck;

import jakarta.annotation.Priority;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

public class CleanupExtension implements Extension {

    public void enhancement(@Priority(Integer.MAX_VALUE) @Observes ProcessAnnotatedType<?> pat) {
        if (LangModelExtension.ENHANCEMENT_INVOKED > 0 &&
            pat.getAnnotatedType().getJavaClass().getName().startsWith("org.jboss.cdi.lang.model.tck")) {
            pat.veto();
        }
    }
}
