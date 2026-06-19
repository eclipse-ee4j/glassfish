/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.annotation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import static javax.lang.model.SourceVersion.RELEASE_21;

/**
 * A JSR 169 compliant annotation processor for Handler annotation This is required for JDK8+, since APT has been
 * deprecated.
 *
 * @author Romain Grecourt
 */
@SupportedAnnotationTypes(value = {
    "com.sun.jsftemplating.annotation.Handler",
    "com.sun.jsftemplating.annotation.HandlerInput",
    "com.sun.jsftemplating.annotation.HandlerOutput" })
@SupportedSourceVersion(RELEASE_21)
@SupportedOptions(value = {
    "AnnotationVerifier.Annotations",
    "AnnotationVerifier.Baseclasses",
    "AnnotationVerifier.ClassAnnotation.Mappings" })
public class HandlerAP extends AbstractProcessor {

    public static final String HANDLER_FILE = "META-INF/jsftemplating/Handler.map";
    private PrintWriter writer = null;
    private boolean _setup = false;
    private Map handlers = new HashMap();

    private boolean setup() {
        if (_setup) {
            // Don't do setup more than once
            return true;
        }
        try {
            // Create factory mapping file
            writer = getMapWriter();
        } catch (IOException ex) {
            StringWriter buf = new StringWriter();
            ex.printStackTrace(new PrintWriter(buf));
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String
                    .format("Unable to write %s file while processing @FormatDefinition annotation %s", HANDLER_FILE, buf.toString()));
            return false;
        }
        _setup = true;
        return _setup;
    }

    private PrintWriter getMapWriter() throws IOException {
        PrintWriter _writer = null;
        ClassLoader cl = this.getClass().getClassLoader();
        URL url;
        for (Enumeration<URL> urls = cl.getResources(HANDLER_FILE); urls.hasMoreElements() && (_writer == null);) {
            url = urls.nextElement();
            if ((url != null) && new File(url.getFile()).canRead()) {
                // Append to the existing file...
                _writer = new PrintWriter(new FileOutputStream(url.getFile(), true));
            }
        }
        if (_writer == null) {
            // File not found, create a new one...
            FileObject fo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", HANDLER_FILE);
            _writer = new PrintWriter(fo.openWriter());
            return _writer;
        }
        return _writer;
    }

    private static final class ElementVisitor extends SimpleElementVisitor6<TypeElement, Void> {

        @Override
        public TypeElement visitType(TypeElement e, Void p) {
            return e;
        }
    }

    private static final class ExecutableElementVisitor extends SimpleElementVisitor6<ExecutableElement, Void> {

        @Override
        public ExecutableElement visitExecutable(ExecutableElement e, Void p) {
            return e;
        }
    }

    private static TypeElement getTypeElement(Element elt) {
        return elt.accept(new ElementVisitor(), null);
    }

    private static TypeElement getDeclaringTypeElement(Element elt) {
        return getTypeElement(elt.getEnclosingElement());
    }

    private String getJavadocComments(Element elt) {
        return processingEnv.getElementUtils().getDocComment(elt);
    }

    /**
     * <p>
     * This is a helper method that writes out property lines to represent either a HandlerInput or a HandlerOutput. The
     * <code>type</code> that is passed in is expected to be either <code>input</code> or <code>output</code>.
     * </p>
     */
    private void writeIOProperties(String id, String type, List<AnnotationValue> ioList) {
        int cnt = 0;
        for (AnnotationValue ioVal : ioList) {
            // Process each @HandlerInput annotation...
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> prop : ((AnnotationMirror) ioVal.getValue())
                    .getElementValues().entrySet()) {
                // Look at each "param": @Handler<I/O>put(param=)
                writer.println(id + "." + type + "[" + cnt + "]." + prop.getKey().getSimpleName() + "="
                        + convertClassName(prop.getValue().getValue().toString()));
            }
            cnt++;
        }
    }

    /**
     * <p>
     * This method attempts to convert the given <code>clsName</code> to a valid class name. The issue is that arrays appear
     * something like "java.lang.String[]" where they should appear "[Ljava.lang.String;".
     * </p>
     */
    private String convertClassName(String str) {
        int idx = str.indexOf("[]");
        if (idx == -1) {
            // For not only worry about Strings that contain array brackets
            return str;
        }

        // Count []'s
        int count = 0;
        while (idx != -1) {
            str = str.replaceFirst("\\[]", "");
            idx = str.indexOf("[]");
            count++;
        }

        // Generate new String
        String brackets = "";
        for (idx = 0; idx < count; idx++) {
            brackets += "[";
        }
        // Return something of the format: [Ljava.lang.String;
        return brackets + "L" + str + ";";
    }

    /**
     * <p>
     * This method strips off HTML tags, converts "&lt;" and "&gt;", inserts '#' characters in front of each line, and
     * ensures there are no trailing returns.
     * </p>
     */
    private String formatComment(String javadoc) {
        if (javadoc == null) {
            // No JavaDoc, return
            return "";
        }

        // First trim off extra stuff
        int idx = javadoc.indexOf("@param");
        if (idx > -1) {
            // Ignore @param stuff
            javadoc = javadoc.substring(0, idx);
        }
        javadoc = javadoc.trim();

        // Now process the String
        StringBuilder buf = new StringBuilder("\n# ");
        int len = javadoc.length();
        char ch;
        idx = 0;
        while (idx < len) {
            ch = javadoc.charAt(idx);
            switch (ch) {
                case '&':
                    if ((idx + 3) < len) {
                        if ((javadoc.charAt(idx + 2) == 't') && (javadoc.charAt(idx + 3) == ';')) {
                            if (javadoc.charAt(idx + 1) == 'g') {
                                buf.append('>');
                                idx += 3;
                            } else if (javadoc.charAt(idx + 1) == 'l') {
                                buf.append('<');
                                idx += 3;
                            }
                        }
                    }
                    break;
                case '<':
                    idx++;
                    while ((idx < len) && (javadoc.charAt(idx) != '>')) {
                        idx++;
                    }
                    break;
                case '>':
                    idx++;
                    while ((idx < len) && (javadoc.charAt(idx) != '<')) {
                        idx++;
                    }
                    break;
                case '\n':
                case '\r':
                    if (((idx + 1) > len) && ((javadoc.charAt(idx + 1) == '\n') || (javadoc.charAt(idx + 1) == '\r'))) {
                        idx++;
                    }
                    buf.append("\n# ");
                    break;
                default:
                    buf.append(ch);
            }
            idx++;
        }

        // Return the stripped javadoc
        return buf.toString();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        String key;
        Object value;
        String id;
        List<AnnotationValue> input;
        List<AnnotationValue> output;

        setup();

        for (Element decl : roundEnv.getElementsAnnotatedWith(Handler.class)) {
            for (AnnotationMirror an : decl.getAnnotationMirrors()) {
                // Loop through the NVPs contained in the annotation
                id = null;
                input = null;
                output = null;
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : an.getElementValues().entrySet()) {
                    // At this point I'm processing a "Handler" annotation
                    // it may contain "id", "input", "output"
                    key = entry.getKey().getSimpleName().toString();
                    value = entry.getValue().getValue();
                    if (key.equals(Handler.ID)) {
                        // Found 'id', save it
                        id = value.toString();
                    } else if (key.equals(Handler.INPUT)) {
                        // Found inputs
                        input = (List<AnnotationValue>) value;
                    } else if (key.equals(Handler.OUTPUT)) {
                        // Found outputs
                        output = (List<AnnotationValue>) value;
                    }
                }

                TypeElement te = getTypeElement(decl);
                TypeElement teDecl = getDeclaringTypeElement(decl);

                // Sanity Check
                if (id == null) {
                    processingEnv.getMessager().printMessage(Kind.ERROR,
                            String.format("'id' is not specified for annotation of method: %s.%s", te.getQualifiedName().toString(),
                                    te.getSimpleName().toString()),
                            decl);
                }

                // Check for duplicate handler definitions
                if (handlers.get(id) != null) {
                    processingEnv.getMessager().printMessage(Kind.WARNING,
                            String.format("Handler with 'id' of '%s' is declared more than once!'", id), decl);
                }
                handlers.put(id, id);

                // Record class / method names (and javadoc comment)
                writer.println(formatComment(getJavadocComments(decl)));
                writer.println(String.format("%s.class=%s", id, teDecl.getQualifiedName()));
                writer.println(String.format("%s.method=%s", id, decl.getSimpleName()));

                // Now record inputs for this handler...
                if (input != null) {
                    writeIOProperties(id, "input", input);
                }

                // Now record outputs for this handler...
                if (output != null) {
                    writeIOProperties(id, "output", output);
                }

                // Method signature checks...
                // Make sure method is accessible (public)
                if (!decl.getModifiers().contains(Modifier.PUBLIC)) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, String.format("Annotated method: %s.%s should be declared public",
                            teDecl.getQualifiedName().toString(), decl.getSimpleName().toString()), decl);
                }

                // Make sure correct args are specified
                ExecutableElement exe = decl.accept(new ExecutableElementVisitor(), null);
                List<? extends VariableElement> params = exe.getParameters();
                String pdec = params.iterator().next().asType().toString();
                if ((params.size() != 1) || !pdec.equals("com.sun.jsftemplating.layout.descriptors.handler.HandlerContext")) {

                    processingEnv.getMessager().printMessage(Kind.ERROR, String.format(
                            "Annotated method: %s.%s  must contain a single parameter of type 'com.sun.jsftemplating.layout.descriptors.handler.HandlerContext', instead type: %s was found",
                            teDecl.getQualifiedName(), decl.getSimpleName(), pdec), decl);
                }
            }
        }

        if (_setup) {
            writer.close();
        }
        return roundEnv.processingOver();
    }
}
