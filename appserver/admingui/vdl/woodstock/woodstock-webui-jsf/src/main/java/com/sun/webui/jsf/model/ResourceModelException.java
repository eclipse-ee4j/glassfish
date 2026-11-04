/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.webui.jsf.model;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.validator.ValidatorException;

/**
 * Need to make this an interface as well.
 * And in our implementation log the exception as well.
 */
public final class ResourceModelException extends ValidatorException {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = -8267067702146533999L;

    /**
     * Create a new instance.
     * @param fmsg faces message
     */
    public ResourceModelException(final FacesMessage fmsg) {
        super(fmsg);
    }

    /**
     * Create a new instance.
     * @param fmsg faces message
     * @param cause cause
     */
    public ResourceModelException(final FacesMessage fmsg,
            final Throwable cause) {

        super(fmsg, cause);
    }
}
