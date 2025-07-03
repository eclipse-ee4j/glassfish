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
package org.glassfish.main.jnosql.jakartapersistence.mapping.glassfishcontext;

import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Repository;
import jakarta.inject.Inject;

import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.jnosql.jakartapersistence.mapping.spi.JakartaPersistenceExtension;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.classmodel.reflect.InterfaceModel;
import org.glassfish.hk2.classmodel.reflect.ParameterizedInterfaceModel;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.ServerModuleCdiRegistry;
import org.glassfish.weld.DeploymentImpl;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINEST;
import static org.glassfish.internal.deployment.Deployment.CDI_BEFORE_EXTENSIONS_STARTED;
import static org.glassfish.internal.deployment.Deployment.CDI_REGISTER_SERVER_MODULES;
import static org.glassfish.weld.WeldDeployer.WELD_DEPLOYMENT;

/**
 *
 * @author Ondro Mihalyi
 */
@Service
@RunLevel(value = StartupRunLevel.VAL, mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
public class GlassFishClassScanner implements PostConstruct, EventListener {

    private static final Logger LOG = Logger.getLogger(GlassFishClassScanner.class.getName());

    @Inject
    private Events events;

    @Override
    public void postConstruct() {
        events.register(this);
    }

    @Override
    public void event(Event<?> event) {
        LOG.log(FINEST, () -> "event(event.name=" + event.name() + ", event.hook=" + event.hook() + ")");
        ApplicationInfo appInfo;
        ServerModuleCdiRegistry registry;
        if (null != (appInfo = CDI_BEFORE_EXTENSIONS_STARTED.getHook((Event<ApplicationInfo>) event))) {
            DeploymentImpl deploymentImpl = appInfo.getTransientAppMetaData(WELD_DEPLOYMENT, DeploymentImpl.class);
            DeploymentContext deploymentContext = appInfo.getTransientAppMetaData(DeploymentContext.class.getName(), DeploymentContext.class);
            final Types types = deploymentContext.getTransientAppMetaData(Types.class.getName(), Types.class);
            deploymentImpl.getExtensions().forEach(ext -> {
                if (ext.getValue() instanceof JakartaPersistenceExtension jpaJnosqlExtension) {
                    jpaJnosqlExtension.setScanner(createScanner(types));
                } else if (ext.getValue() instanceof JnosqlAnnotationsScannerExtension jnosqlExtension) {
                    jnosqlExtension.setTypes(types);
                }
            });
        } else if (null != (registry = CDI_REGISTER_SERVER_MODULES.getHook((Event<ServerModuleCdiRegistry>) event))) {
//            registry.registerModule("jnosql-jakarta-persistence-scanner.jar", JakartaPersistenceExtension.class.getClassLoader());
//            registry.registerModule("jnosql-jakarta-persistence.jar", this.getClass().getClassLoader());
        }
    }

    private ClassScanner createScanner(Types types) {
        return new ClassScanner() {
            @Override
            public Set<Class<?>> entities() {
                throw new UnsupportedOperationException("Not needed yet.");
            }

            @Override
            public Set<Class<?>> repositories() {
                throw new UnsupportedOperationException("Not needed yet.");
            }

            @Override
            public Set<Class<?>> embeddables() {
                throw new UnsupportedOperationException("Not needed yet.");
            }

            @Override
            public <T extends DataRepository<?, ?>> Set<Class<?>> repositories(Class<T> filter) {
                throw new UnsupportedOperationException("Not needed yet.");
            }

            @Override
            public Set<Class<?>> repositoriesStandard() {
                return types.getAllTypes()
                        .stream()
                        .filter(type -> null != type.getAnnotation(Repository.class.getName()))
                        .filter(type -> type instanceof InterfaceModel)
                        .map(InterfaceModel.class::cast)
                        .filter(intfModel -> {
                            Collection<ParameterizedInterfaceModel> interfaces = intfModel.getParameterizedInterfaces();
                            return interfaces.stream().anyMatch(intf -> {
                                return Set.of(DataRepository.class.getName()).contains(intf.getRawInterfaceName());
                            });
                        })
                        .map(intfModel -> {
                            try {
                                return Thread.currentThread().getContextClassLoader().loadClass(intfModel.getName());
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toSet());
            }

            @Override
            public Set<Class<?>> customRepositories() {
                return types.getAllTypes()
                        .stream().filter(type -> null != type.getAnnotation(Repository.class.getName()))
                        .map(Type::getClass)
                        .filter(cls -> cls.isInterface() && !DataRepository.class.isAssignableFrom(cls))
                        .collect(Collectors.toSet());
            }

        };
    }

}
