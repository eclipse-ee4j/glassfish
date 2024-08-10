/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;

/**
 * This class performs the file related validations such as
 * <ul>
 * existence of the file read, write & execute permissions, whether the file is a directory or a file
 * </ul>
 * <b>NOT THREAD SAFE</b>
 */
public class FileValidator extends Validator {
    /**
     * The valid constraint set.
     */
    public static final String validConstraints = "drwx";

    /**
     * i18n strings manager object
     */
    private static final StringManager strMgr = StringManager.getManager(FileValidator.class);

    /**
     * The current constraint set.
     */
    private String constraints = "r";

    /**
     * Constructs a new FileValidator object.
     *
     * @param name The name of the entity that will be validated. This name is used in the error message.
     * @param constraints The constaint set that will be checked for any given file during validation.
     */
    public FileValidator(String name, String constraints) {
        super(name, java.lang.String.class);

        if (isValidConstraints(constraints)) {
            this.constraints = constraints;
        }
    }

    /**
     * @return Returns the current constraint set.
     */
    public String getConstraints() {
        return constraints;
    }

    /**
     * Sets the current constraint set to the given set if it is a valid constriant set.
     */
    public String setConstraints(String constraints) {
        if (isValidConstraints(constraints)) {
            this.constraints = constraints;
        }
        return this.constraints;
    }

    /**
     * Validates the given File.
     *
     * @param str Must be the absolute path of the File that will be validated.
     * @throws InvalidConfigException
     */
    public void validate(Object str) throws InvalidConfigException {
        super.validate(str);
        new StringValidator(getName()).validate(str);
        File f = new File((String) str);
        validateConstraints(f);
    }

    /**
     * Validates the given File against the current constraint set.
     */
    void validateConstraints(File file) throws InvalidConfigException {
        final File f = FileUtils.safeGetCanonicalFile(file);
        final String constriants = getConstraints();
        char[] ca = constriants.toCharArray();
        for (int i = 0; i < ca.length; i++) {
            switch (ca[i]) {
            case 'r':
                if (!f.canRead()) {
                    throw new InvalidConfigException(strMgr.getString("fileValidator.no_read", f.getAbsolutePath()));
                }
                break;
            case 'w':
                if (!f.canWrite()) {
                    throw new InvalidConfigException(strMgr.getString("fileValidator.no_write", f.getAbsolutePath()));
                }
                break;
            case 'd':
                if (!f.isDirectory()) {
                    throw new InvalidConfigException(strMgr.getString("fileValidator.not_a_dir", f.getAbsolutePath()));
                }
                break;
            case 'x':
                //do what
                break;
            default:
                break;
            }
        }
    }

    /**
     * Checks if the given constraint set is a subset of valid constraint set.
     *
     * @param constraints
     * @return Returns true if the given constraint set is subset or equal to the valid constraint set - "drwx".
     */
    boolean isValidConstraints(String constraints) {
        if (constraints == null) {
            return false;
        }
        final int length = constraints.length();
        if ((length == 0) || (length > 4)) {
            return false;
        }
        boolean isValid = true;
        for (int i = 0; i < length; i++) {
            char ch = constraints.charAt(i);
            switch (ch) {
            case 'r':
            case 'w':
            case 'x':
            case 'd':
                continue;
            default:
                isValid = false;
                break;
            }
        }
        return isValid;
    }
}
