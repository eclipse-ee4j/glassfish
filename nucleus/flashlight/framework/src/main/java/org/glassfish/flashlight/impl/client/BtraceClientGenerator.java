/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.flashlight.impl.client;

/**
 * @author Mahesh Kannan
 * Started: Jul 20, 2008
 * @author Byron Nevins, August 2009
 */
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.flashlight.provider.FlashlightProbe;
import org.glassfish.flashlight.provider.ProbeRegistry;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedActionException;
import java.security.ProtectionDomain;
import java.util.Collection;

import static org.objectweb.asm.Opcodes.V11;

public class BtraceClientGenerator {
    private BtraceClientGenerator() {
        // all static class -- no instances allowed
    }

    public static byte[] generateBtraceClientClassData(int clientID, Collection<FlashlightProbe> probes) {
        // create a unique name.  It does not matter what the name is.
        String generatedClassName = "com/sun/btrace/flashlight/BTrace_Flashlight_" + clientID;
        //Start of writing a class using ASM, which will be our BTrace Client
        int cwFlags = ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS;
        ClassWriter cw = new ClassWriter(cwFlags);

        //Define the access identifiers for the BTrace Client class
        int access = Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL;
        cw.visit(V11, access, generatedClassName, null,
                "java/lang/Object", null);
        //Need a @OnMethod annotation, so prepare your Annotation Visitor for that
        cw.visitAnnotation("Lcom/sun/btrace/annotations/BTrace;", true);

        //Iterate through the probes, so you will create one method for each probe
        int methodCounter = 0;
        for (FlashlightProbe probe : probes) {
            //Preparing the class method header and params (type) for @OnMethod annotation
            StringBuilder typeDesc = new StringBuilder("void ");
            StringBuilder methodDesc = new StringBuilder("void __");
            methodDesc.append(probe.getProviderJavaMethodName()).append("__");
            methodDesc.append(clientID).append("_").append(methodCounter).append("_");
            methodDesc.append("(");
            typeDesc.append("(");
            String delim = "";
            String typeDelim = "";
            Class[] paramTypes = probe.getParamTypes();
            for (int index = 0; index < paramTypes.length; index++) {
                Class paramType = paramTypes[index];
                methodDesc.append(delim).append(paramType.getName());
                // Dont add the param type for type desc, if self is the first index
                if (!(probe.hasSelf() && (index == 0))) {
                    typeDesc.append(typeDelim).append(paramType.getName());
                    typeDelim = ",";
                }
                delim = ", ";
            }
            methodDesc.append(")");
            typeDesc.append(")");
            //Creating the class method
            Method m = Method.getMethod(methodDesc.toString());
            GeneratorAdapter gen = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, m, null, null, cw);
            // Add the @Self annotation
            if (probe.hasSelf()) {
                String[] paramNames = probe.getProbeParamNames();
                for (int index = 0; index < paramNames.length; index++) {
                    if (paramNames[index].equalsIgnoreCase(FlashlightProbe.SELF)) {
                        AnnotationVisitor paramVisitor = gen.visitParameterAnnotation(index, "Lcom/sun/btrace/annotations/Self;", true);
                        paramVisitor.visitEnd();
                    }
                }
            }
            //Add the @OnMethod annotation to this method
            AnnotationVisitor av = gen.visitAnnotation("Lcom/sun/btrace/annotations/OnMethod;", true);
            av.visit("clazz", "" + probe.getProviderClazz().getName());
            av.visit("method", probe.getProviderJavaMethodName());
            av.visit("type", typeDesc.toString());
            av.visitEnd();
            //Add the body
            gen.push(probe.getId());
            gen.loadArgArray();
            gen.invokeStatic(Type.getType(
                    ProbeRegistry.class), Method.getMethod("void invokeProbe(int, Object[])"));
            gen.returnValue();
            gen.endMethod();
            methodCounter++;
        }
        BtraceClientGenerator.generateConstructor(cw);
        cw.visitEnd();
        byte[] classData = cw.toByteArray();
        writeClass(classData, generatedClassName);
        return classData;
    }

    private static void writeClass(byte[] classData, String generatedClassName) {
        // only do this if we are in "debug" mode
//        if(Boolean.parseBoolean(System.getenv("AS_DEBUG")) == false)
//            return;

        System.out.println("**** Generated BTRACE Client " + generatedClassName);
        FileOutputStream fos = null;

        try {
            int index = generatedClassName.lastIndexOf('/');
            String rootPath = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) +
                    File.separator + "lib" + File.separator;

            String fileName = rootPath + generatedClassName.substring(index + 1) + ".class";
            //System.out.println("***ClassFile: " + fileName);
            File file = new File(fileName);

            fos = new FileOutputStream(file);
            fos.write(classData);
            fos.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                }
                catch(Exception e) {
                // can't do anything...
                }
            }
        }
    }

    private static void generateConstructor(ClassWriter cw) {
        Method m = Method.getMethod("void <init> ()");
        GeneratorAdapter gen = new GeneratorAdapter(Opcodes.ACC_PUBLIC, m, null, null, cw);
        gen.loadThis();
        gen.invokeConstructor(Type.getType(Object.class), m);
        //return the value from constructor
        gen.returnValue();
        gen.endMethod();
    }
}


/****  Example generated class (bnevins, August 2009)
 *
 * package com.sun.btrace.flashlight.org.glassfish.web.admin.monitor;

import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.OnMethod;
import jakarta.servlet.Servlet;
import org.glassfish.flashlight.provider.ProbeRegistry;

@BTrace
public final class ServletStatsProvider_BTrace_7_
{
  @OnMethod(clazz="org.glassfish.web.admin.monitor.ServletProbeProvider", method="servletInitializedEvent", type="void (jakarta.servlet.Servlet, java.lang.String, java.lang.String)")
  public static void __servletInitializedEvent__7_0_(Servlet paramServlet, String paramString1, String paramString2)
  {
    ProbeRegistry.invokeProbe(78, new Object[] { paramServlet, paramString1, paramString2 });
  }

  @OnMethod(clazz="org.glassfish.web.admin.monitor.ServletProbeProvider", method="servletDestroyedEvent", type="void (jakarta.servlet.Servlet, java.lang.String, java.lang.String)")
  public static void __servletDestroyedEvent__7_1_(Servlet paramServlet, String paramString1, String paramString2)
  {
    ProbeRegistry.invokeProbe(79, new Object[] { paramServlet, paramString1, paramString2 });
  }
}
 */
