/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.glassfish.concurrent.runtime;

import java.util.ArrayList;
import java.util.List;
import org.glassfish.enterprise.concurrent.internal.ManagedFutureTask;
import org.glassfish.internal.deployment.ApplicationLifecycleInterceptor;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author ondro
 */
@Service
public class AppLifecycleListener implements ApplicationLifecycleInterceptor {

    public static class TasksDelayedAfterDeployment {
        private List<DelayedTask> tasks = new ArrayList<>();

        public boolean add(DeploymentAwareManagedExecutorService executor, ManagedFutureTask<?> task) {
            return tasks.add(new DelayedTask(executor, task));
        }
    }

    public static class DelayedTask {

        public DelayedTask(DeploymentAwareManagedExecutorService executor, ManagedFutureTask<?> task) {
            this.executor = executor;
            this.task = task;
        }

        private DeploymentAwareManagedExecutorService executor;
        private ManagedFutureTask<?> task;

        public void executeNow() {
            executor.executeManagedFutureTaskImmediately(task);
        }
    }

    @Override
    public void before(ExtendedDeploymentContext.Phase phase, ExtendedDeploymentContext context) {
        context.addTransientAppMetaData(
                TasksDelayedAfterDeployment.class.getName(), new TasksDelayedAfterDeployment());
    }

    @Override
    public void after(ExtendedDeploymentContext.Phase phase, ExtendedDeploymentContext context) {
        TasksDelayedAfterDeployment delayedTasks = context.getTransientAppMetaData(
                TasksDelayedAfterDeployment.class.getName(), TasksDelayedAfterDeployment.class);
        context.getTransientAppMetadata().remove(
                TasksDelayedAfterDeployment.class.getName());
        delayedTasks.tasks.forEach(DelayedTask::executeNow);
    }

}
