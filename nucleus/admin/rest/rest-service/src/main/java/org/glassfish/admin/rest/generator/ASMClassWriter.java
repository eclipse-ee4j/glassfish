/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.admin.rest.generator;

import com.sun.ejb.codegen.ClassGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.glassfish.admin.rest.RestLogging;
import org.glassfish.admin.rest.utils.ResourceUtil;
import org.glassfish.hk2.api.ServiceLocator;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V17;

/**
 * @author Ludovic Champenois
 */
public class ASMClassWriter implements ClassWriter {
    private final static String INJECTOR_FIELD = "serviceLocator";
    private final static String FORNAME_INJECTOR_TYPE = "Lorg/glassfish/hk2/api/ServiceLocator;";
    private final static String INTERFACE_INJECTOR_TYPE = "org/glassfish/hk2/api/ServiceLocator";
    private final static String CREATE_AND_INITIALIZE = "createAndInitialize";
    private final static String CREATE_AND_INITIALIZE_SIG = "(Ljava/lang/Class;)Ljava/lang/Object;";

    private final org.objectweb.asm.ClassWriter cw = new org.objectweb.asm.ClassWriter(0);
    private String className;
    private ServiceLocator habitat;
    private final String generatedPath;
    private final Map<String, String> generatedMethods = new HashMap<>();
    //  private String baseClassName;
    //  private String resourcePath;

    public ASMClassWriter(ServiceLocator habitat, String generatedPath, String className, String baseClassName, String resourcePath) {
        this.habitat = habitat;
        this.className = className;
        this.generatedPath = generatedPath;
        //     this.baseClassName = baseClassName;
        //     this.resourcePath = resourcePath;
        if (baseClassName.indexOf("TemplateCommand") != -1) { //constructor is created in createCommandResourceConstructor
            return;
        }
        if (baseClassName.indexOf(".") != -1) {
            baseClassName = baseClassName.replace('.', '/');
        } else {
            baseClassName = "org/glassfish/admin/rest/resources/" + baseClassName;
        }
        cw.visit(V17, ACC_PUBLIC + ACC_SUPER, generatedPath + className, null, baseClassName, null);

        if (resourcePath != null) {
            RestLogging.restLogger.log(Level.FINE, "Creating resource with path {0} (1)", resourcePath);
            AnnotationVisitor av0 = cw.visitAnnotation("Ljakarta/ws/rs/Path;", true);
            av0.visit("value", "/" + resourcePath + "/");
            av0.visitEnd();
        }

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, baseClassName, "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

    }

    @Override
    public void createCustomResourceMapping(String resourceClassName, String mappingPath) {

        //gen in custom package!
        String completeName = "org/glassfish/admin/rest/resources/custom/" + resourceClassName;
        String baseClassName = generatedPath + className;
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get" + resourceClassName, "()L" + completeName + ";", null, null);

        RestLogging.restLogger.log(Level.FINE, "Creating resource with path {0} (2)", mappingPath);
        AnnotationVisitor av0 = mv.visitAnnotation("Ljakarta/ws/rs/Path;", true);
        av0.visit("value", mappingPath + "/");
        av0.visitEnd();

        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, baseClassName, INJECTOR_FIELD, FORNAME_INJECTOR_TYPE);
        mv.visitLdcInsn(Type.getType("L" + completeName + ";"));
        mv.visitMethodInsn(INVOKEINTERFACE, INTERFACE_INJECTOR_TYPE, CREATE_AND_INITIALIZE, CREATE_AND_INITIALIZE_SIG, true);
        mv.visitTypeInsn(CHECKCAST, completeName);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, baseClassName, "getEntity", "()Lorg/jvnet/hk2/config/Dom;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, completeName, "setEntity", "(Lorg/jvnet/hk2/config/Dom;)V", false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();

    }

    @Override
    public void createGetCommandResourcePaths(List<CommandResourceMetaData> commandMetaData) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getCommandResourcesPaths", "()[[Ljava/lang/String;", null, null);
        mv.visitCode();
        int size = 0;
        for (CommandResourceMetaData metaData : commandMetaData) {
            if (ResourceUtil.commandIsPresent(habitat, metaData.command)) {
                size++;
            }
        }
        mv.visitIntInsn(BIPUSH, size); //11 number of entries
        mv.visitTypeInsn(ANEWARRAY, "[Ljava/lang/String;"); //first outer array
        int index = -1;
        for (CommandResourceMetaData metaData : commandMetaData) {
            if (ResourceUtil.commandIsPresent(habitat, metaData.command)) {
                index++;
                switch (index) {//inner array has 3 strings,
                case 0:
                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_0);
                    mv.visitInsn(ICONST_3);
                    break;
                case 1:
                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_1);
                    mv.visitInsn(ICONST_3);
                    break;
                case 2:
                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_2);
                    mv.visitInsn(ICONST_3);
                    break;
                case 3:
                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_3);
                    mv.visitInsn(ICONST_3);
                    break;
                case 4:
                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_4);
                    mv.visitInsn(ICONST_3);
                    break;
                case 5:
                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_5);
                    mv.visitInsn(ICONST_3);
                    break;
                default:
                    mv.visitInsn(DUP);
                    mv.visitIntInsn(BIPUSH, index);
                    mv.visitInsn(ICONST_3);
                    break; //6 and bigger is DIFFERENT!!!
                } //switch

                mv.visitTypeInsn(ANEWARRAY, "java/lang/String"); //inner array

                mv.visitInsn(DUP);
                mv.visitInsn(ICONST_0);
                mv.visitLdcInsn(metaData.resourcePath);
                mv.visitInsn(AASTORE);
                mv.visitInsn(DUP);
                mv.visitInsn(ICONST_1);
                mv.visitLdcInsn(metaData.httpMethod);
                mv.visitInsn(AASTORE);
                mv.visitInsn(DUP);
                mv.visitInsn(ICONST_2);
                mv.visitLdcInsn(metaData.command);
                mv.visitInsn(AASTORE);
                mv.visitInsn(AASTORE);
            }

        } //for

        mv.visitInsn(ARETURN);
        mv.visitMaxs(7, 1);
        mv.visitEnd();

    }

    @Override
    public void createGetCommandResource(String commandResourceClassName, String resourcePath) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get" + commandResourceClassName,
                "()L" + generatedPath + commandResourceClassName + ";", null, null);

        RestLogging.restLogger.log(Level.FINE, "Creating resource with path {0} (3)", resourcePath);
        AnnotationVisitor av0 = mv.visitAnnotation("Ljakarta/ws/rs/Path;", true);
        av0.visit("value", resourcePath + "/");
        av0.visitEnd();

        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, generatedPath + className, INJECTOR_FIELD, FORNAME_INJECTOR_TYPE);
        mv.visitLdcInsn(Type.getType("L" + generatedPath + commandResourceClassName + ";"));
        mv.visitMethodInsn(INVOKEINTERFACE, INTERFACE_INJECTOR_TYPE, CREATE_AND_INITIALIZE, CREATE_AND_INITIALIZE_SIG, true);
        mv.visitTypeInsn(CHECKCAST, generatedPath + commandResourceClassName);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    @Override
    public void createCommandResourceConstructor(String commandResourceClassName, String commandName, String httpMethod,
            boolean linkedToParent, CommandResourceMetaData.ParameterMetaData[] commandParams, String commandDisplayName,
            String commandAction) {

        String baseClassName = "";
        if (httpMethod.equals("GET")) {
            baseClassName = "org/glassfish/admin/rest/resources/TemplateCommandGetResource";
        } else if (httpMethod.equals("DELETE")) {
            baseClassName = "org/glassfish/admin/rest/resources/TemplateCommandDeleteResource";
        } else if (httpMethod.equals("POST")) {
            baseClassName = "org/glassfish/admin/rest/resources/TemplateCommandPostResource";
        } else {
            throw new GeneratorException("Invalid httpMethod specified: " + httpMethod);
        }
        boolean isget = (httpMethod.equals("GET"));

        cw.visit(V17, ACC_PUBLIC + ACC_SUPER, generatedPath + commandResourceClassName, null, baseClassName, null);
        //     cw.visitInnerClass(generatedPath + commandResourceClassName +"$1", null, null, 0);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(commandResourceClassName);
        mv.visitLdcInsn(commandName);
        mv.visitLdcInsn(httpMethod);
        if (!isget) {
            mv.visitLdcInsn(commandAction);
            mv.visitLdcInsn(commandDisplayName);
        }
        if (linkedToParent == true) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }

        //next is different based on parent
        if (!isget) {

            mv.visitMethodInsn(INVOKESPECIAL, baseClassName, "<init>",
                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V", false);
        } else {
            mv.visitMethodInsn(INVOKESPECIAL, baseClassName, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V", false);

        }
        mv.visitInsn(RETURN);

        if (!isget) {
            mv.visitMaxs(7, 1); //GET is 5!!!
        } else {
            mv.visitMaxs(5, 1);

        }
        mv.visitEnd();

        if (commandParams != null) {

            mv = cw.visitMethod(ACC_PROTECTED, "getCommandParams", "()Ljava/util/HashMap;",
                    "()Ljava/util/HashMap<Ljava/lang/String;Ljava/lang/String;>;", null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/util/HashMap");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, 1);

            for (CommandResourceMetaData.ParameterMetaData commandParam : commandParams) {
                mv.visitVarInsn(ALOAD, 1);
                mv.visitLdcInsn(commandParam.name);
                mv.visitLdcInsn(commandParam.value);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
                mv.visitInsn(POP);
            }

            mv.visitVarInsn(ALOAD, 1);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(3, 2);
            mv.visitEnd();
        }

    }

    @Override
    public void done() {
        cw.visitEnd();
        try {
            defineClass(this.getClass(), cw.toByteArray());
            if ("true".equals(System.getenv("REST_DEBUG"))) {
                debug(className, cw.toByteArray());
            }
        } catch (Exception ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void createGetDeleteCommand(String commandName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getDeleteCommand", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(commandName);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    @Override
    public void createGetPostCommand(String commandName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getPostCommand", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(commandName);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    @Override
    public void createGetChildResource(String path, String childResourceClassName) {
        String childClass;
        if (childResourceClassName.equals("PropertiesBagResource")) {
            childClass = "org/glassfish/admin/rest/resources/PropertiesBagResource";
        } else {
            childClass = generatedPath + childResourceClassName;
        }

        String methodName = "get" + childResourceClassName;
        if (childClass.equals(generatedMethods.get(methodName))) {
            return;
        }

        generatedMethods.put(methodName, childClass);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, methodName, "()L" + childClass + ";", null, null);

        RestLogging.restLogger.log(Level.FINE, "Creating resource with path {0} (4)", path);
        AnnotationVisitor av0 = mv.visitAnnotation("Ljakarta/ws/rs/Path;", true);
        av0.visit("value", path + "/");
        av0.visitEnd();

        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, generatedPath + className, INJECTOR_FIELD, FORNAME_INJECTOR_TYPE);
        mv.visitLdcInsn(Type.getType("L" + childClass + ";"));
        mv.visitMethodInsn(INVOKEINTERFACE, INTERFACE_INJECTOR_TYPE, CREATE_AND_INITIALIZE, CREATE_AND_INITIALIZE_SIG, true);
        mv.visitTypeInsn(CHECKCAST, childClass);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, generatedPath + className, "getEntity", "()Lorg/jvnet/hk2/config/Dom;", false);
        mv.visitLdcInsn(path);
        mv.visitMethodInsn(INVOKEVIRTUAL, childClass, "setParentAndTagName", "(Lorg/jvnet/hk2/config/Dom;Ljava/lang/String;)V", false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(3, 2);
        mv.visitEnd();
    }

    @Override
    public void createGetChildResourceForListResources(String keyAttributeName, String childResourceClassName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get" + childResourceClassName,
                "(Ljava/lang/String;)L" + generatedPath + childResourceClassName + ";", null, null);

        RestLogging.restLogger.log(Level.FINE, "Creating resource with path {0} (5)", keyAttributeName);
        AnnotationVisitor av0 = mv.visitAnnotation("Ljakarta/ws/rs/Path;", true);
        av0.visit("value", "{" + keyAttributeName + "}/");
        av0.visitEnd();

        av0 = mv.visitParameterAnnotation(0, "Ljakarta/ws/rs/PathParam;", true);
        av0.visit("value", keyAttributeName);
        av0.visitEnd();

        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, generatedPath + "List" + childResourceClassName, INJECTOR_FIELD, FORNAME_INJECTOR_TYPE);
        mv.visitLdcInsn(Type.getType("L" + generatedPath + childResourceClassName + ";"));
        mv.visitMethodInsn(INVOKEINTERFACE, INTERFACE_INJECTOR_TYPE, CREATE_AND_INITIALIZE, CREATE_AND_INITIALIZE_SIG, true);
        mv.visitTypeInsn(CHECKCAST, generatedPath + childResourceClassName);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, generatedPath + "List" + childResourceClassName, "entity", "Ljava/util/List;");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, generatedPath + "List" + childResourceClassName, "tagName", "Ljava/lang/String;");
        mv.visitMethodInsn(INVOKEVIRTUAL, generatedPath + childResourceClassName, "setBeanByKey",
                "(Ljava/util/List;Ljava/lang/String;Ljava/lang/String;)V", false);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(4, 3);
        mv.visitEnd();
    }

    @Override
    public void createGetPostCommandForCollectionLeafResource(String postCommandName) {
        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "getPostCommand", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(postCommandName);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    @Override
    public void createGetDeleteCommandForCollectionLeafResource(String deleteCommandName) {
        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "getDeleteCommand", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(deleteCommandName);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    @Override
    public void createGetDisplayNameForCollectionLeafResource(String displayName) {
        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "getName", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(displayName);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public byte[] getByteClass() {
        return cw.toByteArray();
    }

    private void defineClass(Class similarClass, byte[] classBytes) throws Exception {
        String generatedClassName = "org.glassfish.admin.rest.resources.generatedASM." + className;
        RestLogging.restLogger.log(Level.FINEST, "Generating class {0}", generatedClassName);
        ClassLoader loader = similarClass.getClassLoader();
        ProtectionDomain pd = similarClass.getProtectionDomain();
        byte[] byteContent = getByteClass();
        ClassGenerator.defineClass(loader, generatedClassName, byteContent, pd);
        try {
            loader.loadClass(generatedClassName);
        } catch (ClassNotFoundException cnfEx) {
            throw new GeneratorException(cnfEx);
        }
    }

    /**
     * dump bytecode in class files so that we can  decompile them to check the real content
     */
    private void debug(String clsName, byte[] classData) {

        // the path is horribly long.  Let's just write t directly into the
        // lib dir.  It is not for loading as a class but just for us humans
        // to decompile to figure out what is going on.  No need to make it even harder!
        clsName = clsName.replace('.', '/');
        clsName = clsName.replace('\\', '/'); // just in case Windows?  unlikely...
        int index = clsName.lastIndexOf("/");

        if (index >= 0) {
            clsName = clsName.substring(index + 1);
        }

        FileOutputStream fos = null;
        try {
            String rootPath = System.getProperty(INSTALL_ROOT.getSystemPropertyName()) + File.separator
                + "lib" + File.separator + "rest" + File.separator;
            File parentDir = new File(rootPath);
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new RuntimeException("Unable to create parent directory for generated class file logging");
                }
            }

            fos = new FileOutputStream(new File(parentDir, clsName + ".class"));
            fos.write(classData);
            fos.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    RestLogging.restLogger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
