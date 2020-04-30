/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.connectors.config;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Attribute;

import jakarta.validation.constraints.NotNull;
import java.beans.PropertyVetoException;

@Configured
public interface PrincipalMap extends ConfigBeanProxy {

    /**
     * Gets the value of the EISPrincipal - a Principal in the EIS
     * security domain that is being mapped to a Principal in the
     * application server's security domain.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(key=true)
    @NotNull
    public String getEisPrincipal();

    /**
     * Sets the value of the EISPrincipal - a Principal in the EIS
     * security domain that is being mapped to a Principal in the
     * application server's security domain.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEisPrincipal(String value) throws PropertyVetoException;

    /**
     * Gets the value of the Mapped Principal - A Principal that is
     * valid in the application server's security domain
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    public String getMappedPrincipal();

    /**
     * Sets the value of the Mapped Principal - A Principal that is
     * valid in the application server's security domain
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMappedPrincipal(String value) throws PropertyVetoException;

}
