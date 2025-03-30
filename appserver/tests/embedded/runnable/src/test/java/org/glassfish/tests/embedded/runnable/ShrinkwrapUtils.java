/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.tests.embedded.runnable;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.formatter.Formatters;

/**
 *
 * @author Ondro Mihalyi
 */
public class ShrinkwrapUtils {

    public static void logArchiveContent(Archive javaArchive, String archiveName, Consumer<Supplier<String>> logHandler) throws IllegalArgumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        javaArchive.writeTo(outputStream, Formatters.VERBOSE);
        String archiveContentsAsString = outputStream.toString(StandardCharsets.UTF_8);
        logHandler.accept(() -> "Contents of " + archiveName + ":\n" + archiveContentsAsString);
    }
}
