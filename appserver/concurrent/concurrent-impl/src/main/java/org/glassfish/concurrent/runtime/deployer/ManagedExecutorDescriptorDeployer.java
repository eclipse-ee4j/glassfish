package org.glassfish.concurrent.runtime.deployer;

import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.concurrent.config.ManagedExecutorService;
import org.glassfish.concurrent.runtime.ConcurrentRuntime;
import org.glassfish.enterprise.concurrent.ContextServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedExecutorServiceImpl;
import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.deployment.ManagedExecutorDefinitionDescriptor;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Min;

@Service
@ResourceDeployerInfo(ManagedExecutorDefinitionDescriptor.class)
public class ManagedExecutorDescriptorDeployer implements ResourceDeployer {

    private static final Logger logger = Logger.getLogger(ManagedExecutorDescriptorDeployer.class.getName());

    @Inject
    private InvocationManager invocationManager;

    @Inject
    private ResourceNamingService resourceNamingService;

    @Override
    public void deployResource(Object resource) throws Exception {
        String applicationName = invocationManager.getCurrentInvocation().getAppName();
        String moduleName = invocationManager.getCurrentInvocation().getModuleName();
        deployResource(resource, applicationName, moduleName);
    }

    @Override
    public void deployResource(Object resource, String applicationName, String moduleName) throws Exception {
        ManagedExecutorDefinitionDescriptor managedExecutorDefinitionDescriptor = (ManagedExecutorDefinitionDescriptor) resource;
        ManagedExecutorServiceConfig managedExecutorServiceConfig
                = new ManagedExecutorServiceConfig(new CustomManagedExecutorServiceImpl(managedExecutorDefinitionDescriptor));
        ConcurrentRuntime concurrentRuntime = ConcurrentRuntime.getRuntime();

        // prepare the contextService
        ContextServiceImpl contextService = concurrentRuntime.findOrCreateContextService(
                managedExecutorDefinitionDescriptor.getContext(),
                managedExecutorDefinitionDescriptor.getName(), applicationName, moduleName);

        // prepare name for JNDI
        String customNameOfResource = ConnectorsUtil.deriveResourceName(
                managedExecutorDefinitionDescriptor.getResourceId(), managedExecutorDefinitionDescriptor.getName(), managedExecutorDefinitionDescriptor.getResourceType());
        ResourceInfo resourceInfo = new ResourceInfo(customNameOfResource, applicationName, moduleName);

        ManagedExecutorServiceImpl managedExecutorService = concurrentRuntime.createManagedExecutorService(resourceInfo, managedExecutorServiceConfig, contextService);
        resourceNamingService.publishObject(resourceInfo, customNameOfResource, managedExecutorService, true);
    }

    @Override
    public void undeployResource(Object resource) throws Exception {
    }

    @Override
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception {
    }

    @Override
    public void redeployResource(Object resource) throws Exception {
    }

    @Override
    public void enableResource(Object resource) throws Exception {
    }

    @Override
    public void disableResource(Object resource) throws Exception {
    }

    @Override
    public boolean handles(Object resource) {
        return false;
    }

    @Override
    public boolean supportsDynamicReconfiguration() {
        return false;
    }

    @Override
    public Class[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }

    @Override
    public boolean canDeploy(boolean postApplicationDeployment, Collection<Resource> allResources, Resource resource) {
        return false;
    }

    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource, Resources allResources) throws ResourceConflictException {

    }

    class CustomManagedExecutorServiceImpl implements ManagedExecutorService {

        private ManagedExecutorDefinitionDescriptor managedExecutorDefinitionDescriptor;

        public CustomManagedExecutorServiceImpl(ManagedExecutorDefinitionDescriptor managedExecutorDefinitionDescriptor) {
            this.managedExecutorDefinitionDescriptor = managedExecutorDefinitionDescriptor;
        }
        @Override
        public String getMaximumPoolSize() {
            return String.valueOf(managedExecutorDefinitionDescriptor.getMaximumPoolSize());
        }

        @Override
        public void setMaximumPoolSize(String value) throws PropertyVetoException {

        }

        @Override
        public String getTaskQueueCapacity() {
            return "" + Integer.MAX_VALUE;
        }

        @Override
        public void setTaskQueueCapacity(String value) throws PropertyVetoException {

        }

        @Override
        public String getIdentity() {
            return null;
        }

        @Override
        public String getThreadPriority() {
            return "" + Thread.NORM_PRIORITY;
        }

        @Override
        public void setThreadPriority(String value) throws PropertyVetoException {

        }

        @Override
        public String getLongRunningTasks() {
            return "false";
        }

        @Override
        public void setLongRunningTasks(String value) throws PropertyVetoException {

        }

        @Override
        public String getHungAfterSeconds() {
            return "" + managedExecutorDefinitionDescriptor.getHungAfterSeconds();
        }

        @Override
        public void setHungAfterSeconds(String value) throws PropertyVetoException {

        }

        @Override
        public String getCorePoolSize() {
            return String.valueOf(managedExecutorDefinitionDescriptor.getMaximumPoolSize());
        }

        @Override
        public void setCorePoolSize(String value) throws PropertyVetoException {

        }

        @Override
        public String getKeepAliveSeconds() {
            return "60";
        }

        @Override
        public void setKeepAliveSeconds(String value) throws PropertyVetoException {

        }

        @Override
        public String getThreadLifetimeSeconds() {
            return "0";
        }

        @Override
        public void setThreadLifetimeSeconds(String value) throws PropertyVetoException {

        }

        @Override
        public String getJndiName() {
            return managedExecutorDefinitionDescriptor.getName();
        }

        @Override
        public void setJndiName(String value) throws PropertyVetoException {

        }

        @Override
        public String getEnabled() {
            return "true";
        }

        @Override
        public void setEnabled(String value) throws PropertyVetoException {

        }

        @Override
        public String getObjectType() {
            return "user";
        }

        @Override
        public void setObjectType(String value) throws PropertyVetoException {

        }

        @Override
        public String getDeploymentOrder() {
            return "100";
        }

        @Override
        public void setDeploymentOrder(String value) throws PropertyVetoException {

        }

        @Override
        public String getContextInfoEnabled() {
            return null;
        }

        @Override
        public void setContextInfoEnabled(String value) throws PropertyVetoException {

        }

        @Override
        public String getContextInfo() {
            return null;
        }

        @Override
        public void setContextInfo(String value) throws PropertyVetoException {

        }

        @Override
        public String getDescription() {
            return "Managed Executor Definition";
        }

        @Override
        public void setDescription(String value) throws PropertyVetoException {

        }

        @Override
        public List<Property> getProperty() {
            return null;
        }

        @Override
        public ConfigBeanProxy getParent() {
            return null;
        }

        @Override
        public <T extends ConfigBeanProxy> T getParent(Class<T> type) {
            return null;
        }

        @Override
        public <T extends ConfigBeanProxy> T createChild(Class<T> type) throws TransactionFailure {
            return null;
        }

        @Override
        public ConfigBeanProxy deepCopy(ConfigBeanProxy parent) throws TransactionFailure {
            return null;
        }

        @Override
        public Property addProperty(Property property) {
            return null;
        }

        @Override
        public Property lookupProperty(String name) {
            return null;
        }

        @Override
        public Property removeProperty(String name) {
            return null;
        }

        @Override
        public Property removeProperty(Property removeMe) {
            return null;
        }

        @Override
        public Property getProperty(String name) {
            return null;
        }

        @Override
        public String getPropertyValue(String name) {
            return null;
        }

        @Override
        public String getPropertyValue(String name, String defaultValue) {
            return null;
        }

        @Override
        public String getContext() {
            return managedExecutorDefinitionDescriptor.getContext();
        }

        @Override
        public void setContext(String value) throws PropertyVetoException {
        }

        @Override
        public String getHungLoggerPrintOnce() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setHungLoggerPrintOnce(String value) throws PropertyVetoException {
            // TODO Auto-generated method stub

        }

        @Override
        public @Min(0) String getHungLoggerInitialDelaySeconds() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setHungLoggerInitialDelaySeconds(String value) throws PropertyVetoException {
            // TODO Auto-generated method stub
        }

        @Override
        public @Min(1) String getHungLoggerIntervalSeconds() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setHungLoggerIntervalSeconds(String value) throws PropertyVetoException {
            // TODO Auto-generated method stub
        }
    }
}