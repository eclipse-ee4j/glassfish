/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.nucleus.admin.progress;

import java.io.File;
import java.lang.InterruptedException;
import java.lang.Thread;

import static org.glassfish.tests.utils.NucleusTestUtils.*;
import  org.glassfish.tests.utils.NucleusTestUtils;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * This tests the functionality of JobManager, list-jobs
 * @author Bhakti Mehta
 */
@Test(testName="JobManagerTest", enabled=true)
public class JobManagerTest {
    private static File nucleusRoot  = NucleusTestUtils.getNucleusRoot();

    private final String COMMAND1 = "progress-simple";

    @BeforeTest
    public void setUp() throws Exception {
        nadmin("stop-domain");
        //delete jobs.xml incase there were other jobs run
        deleteJobsFile();
        //osgi-cache workaround
        File osgiCacheDir = new File(nucleusRoot, "domains"+File.separator+"domain1"+File.separator+"osgi-cache");
        deleteDirectoryContents(osgiCacheDir);
        nadmin("start-domain");


    }

    @AfterTest
    public void cleanUp() throws Exception {
        nadmin("stop-domain");
        nadmin("start-domain");

    }

    @Test(enabled=true)
    public void noJobsTest() {
        nadmin("stop-domain");
        //delete jobs.xml incase there were other jobs run
        deleteJobsFile();
        nadmin("start-domain");
        String result = null;
        result = nadminWithOutput("list-jobs").outAndErr;
        assertTrue(matchString("Nothing to list", result));


    }

    @Test(dependsOnMethods = { "noJobsTest" },enabled=true)
    public void runJobTest() {
        String result = null;

        NadminReturn result1 = nadminWithOutput("--terse", "progress-simple");
        assertTrue(result1.returnValue);
        //check list-jobs
        result = nadminWithOutput("list-jobs").out;
        assertTrue( result.contains(COMMAND1) && result.contains("COMPLETED"));
        //check list-jobs with id 1
        result = nadminWithOutput("list-jobs","1").out;
        assertTrue( result.contains(COMMAND1) && result.contains("COMPLETED"));
        //shutdown server
        assertTrue( nadmin("stop-domain"));
        //restart
        assertTrue( nadmin("start-domain"));
        //check jobs
        result = nadminWithOutput("list-jobs","1").out;
        assertTrue( result.contains(COMMAND1) && result.contains("COMPLETED"));
        nadmin("start-domain");

    }

    @Test(dependsOnMethods = { "runJobTest" }, enabled=true)
       public void runDetachTest() {
           String result = null;
           //shutdown server
           assertTrue( nadmin("stop-domain"));

           //delete the jobs file
           deleteJobsFile();

           //restart
           assertTrue( nadmin("start-domain"));
           result = nadminDetachWithOutput( COMMAND1).out;
           //Detached job id is returned
           assertTrue( result.contains("Job ID: "));

           //list-jobs
           result = nadminWithOutput("list-jobs","1").out;
           assertTrue( result.contains(COMMAND1) );
           //attach to the job
           assertTrue(nadmin("attach", "1"));

           //list-jobs   and it should be purged since the user
           //starting is the same as the user who attached to it
           result = nadminWithOutput("list-jobs").outAndErr;
           assertTrue(matchString("Nothing to list", result));

           //delete the jobs file
           deleteJobsFile();



       }

       @Test(dependsOnMethods = { "runDetachTest" }, enabled=false)
       public void runConfigureManagedJobsTest() throws InterruptedException {
           try {
               String result = null;
               //shutdown server
               assertTrue( nadmin("stop-domain"));

               //delete the jobs file
               deleteJobsFile();

               //restart
               assertTrue( nadmin("start-domain"));
               //configure-managed-jobs
               assertTrue( nadmin("configure-managed-jobs","--job-retention-period=6s","--cleanup-initial-delay=2s",
                       "--cleanup-poll-interval=2s"));
               assertTrue(COMMAND1, nadmin(COMMAND1));


               //list-jobs
               result = nadminWithOutput("list-jobs","1").out;
               assertTrue( result.contains(COMMAND1) );
               //shutdown server
               assertTrue( nadmin("stop-domain"));

               //start server
               assertTrue( nadmin("start-domain"));
               Thread.sleep(5000L);

               //list-jobs there should be none since the configure-managed-jobs command will purge it
               result = nadminWithOutput("list-jobs").outAndErr;
               assertTrue(matchString("Nothing to list", result));

           } finally {
                //reset configure-managed-jobs
                assertTrue( nadmin("configure-managed-jobs","--job-retention-period=24h","--cleanup-initial-delay=20m",
                   "--cleanup-poll-interval=20m"));
           }
           //delete the jobs file
           deleteJobsFile();



       }

    /**
     * This will delete the jobs.xml file
     */
    public static void deleteJobsFile() {
        File configDir = new File(nucleusRoot,"domains/domain1/config");
        File jobsFile = new File (configDir,"jobs.xml");
        System.out.println("Deleting.. " + jobsFile);
        if (jobsFile!= null && jobsFile.exists()) {
            jobsFile.delete();
        }
    }


}

