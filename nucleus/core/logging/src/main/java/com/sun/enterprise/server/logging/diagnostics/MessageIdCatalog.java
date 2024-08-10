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

package com.sun.enterprise.server.logging.diagnostics;

import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Simple catalog class to locate Diagnostic Information based on
 * message id as the key.  resource bundle is located using the module name.
 *
 * @author Carla Mott
 */
public class MessageIdCatalog{

    /**
     * Get all the documented DiagnosticCauses for a given message id.
     * The results will be localized based on the current locale of
     * the AppServer's JVM.
     */
    public ArrayList getDiagnosticCausesForMessageId( String messageId, String moduleName ) {
        if (moduleName == null || messageId == null) {
            return null;
        }
        ResourceBundle rb = java.util.logging.Logger.getLogger(moduleName).getResourceBundle();
        String cause = null;
        ArrayList causes = null;
        if( rb != null ) {
            for( int i = 1; i < DiagConstants.MAX_CAUSES_AND_CHECKS; i++ ) {
                // The convention used to document diagnostic causes in
                // resource bundle is
                // <MsgId>.diag.cause.1= <Cause 1>
                // <MsgId>.diag.cause.2= <Cause 2> ....
                try {
                    cause = rb.getString( messageId +
                        DiagConstants.CAUSE_PREFIX + i );
                } catch( MissingResourceException e ) {
                    // We couldn't find any causes listed for the message
                    // id or we have found all. In either case we are
                    // covered here.
                    break;
                }
                if( cause == null ) { break; }
                if( causes == null ) {
                    causes = new ArrayList( );
                }
                causes.add( cause );
            }
        }
        return causes;
    }

    /**
     * Get all the documented DiagnosticChecks for a given message id.
     * The results will be localized based on the current locale of
     * the AppServer's JVM.
     */
    public ArrayList getDiagnosticChecksForMessageId( String messageId, String moduleName ) {

        if (moduleName == null || messageId == null) {
            return null;
        }
        ResourceBundle rb = java.util.logging.Logger.getLogger(moduleName).getResourceBundle();

        String check = null;
        ArrayList checks = null;
        if( rb != null ) {
            for( int i = 1; i < DiagConstants.MAX_CAUSES_AND_CHECKS; i++ ) {
                // The convention used to document diagnostic checks in
                // resource bundle is
                // <MsgId>.diag.check.1= <Check 1>
                // <MsgId>.diag.check.2= <Check 2> ....
                try {
                    check = rb.getString( messageId +
                        DiagConstants.CHECK_PREFIX + i );
                } catch( MissingResourceException e ) {
                    // We couldn't find any checks listed for the message
                    // id or we have found all. In either case we are
                    // covered here.
                    break;
                }
                if( check == null ) {
                    break;
                }
                if( checks == null ) {
                    checks = new ArrayList( );
                }
                checks.add( check );
            }
        }
        return checks;
    }

    /**
     * We may collect lot of diagnostic causes and diagnostic checks for
     * some common message id from the field. We may document those
     * even after the product is shipped. We are planning to generate the
     * HTML's from the resource bundle's diagnostics and update the javadoc
     * or knowledgebase site. This URI should help us to locate the latest
     * and greatest diagnostic info based on the message id.
     */
    /*
     need to get the module id from the logger name.  The first part of the name maps.
     public String getDiagnosticURIForMessageId( String messageId, String moduleName ) {
         ResourceBundle rb = java.util.logging.Logger.getLogger(moduleName).getResourceBundle();
         if( moduleId == null ) { return null; }
         return DiagConstants.URI_PREFIX + moduleId + "/" + messageId;
     }

     public Diagnostics getDiagnosticsForMessageId( String messageId ) {
         ArrayList causes = getDiagnosticCausesForMessageId( messageId );
         ArrayList checks = getDiagnosticChecksForMessageId( messageId );
         if( ( causes == null )
           &&( checks == null ) ) {
             return null;
         }
         Diagnostics diagnostics = new Diagnostics( messageId );
         diagnostics.setPossibleCauses( causes );
         diagnostics.setDiagnosticChecks( checks );
         diagnostics.setURI( getDiagnosticURIForMessageId( messageId ) );
         return diagnostics;
     }
     */
}

