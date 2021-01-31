/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Contract;

/**
 * Interface for defining aspects for AdminCommands. This is used with the CommandAspect annotation to implement an
 * annotation that can be used to add functionality around commands. See the @Async annotation for an example of how
 * this is used.
 *
 * See empty CommandAspectBase implementation to extend.
 *
 * @author andriy.zhdanov
 *
 * @param <T> aspect annotation.
 */
@Contract
public interface CommandAspectImpl<T extends Annotation> {

    /**
     * Execute when command is just completely initialized, i..e injected with parameters.
     */
    void init(T ann, AdminCommand command, AdminCommandContext context, Job instance);

    /**
     * Execute when command is finished successfully or not.
     */
    void done(T ann, AdminCommand command, Job instance);

    /**
     * This methods can be used to wrap generic functionality around command execute.
     */
    AdminCommand createWrapper(T ann, CommandModel model, AdminCommand command, ActionReport report);
}
