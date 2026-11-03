/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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

/**
 * Generator classes that write configuration files and source files based on
 * the component and renderer info found during annotation processing.
 *
 * A generator is invoked in three phases. First, the generator's {@code init()}
 * method will be called, and component or renderer info will be passed to the
 * generator. The exact parameters vary from generator to generator. Next, the
 * annotation processor will ask the generator for information about the
 * configuration or source file to be created, such as its name. Finally, the
 * annotation process will create an appropriate writer to which the file may be
 * written, and call {@code Generator.generate(PrintWriter)}.
 */
package com.sun.faces.mirror.generator;
