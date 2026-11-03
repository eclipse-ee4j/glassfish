/*
 * Copyright (c) 2019, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.webui.jsf.util;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParserFactory;

import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON utilities.
 */
public final class JsonUtilities {

    /**
     * Cannot be instanciated.
     */
    private JsonUtilities() {
    }

    /**
     * JSON builder factory.
     */
    public static final JsonBuilderFactory JSON_BUILDER_FACTORY
            = Json.createBuilderFactory(null);

    /**
     * JSON generator factory.
     */
    public static final JsonWriterFactory JSON_WRITER_FACTORY
            = createJsonWriterFactory();

    /**
     * JSON parser factory.
     */
    public static final JsonParserFactory JSON_PARSER_FACTORY =
            Json.createParserFactory(null);

    /**
     * Write a given JSON object.
     * @param json the object to write
     * @param writer the writer to use
     */
    public static void writeJsonObject(final JsonObject json,
            final Writer writer) {

        JsonWriter jsonWriter = JSON_WRITER_FACTORY.createWriter(writer);
        jsonWriter.writeObject(json);
    }

    /**
     * Parse the given string as a JSON object.
     * @param input the input string to parse
     * @return JsonObject
     */
    public static JsonObject parseJsonObject(final String input) {
        return JSON_PARSER_FACTORY.createParser(new StringReader(input))
                .getObject();
    }

    /**
     * Introspect the type fo the given value object and create a
     * {@code JsonValue}.
     * @param value value to convert
     * @return JsonValue
     */
    public static JsonValue jsonValueOf(final Object value) {
        if (value == null) {
            return JsonValue.NULL;
        }
        JsonValue jsonValue;
        if (value instanceof String) {
            jsonValue = Json.createValue((String) value);
        } else if (value instanceof Integer) {
            jsonValue = Json.createValue((Integer) value);
        } else if (value instanceof Double) {
            jsonValue = Json.createValue((Double) value);
        } else if (value instanceof Long) {
            jsonValue = Json.createValue((Long) value);
        } else {
            throw new IllegalStateException("Unsupported type: "
                    + value.getClass());
        }
        return jsonValue;
    }

    /**
     * Create a JSON writer factory with pretty printing enabled.
     *
     * @return JsonWriterFactory
     */
    private static JsonWriterFactory createJsonWriterFactory() {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        return Json.createWriterFactory(config);
    }
}
