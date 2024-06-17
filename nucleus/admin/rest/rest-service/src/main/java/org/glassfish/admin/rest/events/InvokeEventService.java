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
package org.glassfish.admin.rest.events;

import jakarta.inject.Inject;
import org.glassfish.hk2.api.messaging.Topic;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author Ondro Mihalyi
 */
@Service
public class InvokeEventService {
    @Inject
    Topic<CommandInvokedEvent> commandInvokedTopic;

    public static InvokeEventService get() {
        return Globals.getDefaultHabitat().getService(InvokeEventService.class);
    }

    public Topic<CommandInvokedEvent> getCommandInvokedTopic() {
        return commandInvokedTopic;
    }

}
