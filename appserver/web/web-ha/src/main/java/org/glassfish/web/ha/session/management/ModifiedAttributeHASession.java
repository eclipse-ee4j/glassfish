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

/*
 * ModifiedAttributeHASession.java
 *
 * Created on October 3, 2002, 3:45 PM
 */

package org.glassfish.web.ha.session.management;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Manager;
import org.apache.catalina.util.Enumerator;


/**
 *
 * @author  lwhite
 * @author Rajiv Mordani
 */
public class ModifiedAttributeHASession extends BaseHASession {

    private static final Logger _logger = HAStoreBase._logger;

    private transient Map<String, SessionAttributeState> _attributeStates = new HashMap<String, SessionAttributeState>();
    private transient boolean _dirtyFlag = false;


    /** Creates a new instance of ModifiedAttributeHASession */
    public ModifiedAttributeHASession(Manager manager) {
        super(manager);
    }

    /**
     * return an ArrayList of Strings
     * whose elements are the names of the deleted attributes
     */
    public List<String> getDeletedAttributes() {

        List<String> resultList = new ArrayList<String>();
        for (Map.Entry<String, SessionAttributeState> entry : _attributeStates.entrySet()) {
            SessionAttributeState nextAttrState = entry.getValue();
            String nextAttrName = entry.getKey();
            if(nextAttrState.isDeleted() && nextAttrState.isPersistent()) {
                resultList.add(nextAttrName);
            }
        }
        return resultList;
    }

    /**
     * return an ArrayList of Strings
     * whose elements are the names of the modified attributes
     * attributes must dirty, persistent and not deleted
     */
    public List<String> getModifiedAttributes() {
        List<String> resultList = new ArrayList<String>();
        for (Map.Entry<String, SessionAttributeState> entry : _attributeStates.entrySet()) {
            SessionAttributeState nextAttrState = entry.getValue();
            String nextAttrName = entry.getKey();
            if(nextAttrState.isDirty()
                    && nextAttrState.isPersistent()
                    && (!nextAttrState.isDeleted())) {
                resultList.add(nextAttrName);
            }
        }
        return resultList;
    }

    /**
     * return an ArrayList of Strings
     * whose elements are the names of the added attributes
     */
    public List<String> getAddedAttributes() {
        List<String> resultList = new ArrayList<String>();
        for (Map.Entry<String, SessionAttributeState> entry : _attributeStates.entrySet()) {
            SessionAttributeState nextAttrState = entry.getValue();
            String nextAttrName = entry.getKey();
            if(!nextAttrState.isPersistent() && !nextAttrState.isDirty()) {
                resultList.add(nextAttrName);
            }
        }
        return resultList;
    }

    /**
     * return an ArrayList of Strings
     * whose elements are the names of the added attributes
     */
    public List<String> getAddedAttributesPrevious() {
        List<String> resultList = new ArrayList<String>();
        for (Map.Entry<String, SessionAttributeState> entry : _attributeStates.entrySet()) {
            SessionAttributeState nextAttrState = entry.getValue();
            String nextAttrName = entry.getKey();
            if(!nextAttrState.isPersistent()) {
                resultList.add(nextAttrName);
            }
        }
        return resultList;
    }

    /**
     * clear (empty) the attributeStates
     */
    void clearAttributeStates() {
        if(_attributeStates == null) {
            _attributeStates = new HashMap<String, SessionAttributeState>();
        }
        _attributeStates.clear();
    }

    //this method should only be used for testing
    public void privateResetAttributeState() {
        this.resetAttributeState();
    }

    /**
     * this method called when session is loaded from persistent store
     * or after session state was stored
     * note: pre-condition is that the removed attributes have been
     * removed from _attributeStates; this is taken care of by removeAttribute
     * method
     */
    void resetAttributeState() {
        clearAttributeStates();
        Enumeration<String> attrNames = getAttributeNames();
        while(attrNames.hasMoreElements()) {
            String nextAttrName = attrNames.nextElement();
            SessionAttributeState nextAttrState =
                SessionAttributeState.createPersistentAttribute();
            _attributeStates.put(nextAttrName, nextAttrState);
        }
        setDirty(false);
    }

    /**
     * set the attribute name to the value value
     * and update the attribute state accordingly
     * @param name
     * @param value
     */
    public void setAttribute(String name, Object value) {
        super.setAttribute(name, value);
        SessionAttributeState attributeState = getAttributeState(name);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("ModifiedAttributeHASession>>setAttribute name=" + name + " attributeState=" + attributeState);
        }
        if(value == null) {
            if(attributeState != null) {
                if(attributeState.isPersistent()) {
                    attributeState.setDeleted(true);
                } else {
                    removeAttributeState(name);
                }
            }
        } else {
            if(attributeState == null) {
                SessionAttributeState newAttrState =
                    new SessionAttributeState();
                //deliberately we do no make this newly added attribute dirty
                _attributeStates.put(name, newAttrState);
            } else {
                //if marked for deletion, only un-delete it
                //do not change the dirti-ness
                if(attributeState.isDeleted()) {
                    attributeState.setDeleted(false);
                } else {
                    //only mark dirty if already persistent
                    //else do nothing
                    if(attributeState.isPersistent()) {
                        attributeState.setDirty(true);
                    }
                }
            }
        }
        setDirty(true);
    }

    /**
     * remove the attribute name
     * and update the attribute state accordingly
     * @param name
     */
    public void removeAttribute(String name) {


        super.removeAttribute(name);
        SessionAttributeState attributeState = getAttributeState(name);
        if(attributeState != null) {
            if(attributeState.isPersistent()) {
                attributeState.setDeleted(true);
            } else {
                removeAttributeState(name);
            }
        }
        setDirty(true);
    }

    /**
     * return the SessionAttributeState for attributeName
     * @param attributeName
     */
    SessionAttributeState getAttributeState(String attributeName) {
        return _attributeStates.get(attributeName);
    }

    /**
     * set the SessionAttributeState for attributeName
     * based on persistent value
     * @param attributeName
     * @param persistent
     */
    void setAttributeStatePersistent(String attributeName, boolean persistent) {

        SessionAttributeState attrState = _attributeStates.get(attributeName);
        if (attrState == null) {
                attrState = new SessionAttributeState();
                attrState.setPersistent(persistent);
                _attributeStates.put(attributeName, attrState);
        } else {
                attrState.setPersistent(persistent);
        }
    }

    /**
     * set the SessionAttributeState for attributeName
     * based on dirty value
     * @param attributeName
     * @param dirty
     */
    void setAttributeStateDirty(String attributeName, boolean dirty) {

        SessionAttributeState attrState = _attributeStates.get(attributeName);
        if (attrState == null) {
                attrState = new SessionAttributeState();
                attrState.setDirty(dirty);
                _attributeStates.put(attributeName, attrState);
        } else {
                attrState.setDirty(dirty);
        }
    }

    /**
     * remove the SessionAttributeState for attributeName
     * @param attributeName
     */
    void removeAttributeState(String attributeName) {
        _attributeStates.remove(attributeName);
    }

    /**
     * return isDirty
     */
    public boolean isDirty() {
        return _dirtyFlag;
    }

    /**
     * set isDirty
     * @param isDirty
     */
    public void setDirty(boolean isDirty) {
        _dirtyFlag = isDirty;
    }

    /* Private Helper method to be used in HAAttributeStore only */
    Enumeration<String> privateGetAttributeList() {

        return (new Enumerator<String>(new ArrayList<String>(attributes.keySet())));

    }
}
