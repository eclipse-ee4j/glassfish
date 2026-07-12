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

package org.glassfish.resources.module;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.Resources;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.api.naming.SimpleJndiName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Tests for {@link ResourcesDeployer#collectAppScopedResourceJndiNames(Application, String)}, which feeds
 * the resource validator on the load (server restart) path so that app-scoped resources declared in
 * {@code glassfish-resources.xml} (e.g. {@code java:app/jdbc/mydb}) are recognized as legitimately declared
 * instead of failing a premature live JNDI lookup. Regression test for issue #25961.
 */
public class ResourcesDeployerTest {

    private static final String APP_NAME = "sample";

    @Test
    public void appScopedBindableResourcesAreCollectedUnderAppName() {
        Application app = application(
            resources(pool("dbPool"), jdbcResource("java:app/jdbc/mydb")));

        Map<String, List<SimpleJndiName>> result =
            ResourcesDeployer.collectAppScopedResourceJndiNames(app, APP_NAME);

        assertThat(result, hasKey(APP_NAME));
        assertThat(result.get(APP_NAME), contains(new SimpleJndiName("java:app/jdbc/mydb")));
    }

    @Test
    public void connectionPoolsAreNotTreatedAsBindableNames() {
        // A connection pool is not bindable and has no JNDI name; only the jdbc-resource must be collected.
        Application app = application(
            resources(pool("dbPool"), jdbcResource("java:app/jdbc/mydb")));

        Map<String, List<SimpleJndiName>> result =
            ResourcesDeployer.collectAppScopedResourceJndiNames(app, APP_NAME);

        assertThat(result.get(APP_NAME).size(), is(1));
    }

    @Test
    public void moduleScopedResourcesAreKeyedByModuleName() {
        Application app = application(
            resources(jdbcResource("java:app/jdbc/mydb")),
            module("web_war", resources(jdbcResource("java:module/jdbc/local"))));

        Map<String, List<SimpleJndiName>> result =
            ResourcesDeployer.collectAppScopedResourceJndiNames(app, APP_NAME);

        assertThat(result, hasKey(APP_NAME));
        // "web_war" is normalized back to the archive name "web.war".
        assertThat(result, hasKey("web.war"));
        assertThat(result.get("web.war"), contains(new SimpleJndiName("java:module/jdbc/local")));
    }

    @Test
    public void noResourcesYieldsEmptyMap() {
        Application app = application(null);
        assertThat(ResourcesDeployer.collectAppScopedResourceJndiNames(app, APP_NAME).isEmpty(), is(true));
    }

    @Test
    public void multipleAppScopedResourcesAreAllCollected() {
        Application app = application(
            resources(jdbcResource("java:app/jdbc/mydb"), jdbcResource("java:app/jdbc/other")));

        Map<String, List<SimpleJndiName>> result =
            ResourcesDeployer.collectAppScopedResourceJndiNames(app, APP_NAME);

        assertThat(result.get(APP_NAME), containsInAnyOrder(
            new SimpleJndiName("java:app/jdbc/mydb"), new SimpleJndiName("java:app/jdbc/other")));
        assertThat(result, not(hasKey("web.war")));
    }

    // --- config-bean proxies (no Mockito available in this module) ---

    private static Application application(Resources appResources, Module... modules) {
        Map<String, Object> values = new HashMap<>();
        values.put("getResources", appResources);
        values.put("getModule", List.of(modules));
        return proxy(Application.class, values);
    }

    private static Module module(String name, Resources moduleResources) {
        Map<String, Object> values = new HashMap<>();
        values.put("getName", name);
        values.put("getResources", moduleResources);
        return proxy(Module.class, values);
    }

    private static Resources resources(Resource... resources) {
        return proxy(Resources.class, Map.of("getResources", List.of(resources)));
    }

    private static BindableResource jdbcResource(String jndiName) {
        return proxy(BindableResource.class, Map.of("getJndiName", jndiName));
    }

    private static ResourcePool pool(String name) {
        return proxy(ResourcePool.class, Map.of("getName", name));
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Map<String, Object> values) {
        InvocationHandler handler = (p, method, args) -> values.get(method.getName());
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler);
    }
}
