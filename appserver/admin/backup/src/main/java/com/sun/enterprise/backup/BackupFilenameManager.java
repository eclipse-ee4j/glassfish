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

/*
 * BackupFilenameManager.java
 *
 * Created on August 13, 2004, 12:10 PM
 */

package com.sun.enterprise.backup;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Manage filenames to select for backups.
 * @author bnevins
 */
class BackupFilenameManager {
    BackupFilenameManager(File backupDir, String domainName) throws BackupException {
        this.dir = backupDir;
        this.domainName = domainName;
        findZips();
    }

    ///////////////////////////////////////////////////////////////////////////

    File next() throws BackupException {
        int newVersionNum = 1;

        ZipFileAndNumber latest = (zipFiles.isEmpty())
            ? null : zipFiles.get(zipFiles.lastKey());

        if  (latest != null) {
            newVersionNum = latest.num + 1;
        }

        // Generate a file name of form domain1_2010_06_11_v0001.zip
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
        Date date = new Date();
        String fname = domainName + "_" + formatter.format(date) + "_v";
        String suffix = padWithLeadingZeroes(newVersionNum);

        desc = domainName + " backup created on "
            + formatter.format(date) + " by user "
            + System.getProperty(Constants.PROPS_USER_NAME);

        return new File(dir, fname + suffix + ".zip");
    }

    ///////////////////////////////////////////////////////////////////////////

    File latest() throws BackupWarningException {
        if (zipFiles.isEmpty()) {
            throw new BackupWarningException("backup-res.NoBackupFiles", dir);
        }
        return zipFiles.get(zipFiles.lastKey()).zip;
    }

    /**
     * Returns backup files to be recycled, which are the oldest backups
     * over the limit. If the limit is zero (or less), then no files are
     * returned for recycling.
     * @param limit
     * @return the oldest backup files over the specific limit.
     */
    List<File> getRecycleFiles(int limit) {
        List<File> files = new ArrayList<File>();

        if (limit > 0) {
            int recycleCount = (zipFiles.size() > limit)
                ? zipFiles.size() - limit : 0;
            Iterator<ZipFileAndNumber> it = zipFiles.values().iterator();
            for (int i = 0; i < recycleCount; i++) {
                files.add(it.next().zip);
            }
        }

        return files;
    }

    ///////////////////////////////////////////////////////////////////////////////

    /** Looks through the backups directory and assembles
     * a list of all backup files found.
     */
    private void findZips() {
        File[] zips = dir.listFiles(new ZipFilenameFilter());
        for(int i = 0; (zips != null) && (i < zips.length); i++) {
            ZipFileAndNumber zfan = new ZipFileAndNumber(zips[i]);
            zipFiles.put(zfan.num, zfan);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////

    /** Convert the array of zip filenames into an array of the number suffixes.
     */
    private String padWithLeadingZeroes(int num) throws BackupException {
        if (num < 10)
            return "0000" + num;

        if (num < 100)
            return "000" + num;

        if (num < 1000)
            return "00" + num;

        if (num < 10000)
            return "0" + num;

        if (num < 100000)
            return "" + num;

        throw new BackupException("Latest version >= 100,000.  Delete some backup files.");
    }

    /*
     * Return description string
     *
     */
    public String getCustomizedDescription() {
        return desc;
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ZipFileAndNumber {
        private ZipFileAndNumber(File zip) {
            this.zip = zip;
            String fname = zip.getName();

            if(isValid()) {
                fname = fname.substring(fname.lastIndexOf("_v")+2, fname.length() - 4);
                try {
                    num = Integer.parseInt(fname);
                } catch(Exception e) {
                    // nothing to do -- num is already set to -1
                }
            }
        }

        /**
         * make sure that:
         * (1) the filename is the right format
         * (2) that it is internally correct (has a status file with a timestamp
         **/

        private boolean isValid() {
            Status status = new Status();
            long time = status.getInternalTimestamp(zip);
            return time > 0;
        }

        private File zip;
        private int num = -1;
    }

    ///////////////////////////////////////////////////////////////////////////

    private final File dir;
    private final String domainName;
    private String desc;
    private final SortedMap<Integer, ZipFileAndNumber> zipFiles = new TreeMap<>();

    ///////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
        try {
            File f = new File(args[0]);
            BackupFilenameManager mgr = new BackupFilenameManager(f, "foo");
            File fnew = mgr.next();
            System.out.println("Next backup file: " + fnew);
            File fold = mgr.latest();
            System.out.println("Latest backup file: " + fold);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
