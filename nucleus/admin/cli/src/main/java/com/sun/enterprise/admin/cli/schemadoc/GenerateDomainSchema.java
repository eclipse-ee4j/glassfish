/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli.schemadoc;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.objectweb.asm.ClassReader;

@Service(name = "generate-domain-schema")
@PerLookup
@ExecuteOn(value = { RuntimeType.DAS })
@TargetType(value = { CommandTarget.DOMAIN, CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER })
@RestEndpoints({ @RestEndpoint(configBean = Domain.class, opType = RestEndpoint.OpType.POST, // TODO: Should probably be GET
        path = "generate-domain-schema", description = "generate-domain-schema") })
@AccessRequired(resource = "domain", action = "generate-schema")
public class GenerateDomainSchema implements AdminCommand {
    @Inject
    private Domain domain;
    @Inject
    private ServiceLocator habitat;
    @Param(name = "format", defaultValue = "html", optional = true)
    private String format;
    private static final Logger logger = Logger.getLogger(GenerateDomainSchema.class.getPackage().getName());;
    File docDir;
    private Map<String, ClassDef> classDefs = new HashMap<String, ClassDef>();
    @Param(name = "showSubclasses", defaultValue = "false", optional = true)
    private Boolean showSubclasses;
    @Param(name = "showDeprecated", defaultValue = "false", optional = true)
    private Boolean showDeprecated;

    @Override
    public void execute(AdminCommandContext context) {
        try {
            URI uri = new URI(System.getProperty("com.sun.aas.instanceRootURI"));
            docDir = new File(new File(uri), "config");
            findClasses(classDefs, locateJarFiles(System.getProperty("com.sun.aas.installRoot") + "/modules"));

            getFormat().output(new Context(classDefs, docDir, showDeprecated, showSubclasses, Domain.class.getName()));
            context.getActionReport().setMessage("Finished generating " + format + " documentation in " + docDir);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private SchemaOutputFormat getFormat() {
        return habitat.getService(SchemaOutputFormat.class, format);
    }

    private List<JarFile> locateJarFiles(String modulesDir) throws IOException {
        List<JarFile> result = new ArrayList<JarFile>();
        final File[] files = new File(modulesDir).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        for (File f : files) {
            result.add(new JarFile(f));
        }
        return result;
    }

    private void findClasses(Map<String, ClassDef> classDefs, List<JarFile> jarFiles) throws IOException {
        for (JarFile jf : jarFiles) {
            if (logger.isLoggable(Level.FINE)) {
                logger.finer("GenerateDomainSchema: Jar name = " + jf.getName());
            }
            for (Enumeration<JarEntry> entries = jf.entries(); entries.hasMoreElements();) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.finer("GenerateDomainSchema: Parsing class = " + entry.getName());
                    }
                    ClassDef def = parse(jf.getInputStream(entry));
                    if (def != null) {
                        classDefs.put(def.getDef(), def);
                        for (String intf : def.getInterfaces()) {
                            final ClassDef parent = classDefs.get(intf);
                            if (parent != null) {
                                parent.addSubclass(def);
                            }
                        }
                    }
                }
            }
        }
        if (showSubclasses) {
            for (ClassDef def : classDefs.values()) {
                for (String anInterface : def.getInterfaces()) {
                    final ClassDef parent = classDefs.get(anInterface);
                    if (parent != null) {
                        parent.addSubclass(def);
                    }
                }
            }
        }
    }

    private ClassDef parse(InputStream is) throws IOException {
        DocClassVisitor visitor = new DocClassVisitor(showDeprecated);
        new ClassReader(is).accept(visitor, 0);
        return visitor.isConfigured() ? visitor.getClassDef() : null;
    }

    public static String toClassName(String value) {
        int start = value.startsWith("()") ? 2 : 0;
        start = value.substring(start).startsWith("L") ? start + 1 : start;
        final int end = value.endsWith(";") ? value.length() - 1 : value.length();
        return value.substring(start, end).replace('/', '.');
    }
}
