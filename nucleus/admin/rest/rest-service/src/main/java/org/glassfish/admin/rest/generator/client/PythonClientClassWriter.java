/*
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

import org.glassfish.admin.rest.utils.Util;
import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandModel;
import org.jvnet.hk2.config.ConfigModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

/**
 *
 * @author jdlee
 */
class PythonClientClassWriter implements ClientClassWriter {
    private String className;
    private StringBuilder source;
    private File packageDir;
    private static String TMPL_CTOR = "from restclientbase import *\n\nclass CLASS(RestClientBase):\n"
            + "    def __init__(self, connection, parent, name = None):\n" + "        self.name = name\n"
            + "        RestClientBase.__init__(self, connection, parent, name)\n" + "        self.parent = parent\n"
            + "        self.connection = connection\n\n" + "    def getRestUrl(self):\n"
            + "        return self.getParent().getRestUrl() + self.getSegment() + (('/' + self.name) if self.name else '')\n";
    private String TMPL_GET_SEGMENT = "    def getSegment(self):\n" + "        return '/SEGMENT'\n";
    private static String TMPL_COMMAND_METHOD = "\n    def COMMAND(self PARAMS, optional={}):\n" + "MERGE"
            + "        return self.execute('/PATH', 'METHOD', optional, MULTIPART)\n";
    private static String TMPL_GETTER_AND_SETTER = "\n    def getMETHOD(self):\n" + "        return self.getValue('FIELD')\n\n"
            + "    def setMETHOD(self, value):\n" + "        self.setValue('FIELD', value)\n";
    private static String TMPL_GET_CHILD_RESOURCE = "\n    def getELEMENT(self, name):\n" + "        from IMPORT import CHILD\n"
            + "        child = CHILD(self.connection, self, name)\n" + "        return child if (child.status == 200) else None\n";

    public PythonClientClassWriter(ConfigModel model, String className, Class parent, File baseDirectory) {
        this.className = className;

        packageDir = baseDirectory;
        packageDir.deleteOnExit();
        boolean success = packageDir.exists() || packageDir.mkdirs();
        if (!success) {
            throw new RuntimeException("Unable to create output directory"); // i18n
        }
        source = new StringBuilder(TMPL_CTOR.replace("CLASS", className));
    }

    @Override
    public void generateGetSegment(String tagName) {
        source.append(TMPL_GET_SEGMENT.replace("SEGMENT", tagName));
    }

    @Override
    public void generateCommandMethod(String methodName, String httpMethod, String resourcePath, CommandModel cm) {
        String parametersSignature = Util.getMethodParameterList(cm, true, false);
        Boolean needsMultiPart = parametersSignature.contains("java.io.File");
        String parameters = Util.getMethodParameterList(cm, false, false);
        if (!parameters.isEmpty()) {
            parameters = ", " + parameters;
        }

        StringBuilder merge = new StringBuilder();
        Collection<CommandModel.ParamModel> params = cm.getParameters();
        if ((params != null) && (!params.isEmpty())) {
            for (CommandModel.ParamModel model : params) {
                Param param = model.getParam();
                if (param.optional()) {
                    continue;
                }
                String key = (!param.alias().isEmpty()) ? param.alias() : model.getName();
                String paramName = Util.eleminateHypen(model.getName());
                merge.append("        optional['").append(key).append("'] = _").append(paramName).append("\n");
            }
        }

        source.append(TMPL_COMMAND_METHOD.replace("COMMAND", methodName).replace("PARAMS", parameters).replace("MERGE", merge.toString())
                .replace("PATH", resourcePath).replace("METHOD", httpMethod)
                .replace("MULTIPART", Util.upperCaseFirstLetter(needsMultiPart.toString())));
    }

    @Override
    public String generateMethodBody(CommandModel cm, String httpMethod, String resourcePath, boolean includeOptional,
            boolean needsMultiPart) {
        return null;
    }

    @Override
    public void generateGettersAndSetters(String type, String methodName, String fieldName) {
        source.append(TMPL_GETTER_AND_SETTER.replace("METHOD", methodName).replace("FIELD", fieldName));
    }

    @Override
    public void createGetChildResource(ConfigModel model, String elementName, String childResourceClassName) {
        final boolean hasKey = Util.getKeyAttributeName(model) != null;
        String method = TMPL_GET_CHILD_RESOURCE.replace("CHILD", childResourceClassName)
                .replace("IMPORT", childResourceClassName.toLowerCase(Locale.getDefault())).replace("ELEMENT", elementName);
        if (!hasKey) {
            method = method.replace(", name", "");
        }
        source.append(method);
    }

    @Override
    public void generateCollectionLeafResourceGetter(String className) {
        source.append(TMPL_GET_CHILD_RESOURCE.replace("CHILD", className).replace("IMPORT", className.toLowerCase(Locale.getDefault()))
                .replace("ELEMENT", className).replace(", name", ""));
    }

    @Override
    public void generateRestLeafGetter(String className) {
        generateCollectionLeafResourceGetter(className);
    }

    @Override
    public void done() {
        File classFile = new File(packageDir, className.toLowerCase(Locale.getDefault()) + ".py");
        BufferedWriter writer = null;
        try {
            try {
                if (!classFile.createNewFile()) {
                    throw new RuntimeException("Unable to create new file"); //i18n
                }
                classFile.deleteOnExit();
                writer = new BufferedWriter(new FileWriter(classFile));
                writer.append(source.toString());
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                }
            }
        }

    }
}
