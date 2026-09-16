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



import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;

import jakarta.ejb.CreateException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

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
        if (isStateful(ejbId)) {
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

    @Override
    public byte[] createSession(String appName, String moduleName, String distinctName,
                                String beanName) throws NoSuchTargetException {
        EjbKey key = resolve(appName, moduleName, distinctName, beanName, null);
        EjbContainerFacade facade = facade(key);
        try {
            return facade.createSession(remoteViewFor(facade));
        } catch (CreateException | RemoteException e) {
            // The container refuses this for anything that is not a stateful
            // session bean, which is the honest answer: there is no
            // conversation to start.
            throw new NoSuchTargetException("cannot create a session for "
                    + beanName + ": " + e.getMessage());
        }
    }

    @Override
    public void removeSession(EjbKey key) throws NoSuchTargetException {
        EjbContainerFacade facade = facade(key);
        try {
            facade.removeSession(key.instanceKey());
        } catch (RemoteException e) {
            throw new NoSuchTargetException("cannot remove session for " + key + ": " + e.getMessage());
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
            return com.sun.ejb.codegen.RemoteGenerator.getGeneratedRemoteIntfName(viewClassName);
        } catch (RuntimeException e) {
            throw new NoSuchTargetException("cannot derive the generated interface for "
                    + viewClassName + ": " + e);
        }
    }

    /**
     * @param ejbId the bean to ask about
     * @return whether its container keeps a conversation per client
     */
    static boolean isStateful(long ejbId) {
        EjbContainerFacade facade = util().getContainer(ejbId);
        return facade != null
                && facade.getEjbDescriptor() instanceof EjbSessionDescriptor session
                && session.isStateful();
    }

    private EjbContainerFacade facade(EjbKey key) throws NoSuchTargetException {
        EjbContainerFacade facade = util().getContainer(key.ejbId());
        if (facade == null) {
            // The index named a bean the container no longer has - an
            // application undeployed between the lookup and the call.
            index.refresh();
            throw new NoSuchTargetException("no container for ejbId " + key.ejbId());
        }
        return facade;
    }

    private static EjbContainerUtil util() {
        return EjbContainerUtilImpl.getInstance();
    }
}
