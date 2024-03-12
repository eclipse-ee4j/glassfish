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

package org.glassfish.admingui.common.tree;
//

import com.sun.jsftemplating.layout.event.EventObjectBase;
import java.util.List;
import jakarta.faces.component.UIComponent;

/**
 *
 *  @author Ken Paulsen        (ken.paulsen@sun.com)
 */
public class FilterTreeEvent extends EventObjectBase {

    /**
     * <p> Constructor.</p>
     *
     * @param        component   The <code>UIComponent</code> associated with this
     *                     <code>EventObject</code>.
     */
    public FilterTreeEvent(UIComponent component, List childObjects) {
        super(component);
        setChildObjects(childObjects);
    }

    /**
     * <p> This method provides access to an array of Objects that are to
     *     become child <code>TreeNode</code>s.  This allows you to manipluate
     *     them (filter them) before they are processed.  You may return a new
     *     List from your handler that processes this event.  Note that
     *     you NOT set the child object array using this event.</p>
     */
    public List getChildObjects() {
        return _childObjects;
    }

    /**
     * <p> This method is protected because it is only meaningful to set this
     *     array during the creation of this event.  Setting it any other
     *     time would not effect the original data structure and would serve
     *     no purpose.  To provide a different object array, return a new
     *     <code>Object[]</code> from your handler that processes this
     *     event.</p>
     */
    protected void setChildObjects(List objects) {
        _childObjects = objects;
    }

    /**
     * <p> The "filterTree" event type. ("filterTree")</p>
     */
    public static final String        EVENT_TYPE  = "filterTree";

    private List _childObjects = null;
}
