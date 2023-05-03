/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.component.MultiMap;

/**
 * Represents a whole DOM tree.
 *
 * @author Kohsuke Kawaguchi
 */
public class DomDocument<T extends Dom> {
    /**
     * A hook to perform variable replacements on the attribute/element values
     * that are found in the configuration file.
     * The translation happens lazily when objects are actually created, not when
     * configuration is parsed, so this allows circular references &mdash;
     * {@link Translator} may refer to objects in the configuration file being read.
     */
    private volatile Translator translator = Translator.NOOP;

    protected final Map<ActiveDescriptor<? extends ConfigInjector>,ConfigModel> models = new HashMap<>();
    private final MultiMap<Class, List<ConfigModel>> implementorsOf = new MultiMap<>();

    /*package*/ final ServiceLocator habitat;

    /*package*/ T root;

    private final DomDecorator decorator;

    private final Map<String, DataType> validators = new HashMap<>();

    /*package*/ static final List<String> PRIMS = Collections.unmodifiableList(Arrays.asList(
    "boolean", "char", "long", "int", "java.lang.Boolean", "java.lang.Character", "java.lang.Long", "java.lang.Integer"));

    private final Map<String, ActiveDescriptor<? extends ConfigInjector<?>>> cache = new HashMap<>();

    public DomDocument(ServiceLocator habitat) {
        this.habitat = habitat;
        for (String prim : PRIMS) {
            validators.put(prim, new PrimitiveDataType(prim) );
        }

        decorator = habitat.getService(DomDecorator.class);
    }

    public Dom getRoot() {
        return root;
    }

    public Translator getTranslator() {
        return translator;
    }

    public void setTranslator(Translator translator) {
        this.translator = translator;
    }

    /**
     * Creates {@link ConfigModel} for the given {@link ConfigInjector} if we haven't done so.
     */
    /*package*/ ConfigModel buildModel(ActiveDescriptor<? extends ConfigInjector> i) {
        ConfigModel m = models.get(i);
        if(m==null) {
            m = new ConfigModel(this, i, i.getMetadata(), habitat);
        }
        return m;
    }

    /**
     * Obtains a {@link ConfigModel} for the given class (Which should have {@link Configured} annotation on it.)
     */
    public ConfigModel buildModel(Class<?> clazz) {
        return buildModel(clazz.getName());
    }

    /**
     * Obtains a {@link ConfigModel} for the given class (Which should have {@link Configured} annotation on it.)
     */
    public ConfigModel buildModel(String fullyQualifiedClassName) {

        ActiveDescriptor<? extends ConfigInjector<?>> desc;
        synchronized (cache) {
            desc = cache.get(fullyQualifiedClassName);
            if (desc == null) {
                desc = (ActiveDescriptor<? extends ConfigInjector<?>>)
                    habitat.getBestDescriptor(new InjectionTargetFilter(fullyQualifiedClassName));

                if (desc == null) {
                    throw new ConfigurationException("ConfigInjector for %s is not found, is it annotated with @Configured",fullyQualifiedClassName);
                }

                cache.put(fullyQualifiedClassName, desc);
            }
        }

        return buildModel(desc);
    }

    /**
     * Obtains the {@link ConfigModel} from the "global" element name.
     *
     * <p>
     * This method uses {@link #buildModel} to lazily build models if necessary.
     *
     * @return
     *      Null if no configurable component is registered under the given global element name.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ConfigModel getModelByElementName(String elementName) {
        ActiveDescriptor<?> i = habitat.getBestDescriptor(
                BuilderHelper.createNameAndContractFilter(ConfigInjector.class.getName(), elementName));
        if(i==null) {
            return null;
        }
        return buildModel((ActiveDescriptor<? extends ConfigInjector>) i);
    }

    private class InjectionTargetFilter
        implements IndexedFilter {

        String targetName;

        private InjectionTargetFilter(String targetName) {
            this.targetName = targetName;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean matches(Descriptor d) {
            if (d.getQualifiers().contains(InjectionTarget.class.getName())) {
                List<String> list = d.getMetadata().get("target");
                if (list == null) {
                    return false;
                }

                String value = list.get(0) ;

                // No need to synchronize on cache, it is already synchronized
                cache.put(value, (ActiveDescriptor<? extends ConfigInjector<?>>) habitat.reifyDescriptor(d));

                if (value.equals(targetName)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public String getAdvertisedContract() {
            return ConfigInjector.class.getName();
        }

        @Override
        public String getName() {
            return null;
        }
    }

    /**
     * Calculates all @Configured interfaces subclassing the passed interface type.
     *
     * @param intf a @Configured interface
     * @return List of all @Configured subclasses
     */
    public synchronized List<ConfigModel> getAllModelsImplementing(Class<?> intf) throws ClassNotFoundException {
        if (implementorsOf.size()==0) {
            initXRef();
        }
        return implementorsOf.getOne(intf);
    }

    /**
     * probably a bit slow, calculates all the @Configured interfaces subclassing, useful
     * to find all possible subclasses of a type.
     */
    private void initXRef() throws ClassNotFoundException {

        // force initialization of all the config models.
        for (ServiceHandle<?> i : habitat.getAllServiceHandles(ConfigInjector.class)) {
            buildModel((ActiveDescriptor<? extends ConfigInjector>) i.getActiveDescriptor());
        }

        for (ConfigModel cm : models.values()) {
            Class<?> targetType = cm.classLoaderHolder.loadClass(cm.targetTypeName);

            Set<Class<?>> visited = new HashSet<>();
            do {
                Deque<Class<?>> interfaces = new ArrayDeque<>(Arrays.asList(targetType.getInterfaces()));
                Class<?> intf;
                while ((intf = interfaces.poll()) != null) {
                    if (visited.add(intf)) {
                        if (intf.isAnnotationPresent(Configured.class)) {
                            addXRef(intf, cm);
                        }
                        interfaces.addAll(Arrays.asList(intf.getInterfaces()));
                    }
                }
                targetType = targetType.getSuperclass();
            } while (targetType != null);
        }
    }

    private void addXRef(Class<?> type, ConfigModel cm) {
        List<ConfigModel> models = implementorsOf.getOne(type);
        if (models == null) {
            models = new ArrayList<>();
            implementorsOf.add(type, models);
        }
        models.add(cm);
    }


    // TODO: to be removed once we make sure that no one is using it anymore
    @Deprecated
    public ConfigModel getModel(Class c) {
        return buildModel(c);
    }

    public Dom make(ServiceLocator habitat, XMLStreamReader in, T parent, ConfigModel model) {
        return decorator != null
            ? decorator.decorate(habitat, this, parent, model, in)
            : new Dom(habitat,this,parent,model,in);
    }

    /**
     * Writes back the whole DOM tree as an XML document.
     *
     * <p>
     * To support writing a subtree, this method doesn't invoke the start/endDocument
     * events. Those are the responsibility of the caller.
     *
     * @param w
     *      Receives XML infoset stream.
     */
    public void writeTo(XMLStreamWriter w) throws XMLStreamException {
        root.writeTo(null,w);
    }

    /*package*/
    DataType getValidator(String dataType) {
        synchronized(validators) {
            DataType validator = validators.get(dataType);
            if (validator != null) {
                return (validator);
            }
        }
        Collection<DataType> dtfh = habitat.getAllServices(DataType.class);
        synchronized(validators) {
            for (DataType dt : dtfh) {
                validators.put(dt.getClass().getName(), dt);
            }
            return (validators.get(dataType));
        }
    }

}
