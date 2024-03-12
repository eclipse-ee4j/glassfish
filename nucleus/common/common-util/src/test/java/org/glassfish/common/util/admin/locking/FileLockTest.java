/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.common.util.admin.locking;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

import org.glassfish.common.util.admin.ManagedFile;
import org.glassfish.common.util.admin.ManagedFile.ManagedLock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for ManagedFile.writeLock and ManagedFile.readLock.
 */
public class FileLockTest {

    enum States {LOCKED, RELEASED}
    volatile States mainWriteState;
    volatile States[] writeStates = new States[5];
    volatile States[] readStates = new States[5];

    @Test
    public void writeLock() throws Exception {

        final Random random = new Random();
        File f = getFile();
        final ManagedFile managed = new ManagedFile(f, 1000, 1000);
        Lock fl = managed.accessWrite();
        mainWriteState = States.LOCKED;
        List<Future<Boolean>> results = new ArrayList<>();
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 3; i++) {
            final int number = i;
            results.add(executor.submit(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    try {
                        final Lock second = managed.accessWrite();
                        writeStates[number] = States.LOCKED;
                        assertWriteStates();
                        writeStates[number] = States.RELEASED;
                        second.unlock();
                        assertWriteStates();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return Boolean.TRUE;
                }
            }));
            results.add(executor.submit(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    try {
                        final Lock second = managed.accessRead();
                        readStates[number] = States.LOCKED;
                        assertWriteStates();
                        Thread.sleep(random.nextInt(300));
                        readStates[number] = States.RELEASED;
                        second.unlock();
                        assertWriteStates();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return Boolean.TRUE;
                }
            }));

        }
        Thread.sleep(100);
        mainWriteState = States.RELEASED;
        fl.unlock();
        for (Future<Boolean> result : results) {
            Boolean exitCode = result.get();
            assertTrue(exitCode.booleanValue());
        }
    }

    @Test
    public void mixedLock() throws Exception {

        final Random random = new Random();
        File f = getFile();
        final ManagedFile managed = new ManagedFile(f, 1000, 1000);
        Lock fl = managed.accessWrite();
        mainWriteState = States.LOCKED;
        List<Future<Boolean>> results = new ArrayList<>();
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 3; i++) {
            final int number = i;
            results.add(executor.submit(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    try {
                        final Lock second = managed.accessRead();
                        readStates[number] = States.LOCKED;
                        assertWriteStates();
                        Thread.sleep(random.nextInt(300));
                        readStates[number] = States.RELEASED;
                        second.unlock();
                        assertWriteStates();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return Boolean.TRUE;
                }
            }));

        }
        Thread.sleep(300);
        mainWriteState = States.RELEASED;
        fl.unlock();
        for (Future<Boolean> result : results) {
            Boolean exitCode = result.get();
            assertTrue(exitCode.booleanValue());
        }
    }


    public void assertWriteStates() {
        int writeLocked = 0;
        int readLocked = 0;
        if (mainWriteState==States.LOCKED) {
            writeLocked++;
        }
        for (int i = 0; i < 5; i++) {
            if (writeStates[i]==States.LOCKED) {
                writeLocked++;
            }
            if (readStates[i]==States.LOCKED) {
                readLocked++;
            }
        }
        System.out.println("Status M : " + mainWriteState + " W " + writeLocked + " R " + readLocked);

        // never more than 1 locked writer
        if (writeLocked>1) {
            throw new AssertionError("More than 1 thread in write state");
        }
    }

    @Test
    public void readLock() throws Exception {

        File f = getFile();
        final ManagedFile managed = new ManagedFile(f, 1000, 1000);
        Lock fl = managed.accessRead();

        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            results.add(Executors.newFixedThreadPool(2).submit(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    try {
                        Lock second = managed.accessRead();
                        second.unlock();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return Boolean.TRUE;
                }
            }));
        }
        Thread.sleep(100);
        fl.unlock();
        for (Future<Boolean> result : results) {
            Boolean exitCode = result.get();
            assertTrue(exitCode.booleanValue());
        }
    }

    @Test
    public void timeOutTest() throws Exception {
        final File f = getFile();
        final ManagedFile managed = new ManagedFile(f, 1000, 10000);
        Lock fl;
        try {
            fl = managed.accessWrite();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Obtained first lock, waiting about 500 for secondary lock to timeout...");
        try {
            Executors.newFixedThreadPool(2).submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    long now = System.currentTimeMillis();
                    ManagedFile m = new ManagedFile(f, 500, 1000);
                    try {
                        Lock lock = m.accessRead();
                        lock.unlock();
                        throw new RuntimeException("Test failed, got the lock that should have timed out");
                    } catch (TimeoutException e) {
                        System.out.println("Great, got timed out after " + (System.currentTimeMillis() - now));
                    }
                    return null;
                }
            }).get();
            // let's check we also cannot get the write lock...
            ManagedFile m = new ManagedFile(f, 100, 100);
            try {
                Lock lock = m.accessWrite();
                lock.unlock();
                throw new RuntimeException("Test failed, got the write lock that should have timed out");
            } catch (TimeoutException e) {
                System.out.println("Even better, got timed out trying to get another write lock");
            }
            fl.unlock();
        } catch (Exception e) {
            fl.unlock();
            throw e;
        }
    }

    @Test
    public void lockAndReadTest() throws Exception {
         File f = File.createTempFile("common-util-FileLockTest", "tmp");
         try {
             // Now let's try to write the file.
             try (FileWriter fw = new FileWriter(f)) {
                 fw.append("FileLockTest reading passed !");
             }

             final ManagedFile managed = new ManagedFile(f, 1000, 1000);
             Lock fl = managed.accessRead();
             try (FileReader fr = new FileReader(f)) {
                 char[] chars = new char[1024];
                 int length = fr.read(chars);
             }

             fl.unlock();
         } finally {
             f.delete();
         }
     }


     @Test
     @DisabledOnOs(OS.WINDOWS)
     public void lockForReadAndWriteTest_Unix() throws Exception {
         final File file = File.createTempFile("common-util-FileLockTest", "tmp");
         final Path filePath = file.toPath();
         try {
             // Now let's try to write the file.
             final String message = "lockForReadAndWriteTest passed!";
             Files.writeString(filePath, message);
             assertEquals(message, Files.readString(filePath));

             final ManagedFile managed = new ManagedFile(file, 1000, 1000);
             final Lock fl = managed.accessRead();

             final String message2 = "\nlockForReadAndWriteTest passed!";
             Files.writeString(filePath, message2, StandardOpenOption.APPEND);
             fl.unlock();

             assertThat(Files.readString(filePath), equalTo(message + message2));
         } finally {
            file.delete();
        }
    }


    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void lockForReadAndWriteTest_Windows() throws Exception {
        final File file = File.createTempFile("common-util-FileLockTest", "tmp");
        final Path filePath = file.toPath();
        try {
            // Now let's try to write the file.
            final String message = "lockForReadAndWriteTest passed!";
            Files.writeString(filePath, message);
            assertEquals(message, Files.readString(filePath));

            final ManagedFile managed = new ManagedFile(file, 1000, 1000);
            final Lock fl = managed.accessRead();

            final String message2 = "\nlockForReadAndWriteTest failed!";
            assertThrows(IOException.class, () -> Files.writeString(filePath, message2, StandardOpenOption.APPEND));
            fl.unlock();
            assertEquals(message, Files.readString(filePath));

            final String message3 = "\nlockForReadAndWriteTest passed!";
            Files.writeString(filePath, message3, StandardOpenOption.APPEND);
            assertThat(Files.readString(filePath), equalTo(message + message3));
        } finally {
            file.delete();
        }
    }


    @Test
    public void lockAndWriteTest() throws Exception {
        File file = File.createTempFile("common-util-FileLockTest", "tmp");
        try {
            final ManagedFile managed = new ManagedFile(file, 1000, 1000);
            final ManagedLock fileLock = managed.accessWrite();
            // Now let's try to write the file.
            final RandomAccessFile raf = fileLock.getLockedFile();
            final String message = "lockAndWriteTest Passed!";
            raf.writeUTF(message);
            fileLock.unlock();

            // Let's read it back; trim leading UTF-8 bytes.
            assertEquals(message, Files.readString(file.toPath()).trim());
        } finally {
            file.delete();
        }
    }


    @Test
    @DisabledOnOs(OS.MAC)
    public void lockAndRenameTest() throws Exception {
        File f = File.createTempFile("common-util-FileLockTest", "tmp");
        final ManagedFile managed = new ManagedFile(f, 1000, 1000);
        managed.accessWrite();
        File dest = new File("filelock");
        try {
            assertTrue(!f.renameTo(new File("filelock")), "File renaming blocked");
        } finally {
            dest.delete();
        }
    }

    private File getFile() throws URISyntaxException {
        URL url = getClass().getClassLoader().getResource("adminport.xml");
        return new File(url.toURI());
    }
}
