/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.embeddable;

/**
 * Represents a GlassFish instance and provides the ability to:
 * <ul>
 * <li>perform life cycle operations viz., start, stop and dispose.</li>
 * <li>access {@link Deployer} to deploy/undeploy applications.</li>
 * <li>access {@link CommandRunner} to perform runtime configurations.</li>
 * <li>access to available service(s).</li>
 * </ul>
 * <p>
 * Usage example:
 *
 * <pre>{@code
    // Create and start GlassFish
    GlassFish glassfish = {@link GlassFishRuntime}.bootstrap().newGlassFish();
    glassfish.start();

    // Deploy a web application simple.war with /hello as context root.
    Deployer deployer = glassfish.getService(Deployer.class);
    String deployedApp = deployer.deploy(new File("simple.war").toURI(), "--contextroot=hello", "--force=true");

    // Run commands (as per your need). Here is an example to create
    // a http listener and dynamically set its thread pool size.
    CommandRunner commandRunner = glassfish.getService(CommandRunner.class);

    // Run a command create 'my-http-listener' to listen at 9090
    CommandResult commandResult = commandRunner.run(
            "create-http-listener", "--listenerport=9090",
            "--listeneraddress=0.0.0.0", "--defaultvs=server",
            "my-http-listener");

    // Run a command to create your own thread pool
    commandResult = commandRunner.run("create-threadpool",
            "--maxthreadpoolsize=200", "--minthreadpoolsize=200",
            "my-thread-pool");

    // Run a command to associate my-thread-pool with my-http-listener
    commandResult = commandRunner.run("set",
            "server.network-config.network-listeners.network-listener.my-http-listener.thread-pool=my-thread-pool");

    // Undeploy the application
    deployer.undeploy(deployedApp);

    /**Stop and dispose GlassFish.
    glassfish.stop();
    glassfish.dispose();
 * }</pre>
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface GlassFish {
    /**
     * Start GlassFish.
     * When this method is called, all the lifecycle (aka startup) services are started.
     * Calling this method while the server is in {@link Status#STARTED} state is a no-op.
     *
     * @throws IllegalStateException if server is already started.
     * @throws GlassFishException if server can't be started for some unknown reason.
     */
    void start() throws GlassFishException;

    /**
     * Stop GlassFish. When this method is called, all the lifecycle (aka startup) services are stopped.
     * GlassFish can be started again by calling the start method.
     * Calling this method while the server is in {@link Status#STARTED} state is a no-op.
     *
     * @throws IllegalStateException if server is already stopped.
     * @throws GlassFishException if server can't be started for some unknown reason.
     */
    void stop() throws GlassFishException;

    /**
     * Call this method if you don't need this GlassFish instance any more. This method will stop GlassFish
     * if not already stopped. After this method is called, calling any method except {@link #getStatus}
     * on the GlassFish object will cause an IllegalStateException to be thrown. When this method is called,
     * any resource (like temporary files, threads, etc.) is also released.
     */
    void dispose() throws GlassFishException;

    /**
     * Get the current status of GlassFish.
     *
     * @return Status of GlassFish
     */
    Status getStatus() throws GlassFishException;

    /**
     * A service has a service interface and optionally a name. For a service which is just a class with no interface,
     * then the service class is the service interface. This method is used to look up a service.
     * @param serviceType type of component required.
     * @param <T>
     * @return Return a service matching the requirement, null if no service found.
     */
    <T> T getService(Class<T> serviceType) throws GlassFishException;

    /**
     * A service has a service interface and optionally a name. For a service which is just a class with no interface,
     * then the service class is the service interface. This method is used to look up a service.
     * @param serviceType type of component required.
     * @param serviceName name of the component.
     * @param <T>
     * @return Return a service matching the requirement, null if no service found.
     */
    <T> T getService(Class<T> serviceType, String serviceName) throws GlassFishException;

    /**
     * Gets a Deployer instance to deploy an application.
     * Each invocation of this method returns a new Deployer object.
     * Calling this method is equivalent to calling <code>getService(Deployer.class, null)</code>
     *
     * @return A new Deployer instance
     */
    Deployer getDeployer() throws GlassFishException;

    /**
     * Gets a CommandRunner instance, using which the user can run asadmin commands.
     * Calling this method is equivalent to calling <code>getService(CommandRunner.class, null)</code>
     * Each invocation of this method returns a new CommandRunner object.
     *
     * @return a new CommandRunner instance
     */
    CommandRunner getCommandRunner() throws GlassFishException;

    /**
     * Represents the status of {@link GlassFish}.
     */
    enum Status {
        /**
         * Initial state of a newly created GlassFish.
         *
         * <p/>This will be the state just after {@link GlassFishRuntime#newGlassFish()}
         * before performing any lifecycle operations.
         */
        INIT,

        /**
         * GlassFish is being started.
         *
         * <p/>This will be the state after {@link GlassFish#start()} has been called
         * until the GlassFish is fully started.
         */
        STARTING,

        /**
         * GlassFish is up and running.
         *
         * <p/> This will be the state once {@link GlassFish#start()} has fully
         * started the GlassFish.
         */
        STARTED,

        /**
         * GlassFish is being stopped.
         *
         * <p/> This will be the state after {@link GlassFish#stop()} has been
         * called until the GlassFish is fully stopped.
         */
        STOPPING,

        /**
         * GlassFish is stopped.
         *
         * <p/>This will be the state after {@link GlassFish#stop()} has
         * fully stopped the GlassFish.
         */
        STOPPED,

        /**
         * GlassFish is disposed and ready to be garbage collected.
         *
         * <p/>This will be the state  after {@link GlassFish#dispose()} or
         * {@link GlassFishRuntime#shutdown()} has been called.
         */
        DISPOSED
    }
}
