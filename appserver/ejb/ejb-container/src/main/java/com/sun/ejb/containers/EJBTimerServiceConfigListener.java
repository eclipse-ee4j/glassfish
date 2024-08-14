/*
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

package com.sun.ejb.containers;

import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.ejb.config.EjbTimerService;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * ConfigListener class for the EJB Timer Service changes
 *
 * @author Marina Vatkina
 */
@Service
public class EJBTimerServiceConfigListener implements ConfigListener {

    private static final Logger _logger = LogDomains.getLogger(
            EJBTimerServiceConfigListener.class, LogDomains.EJB_LOGGER);

    // Injecting @Configured type triggers the corresponding change
    // events to be sent to this instance
    @Inject private EjbTimerService ejbt;

    /****************************************************************************/
    /** Implementation of org.jvnet.hk2.config.ConfigListener *********************/
    /****************************************************************************/
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {

        // Events that we can't process now because they require server restart.
        List<UnprocessedChangeEvent> unprocessedEvents = new ArrayList<UnprocessedChangeEvent>();

        for (PropertyChangeEvent event : events) {
            if (event.getSource() instanceof EjbTimerService) {
                Object oldValue = event.getOldValue();
                Object newValue = event.getNewValue();

                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "Got EjbTimerService change event ==== "
                            + event.getSource() + " "
                            + event.getPropertyName() + " " + oldValue + " " + newValue);
                }

                if (oldValue != null && oldValue.equals(newValue)) {
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.log(Level.FINE, "Event " + event.getPropertyName()
                                + " did not change existing value of " + oldValue);
                    }
                } else {
                    unprocessedEvents.add(new UnprocessedChangeEvent(event, "Restart required to reconfigure EJB Timer Service"));
                }
            }
        }

        return (unprocessedEvents.size() > 0)
                ? new UnprocessedChangeEvents(unprocessedEvents) : null;

    }
}
