/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.generator.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.logging.Level;

import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.RestLogging;
import org.glassfish.admin.rest.client.RestClientBase;
import org.glassfish.admin.rest.utils.Util;
import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandModel.ParamModel;
import org.jvnet.hk2.config.ConfigModel;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author jdlee
 */
public class JavaClientClassWriter implements ClientClassWriter {
    private final String className;
    private BufferedWriter source;

    private static final String TMPL_CLASS_HEADER = "package " + Constants.CLIENT_JAVA_PACKAGE + ";\n" + "import java.util.HashMap;\n"
            + "import java.util.Map;\n" + "import jakarta.ws.rs.client.Client;\n\n" + "public class CLASSNAME extends RestClientBase {\n";
    private static final String TMPL_CTOR_DOMAIN = "    private RestClient parent;\n" + "    public CLASSNAME (RestClient parent) {\n"
            + "        super(parent.client, null);\n" + "        this.parent = parent;\n" + "    }\n\n";
    private static final String TMPL_CTOR_OTHER_WITH_KEY = "    private String name;\n"
            + "    protected CLASSNAME (Client c, RestClientBase p, String name) {\n" + "        super(c, p);\n"
            + "        this.name = name;\n" + "    }\n\n";
    private static final String TMPL_CTOR_OTHER_NO_KEY = "    protected  CLASSNAME (Client c, RestClientBase p) {\n"
            + "        super(c,p);\n" + "    }\n\n";
    private static final String TMPL_GET_REST_URL = // TODO: Test this code heavily
            "    @Override\n" + "    protected String getRestUrl() {\n" + "        return super.getRestUrl()HASKEY;\n" + "    }\n\n";
    private static final String TMPL_CTOR_SIMPLE = "package " + Constants.CLIENT_JAVA_PACKAGE + ";\n"
            + "import jakarta.ws.rs.client.Client;\n\n" + "public class CLASSNAME extends PARENTCLASS {\n"
            + "    protected  CLASSNAME (Client c, RestClientBase p) {\n" + "        super(c,p);\n" + "    }\n\n";
    private static final String TMPL_GET_SEGMENT = "    @Override protected String getSegment() {\n" + "        return \"/TAGNAME\";\n"
            + "    }\n\n";
    private static final String TMPL_GETTERS_AND_SETTERS = "    public TYPE getMETHOD() {\n"
            + "        return getValue(\"FIELDNAME\", TYPE.class);\n" + "    }\n\n" + "    public void setMETHOD(TYPE value) {\n"
            + "        setValue(\"FIELDNAME\", value);\n" + "    }\n\n";
    private static final String TMPL_COLLECTION_LEAF_RESOURCE = "    public CLASSNAME getCLASSNAME() {\n"
            + "        return new CLASSNAME (client, this);\n" + "    }\n\n";
    private static final String TMPL_COMMAND = "    public RestResponse METHODNAME(SIG1) {\n"
            + "        return METHODNAME(PARAMS new HashMap<String, Object>());\n" + "    }\n\n"
            + "    public RestResponse METHODNAME(SIG2 Map<String, Object> additional) {\n" + "METHODBODY" + "    }\n\n";
    private static final String TMPL_METHOD_BODY = "        Map<String, Object> payload = new HashMap<String, Object>();\n" + "PUTS"
            + "        payload.putAll(additional);\n"
            + "        return execute(Method.HTTPMETHOD, \"/RESOURCEPATH\", payload, NEEDSMULTIPART);\n";

    public JavaClientClassWriter(final ConfigModel model, final String className, Class parent, File baseDirectory) {
        this.className = className;

        File packageDir = new File(baseDirectory, Constants.CLIENT_JAVA_PACKAGE_DIR);
        packageDir.deleteOnExit();
        boolean success = packageDir.exists() || packageDir.mkdirs();
        if (!success) {
            throw new RuntimeException("Unable to create output directory"); // i18n
        }
        File classFile = new File(packageDir, className + ".java");
        try {
            boolean createSuccess = classFile.createNewFile();
            if (!createSuccess) {
                RestLogging.restLogger.log(Level.SEVERE, RestLogging.FILE_CREATION_FAILED, classFile.getName());
            }
            classFile.deleteOnExit();
            source = new BufferedWriter(new FileWriter(classFile, UTF_8));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        if (parent.isAssignableFrom(RestClientBase.class)) {
            generateRestClientBaseChild(model);
        } else {
            generateSimpleCtor(parent.getName());
        }
    }

    protected final void generateRestClientBaseChild(ConfigModel model) {
        try {
            boolean hasKey = (Util.getKeyAttributeName(model) != null);
            boolean isDomain = className.equals("Domain");

            source.append(TMPL_CLASS_HEADER.replace("CLASSNAME", className));

            if (isDomain) {
                source.append(TMPL_CTOR_DOMAIN.replace("CLASSNAME", className));
            } else {
                if (hasKey) {
                    source.append(TMPL_CTOR_OTHER_WITH_KEY.replace("CLASSNAME", className));
                } else {
                    source.append(TMPL_CTOR_OTHER_NO_KEY.replace("CLASSNAME", className));
                }
            }

            if (hasKey || isDomain) {
                String method = TMPL_GET_REST_URL.replace("HASKEY", hasKey ? " + \"/\" + name" : "");
                if (isDomain) {
                    method = method.replace("super.getRestUrl()", "parent.getRestUrl() + getSegment()");
                }
                source.append(method);
            }
        } catch (IOException ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }
    }

    // TODO: The next two generated ctors are identical, other than the parent class
    protected final void generateSimpleCtor(String parentClassName) {
        try {
            source.append(TMPL_CTOR_SIMPLE.replace("CLASSNAME", className).replace("PARENTCLASS", parentClassName));
        } catch (IOException ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void generateGetSegment(String tagName) {
        try {
            source.append(TMPL_GET_SEGMENT.replace("TAGNAME", tagName));
        } catch (IOException ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void generateCommandMethod(String methodName, String httpMethod, String resourcePath, CommandModel cm) {
        try {
            String parametersSignature = Util.getMethodParameterList(cm, true, false);
            boolean needsMultiPart = parametersSignature.contains("java.io.File");

            String parameters = Util.getMethodParameterList(cm, false, false);
            String method = TMPL_COMMAND.replace("METHODNAME", methodName).replace("SIG1", parametersSignature)
                    .replace("PARAMS", !parameters.isEmpty() ? (parameters + ",") : "")
                    .replace("SIG2", !parametersSignature.isEmpty() ? (parametersSignature + ",") : "")
                    .replace("METHODBODY", generateMethodBody(cm, httpMethod, resourcePath, false, needsMultiPart));

            source.append(method);
        } catch (IOException ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String generateMethodBody(CommandModel cm, String httpMethod, String resourcePath, boolean includeOptional,
            boolean needsMultiPart) {
        StringBuilder sb = new StringBuilder();
        Collection<ParamModel> params = cm.getParameters();
        if ((params != null) && (!params.isEmpty())) {
            for (ParamModel model : params) {
                Param param = model.getParam();
                boolean include = true;
                if (param.optional() && !includeOptional) {
                    continue;
                }
                String key = (!param.alias().isEmpty()) ? param.alias() : model.getName();
                String paramName = Util.eleminateHypen(model.getName());
                String put = "        payload.put(\"" + key + "\", _" + paramName + ");\n";
                sb.append(put);
            }
        }
        return TMPL_METHOD_BODY.replace("PUTS", sb.toString()).replace("HTTPMETHOD", httpMethod.toUpperCase(Locale.US))
                .replace("RESOURCEPATH", resourcePath).replace("NEEDSMULTIPART", Boolean.toString(needsMultiPart));
    }

    @Override
    public void generateGettersAndSetters(String type, String methodName, String fieldName) {
        try {
            source.append(TMPL_GETTERS_AND_SETTERS.replace("METHOD", methodName).replace("TYPE", type).replace("FIELDNAME", fieldName));
        } catch (IOException ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void createGetChildResource(ConfigModel model, String elementName, String childResourceClassName) {
        try {
            final String TMPL_GET_CHILD_RESOURCE = "    public CHILDRESOURCE getELEMENTNAME(HASKEY1) {\n"
                    + "        CHILDRESOURCE child = new CHILDRESOURCE(client, this HASKEY2);\n" + "        child.initialize();\n"
                    + "        return (child.status == 200) ? child : null;\n" + "    }\n";
            final boolean hasKey = Util.getKeyAttributeName(model) != null;
            source.append(
                    TMPL_GET_CHILD_RESOURCE.replace("CHILDRESOURCE", childResourceClassName).replace("HASKEY1", hasKey ? "String name" : "")
                            .replace("HASKEY2", hasKey ? ", name" : "").replace("ELEMENTNAME", elementName));
        } catch (IOException ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }
    }

    // TODO: Merge generateCollectionLeafResourceGetter() and generateRestLeafGetter().  Must find a meaningful name first.
    @Override
    public void generateCollectionLeafResourceGetter(String className) {
        try {
            source.append(TMPL_COLLECTION_LEAF_RESOURCE.replace("CLASSNAME", className));
        } catch (IOException ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void generateRestLeafGetter(String className) {
        try {
            source.append(TMPL_COLLECTION_LEAF_RESOURCE.replace("CLASSNAME", className));
        } catch (IOException ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void done() {
        finishClass();
    }

    private void finishClass() {
        try {
            source.append("}");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            if (source != null) {
                try {
                    source.close();
                } catch (IOException ex) {
                    RestLogging.restLogger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
