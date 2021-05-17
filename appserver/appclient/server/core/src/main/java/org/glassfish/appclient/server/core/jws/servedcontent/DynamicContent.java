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

package org.glassfish.appclient.server.core.jws.servedcontent;

import java.util.Date;
import java.util.Properties;

/**
 * Prescribes the contract for dynamic content.
 * <p>
 * Each DynamicContent reports its MIME type (for use in setting the MIME type
 * in the HTTP response back to the client).
 * <p>
 * Further, each DynamicContent object must return an "instance" which
 * represents a version of the dynamic content at a given point in time.
 * It is open to the implementer whether the DynamicContent returns a
 * newly-created Instance each time or whether it caches some number of
 * instances as an optimization.
 *
 * @author tjquinn
 */
public interface DynamicContent extends Content {

    /**
     * Retrieves an "instance" of this dynamic content, with placeholders
     * substituted using the provided properties.
     * @param tokenValues maps placeholder tokens to values
     * @return matching Instance; null if no matching instance exists
     */
    public Instance getExistingInstance(Properties tokenValues);

    /**
     * Retrieves an existing "instance" of this dynamic content, with placeholders
     * substituted, creating a new one if none already exists.
     * @param tokenValues maps placeholder tokens to values
     * @return matching or newly-created Instance
     */

    public Instance getOrCreateInstance(Properties tokenValues);
    /**
     * Retrieve the MIME type for this dynamic content.
     * @return
     */
    public String getMimeType();

    /**
     * Reports whether this dynamic content represents the main JNLP document
     * for an app client.
     * @return
     */
    public boolean isMain();

    /**
     * Defines the contract for a given version of dynamic content at a single
     * moment in time.
     */
    public interface Instance {

        /**
         * Returns the text of the dynamic content instance.
         * @return
         */
        public String getText();

        /**
         * Returns the timestamp when the instance was created.
         * @return
         */
        public Date getTimestamp();
    }

    /**
     * Convenience implementation of Instance.
     */
    public static class InstanceAdapter implements Instance {
        /** when this instance was created */
        private final Date timestamp;

        /** the content of this instance */
        private final String text;

        /**
         *Creates a new instance of InstanceImpl (!) holding the result of a
         *specific substitution of values for placeholders.
         *@param text the content for this new InstanceImpl
         */
        public InstanceAdapter(final String text) {
            this.text = text;
            timestamp = new Date();
        }

        /**
         *Returns the time stamp associated with this content InstanceImpl.
         *@return the Date representing when this InstanceImpl was created
         */
        public Date getTimestamp() {
            return timestamp;
        }

        /**
         *Returns the content associated with this InstanceImpl.
         *@return the text content stored in the InstanceImpl
         */
        public String getText() {
            return text;
        }
    }
}
