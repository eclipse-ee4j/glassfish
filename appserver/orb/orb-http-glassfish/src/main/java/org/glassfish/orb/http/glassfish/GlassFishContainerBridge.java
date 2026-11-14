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

package org.glassfish.orb.http.glassfish;

import com.sun.ejb.codegen.RemoteGenerator;
import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;

import jakarta.ejb.CreateException;
import jakarta.ejb.NoSuchObjectLocalException;
import jakarta.ejb.RemoveException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.function.LongFunction;

import org.glassfish.enterprise.iiop.spi.EjbContainerFacade;
import org.glassfish.orb.http.protocol.EjbKey;
import org.glassfish.orb.http.server.ContainerBridge;
import org.jvnet.hk2.annotations.Service;

/**
 * Binds the transport's dispatch to this container.
 *
 * <p>Almost nothing here is translation. {@code EjbContainerFacade} already
 * says what an arriving remote invocation needs - resolve a key to a target,
 * release it afterwards, and hand over the deployment's class loader - and
 * {@code BaseContainer} implements it with a comment recording that it is
 * called "from the ProtocolManager when a remote invocation arrives". This
 * class is a second such protocol manager.
 *
 * <p>Two things do need care.
 *
 * <p>The container addresses beans by {@code ejbId}, and this transport's
 * paths carry names; {@link EjbNameIndex} closes that gap.
 *
 * <p>And {@code getTargetObject} wants the name of the <em>generated</em>
 * remote interface, not the business interface the client named:
 * {@code BaseContainer} keys its view table by the generated type. Passing the
 * business interface through unchanged would miss the table and look, from the
 * client's side, exactly like a bean that does not exist.
 */
@Service
@Singleton
public class GlassFishContainerBridge implements ContainerBridge {

    private static final Logger LOG = System.getLogger(GlassFishContainerBridge.class.getName());

    /**
     * The instance key every stateless and singleton reference carries.
     *
     * <p>Not invented here: {@code StatelessSessionContainer} and
     * {@code AbstractSingletonContainer} both hold exactly this array, because
     * all instances of such a bean are interchangeable and so share one remote
     * reference. Its shape is also load-bearing - the container's comment says
     * "the first byte of instanceKey must be left empty" - which is why it is
     * copied rather than approximated.
     *
     * <p>Using the home key here instead, as this did, asks the container for
     * the bean's <em>home</em> and gets it: a GenericEJBHome, which has no
     * business methods. Every invocation then failed as though the bean did
     * not exist.
     */
    private static final byte[] SHARED_INSTANCE_KEY = { 0, 0, 0, 1 };

    @Inject
    private EjbNameIndex index;

    /**
     * The facade that produced the current target.
     *
     * <p>{@link #releaseTargetObject} is handed the target and not the key, and
     * the release has to reach the same container that produced it. A thread
     * local is faithful rather than expedient: the pairing it stands in for -
     * {@code externalPreInvoke} and {@code externalPostInvoke} - is itself
     * thread-scoped, because what it restores is the context class loader.
     */
    private final ThreadLocal<EjbContainerFacade> currentFacade = new ThreadLocal<>();

    /** Where a bean's container is found by id; the server's own registry unless a test says otherwise. */
    private final LongFunction<EjbContainerFacade> containers;

    public GlassFishContainerBridge() {
        this(id -> util().getContainer(id));
    }

    /** For tests: the same bridge over containers that are not the server's. */
    GlassFishContainerBridge(LongFunction<EjbContainerFacade> containers) {
        this.containers = containers;
    }

    /** For tests: the name index the bridge consults. */
    void useIndex(EjbNameIndex index) {
        this.index = index;
    }

    @Override
    public EjbKey resolve(String appName, String moduleName, String distinctName,
                          String beanName, byte[] sessionId) throws NoSuchTargetException {
        Long ejbId = index.lookup(appName, moduleName, beanName);
        if (ejbId == null) {
            throw new NoSuchTargetException("no bean " + beanName
                    + " in " + appName + '/' + moduleName);
        }
        if (sessionId != null) {
            return new EjbKey(ejbId, sessionId);
        }
        if (isStateful(containers.apply(ejbId))) {
            // The shared key is four bytes that mean "the one instance". A
            // stateful container reads an instance key as a session key and
            // runs off the end of it, which surfaces as an
            // ArrayIndexOutOfBoundsException from inside the container rather
            // than as anything a caller could act on.
            throw new NoSuchTargetException(beanName
                    + " is stateful: this call needs a session, and none was named");
        }
        // Everything else shares the one reference the container publishes.
        return new EjbKey(ejbId, SHARED_INSTANCE_KEY);
    }

    @Override
    public Object getTargetObject(EjbKey key, String viewClassName) throws NoSuchTargetException {
        EjbContainerFacade facade = facade(key);
        String generatedView = generatedViewName(viewClassName);

        Remote target = facade.getTargetObject(key.instanceKey(), generatedView);
        if (target == null) {
            // Rare, and the container's own comment says so: for stateful and
            // entity beans this can be null when the instance has gone.
            throw new NoSuchTargetException("no live instance for " + key);
        }
        currentFacade.set(facade);
        return target;
    }

    @Override
    public void releaseTargetObject(Object target) {
        EjbContainerFacade facade = currentFacade.get();
        currentFacade.remove();
        if (facade != null && target instanceof Remote remote) {
            facade.releaseTargetObject(remote);
        }
    }

    @Override
    public ClassLoader classLoader(EjbKey key) {
        ClassLoader loader = util().getClassLoader(key.ejbId());
        return loader != null ? loader : getClass().getClassLoader();
    }

    /**
     * Opens a session on the named bean.
     *
     * <p>This does not go through {@link #resolve}. That method answers a
     * stateful bean with "this call needs a session, and none was named",
     * which is the right answer for an invocation and a wrong one here: the
     * session is what this call is asking for. Going through it made every
     * open fail, so the bean is looked up in the index and the container's own
     * {@code createSession} is called, which is also what
     * {@code GlassFishNamingBridge} does for a stateful lookup.
     *
     * @throws NoSuchTargetException if no such bean is deployed, or it has no
     *         remote view
     * @throws SessionRefusedException if the bean is deployed but is not
     *         stateful, or the container refused to create the session
     */
    @Override
    public byte[] createSession(String appName, String moduleName, String distinctName,
                                String beanName) throws NoSuchTargetException, SessionRefusedException {
        Long ejbId = index.lookup(appName, moduleName, beanName);
        if (ejbId == null) {
            throw new NoSuchTargetException("no bean " + beanName
                    + " in " + appName + '/' + moduleName);
        }
        EjbContainerFacade facade = facade(ejbId);
        if (!isStateful(facade)) {
            // The container would refuse as well, but with a RemoteException
            // that reads as a transport failure. Say what is actually wrong.
            throw new SessionRefusedException(beanName
                    + " is not a stateful session bean, so it has no sessions to open");
        }
        try {
            return facade.createSession(remoteViewFor(facade));
        } catch (CreateException | RemoteException e) {
            // What the caller is told is fixed: the container's own text can
            // name the bean's internals. The cause is kept and logged.
            LOG.log(Level.DEBUG, "cannot create a session for " + beanName, e);
            SessionRefusedException refused = new SessionRefusedException(
                    "cannot create a session for " + beanName);
            refused.initCause(e);
            throw refused;
        }
    }

    /**
     * Removes a session, and treats one that is already gone as removed.
     *
     * <p>The facade's contract is that removal is idempotent, because a client
     * that retries after a timeout must not be told its own cleanup failed. The
     * stateful container answers an unknown or expired key with
     * {@code NoSuchObjectLocalException}, and without this that reached the
     * endpoint as a 500 with a stack trace in the log for a second remove or
     * for any session id at all.
     *
     * <p>A bean that has no sessions, or a session the container will not
     * remove now, is a refusal and not a success: the caller must not be told
     * its cleanup worked when it did not.
     */
    @Override
    public void removeSession(EjbKey key) throws NoSuchTargetException, SessionRefusedException {
        EjbContainerFacade facade = facade(key);
        if (!isStateful(facade)) {
            throw new SessionRefusedException("this bean has no sessions to remove");
        }
        try {
            facade.removeSession(key.instanceKey());
        } catch (NoSuchObjectLocalException gone) {
            LOG.log(Level.DEBUG, "session already gone for " + key, gone);
        } catch (RemoveException | RemoteException e) {
            // The container's own wording is not the caller's business.
            LOG.log(Level.DEBUG, "cannot remove session for " + key, e);
            SessionRefusedException refused = new SessionRefusedException(
                    "the session cannot be removed now");
            refused.initCause(e);
            throw refused;
        }
    }

    /**
     * Chooses which remote view the session is created through.
     *
     * <p>A bean with an EJB 2.x remote interface is created through its home,
     * which is what {@code null} selects. A bean with only business interfaces
     * is created through one of them, and the container keys its view table by
     * the generated name rather than the interface the application declared.
     *
     * @param facade the container being asked
     * @return the generated business interface name, or {@code null} for the
     *         remote home view
     * @throws NoSuchTargetException if the bean has no remote view at all, in
     *         which case there is nothing this transport can reach
     */
    private String remoteViewFor(EjbContainerFacade facade) throws NoSuchTargetException {
        EjbDescriptor descriptor = facade.getEjbDescriptor();
        if (descriptor.isRemoteInterfacesSupported()) {
            return null;
        }
        Set<String> business = descriptor.getRemoteBusinessClassNames();
        if (business == null || business.isEmpty()) {
            throw new NoSuchTargetException(descriptor.getName()
                    + " has no remote view, so it cannot be reached over this transport");
        }
        // Any of them creates the same instance - the view decides which
        // interface the reference is typed as, not which bean is made - so
        // when a bean has several, the first is as good as any.
        return generatedViewName(business.iterator().next());
    }

    /**
     * @param viewClassName the business interface the client named, or null for
     *                      the remote home view
     * @return the generated interface name the container's view table is keyed
     *         by, or null to select the home view
     */
    static String generatedViewName(String viewClassName) throws NoSuchTargetException {
        if (viewClassName == null) {
            return null;
        }
        try {
            return RemoteGenerator.getGeneratedRemoteIntfName(viewClassName);
        } catch (RuntimeException e) {
            LOG.log(Level.DEBUG, "cannot derive the generated interface for " + viewClassName, e);
            throw new NoSuchTargetException("cannot derive the generated interface for " + viewClassName);
        }
    }

    /**
     * @param ejbId the bean to ask about
     * @return whether its container keeps a conversation per client
     */
    static boolean isStateful(long ejbId) {
        return isStateful(util().getContainer(ejbId));
    }

    static boolean isStateful(EjbContainerFacade facade) {
        return facade != null
                && facade.getEjbDescriptor() instanceof EjbSessionDescriptor session
                && session.isStateful();
    }

    private EjbContainerFacade facade(EjbKey key) throws NoSuchTargetException {
        return facade(key.ejbId());
    }

    private EjbContainerFacade facade(long ejbId) throws NoSuchTargetException {
        EjbContainerFacade facade = containers.apply(ejbId);
        if (facade == null) {
            // The index named a bean the container no longer has - an
            // application undeployed between the lookup and the call.
            index.forget(ejbId);
            throw new NoSuchTargetException("no container for ejbId " + ejbId);
        }
        return facade;
    }

    private static EjbContainerUtil util() {
        return EjbContainerUtilImpl.getInstance();
    }
}
