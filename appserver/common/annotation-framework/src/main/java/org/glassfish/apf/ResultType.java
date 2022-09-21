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

package org.glassfish.apf;

/**
 * Defines the result from a single annotation processing, UNPROCESSED mean the
 * annotation has not been processed by any handler yet, PROCESSED mean that all
 * processing had been done and the annotation was fully processed, FAILED
 * indicate an error condition.
 *
 * @author Jerome Dochez
 */
public enum ResultType {

    /**
     * Annotation has not been processed
     */
    UNPROCESSED,
    /**
     * Annotation has been processed successfully
     */
    PROCESSED,
    /**
     * Annotation has been processed unsuccessfully
     */
    FAILED
}
