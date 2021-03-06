/*
 * Copyright (c) 2002, 2020 Oracle and/or its affiliates. All rights reserved.
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

package samples.lifecycle.simple;
import jakarta.jms.*;
import javax.naming.*;
import java.sql.*;

import com.sun.appserv.server.LifecycleListener;
import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.appserv.server.LifecycleEventContext;


/**
 *  LifecycleTopic is an implementation for the LifecycleListener interface.
 *  <p>
 *  Sun ONE Application Server emits five events during its lifecycle -
 *  1. INIT_EVENT: Server is initializing subsystems and setting up the runtime environment.
 *  2. STARTUP_EVENT: Server is starting up applications
 *  3. READY_EVENT: Server started up applications and is ready to service requests
 *  4. SHUTDOWN_EVENT: Server is shutting down applications
 *  5. TERMINATION_EVENT: Server is terminating the subsystems and the runtime environment.
 *
 *  In this sample, on STARTUP_EVENT, a thread is started which sends a simple JMS message to
 *  sampleTopic every minute. On SHUTDOWN_EVENT, this thread is stopped.
 *  </p>
 */

public class LifecycleListenerImpl implements LifecycleListener {

    /**
     *  Life cycle event context
     */
    LifecycleEventContext ctx;

    /**
     *  Receives a server lifecycle event
     *  @param event associated event
     *  @throws <code>ServerLifecycleException</code> for exceptional condition.
     */
    public void handleEvent(LifecycleEvent event)
                         throws ServerLifecycleException {

        ctx = event.getLifecycleEventContext();

        switch(event.getEventType()) {
            case LifecycleEvent.INIT_EVENT:
                onInitTask();
                  break;

            case LifecycleEvent.STARTUP_EVENT:
                onStartTask();
                  break;

            case LifecycleEvent.READY_EVENT:
                onReadyTask();
                break;

            case LifecycleEvent.SHUTDOWN_EVENT:
                onShutdownTask();
                  break;

            case LifecycleEvent.TERMINATION_EVENT:
                onTerminationTask();
                  break;
        }

    }

    /**
     *  Task to be carried out in the INIT_EVENT.
     *  Logs a message.
     */
    private void onInitTask() {
        ctx.log("LifecycleTopic: INIT_EVENT");
    }

    /**
     *  Tasks to be carried out in the STARTUP_EVENT.
     *  Logs a message
     */
    private void onStartTask() {
        ctx.log("LifecycleTopic: STARTUP_EVENT");
        // my code
        QueueSession qsession[] = new QueueSession[10];
        Queue queue[] = new Queue[10];

            try{
                for (int i =0; i < 10; i++) {
                    // Get initial context
                    ctx.log("Get initial context");
                    InitialContext initialContext = new InitialContext();

                    // look up the connection factory from the object store
                    ctx.log("Looking up the queue connection factory from JNDI");
                    QueueConnectionFactory factory = (QueueConnectionFactory) initialContext.lookup("jms/QCFactory");

                    // look up queue from the object store
                    ctx.log("Create queue connection");
                    QueueConnection qconn = factory.createQueueConnection();

                    ctx.log("Create queue session");
                    qsession[i] = qconn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

                    ctx.log("Looking up the queue from JNDI");
                    queue[i] = (Queue) initialContext.lookup("jms/SampleQueue");
                }

                updateDB();

            }
            catch( Exception e ){
                ctx.log( "Exception caught in test code" );
                e.printStackTrace();
            }



        // end my code

        // my code
        //createAccount();
        // end my code
    }

    /**
     *  Tasks to be carried out in the READY_EVENT.
     *  Logs a message.
     */
    private void onReadyTask() {
        ctx.log("LifecycleTopic: READY_EVENT");
    }

    /**
     *  Tasks to be carried out in the SHUTDOWN_EVENT.
     *  Logs a message
     */
    private void onShutdownTask() {
        ctx.log("LifecycleTopic: SHUTDOWN_EVENT");
    }

    private void updateDB() {
        try {
            //Class.forName("com.inet.ora.OraDriver");
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            ////String url = "jdbc:inetora::wrx.india.sun.com:1521:dbsmpl1";
            String url = "jdbc:derby://localhost:1527/testdb;create=true;";
            java.sql.Connection con = DriverManager.getConnection(url,"dbuser", "dbpassword");
            String qry = "update lifecycle_test1 set status=1" ;
            con.createStatement().executeUpdate(qry);
            con.close();
        } catch(Exception e) {
           System.out.println("Error:" + e.getMessage());
        }
    }


    /**
     *  Tasks to be carried out in the TERMINATION_EVENT.
     *  Log a message.
     */
    private void onTerminationTask() {
        ctx.log("LifecycleTopic: TERMINATION_EVENT");
    }

}
