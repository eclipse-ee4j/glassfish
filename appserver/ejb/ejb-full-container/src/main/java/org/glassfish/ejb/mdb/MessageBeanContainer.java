/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.mdb;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ResourceHandle;
import com.sun.appserv.connectors.internal.api.TransactedPoolManager;
import com.sun.ejb.ComponentContext;
import com.sun.ejb.EjbInvocation;
import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.EJBContextImpl;
import com.sun.ejb.containers.EJBContextImpl.BeanState;
import com.sun.ejb.containers.EJBLocalRemoteObject;
import com.sun.ejb.containers.EJBObjectImpl;
import com.sun.ejb.containers.EJBTimerService;
import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.ejb.containers.RuntimeTimerState;
import com.sun.ejb.containers.util.pool.AbstractPool;
import com.sun.ejb.containers.util.pool.NonBlockingPool;
import com.sun.ejb.containers.util.pool.ObjectFactory;
import com.sun.ejb.monitoring.stats.EjbMonitoringStatsProvider;
import com.sun.ejb.monitoring.stats.EjbPoolStatsProvider;
import com.sun.ejb.spi.container.OptionalLocalInterfaceProvider;
import com.sun.enterprise.admin.monitor.callflow.ComponentType;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.runtime.BeanPoolDescriptor;
import com.sun.enterprise.security.SecurityManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.Utility;
import com.sun.logging.LogDomains;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.MessageDrivenBean;
import jakarta.ejb.RemoveException;
import jakarta.resource.spi.endpoint.MessageEndpoint;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.ejb.api.MessageBeanListener;
import org.glassfish.ejb.api.MessageBeanProtocolManager;
import org.glassfish.ejb.api.ResourcesExceededException;
import org.glassfish.ejb.config.MdbContainer;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;
import org.glassfish.ejb.mdb.monitoring.stats.MessageDrivenBeanStatsProvider;
import org.glassfish.ejb.spi.MessageBeanClient;
import org.glassfish.ejb.spi.MessageBeanClientFactory;

import static com.sun.ejb.containers.EJBContextImpl.BeanState.DESTROYED;
import static com.sun.ejb.containers.EJBContextImpl.BeanState.POOLED;
import static com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType.POST_CONSTRUCT;
import static com.sun.enterprise.util.Utility.setContextClassLoader;
import static jakarta.transaction.Status.STATUS_MARKED_ROLLBACK;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.transaction.xa.XAResource.TMSUCCESS;

/**
 * This class provides container functionality specific to message-driven EJBs. At deployment time, one instance of this
 * class is created for each message-driven bean in an application.
 * <P>
 * The 3 states of a Message-driven EJB (an EJB can be in only 1 state at a time):
 * <pre>
 * 1. POOLED : ready for invocations, no transaction in progress
 * 2. INVOKING : processing an invocation
 * 3. DESTROYED : does not exist
 * </pre>
 *
 * A Message-driven Bean can hold open DB connections across invocations. It's assumed that the Resource Manager can
 * handle multiple incomplete transactions on the same connection.
 *
 * @author Kenneth Saks
 */
public final class MessageBeanContainer extends BaseContainer implements MessageBeanProtocolManager {
    private static final Logger _logger = LogDomains.getLogger(MessageBeanContainer.class, LogDomains.MDB_LOGGER);

    private final String appEJBName_;

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(MessageBeanContainer.class);

    private MessageBeanClient messageBeanClient;

    private AbstractPool messageBeanPool;

    private BeanPoolDescriptor beanPoolDescriptor;
    private int maxMessageBeanListeners_;
    private int numMessageBeanListeners_;
    private Class<?> messageBeanInterface;
    private Class<?> messageBeanSubClass;

    // Property used to bootstrap message bean client factory for inbound
    // message delivery.
    private static final String MESSAGE_BEAN_CLIENT_FACTORY_PROP = "com.sun.enterprise.MessageBeanClientFactory";

    private static final String DEFAULT_MESSAGE_BEAN_CLIENT_FACTORY = ConnectorConstants.CONNECTOR_MESSAGE_BEAN_CLIENT_FACTORY;

    private static final int DEFAULT_RESIZE_QUANTITY = 8;
    private static final int DEFAULT_STEADY_SIZE = 1;
    private static final int DEFAULT_MAX_POOL_SIZE = 32;
    private static final int DEFAULT_IDLE_TIMEOUT = 600;

    // issue 4629. 0 means a bean can remain idle indefinitely.
    private static final int MIN_IDLE_TIMEOUT = 0;

    private TransactedPoolManager transactedPoolManager;
    private final Class<?> messageListenerType_;

    MessageBeanContainer(EjbDescriptor ejbDescriptor, ClassLoader classLoader, SecurityManager securityManager) throws Exception {
        super(ContainerType.MESSAGE_DRIVEN, ejbDescriptor, classLoader, securityManager);

        // Instantiate the ORB and Remote naming manager
        // to allow client lookups of JMS queues/topics/connectionfactories
        //
        // TODO - implement the sniffer for DAS/cluster instance - listening on the naming port that will
        // instantiate the orb/remote naming service on demand upon initial access.
        // Once that's available, this call can be removed.
        initializeProtocolManager();

        isMessageDriven = true;

        appEJBName_ = ejbDescriptor.getApplication().getRegistrationName() + ":" + ejbDescriptor.getName();

        EjbMessageBeanDescriptor msgBeanDesc = (EjbMessageBeanDescriptor) ejbDescriptor;

        ComponentInvocation componentInvocation = null;
        try {

            Class<?> beanClass = classLoader.loadClass(ejbDescriptor.getEjbClassName());
            messageListenerType_ = classLoader.loadClass(msgBeanDesc.getMessageListenerType());

            Class<?> messageListenerType_1 = messageListenerType_;
            if (isModernMessageListener(messageListenerType_1)) {
                // Generate interface and subclass for EJB 3.2 No-interface MDB VIew
                MessageBeanInterfaceGenerator generator = new MessageBeanInterfaceGenerator();
                messageBeanInterface = generator.generateMessageBeanInterface(beanClass);
                messageBeanSubClass = generator.generateMessageBeanSubClass(beanClass, messageBeanInterface);
            }

            // Register the tx attribute for each method on MessageListener
            // interface.
            //
            // NOTE : These method objects MUST come from the
            // MessageListener interface, NOT the bean class itself. This
            // is because the message bean container clients do not have
            // access to the message bean class.
            Method[] msgListenerMethods = msgBeanDesc.getMessageListenerInterfaceMethods(classLoader);

            for (Method msgListenerMethod : msgListenerMethods) {
                addInvocationInfo(msgListenerMethod, MethodDescriptor.EJB_BEAN, null);
            }

            transactedPoolManager = ejbContainerUtilImpl.getServices().getService(TransactedPoolManager.class);

            // NOTE : No need to register tx attribute for ejbTimeout. It's
            // done in BaseContainer intialization.
            // Message-driven beans can be timed objects.

            // Bootstrap message bean client factory. If the class name is
            // specified as a system property, that value takes precedence.
            // Otherwise use default client factory. The default is set to
            // a client factory that uses the S1AS 7 style JMS connection
            // consumer contracts. This will be changed once the Connector 1.5
            // implementation is ready.
            String factoryClassName = System.getProperty(MESSAGE_BEAN_CLIENT_FACTORY_PROP);
            MessageBeanClientFactory clientFactory = null;
            if (factoryClassName != null) {
                Class<?> clientFactoryClass = classLoader.loadClass(factoryClassName);
                clientFactory = (MessageBeanClientFactory) clientFactoryClass.getDeclaredConstructor().newInstance();
            } else {
                clientFactory = ejbContainerUtilImpl.getServices().getService(MessageBeanClientFactory.class,
                        DEFAULT_MESSAGE_BEAN_CLIENT_FACTORY);
            }
            _logger.log(FINE, "Using " + clientFactory.getClass().getName() + " for message bean client factory in " + appEJBName_);

            // Create message bean pool before calling setup on
            // Message-bean client, since pool properties can be retrieved
            // through MessageBeanProtocolManager interface.
            createMessageBeanPool(msgBeanDesc);

            // Set resource limit for message bean listeners created through
            // Protocol Manager. For now, just use max pool size. However,
            // we might want to bump this up once the ejb timer service is
            // integrated.
            maxMessageBeanListeners_ = beanPoolDescriptor.getMaxPoolSize();
            numMessageBeanListeners_ = 0;

            messageBeanClient = clientFactory.createMessageBeanClient(msgBeanDesc);

            componentInvocation = createComponentInvocation();
            componentInvocation.container = this;
            invocationManager.preInvoke(componentInvocation);
            messageBeanClient.setup(this);

            registerMonitorableComponents(msgListenerMethods);

            createCallFlowAgent(ComponentType.MDB);
        } catch (Exception ex) {
            if (messageBeanClient != null) {
                messageBeanClient.close();
            }
            throw ex;
        } finally {
            if (componentInvocation != null) {
                invocationManager.postInvoke(componentInvocation);
            }
        }
    }

    /**
     * Called when the application containing this message-bean has successfully gotten through the initial load phase of
     * each module. Now we can "turn on the spigot" and allow incoming requests, which could result in the creation of
     * message-bean instances.
     *
     * @param deploy true if this method is called during application deploy
     */
    @Override
    public void startApplication(boolean deploy) {
        super.startApplication(deploy);

        if (messageBeanPool instanceof NonBlockingPool) {
            NonBlockingPool nonBlockingPool = (NonBlockingPool) messageBeanPool;
            nonBlockingPool.prepopulate(beanPoolDescriptor.getSteadyPoolSize());
        }

        // Start delivery of messages to message bean instances.
        try {
            messageBeanClient.start();
        } catch (Exception e) {
            _logger.log(FINE, e.getClass().getName(), e);

            throw new RuntimeException("MessageBeanContainer.start failure for app " + appEJBName_, e);
        }
    }

    protected void registerMonitorableComponents(Method[] msgListenerMethods) {
        super.registerMonitorableComponents();

        poolProbeListener =
            new EjbPoolStatsProvider(
                messageBeanPool,
                getContainerId(),
                containerInfo.appName,
                containerInfo.modName,
                containerInfo.ejbName);

        poolProbeListener.register();

        _logger.log(FINE, "[MessageBeanContainer] registered monitorable");
    }

    @Override
    protected EjbMonitoringStatsProvider getMonitoringStatsProvider(String appName, String modName, String ejbName) {
        return new MessageDrivenBeanStatsProvider(getContainerId(), appName, modName, ejbName);
    }

    @Override
    public boolean scanForEjbCreateMethod() {
        return true;
    }

    @Override
    protected void initializeHome() throws Exception {
        throw new UnsupportedOperationException("MessageDrivenBean needn't initialize home");
    }

    @Override
    protected void addLocalRemoteInvocationInfo() throws Exception {
        // Nothing to do for MDBs
    }

    @Override
    protected boolean isCreateHomeFinder(Method method) {
        return false;
    }

    private void createMessageBeanPool(EjbMessageBeanDescriptor descriptor) {
        beanPoolDescriptor = descriptor.getIASEjbExtraDescriptors().getBeanPool();

        if (beanPoolDescriptor == null) {
            beanPoolDescriptor = new BeanPoolDescriptor();
        }

        MdbContainer mdbContainer =
            ejbContainerUtilImpl.getServices()
                                .getService(Config.class, ServerEnvironment.DEFAULT_INSTANCE_NAME)
                                .getExtensionByType(MdbContainer.class);

        int maxPoolSize = beanPoolDescriptor.getMaxPoolSize();
        if (maxPoolSize < 0) {
            maxPoolSize = stringToInt(mdbContainer.getMaxPoolSize(), appEJBName_, _logger);
        }
        maxPoolSize = validateValue(maxPoolSize, 1, -1, DEFAULT_MAX_POOL_SIZE, "max-pool-size", appEJBName_, _logger);
        beanPoolDescriptor.setMaxPoolSize(maxPoolSize);

        int value = beanPoolDescriptor.getSteadyPoolSize();
        if (value < 0) {
            value = stringToInt(mdbContainer.getSteadyPoolSize(), appEJBName_, _logger);
        }
        value = validateValue(value, 0, maxPoolSize, DEFAULT_STEADY_SIZE, "steady-pool-size", appEJBName_, _logger);
        beanPoolDescriptor.setSteadyPoolSize(value);

        value = beanPoolDescriptor.getPoolResizeQuantity();
        if (value < 0) {
            value = stringToInt(mdbContainer.getPoolResizeQuantity(), appEJBName_, _logger);
        }
        value = validateValue(value, 1, maxPoolSize, DEFAULT_RESIZE_QUANTITY, "pool-resize-quantity", appEJBName_, _logger);
        beanPoolDescriptor.setPoolResizeQuantity(value);

        // If ejb pool idle-timeout-in-seconds is not explicitly set in
        // glassfish-ejb-jar.xml, returned value is -1
        value = beanPoolDescriptor.getPoolIdleTimeoutInSeconds();
        if (value < MIN_IDLE_TIMEOUT) {
            value = stringToInt(mdbContainer.getIdleTimeoutInSeconds(), appEJBName_, _logger);
        }
        value = validateValue(value, MIN_IDLE_TIMEOUT, -1, DEFAULT_IDLE_TIMEOUT, "idle-timeout-in-seconds", appEJBName_, _logger);
        beanPoolDescriptor.setPoolIdleTimeoutInSeconds(value);

        _logger.log(FINE, () ->
            appEJBName_ + ": Setting message-driven bean pool max-pool-size=" + beanPoolDescriptor.getMaxPoolSize() +
            ", steady-pool-size=" + beanPoolDescriptor.getSteadyPoolSize() +
            ", pool-resize-quantity=" + beanPoolDescriptor.getPoolResizeQuantity() +
            ", idle-timeout-in-seconds=" + beanPoolDescriptor.getPoolIdleTimeoutInSeconds());

        // Create a non-blocking pool of message bean instances.
        // The protocol manager implementation enforces a limit
        // on message bean resources independent of the pool.

        messageBeanPool =
            new NonBlockingPool(
                getContainerId(),
                appEJBName_,
                new MessageBeanContextFactory(),
                beanPoolDescriptor.getSteadyPoolSize(),
                beanPoolDescriptor.getPoolResizeQuantity(),
                beanPoolDescriptor.getMaxPoolSize(),
                beanPoolDescriptor.getPoolIdleTimeoutInSeconds(),
                loader,
                Boolean.parseBoolean(descriptor.getEjbBundleDescriptor().getEnterpriseBeansProperty(SINGLETON_BEAN_POOL_PROP)));
    }

    protected static int stringToInt(String val, String appName, Logger logger) {
        int value = -1;
        try {
            value = Integer.parseInt(val);
        } catch (Exception e) {
            _logger.log(WARNING, "containers.mdb.invalid_value", new Object[] { appName, val, e.toString(), "0" });
            _logger.log(WARNING, "", e);
        }
        return value;
    }

    // deft should always >= lowLimit
    protected int validateValue(int value, int lowLimit, int highLimit, int deft, String emsg, String appName, Logger logger) {
        if (value < lowLimit) {
            _logger.log(WARNING, "containers.mdb.invalid_value", new Object[] { appName, value, emsg, deft });
            value = deft;
        }

        if ((highLimit >= 0) && (value > highLimit)) {
            _logger.log(WARNING, "containers.mdb.invalid_value", new Object[] { appName, value, emsg, highLimit });
            value = highLimit;
        }

        return value;
    }

    private boolean containerStartsTx(Method method) {
        int txMode = getTxAttr(method, MethodDescriptor.EJB_BEAN);

        return isEjbTimeoutMethod(method) ? ((txMode == TX_REQUIRES_NEW) || (txMode == TX_REQUIRED)) : (txMode == TX_REQUIRED);
    }

    public String getMonitorAttributeValues() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("MESSAGEDRIVEN ");
        sbuf.append(appEJBName_);

        sbuf.append(messageBeanPool.getAllAttrValues());
        sbuf.append("]");
        return sbuf.toString();
    }

    @Override
    public boolean userTransactionMethodsAllowed(ComponentInvocation inv) {
        boolean utMethodsAllowed = false;
        if (isBeanManagedTran) {
            if (inv instanceof EjbInvocation) {
                EjbInvocation ejbInvocation = (EjbInvocation) inv;
                MessageBeanContextImpl mdc = (MessageBeanContextImpl) ejbInvocation.context;
                utMethodsAllowed = mdc.operationsAllowed();
            }
        }

        return utMethodsAllowed;
    }

    public void setEJBHome(EJBHome ejbHome) throws Exception {
        throw new Exception("Can't set EJB Home on Message-driven bean");
    }

    @Override
    public EJBObjectImpl getEJBObjectImpl(byte[] instanceKey) {
        throw new EJBException("No EJBObject for message-driven beans");
    }

    @Override
    public EJBObjectImpl createEJBObjectImpl() throws CreateException {
        throw new EJBException("No EJBObject for message-driven beans");
    }

    @Override
    protected void removeBean(EJBLocalRemoteObject ejbo, Method removeMethod, boolean local) throws RemoveException, EJBException {
        throw new EJBException("not used in message-driven beans");
    }

    /**
     * Override callEJBTimeout from BaseContainer since delivery to message driven beans is a bit different from
     * session/entity.
     */
    @Override
    protected boolean callEJBTimeout(RuntimeTimerState timerState, EJBTimerService timerService) throws Exception {
        boolean redeliver = false;

        // There is no resource associated with the delivery of the timeout.
        try {

            Method timeoutMethod = getTimeoutMethod(timerState);

            // Do pre-invoke logic for message bean with tx import = false
            // and a null resource handle.
            beforeMessageDelivery(timeoutMethod, MessageDeliveryType.Timer, false, null);

            ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();
            if (componentInvocation instanceof EjbInvocation) {
                prepareEjbTimeoutParams((EjbInvocation) componentInvocation, timerState, timerService);
            }

            // Method arguments had been set already
            deliverMessage(null);

        } catch (Throwable t) {
            // A runtime exception thrown from ejbTimeout, independent of
            // its transactional setting(CMT, BMT, etc.), should result in
            // a redelivery attempt. The instance that threw the runtime
            // exception will be destroyed, as per the EJB spec.
            redeliver = true;

            _logger.log(FINE, "ejbTimeout threw Runtime exception", t);

        } finally {
            if (!isBeanManagedTran && transactionManager.getStatus() == STATUS_MARKED_ROLLBACK) {
                redeliver = true;
                _logger.log(FINE, "ejbTimeout called setRollbackOnly");
            }

            // Only call postEjbTimeout if there are no errors so far.
            if (!redeliver) {
                boolean successfulPostEjbTimeout = postEjbTimeout(timerState, timerService);
                redeliver = !successfulPostEjbTimeout;
            }

            // afterMessageDelivery takes care of postInvoke and postInvokeTx
            // processing. If any portion of that work fails, mark
            // timer for redelivery.
            boolean successfulAfterMessageDelivery = afterMessageDeliveryInternal(null);
            if (!redeliver && !successfulAfterMessageDelivery) {
                redeliver = true;
            }
        }

        return redeliver;
    }

    /**
     * Force destroy the EJB. Called from postInvokeTx. Note: EJB2.0 section 18.3.1 says that discarding an EJB means that
     * no methods other than finalize() should be invoked on it.
     */
    @Override
    protected void forceDestroyBean(EJBContextImpl ejbContext) {
        MessageBeanContextImpl messageBeanContext = (MessageBeanContextImpl) ejbContext;

        if (messageBeanContext.isInState(DESTROYED)) {
            return;
        }

        // Mark context as destroyed
        messageBeanContext.setState(DESTROYED);

        messageBeanPool.destroyObject(ejbContext);
    }

    // This particular preInvoke signature not used
    @Override
    public void preInvoke(EjbInvocation inv) {
        throw new EJBException("preInvoke(Invocation) not supported");
    }

    private class MessageBeanContextFactory implements ObjectFactory {
        @Override
        public Object create(Object param) {
            try {
                return createMessageDrivenEJB();
            } catch (CreateException ex) {
                throw new EJBException(ex);
            }
        }

        @Override
        public void destroy(Object obj) {
            MessageBeanContextImpl beanContext = (MessageBeanContextImpl) obj;

            Object ejb = beanContext.getEJB();

            if (!beanContext.isInState(DESTROYED)) {

                // Called from pool implementation to reduce the pool size.
                // So we need to call ejb.ejbRemove() and mark context as destroyed.
                EjbInvocation ejbInvocation = null;

                try {
                    // NOTE : Context class-loader is already set by Pool

                    ejbInvocation = createEjbInvocation(ejb, beanContext);

                    ejbInvocation.isMessageDriven = true;
                    invocationManager.preInvoke(ejbInvocation);

                    beanContext.setInEjbRemove(true);
                    intercept(CallbackType.PRE_DESTROY, beanContext);

                    cleanupInstance(beanContext);
                    ejbProbeNotifier.ejbBeanDestroyedEvent(getContainerId(), containerInfo.appName, containerInfo.modName,
                            containerInfo.ejbName);
                } catch (Throwable t) {
                    _logger.log(SEVERE, "containers.mdb_preinvoke_exception_indestroy", new Object[] { appEJBName_, t.toString() });
                    _logger.log(SEVERE, t.getClass().getName(), t);
                } finally {
                    beanContext.setInEjbRemove(false);
                    if (ejbInvocation != null) {
                        invocationManager.postInvoke(ejbInvocation);
                    }
                }

                beanContext.setState(BeanState.DESTROYED);

            }

            // Tell the TM to release resources held by the bean
            transactionManager.componentDestroyed(beanContext);

            // Message-driven beans can't have transactions across invocations.
            beanContext.setTransaction(null);
        }
    }

    @Override
    protected ComponentContext _getContext(EjbInvocation inv) {
        MessageBeanContextImpl messageBeanContext = null;
        try {
            messageBeanContext = (MessageBeanContextImpl) messageBeanPool.getObject(null);
            messageBeanContext.setState(BeanState.INVOKING);
        } catch (Exception e) {
            throw new EJBException(e);
        }

        return messageBeanContext;
    }

    /**
     * Return instance to a pooled state.
     */
    @Override
    public void releaseContext(EjbInvocation inv) {
        MessageBeanContextImpl beanContext = (MessageBeanContextImpl) inv.context;

        if (beanContext.isInState(DESTROYED)) {
            return;
        }

        beanContext.setState(POOLED);

        // Message-driven beans can't have transactions across invocations.
        beanContext.setTransaction(null);

        // Update last access time so pool's time-based logic will work best
        beanContext.touch();

        messageBeanPool.returnObject(beanContext);
    }

    // This particular postInvoke signature not used
    @Override
    public void postInvoke(EjbInvocation inv) {
        throw new EJBException("postInvoke(Invocation) not supported " + "in message-driven bean container");
    }

    /****************************************************************
     * The following are implementation for methods required by the * MessageBeanProtocalManager interface. *
     ****************************************************************/

    @Override
    public MessageBeanListener createMessageBeanListener(ResourceHandle resource) throws ResourcesExceededException {
        boolean resourcesExceeded = false;

        synchronized (this) {
            if (numMessageBeanListeners_ < maxMessageBeanListeners_) {
                numMessageBeanListeners_++;
            } else {
                resourcesExceeded = true;
            }
        }

        if (resourcesExceeded) {
            ResourcesExceededException ree = new ResourcesExceededException(
                    "Message Bean Resources " + "exceeded for message bean " + appEJBName_);
            _logger.log(FINE, "exceeded max of " + maxMessageBeanListeners_, ree);
            throw ree;
        }

        //
        // Message bean context/instance creation is decoupled from
        // MessageBeanListener instance creation. This typically means
        // the message bean instances are instantiated lazily upon actual
        // message delivery. In addition, MessageBeanListener instances
        // are not pooled since they are currently very small objects without
        // much initialization overhead. This is the simplest approach since
        // there is minimal state to track between invocations and upon
        // error conditions such as message bean instance failure. However,
        // it could be optimized in the following ways :
        //
        // 1. Implement MessageBeanListener within MessageBeanContextImpl.
        // This reduces the number of objects created per thread of delivery.
        //
        // 2. Associate message bean context/instance with MessageBeanListener
        // across invocations. This saves one pool retrieval and one
        // pool replacement operation for each invocation.
        //
        //
        return new MessageBeanListenerImpl(this, resource);
    }

    @Override
    public void destroyMessageBeanListener(MessageBeanListener listener) {
        synchronized (this) {
            numMessageBeanListeners_--;
        }
    }

    /**
     * @param method One of the methods used to deliver messages, e.g. onMessage method for jakarta.jms.MessageListener.
     * Note that if the <code>method</code> is not one of the methods for message delivery, the behavior of this method is
     * not defined.
     */
    @Override
    public boolean isDeliveryTransacted(Method method) {
        return containerStartsTx(method);
    }

    @Override
    public BeanPoolDescriptor getPoolDescriptor() {
        return beanPoolDescriptor;
    }

    /**
     * Generates the appropriate Proxy based on the message listener type.
     *
     * @param handler InvocationHandler responsible for calls on the proxy
     * @return an object implementing MessageEndpoint and the appropriate MDB view
     * @throws Exception
     */
    @Override
    public Object createMessageBeanProxy(InvocationHandler handler) throws Exception {
        if (isModernMessageListener(messageListenerType_)) {
            // EJB 3.2 No-interface MDB View

            Proxy proxy = (Proxy) Proxy.newProxyInstance(loader, new Class[] { messageBeanInterface }, handler);
            OptionalLocalInterfaceProvider provider = (OptionalLocalInterfaceProvider) messageBeanSubClass.getDeclaredConstructor().newInstance();
            provider.setOptionalLocalIntfProxy(proxy);

            return provider;
        }

        // EJB 3.1 - 2.0 Interface View
        return Proxy.newProxyInstance(loader, new Class[] { messageListenerType_, MessageEndpoint.class }, handler);
    }

    /**
     * Detects if the message-listener type indicates an EJB 3.2 MDB No-Interface View
     *
     * In the future this method could potentially just return:
     *
     * <pre>
     *     return Annotation.class.isAssignableFrom(messageListenerType)
     * </pre>
     *
     * @param messageListenerType
     * @return true of the specified interface has no methods
     */
    private static boolean isModernMessageListener(Class<?> messageListenerType) {
        // DMB: In the future, this can just return 'Annotation.class.isAssignableFrom(messageListenerType)'
        return messageListenerType.getMethods().length == 0;
    }

    @Override
    protected EJBContextImpl _constructEJBContextImpl(Object instance) {
        return new MessageBeanContextImpl(instance, this);
    }

    /**
     * Instantiate and initialize a message-driven bean instance.
     */
    private MessageBeanContextImpl createMessageDrivenEJB() throws CreateException {
        EjbInvocation ejbInvocation = null;
        MessageBeanContextImpl messageBeanContext = null;
        ClassLoader originalClassLoader = null;

        try {
            // Set application class loader before invoking instance.
            originalClassLoader = setContextClassLoader(getClassLoader());

            messageBeanContext = (MessageBeanContextImpl) createEjbInstanceAndContext();

            Object ejb = messageBeanContext.getEJB();

            // java:comp/env lookups are allowed from here on...
            ejbInvocation = createEjbInvocation(ejb, messageBeanContext);

            ejbInvocation.isMessageDriven = true;
            invocationManager.preInvoke(ejbInvocation);

            if (ejb instanceof MessageDrivenBean) {
                // setMessageDrivenContext will be called without a Tx
                // as required by the spec
                ((MessageDrivenBean) ejb).setMessageDrivenContext(messageBeanContext);
            }

            // Perform injection right after where setMessageDrivenContext
            // would be called. This is important since injection methods
            // have the same "operations allowed" permissions as
            // setMessageDrivenContext.
            injectEjbInstance(messageBeanContext);

            // Set flag in context so UserTransaction can
            // be used from ejbCreate. Didn't want to add
            // a new state to lifecycle since that would
            // require either changing lots of code in
            // EJBContextImpl or re-implementing all the
            // context methods within MessageBeanContextImpl.
            messageBeanContext.setContextCalled();

            // Call ejbCreate OR @PostConstruct on the bean.
            intercept(POST_CONSTRUCT, messageBeanContext);

            ejbProbeNotifier.ejbBeanCreatedEvent(getContainerId(), containerInfo.appName, containerInfo.modName, containerInfo.ejbName);

            // Set the state to POOLED after ejbCreate so that
            // EJBContext methods not allowed will throw exceptions
            messageBeanContext.setState(POOLED);
        } catch (Throwable t) {
            _logger.log(SEVERE, "containers.mdb.ejb_creation_exception", new Object[] { appEJBName_, t.toString() });

            if (t instanceof InvocationTargetException) {
                _logger.log(SEVERE, t.getClass().getName(), t.getCause());
            }
            _logger.log(SEVERE, t.getClass().getName(), t);

            CreateException ce = new CreateException("Could not create Message-Driven EJB");
            ce.initCause(t);
            throw ce;

        } finally {
            if (originalClassLoader != null) {
                setContextClassLoader(originalClassLoader);
            }
            if (ejbInvocation != null) {
                invocationManager.postInvoke(ejbInvocation);
            }
        }

        return messageBeanContext;
    }

    /**
     * Make the work performed by a message-bean instance's associated XA resource part of any global transaction
     */
    private void registerMessageBeanResource(ResourceHandle resourceHandle) throws Exception {
        if (resourceHandle != null) {
            transactedPoolManager.registerResource(resourceHandle);
        }
    }

    private void unregisterMessageBeanResource(ResourceHandle resourceHandle) {
        // resource handle may be null if preInvokeTx error caused
        // ResourceAllocator.destroyResource()
        if (resourceHandle != null) {
            transactedPoolManager.unregisterResource(resourceHandle, TMSUCCESS);
        }
    }

    @Override
    protected void afterBegin(EJBContextImpl context) {
        // Message-driven Beans cannot implement SessionSynchronization!!
    }

    @Override
    protected void beforeCompletion(EJBContextImpl context) {
        // Message-driven beans cannot implement SessionSynchronization!!
    }

    @Override
    protected void afterCompletion(EJBContextImpl ctx, int status) {
        // Message-driven Beans cannot implement SessionSynchronization!!
    }

    // default
    @Override
    public boolean passivateEJB(ComponentContext context) {
        return false;
    }

    // default
    public void activateEJB(Object ctx, Object instanceKey) {
    }

    private ComponentInvocation createComponentInvocation() {
        EjbBundleDescriptor ejbBundleDesc = getEjbDescriptor().getEjbBundleDescriptor();

        return new
            ComponentInvocation(
                getComponentId(),
                ComponentInvocation.ComponentInvocationType.SERVLET_INVOCATION,
                this,
                ejbBundleDesc.getApplication().getAppName(),
                ejbBundleDesc.getModuleName());
    }

    private void cleanupResources() {
        ComponentInvocation componentInvocation = createComponentInvocation();

        ASyncClientShutdownTask task =
            new ASyncClientShutdownTask(appEJBName_, messageBeanClient, loader, messageBeanPool, componentInvocation);
        long timeout = 0;

        try {
            timeout = ejbContainerUtilImpl.getServices()
                                          .getService(ConnectorRuntime.class)
                                          .getShutdownTimeout();
        } catch (Throwable th) {
            _logger.log(WARNING, "[MDBContainer] Got exception while trying to get shutdown timeout", th);
        }

        try {
            boolean addedAsyncTask = false;
            if (timeout > 0) {
                try {
                    ejbContainerUtilImpl.addWork(task);
                    addedAsyncTask = true;
                } catch (Throwable th) {
                    // Since we got an exception while trying to add the async task
                    // we will have to do the cleanup in the current thread itself.
                    addedAsyncTask = false;
                    _logger.log(WARNING, "[MDBContainer] Got exception while trying "
                            + "to add task to ContainerWorkPool. Will execute cleanupResources on current thread", th);
                }
            }

            if (addedAsyncTask) {
                synchronized (task) {
                    if (!task.isDone()) {
                        _logger.log(FINE, "[MDBContainer] Going to wait for a maximum of {0} milliseconds.", timeout);
                        long maxWaitTime = System.currentTimeMillis() + timeout;
                        // wait in loop to guard against spurious wake-up
                        do {
                            long timeTillTimeout = maxWaitTime - System.currentTimeMillis();
                            if (timeTillTimeout <= 0) {
                                break;
                            }
                            task.wait(timeTillTimeout);
                        } while (!task.isDone());
                    }

                    if (!task.isDone()) {
                        _logger.log(WARNING,
                                "[MDBContainer] ASync task has not finished. Giving up after {0} milliseconds.", timeout);
                    } else {
                        _logger.log(FINE, "[MDBContainer] ASync task has completed");
                    }
                }
            } else {
                // Execute in the same thread
                _logger.log(FINE, "[MDBContainer] Attempting to do cleanup()in current thread...");
                task.run();
                _logger.log(FINE, "[MDBContainer] Current thread done cleanup()... ");
            }
        } catch (InterruptedException inEx) {
            _logger.log(SEVERE, "containers.mdb.cleanup_exception", new Object[] { appEJBName_, inEx.toString() });
        } catch (Exception ex) {
            _logger.log(SEVERE, "containers.mdb.cleanup_exception", new Object[] { appEJBName_, ex.toString() });
        }
    }

    private static class ASyncClientShutdownTask implements Runnable {
        private boolean done = false;

        String appName;
        MessageBeanClient messageBeanClient;
        ClassLoader classLoader;
        AbstractPool mdbPool;
        ComponentInvocation componentInvocation;

        ASyncClientShutdownTask(String appName, MessageBeanClient mdbClient, ClassLoader loader, AbstractPool mdbPool,  ComponentInvocation componentInvocation) {
            this.appName = appName;
            this.messageBeanClient = mdbClient;
            this.classLoader = loader;
            this.mdbPool = mdbPool;
            this.componentInvocation = componentInvocation;
        }

        @Override
        public void run() {
            ClassLoader previousClassLoader = null;
            InvocationManager invocationManager = EjbContainerUtilImpl.getInstance().getInvocationManager();
            try {
                previousClassLoader = setContextClassLoader(classLoader);

                invocationManager.preInvoke(componentInvocation);
                // Cleanup the message bean client resources.
                messageBeanClient.close();

                _logger.log(FINE, "[MDBContainer] ASync thread done with mdbClient.close()");
            } catch (Exception e) {
                _logger.log(SEVERE, "containers.mdb.cleanup_exception", new Object[] { appName, e.toString() });
                _logger.log(SEVERE, e.getClass().getName(), e);
            } finally {
                synchronized (this) {
                    this.done = true;
                    this.notifyAll();
                }

                try {
                    mdbPool.close();
                } catch (Exception ex) {
                    _logger.log(FINE, "Exception while closing pool", ex);
                }
                invocationManager.postInvoke(componentInvocation);

                if (previousClassLoader != null) {
                    setContextClassLoader(previousClassLoader);
                }
            }
        }

        public synchronized boolean isDone() {
            return this.done;
        }
    }

    /**
     * Called by BaseContainer during container shutdown sequence
     */
    @Override
    protected void doConcreteContainerShutdown(boolean appBeingUndeployed) {
        _logger.log(FINE, "containers.mdb.shutdown_cleanup_start", appEJBName_);
        monitorOn = false;
        cleanupResources();
        _logger.log(FINE, "containers.mdb.shutdown_cleanup_end", appEJBName_);
    }

    /**
     * Actual message delivery happens in three steps :
     *
     * 1) beforeMessageDelivery(Message, MessageListener) This is our chance to make the message delivery itself part of the
     * instance's global transaction.
     *
     * 2) onMessage(Message, MessageListener) This is where the container delegates to the actual ejb instance's onMessage
     * method.
     *
     * 3) afterMessageDelivery(Message, MessageListener) Perform transaction cleanup and error handling.
     *
     * We use the EjbInvocation manager's thread-specific state to track the invocation across these three calls.
     *
     */

    public void beforeMessageDelivery(Method method, MessageDeliveryType deliveryType, boolean txImported, ResourceHandle resourceHandle) {
        if (containerState != CONTAINER_STARTED) { // i.e. no invocation
            String errorMsg =
                localStrings.getLocalString(
                    "containers.mdb.invocation_closed",
                    appEJBName_ + ": Message-driven bean invocation closed by container", new Object[] { appEJBName_ });

            throw new EJBException(errorMsg);
        }

        EjbInvocation invocation = createEjbInvocation();

        try {

            MessageBeanContextImpl context = (MessageBeanContextImpl) getContext(invocation);

            if (deliveryType == MessageDeliveryType.Timer) {
                invocation.isTimerCallback = true;
            }

            // Set the context class loader here so that message producer will
            // have access to application class loader during message
            // processing.
            // The previous context class loader will be restored in
            // afterMessageDelivery.

            invocation.setOriginalContextClassLoader(Utility.setContextClassLoader(getClassLoader()));
            invocation.isMessageDriven = true;
            invocation.method = method;

            context.setState(BeanState.INVOKING);

            invocation.context = context;
            invocation.instance = context.getEJB();
            invocation.ejb = context.getEJB();
            invocation.container = this;

            // Message Bean Container only starts a new transaction if
            // there is no imported transaction and the message listener
            // method has tx attribute TX_REQUIRED or the ejbTimeout has
            // tx attribute TX_REQUIRES_NEW/TX_REQUIRED
            boolean startTx = false;
            if (!txImported) {
                startTx = containerStartsTx(method);
            }

            // keep track of whether tx was started for later.
            invocation.setContainerStartsTx(startTx);

            this.invocationManager.preInvoke(invocation);

            if (startTx) {
                // Register the session associated with the message-driven
                // bean's destination so the message delivery will be
                // part of the container-managed transaction.
                registerMessageBeanResource(resourceHandle);
            }

            preInvokeTx(invocation);

        } catch (Throwable c) {
            if (containerState != CONTAINER_STARTED) {
                _logger.log(SEVERE, "containers.mdb.preinvoke_exception", new Object[] { appEJBName_, c.toString() });
                _logger.log(SEVERE, c.getClass().getName(), c);
            }
            invocation.exception = c;
        }
    }

    public Object deliverMessage(Object[] params) throws Throwable {
        EjbInvocation invocation = null;
        Object result = null;

        invocation = (EjbInvocation) invocationManager.getCurrentInvocation();

        if (invocation == null && _logger.isLoggable(FINEST)) {
            if (containerState != CONTAINER_STARTED) {
                _logger.log(FINEST, "No invocation in onMessage (container closing)");
            } else {
                _logger.log(FINEST, "No invocation in onMessage : ");
            }
        }

        if (invocation != null && invocation.exception == null) {

            try {

                // NOTE : Application classloader already set in
                // beforeMessageDelivery

                if (isTimedObject() && isEjbTimeoutMethod(invocation.method)) {
                    invocation.beanMethod = invocation.method;
                    intercept(invocation);
                } else {
                    // invocation.beanMethod is the actual target method from
                    // the bean class. The bean class is not required to be
                    // a formal subtype of the message listener interface, so
                    // we need to be careful to invoke through the bean class
                    // method itself. This info is also returned from the
                    // interceptor context info.

                    invocation.methodParams = params;

                    invocation.beanMethod =
                        invocation.ejb.getClass()
                                      .getMethod(
                                          invocation.method.getName(),
                                          invocation.method.getParameterTypes());

                    result = super.intercept(invocation);
                }

            } catch (InvocationTargetException ite) {

                //
                // In EJB 2.1, message listener method signatures do not have
                // any restrictions on what kind of exceptions can be thrown.
                // This was not the case in J2EE 1.3, since JMS message driven
                // beans could only implement
                // void jakarta.jms.MessageListener.onMessage() , which does
                // not declare any exceptions.
                //
                // In the J2EE 1.3 implementation, exceptions were only
                // propagated when the message driven bean was not configured
                // with CMT/Required transaction mode. This has been changed
                // due to the Connector 1.5 integration. Now, all exceptions
                // are propagated regardless of the tx mode. (18.2.2)
                // Application exceptions are propagated as is, while system
                // exceptions are wrapped in an EJBException.
                //
                // If an exception is thrown and there is a container-started
                // transaction, the semantics are the same as for other ejb
                // types whose business methods throw an exception.
                // Specifically, if the exception thrown is an Application
                // exception(defined in 18.2.1), it does not automatically
                // result in a rollback of the container-started transaction.
                //

                Throwable cause = ite.getCause();
                // set cause on invocation , rather than the propagated
                // EJBException
                invocation.exception = cause;

                if (isSystemUncheckedException(cause)) {
                    EJBException ejbEx = new EJBException("message-driven bean method " + invocation.method + " system exception");
                    ejbEx.initCause(cause);
                    cause = ejbEx;
                }
                throw cause;
            } catch (Throwable t) {
                EJBException ejbEx = new EJBException("message-bean container dispatch error");
                ejbEx.initCause(t);
                invocation.exception = ejbEx;
                throw ejbEx;
            } finally {
                /*
                 * FIXME if ( AppVerification.doInstrument() ) { AppVerification.getInstrumentLogger().doInstrumentForEjb
                 * (getEjbDescriptor(), invocation.method, invocation.exception); }
                 */
            }

        } // End if -- invoke instance's onMessage method
        else {
            if (invocation == null) {
                String errorMsg =
                    localStrings.getLocalString(
                        "containers.mdb.invocation_closed",
                        appEJBName_ + ": Message-driven bean invocation " + "closed by container", new Object[] { appEJBName_ });
                throw new EJBException(errorMsg);
            } else {
                _logger.log(SEVERE, "containers.mdb.invocation_exception",
                        new Object[] { appEJBName_, invocation.exception.toString() });
                _logger.log(SEVERE, invocation.exception.getClass().getName(), invocation.exception);
                EJBException ejbEx = new EJBException();
                ejbEx.initCause(invocation.exception);
                throw ejbEx;
            }
        }

        return result;
    }

    public void afterMessageDelivery(ResourceHandle resourceHandle) {
        afterMessageDeliveryInternal(resourceHandle);
    }

    private boolean afterMessageDeliveryInternal(ResourceHandle resourceHandle) {
        // return value. assume failure until proven otherwise.
        boolean success = false;

        EjbInvocation invocation = null;

        invocation = (EjbInvocation) invocationManager.getCurrentInvocation();
        if (invocation == null) {
            _logger.log(SEVERE, "containers.mdb.no_invocation", new Object[] { appEJBName_, "" });
        } else {
            try {
                if (invocation.isContainerStartsTx()) {
                    // Unregister the session associated with
                    // the message-driven bean's destination.
                    unregisterMessageBeanResource(resourceHandle);
                }

                // counterpart of invocationManager.preInvoke
                invocationManager.postInvoke(invocation);

                // Commit/Rollback container-managed transaction.
                postInvokeTx(invocation);

                // Consider successful delivery. Commit failure will be
                // checked below.
                success = true;

                // TODO: Check if Tx existed / committed
                ejbProbeNotifier.messageDeliveredEvent(getContainerId(), containerInfo.appName, containerInfo.modName,
                        containerInfo.ejbName);

            } catch (Throwable ce) {
                _logger.log(SEVERE, "containers.mdb.postinvoke_exception", new Object[] { appEJBName_, ce.toString() });
                _logger.log(SEVERE, ce.getClass().getName(), ce);
            } finally {
                releaseContext(invocation);
            }

            // Reset original class loader
            setContextClassLoader(invocation.getOriginalContextClassLoader());

            if (invocation.exception != null) {
                if (isSystemUncheckedException(invocation.exception)) {
                    success = false;
                }

                // Log system exceptions by default and application exceptions
                // only when log level is FINE or higher.
                Level exLogLevel = isSystemUncheckedException(invocation.exception) ? WARNING : FINE;

                _logger.log(exLogLevel, "containers.mdb.invocation_exception",
                        new Object[] { appEJBName_, invocation.exception.toString() });
                _logger.log(exLogLevel, invocation.exception.getClass().getName(), invocation.exception);
            }
        }

        return success;
    }

    public enum MessageDeliveryType {
        Message, Timer
    }

}
