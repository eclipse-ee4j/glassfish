/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina;


import jakarta.servlet.http.HttpSession;

import java.security.Principal;
import java.util.Iterator;
import java.util.Map;


/**
 * A <b>Session</b> is the Catalina-internal facade for an
 * <code>HttpSession</code> that is used to maintain state information
 * between requests for a particular user of a web application.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2005/12/08 01:27:20 $
 */

public interface Session {


    // ----------------------------------------------------- Manifest Constants


    /**
     * The SessionEvent event type when a session is created.
     */
    String SESSION_CREATED_EVENT = "createSession";


    /**
     * The SessionEvent event type when a session is destroyed.
     */
    String SESSION_DESTROYED_EVENT = "destroySession";


    // ------------------------------------------------------------- Properties


    /**
     * Return the authentication type used to authenticate our cached
     * Principal, if any.
     */
    String getAuthType();


    /**
     * Set the authentication type used to authenticate our cached
     * Principal, if any.
     *
     * @param authType The new cached authentication type
     */
    void setAuthType(String authType);


    /**
     * Return the creation time for this session.
     */
    long getCreationTime();


    /**
     * Set the creation time for this session.  This method is called by the
     * Manager when an existing Session instance is reused.
     *
     * @param time The new creation time
     */
    void setCreationTime(long time);


    /**
     * Return the session identifier for this session.
     */
    String getId();


    /**
     * Return the session identifier for this session.
     */
    String getIdInternal();


    /**
     * Set the session identifier for this session.
     *
     * @param id The new session identifier
     */
    void setId(String id);


    /**
     * Return descriptive information about this Session implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    String getInfo();


    /**
     * Return the last time the client sent a request associated with this
     * session, as the number of milliseconds since midnight, January 1, 1970
     * GMT.  Actions that your application takes, such as getting or setting
     * a value associated with the session, do not affect the access time.
     */
    long getLastAccessedTime();


    /**
     * Return the Manager within which this Session is valid.
     */
    Manager getManager();


    /**
     * Set the Manager within which this Session is valid.
     *
     * @param manager The new Manager
     */
    void setManager(Manager manager);


    /**
     * Return the maximum time interval, in seconds, between client requests
     * before the servlet container will invalidate the session.  A negative
     * time indicates that the session should never time out.
     */
    int getMaxInactiveInterval();


    /**
     * Set the maximum time interval, in seconds, between client requests
     * before the servlet container will invalidate the session.  A negative
     * time indicates that the session should never time out.
     *
     * @param interval The new maximum interval
     */
    void setMaxInactiveInterval(int interval);


    /**
     * Set the <code>isNew</code> flag for this session.
     *
     * @param isNew The new value for the <code>isNew</code> flag
     */
    void setNew(boolean isNew);


    /**
     * Return the authenticated Principal that is associated with this Session.
     * This provides an <code>Authenticator</code> with a means to cache a
     * previously authenticated Principal, and avoid potentially expensive
     * <code>Realm.authenticate()</code> calls on every request.  If there
     * is no current associated Principal, return <code>null</code>.
     */
    Principal getPrincipal();


    /**
     * Set the authenticated Principal that is associated with this Session.
     * This provides an <code>Authenticator</code> with a means to cache a
     * previously authenticated Principal, and avoid potentially expensive
     * <code>Realm.authenticate()</code> calls on every request.
     *
     * @param principal The new Principal, or <code>null</code> if none
     */
    void setPrincipal(Principal principal);


    /**
     * Return the <code>HttpSession</code> for which this object
     * is the facade.
     */
    HttpSession getSession();


    /**
     * Set the <code>isValid</code> flag for this session.
     *
     * @param isValid The new value for the <code>isValid</code> flag
     */
    void setValid(boolean isValid);


    /**
     * Expire the expired session if necessary and
     * return the <code>isValid</code> flag for this session.
     */
    boolean isValid();


    /**
     * Return the <code>isValid</code> flag for this session.
     */
    boolean getIsValid();


    // --------------------------------------------------------- Public Methods


    /**
     * Update the accessed time information for this session.  This method
     * should be called by the context when a request comes in for a particular
     * session, even if the application does not reference it.
     */
    void access();


    /**
     * Add a session event listener to this component.
     */
    void addSessionListener(SessionListener listener);


    /**
     * End access to the session.
     */
    void endAccess();


    /**
     * Perform the internal processing required to invalidate this session,
     * without triggering an exception if the session has already expired.
     */
    void expire();


    /**
     * Return the object bound with the specified name to the internal notes
     * for this session, or <code>null</code> if no such binding exists.
     *
     * @param name Name of the note to be returned
     */
    Object getNote(String name);


    /**
     * Return an Iterator containing the String names of all notes bindings
     * that exist for this session.
     */
    Iterator getNoteNames();


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    void recycle();


    /**
     * Remove any object bound to the specified name in the internal notes
     * for this session.
     *
     * @param name Name of the note to be removed
     */
    void removeNote(String name);


    /**
     * Remove a session event listener from this component.
     */
    void removeSessionListener(SessionListener listener);


    /**
     * Bind an object to a specified name in the internal notes associated
     * with this session, replacing any existing binding for this name.
     *
     * @param name Name to which the object should be bound
     * @param value Object to be bound to the specified name
     */
    void setNote(String name, Object value);


    // START SJSAS 6329289
    /**
     * Checks whether this Session has expired.
     *
     * @return true if this Session has expired, false otherwise
     */
    boolean hasExpired();
    // END SJSAS 6329289


    /**
     * Gets the version number of this Session
     */
    long getVersion();

    /**
     * Gets the attributes of this session.
     *
     * @return the attributes of this session
     */
    Map<String, Object> getAttributes();


    /**
     * Return the single sign on id.
     * It is null if there is no SSO.
     */
    String getSsoId();


    /**
     * Set the single sign on id.
     */
    void setSsoId(String ssoId);


    /**
     * Return the single sign on version.
     */
    long getSsoVersion();


    /**
     * Set the single sign on version.
     */
    void setSsoVersion(long ssoVersion);


    /**
     * lock the session for background
     * returns true if successful; false if unsuccessful
     */
    boolean lockForeground();


    /**
     * unlock the session from foreground
     */
    void unlockForeground();
}
