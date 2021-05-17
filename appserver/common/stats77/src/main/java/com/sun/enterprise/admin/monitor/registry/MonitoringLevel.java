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

package com.sun.enterprise.admin.monitor.registry;

/**
 * Provides enumerated constants related to various levels
 * at which monitoring could be set
 * @author  Shreedhar Ganapathy<mailto:shreedhar.ganapathy@sun.com>
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 */
public class MonitoringLevel {

    public static final MonitoringLevel OFF  = new MonitoringLevel("OFF");
    public static final MonitoringLevel LOW  = new MonitoringLevel("LOW");
    public static final MonitoringLevel HIGH = new MonitoringLevel("HIGH");

    private final String name;

    /**
     * Constructor
     */
    private MonitoringLevel(String name ) {
        this.name = name;
    }

    public String toString() {
        return ( name );
    }

    /**
     * Returns an instance of MonitoringLevel for the given String.
     * The given String has to correspond to one of the public fields declared
     * in this class.
     *
     * @param name String representing the MonitoringLevel
     * @return MonitoringLevel corresponding to given parameter, or null
     * if the parameter is null or does not correspond to any of the
     * Monitoring Levels supported.
     * For $Revision: 1.2 $ of this class, "off", "high" and "low" are
     * supported strings. The comparison is done case insensitively.
     */
    public static MonitoringLevel instance(String name) {
        if (OFF.toString().equalsIgnoreCase(name))
            return ( OFF );
        else if (LOW.toString().equalsIgnoreCase(name))
            return ( LOW );
        else if (HIGH.toString().equalsIgnoreCase(name))
            return ( HIGH );
        return ( null );
    }

    /**
     * Checks two MonitoringLevel objects for equality.
     *
     * <p>Checks that <i>obj</i> is a MonitoringLevel, and has the same name as
     * this object.
     *
     * @param obj the object we are testing for equality with this object.
     * @return true if obj is a MonitoringLevel, and has the same name as this
     * MonitoringLevel object.
     */
    public boolean equals(Object obj) {
    if (obj == this)
        return true;

    if (! (obj instanceof MonitoringLevel))
        return false;

    MonitoringLevel that = (MonitoringLevel) obj;

    return (this.name.equals(that.name));
    }

    /**
     * Returns the hash code value for this object.
     *
     * <p>The hash code returned is the hash code of the name of this
     * MonitoringLevel object.
     *
     * @return Hash code value for this object.
     */
    public int hashCode() {
    return this.name.hashCode();
    }

}
