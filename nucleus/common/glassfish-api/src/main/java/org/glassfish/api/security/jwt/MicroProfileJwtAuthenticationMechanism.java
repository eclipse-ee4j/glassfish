/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.api.security.jwt;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * CDI qualifier for injecting the GlassFish built-in MicroProfile JWT
 * Jakarta Security Authentication mechanism.
 *
 * <p>This qualifier allows direct injection of the GlassFish built-in MicroProfile JWT authentication mechanism.</p>
 *
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * @ApplicationScoped
 * public class SecurityService {
 *
 *     @Inject
 *     @MicroProfileJwtAuthenticationMechanism
 *     private HttpAuthenticationMechanism jwtAuthMechanism;
 *
 *     // Use the JWT authentication mechanism directly
 * }
 * }</pre>
 *
 * <h2>Features</h2>
 * <ul>
 * <li><strong>Direct Access:</strong> Direct access to the GlassFish JWT authentication mechanism</li>
 * <li><strong>CDI Integration:</strong> Seamless integration with CDI dependency injection</li>
 * <li><strong>Type Safety:</strong> Uses the standard {@code jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism} interface</li>
 * </ul>
 *
 * <h2>Combining with Other Mechanisms</h2>
 * <p>The {@code @MicroProfileJwtAuthenticationMechanism} qualifier is complementary to the qualifiers for the standard Jakarta Security
 * mechanisms like the Basic authentication mechanism. It can be combined with the other standard mechanisms
 * in the same {@code HttpAuthenticationMechanismHandler}.</p>
 *
 * <pre>{@code
 * @ApplicationScoped
 * @Alternative
 * @Priority(Interceptor.Priority.APPLICATION)
 * public class CustomAuthenticationHandler implements HttpAuthenticationMechanismHandler {
 *
 *     @Inject
 *     @MicroProfileJwtAuthenticationMechanism
 *     HttpAuthenticationMechanism jwtAuthentication;
 *
 *     @Inject
 *     @BasicAuthenticationMechanismDefinition.BasicAuthenticationMechanism
 *     HttpAuthenticationMechanism basicAuthentication;
 *
 *     @Override
 *     public AuthenticationStatus validateRequest(HttpServletRequest request,
 *                                               HttpServletResponse response,
 *                                               HttpMessageContext messageContext)
 *                                               throws AuthenticationException {
 *         // delegate to one of the two mechanisms
 *     }
 * }
 * }</pre>
 *
 * @since GlassFish 8.0
 */
@Qualifier
@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
@Documented
public @interface MicroProfileJwtAuthenticationMechanism {

    /**
     * Literal implementation for programmatic use.
     */
    final class Literal extends AnnotationLiteral<MicroProfileJwtAuthenticationMechanism>
            implements MicroProfileJwtAuthenticationMechanism {

        /**
         * Singleton instance of the literal.
         */
        public static final Literal INSTANCE = new Literal();
    }
}
