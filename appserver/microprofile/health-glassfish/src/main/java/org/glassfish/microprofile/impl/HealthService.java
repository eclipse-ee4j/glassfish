package org.glassfish.microprofile.impl;

import org.apache.catalina.Deployer;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.event.EventListener;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.microprofile.health.HealthReporter;
import org.jvnet.hk2.annotations.Service;

@Service(name = "healthcheck-service")
@RunLevel(StartupRunLevel.VAL)
public class HealthService implements EventListener {


    @Override
    public void event(Event<?> event) {

        HealthReporter service = Globals.getDefaultHabitat().getService(HealthReporter.class);

        if (event.is(Deployment.APPLICATION_UNLOADED) && event.hook() instanceof ApplicationInfo appInfo) {
            service.removeAllHealthChecksFrom(appInfo.getName());
        }

        if (event.is(Deployment.APPLICATION_STARTED) && event.hook() instanceof ApplicationInfo appInfo) {

        }
    }
}
