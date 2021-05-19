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

package org.glassfish.admingui.common.handlers;

import java.util.ArrayList;
import java.util.List;

/**
 *  <p>Tag Class.</p>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class Tag implements java.io.Serializable {

    private static final long serialVersionUID = 7437635853139196986L;
    private String tagViewId = null;
    private String tagName = null;
    private String displayName = null;
    private List<String> users = null;

    /**
     * <p> Default constructor.</p>
     */
    Tag() {
    }

    /**
     * <p> The constructor that should normally be used.</p>
     */
    public Tag(String tagName, String tagViewId, String displayName, String user) {
        this.tagName = tagName;
        this.tagViewId = tagViewId;
        this.displayName = displayName;
        if (user != null) {
            this.users = new ArrayList<String>();
            this.users.add(user);
        }
    }

    /**
     * <p> Allows an additional user to be added as a Tag creator.</p>
     */
    public void addUser(String name) {
        if (users == null) {
            users = new ArrayList<String>();
        }
        users.add(name);
    }

    /**
     * <p> Provides access to all the users that have created this Tag.  This
     *     may be null.</p>
     */
    public List<String> getUsers() {
        return users;
    }

    /**
     *  <p>Checks to see if the given user is an owner of this Tag.</p>
     */
    public boolean containsUser(String name) {
        return (users == null) ? false : users.contains(name);
    }

    /**
     *  <p> This method ensures the specified <code>user</code> is removed
     *     from the list of users for this <code>Tag</code>.</p>
     *
     *  <p> While a <code>Tag</code> is of little or no use when 0 users own
     *     the <code>Tag</code>, it is not the responsibility of this method
     *     to remove the <code>Tag</code> if this state occurs as a result of
     *     a call to this method.</p>
     *
     * @return        The <code>List</code> of users remaining after removing this
     *         user, or <code>null</code> if none.
     */
    public List<String> removeUser(String name) {
        if (users != null) {
            users.remove(name);
            if (users.size() == 0) {
                users = null;
            }
        }
        return users;
    }

    /**
     * <p> This implementation of equals only checks the tagName and the
     *     tagViewId for equality.  This means 2 tags with different user
     *     Lists are still considered equal.  The Display Name is also of no
     *     importance to this implementation of equality.</p>
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof Tag) {
            Tag testTag = (Tag) obj;
            result = getTagName().equals(testTag.getTagName()) && getTagViewId().equals(testTag.getTagViewId());
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.tagViewId != null ? this.tagViewId.hashCode() : 0);
        hash = 89 * hash + (this.tagName != null ? this.tagName.hashCode() : 0);
        hash = 89 * hash + (this.displayName != null ? this.displayName.hashCode() : 0);
        return hash;
    }

    /**
     *  <p>String representation of this Tag.</p>
     */
    @Override
    public String toString() {
        return "[" + getTagName() + ", " + getTagViewId() + ", " + getDisplayName() + ", Users: {" + users + "}]";
    }

    /**
     * <p> This provides access to the tag name.</p>
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * <p> This provides access to the TagViewId value.</p>
     */
    public String getTagViewId() {
        return tagViewId;
    }

    /**
     * <p> This returns a <code>String</code> that is meaningful to the user
     *     which represents the content of this <code>Tag</code> instance.</p>
     */
    public String getDisplayName() {
        // FIXME: I have I18N concerns about this... perhaps it's acceptible to
        // FIXME: store a String localized at the time the page is tagged.
        // FIXME: Multiple language environments may not like this.  To fix
        // FIXME: this correctly, we not only need the ValueExpression (i.e.
        // FIXME: #{i18n.foo}), but we also need the resource bundle to use.
        return displayName;
    }
}
