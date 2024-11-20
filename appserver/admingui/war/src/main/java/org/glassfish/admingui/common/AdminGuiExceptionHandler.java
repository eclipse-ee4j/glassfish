/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.admingui.common;

import jakarta.faces.FacesException;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;

import java.io.IOException;
import java.util.Iterator;

public class AdminGuiExceptionHandler extends ExceptionHandlerWrapper {

    public AdminGuiExceptionHandler(ExceptionHandler wrapped) {
        super(wrapped);
    }

    @Override
    public void handle() throws FacesException {
        for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext eventContext = event.getContext();
            if (eventContext.getException() instanceof ViewExpiredException) {
                try {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    if (facesContext == null) {
                        continue;
                    }
                    ExternalContext externalContext = facesContext.getExternalContext();
                    if (facesContext.getPartialViewContext().isAjaxRequest()) {
                        externalContext.getResponseOutputWriter().write(
                                "<script type='text/javascript'>"
                                        + "window.location.href = '" + externalContext.getRequestContextPath() + "/';"
                                        + "</script>");
                        facesContext.responseComplete();
                    } else {
                        externalContext.redirect(externalContext.getRequestContextPath() + "/");
                    }
                } catch (IOException e) {
                    throw new FacesException(e);
                } finally {
                    i.remove();
                }
            }
        }
        getWrapped().handle();
    }
}
