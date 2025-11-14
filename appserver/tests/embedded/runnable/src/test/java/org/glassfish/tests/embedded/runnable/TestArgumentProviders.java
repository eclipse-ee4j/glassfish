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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

/**
 *
 * @author Ondro Mihalyi
 */
public class TestArgumentProviders {

    public static class GfEmbeddedJarNameProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations declarations, ExtensionContext ec) throws Exception {
            List<Arguments> arguments = new ArrayList<>();
            arguments.add(Arguments.of("glassfish-embedded-all.jar"));
            if (!GfEmbeddedUtils.isDebugEnabled()) {
                arguments.add(Arguments.of("glassfish-embedded-web.jar"));
                arguments.add(Arguments.of("glassfishXX/glassfish/lib/embedded/glassfish-embedded-static-shell.jar"
                        .replace("XX", getGlassFishMajorVersion())));
            }
            return arguments.stream();
        }

        private static String getGlassFishMajorVersion() {
            String versionProperty = "glassfish.version";
            String version = System.getProperty(versionProperty);
            if (version != null) {
                return version.split("\\.")[0];
            } else {
                throw new IllegalArgumentException("The " + versionProperty + " system property is not defined. It should define the GlassFish version");
            }
        }

    }

}
