/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package demo.client;

import demo.Greeter;

import java.net.URI;

import org.glassfish.orb.http.client.ClientConfiguration;
import org.glassfish.orb.http.client.EjbLocator;
import org.glassfish.orb.http.client.HttpEjbClient;

/**
 * Calls a remote EJB on a running GlassFish over the HTTP transport.
 *
 * <p>The unit tests round-trip this transport against a fake container over a
 * real HTTP server, which proves the wire and the dispatch. What they cannot
 * prove is the half that only exists in a server: the module's OSGi manifest,
 * the endpoint being mounted on the listener, the container resolving a name
 * to a deployed bean, and the deployment's own classloader on both sides of
 * the codec. Every defect the Fory gRPC live job found was in that half.
 *
 * <p>The codec is named rather than left to negotiation. A client that names
 * one is never walked back to another - {@code HttpEjbClient} downgrades only
 * when it was given a free choice - so a run asked to speak Fory either speaks
 * Fory or fails, and cannot quietly repeat the run before it.
 *
 * <pre>
 * ejb-http-live-client -target http://host:port -codec jser|fory \
 *     -app greeter -module greeter -bean GreeterBean -name Ada -expect "Hello Ada"
 * </pre>
 */
public final class EjbHttpLiveClient {

    private EjbHttpLiveClient() {
    }

    public static void main(String[] args) {
        Arguments arguments = Arguments.of(args);
        ClientConfiguration config = ClientConfiguration
                .builder(URI.create(arguments.target))
                .codec(arguments.codec)
                .build();

        String reply;
        try (HttpEjbClient client = new HttpEjbClient(config)) {
            Greeter greeter = client.createProxy(Greeter.class,
                    new EjbLocator(arguments.app, arguments.module, null, arguments.bean));
            reply = greeter.sayHello(arguments.name);
        } catch (Exception e) {
            // A proxy wraps anything undeclared, so the exception printed here
            // would otherwise be UndeclaredThrowableException and nothing
            // else. What a failing job needs is the cause at the bottom.
            fail(arguments.codec + ": calling the bean failed: " + describe(e));
            return;
        }

        System.out.println(arguments.codec + ": " + reply);
        if (!arguments.expect.isEmpty() && !arguments.expect.equals(reply)) {
            fail(arguments.codec + ": expected \"" + arguments.expect + "\", got \"" + reply + '"');
        }
    }

    private static void fail(String message) {
        System.err.println(message);
        System.exit(1);
    }

    /** The exception and every cause under it, outermost first. */
    private static String describe(Throwable failure) {
        StringBuilder out = new StringBuilder();
        for (Throwable t = failure; t != null; t = t.getCause()) {
            if (out.length() > 0) {
                out.append("\n  caused by: ");
            }
            out.append(t);
            if (t.getCause() == t) {
                break;
            }
        }
        return out.toString();
    }

    /** The named arguments, with the defaults the live job uses. */
    private static final class Arguments {
        private String target = "http://localhost:8080/glassfish-services";
        private String codec = "jser";
        private String app = "greeter";
        private String module = "greeter";
        private String bean = "GreeterBean";
        private String name = "Ada";
        private String expect = "";

        static Arguments of(String[] args) {
            Arguments arguments = new Arguments();
            for (int i = 0; i < args.length - 1; i += 2) {
                String value = args[i + 1];
                switch (args[i]) {
                    case "-target" -> arguments.target = value;
                    case "-codec" -> arguments.codec = value;
                    case "-app" -> arguments.app = value;
                    case "-module" -> arguments.module = value;
                    case "-bean" -> arguments.bean = value;
                    case "-name" -> arguments.name = value;
                    case "-expect" -> arguments.expect = value;
                    default -> throw new IllegalArgumentException("unknown argument: " + args[i]);
                }
            }
            return arguments;
        }
    }
}
