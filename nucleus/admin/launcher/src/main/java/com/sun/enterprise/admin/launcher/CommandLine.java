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

package com.sun.enterprise.admin.launcher;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Universal command line for launching the GlassFish reliably in all scenarios.
 */
public final class CommandLine implements Iterable<String> {

    private final List<String> command = new ArrayList<>();
    private final CommandFormat format;

    /**
     * @param format BAT files use specific format compared to command line.
     */
    public CommandLine(CommandFormat format) {
        this.format = Objects.requireNonNull(format, "Unspecified format");
    }

    /**
     * @return {@link CommandFormat}
     */
    public CommandFormat getFormat() {
        return format;
    }

    /**
     * Appends new item without any formatting.
     *
     * @param item
     */
    public void append(String item) {
        command.add(item);
    }


    /**
     * Appends classpath argument (whole <code>-cp [paths]</code>)
     *
     * @param paths
     */
    public void appendClassPath(File... paths) {
        String value = Stream.of(paths).map(File::toPath).map(CommandLine::toJavaPath)
            .collect(Collectors.joining(File.pathSeparator));
        command.add("-cp");
        command.add(toQuotedIfNeeded(value));
    }


    /**
     * Appends java system option {@value GFLauncherConstants#JAVA_NATIVE_SYSPROP_NAME}.
     * The value will be wrapped into quotation marks.
     *
     * @param paths
     */
    public void appendNativeLibraryPath(File... paths) {
        String value = Stream.of(paths).map(File::toPath).map(CommandLine::toJavaPath)
            .collect(Collectors.joining(File.pathSeparator));
        command.add("-D" + GFLauncherConstants.JAVA_NATIVE_SYSPROP_NAME + '=' + toQuotedIfNeeded(value));
    }


    /**
     * Appends path, normalized, absolute and quoted if needed.
     *
     * @param path
     */
    public void append(Path path) {
        command.add(toQuotedIfNeeded(toJavaPath(path)));
    }


    /**
     * Appends java option without any changes.
     *
     * @param item
     */
    public void appendJavaOption(String item) {
        command.add(item);
    }


    /**
     * Appends java option. The path will be wrapped into quotation marks.
     *
     * @param itemKey
     * @param path
     */
    public void appendJavaOption(String itemKey, Path path) {
        command.add(itemKey + '=' + toQuotedIfNeeded(toJavaPath(path)));
    }


    /**
     * Appends java system option. The value will be wrapped into quotation marks.
     *
     * @param itemKey
     * @param itemValue
     */
    public void appendSystemOption(String itemKey, String itemValue) {
        command.add("-D" + itemKey + '=' + toQuotedIfNeeded(itemValue));
    }


    @Override
    public Iterator<String> iterator() {
        return command.iterator();
    }


    /**
     * @return {@link ListIterator}
     */
    public ListIterator<String> listIterator() {
        return command.listIterator();
    }


    /**
     * Generates the final command using an empty space as a separator.
     */
    @Override
    public String toString() {
        return command.stream().collect(Collectors.joining(" "));
    }


    /**
     * Generates the final command using given separator.
     *
     * @param separator
     * @return the command as a string
     */
    public String toString(String separator) {
        return command.stream().collect(Collectors.joining(separator));
    }


    /**
     * Converts the object to an unmodifiable list of elements.
     *
     * @return list of strings
     */
    public List<String> toList() {
        return List.copyOf(command);
    }


    private String toQuotedIfNeeded(String value) {
        // BAT files have issues when then string doesn't contain a space
        // and we would use quotation marks.
        if (format == CommandFormat.BatFile && value.indexOf(' ') >= 0) {
            return toQuoted(value);
        }
        // ProcessBuilder resolves it on its own.
        return value;
    }


    private static String toQuoted(String value) {
        return "\"" + value  + "\"";
    }


    private static String toJavaPath(Path path) {
        // Even windows can work with /, however it has some rules.
        // ProcessBuilder escapes what is needed automatically - but that is not a case of
        // a bat file content.
        // Paths with backslash can be used without quotation marks - not in bat,
        // because space is a separator.
        // Strings with backspaces quoted are taken as strings -> backslash means escaping.
        return path.normalize().toAbsolutePath().toString().replace('\\', '/');
    }

    static enum CommandFormat {
        /**
         * Execution using Java ProcessBuilder.
         * The ProcessBuilder Automatically escapes spaces and special characters.
         */
        ProcessBuilder,
        /**
         * To be written into a bat file which will be executed.
         * We need to ensure the proper formatting for the Windows BAT file.
         */
        BatFile,;
    }
}
