/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2011 Ken Paulsen
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

package com.sun.jsft.facelets;

import com.sun.jsft.event.Command;
import com.sun.jsft.event.CommandEventListener;
import com.sun.jsft.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.event.PostConstructViewMapEvent;
import jakarta.faces.event.PostRestoreStateEvent;
import jakarta.faces.event.PostValidateEvent;
import jakarta.faces.event.PreDestroyViewMapEvent;
import jakarta.faces.event.PreRemoveFromViewEvent;
import jakarta.faces.event.PreRenderComponentEvent;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.event.PreValidateEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This is the TagHandler for the jsft:event tag.
 * </p>
 *
 * Created March 29, 2011
 *
 * @author Ken Paulsen (kenapaulsen@gmail.com)
 */
public class EventHandler extends TagHandler {

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public EventHandler(TagConfig config) {
        super(config);

        this.type = this.getRequiredAttribute("type");
        if (!(config.getNextHandler().getClass().getName().equals("com.sun.faces.facelets.compiler.UIInstructionHandler"))) {
            // This occurs when an empty jsft:event tag is used... ignore
            return;
        }

        // Create a CommandReader
        CommandReader reader = new CommandReader(config.getNextHandler().toString());

        // Read the Commands
        try {
            commands = reader.read();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to parse Commands for event type '" + type + "'.", ex);
        }
    }

    /**
     * <p>
     * This method is responsible for queueing up the EL that should be invoked when the event is fired.
     * </p>
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (ComponentHandler.isNew(parent)) {
            Class<? extends SystemEvent> eventClass = getEventClass(ctx);
            // ensure that f:event can be used anywhere on the page for
            // these events, not just as a direct child of the viewRoot
            if ((PreRenderViewEvent.class == eventClass) || (PostConstructViewMapEvent.class == eventClass)
                    || (PreDestroyViewMapEvent.class == eventClass)) {
                parent = ctx.getFacesContext().getViewRoot();
            }
            if ((eventClass != null) && (parent != null)) {
                parent.subscribeToEvent(eventClass, new CommandEventListener(this.commands));
            }
        } else {
            // already done...
        }
    }

    /**
     * <p>
     * This method returns the event <code>Class</code>. Many event types have short aliases that are recognized by this
     * method, others may need the fully qualified classname. The supported types are:
     * </p>
     *
     * <ul>
     * <li>afterCreate</li>
     * <li>afterCreateView</li>
     * <li>afterValidate</li>
     * <li>beforeEncode</li>
     * <li>beforeEncodeView
     * <li>
     * <li>beforeValidate</li>
     * <li>preRenderComponent</li>
     * <li>jakarta.faces.event.PreRenderComponent</li>
     * <li>preRenderView</li>
     * <li>jakarta.faces.event.PreRenderView</li>
     * <li>postAddToView</li>
     * <li>jakarta.faces.event.PostAddToView</li>
     * <li>preValidate</li>
     * <li>jakarta.faces.event.PreValidate</li>
     * <li>postValidate</li>
     * <li>jakarta.faces.event.PostValidate</li>
     * <li>preRemoveFromView</li>
     * <li>jakarta.faces.event.PreRemoveFromViewEvent</li>
     * <li>postRestoreState</li>
     * <li>jakarta.faces.event.PostRestoreStateEvent</li>
     * <li>postConstructViewMap</li>
     * <li>jakarta.faces.event.PostConstructViewMapEvent</li>
     * <li>preDestroyViewMap</li>
     * <li>jakarta.faces.event.PreDestroyViewMapEvent</li>
     * </ul>
     *
     * @param ctx The <code>FaceletContext</code>.
     *
     * @return The <code>SystemEvent</code> class associated with the event type.
     */
    protected Class<? extends SystemEvent> getEventClass(FaceletContext ctx) {
        String eventType = (String) this.type.getValueExpression(ctx, String.class).getValue(ctx);
        if (eventType == null) {
            throw new FacesException("Attribute 'type' can not be null!");
        }

        // Check the pre-defined types / aliases
        Class cls = eventAliases.get(eventType);

        if (cls == null) {
            // Not found, try reflection...
            try {
                cls = Util.loadClass(eventType, eventType);
            } catch (ClassNotFoundException ex) {
                throw new FacesException("Invalid event type: " + eventType, ex);
            }
        }

        // Return the result...
        return cls;
    }

    /**
     * <p>
     * Print out the <code>Command</code>.
     * </p>
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("");
        Iterator<Command> it = commands.iterator();
        while (it.hasNext()) {
            buf.append(it.next().toString());
        }
        return buf.toString();
    }

    private static Map<String, Class<? extends SystemEvent>> eventAliases = new HashMap<>(20);
    static {
        eventAliases.put("beforeEncode", PreRenderComponentEvent.class);
        eventAliases.put("preRenderComponent", PreRenderComponentEvent.class);
        eventAliases.put("jakarta.faces.event.PreRenderComponent", PreRenderComponentEvent.class);

        eventAliases.put("beforeEncodeView", PreRenderViewEvent.class);
        eventAliases.put("preRenderView", PreRenderViewEvent.class);
        eventAliases.put("jakarta.faces.event.PreRenderView", PreRenderViewEvent.class);

        eventAliases.put("afterCreate", PostAddToViewEvent.class);
        eventAliases.put("postAddToView", PostAddToViewEvent.class);
        eventAliases.put("jakarta.faces.event.PostAddToView", PostAddToViewEvent.class);

        eventAliases.put("afterCreateView", PostRestoreStateEvent.class);
        eventAliases.put("postRestoreState", PostRestoreStateEvent.class);
        eventAliases.put("jakarta.faces.event.PostRestoreStateEvent", PostRestoreStateEvent.class);

        eventAliases.put("beforeValidate", PreValidateEvent.class);
        eventAliases.put("preValidate", PreValidateEvent.class);
        eventAliases.put("jakarta.faces.event.PreValidate", PreValidateEvent.class);

        eventAliases.put("afterValidate", PostValidateEvent.class);
        eventAliases.put("postValidate", PostValidateEvent.class);
        eventAliases.put("jakarta.faces.event.PostValidate", PostValidateEvent.class);

        eventAliases.put("preRemoveFromView", PreRemoveFromViewEvent.class);
        eventAliases.put("jakarta.faces.event.PreRemoveFromViewEvent", PreRemoveFromViewEvent.class);
        eventAliases.put("postConstructViewMap", PostConstructViewMapEvent.class);
        eventAliases.put("jakarta.faces.event.PostConstructViewMapEvent", PostConstructViewMapEvent.class);
        eventAliases.put("preDestroyViewMap", PreDestroyViewMapEvent.class);
        eventAliases.put("jakarta.faces.event.PreDestroyViewMapEvent", PreDestroyViewMapEvent.class);
        /*
         * FIXME: Look at supporting these too... Non component, system events: postConstructApplication ActionEvent... hmm...
         * ValueChangedEvent exceptionQueued BehaviorEvent - AjaxBehaviorEvent
         */
    }

    protected final TagAttribute type;
    protected List<Command> commands = new ArrayList<>(5);
}
