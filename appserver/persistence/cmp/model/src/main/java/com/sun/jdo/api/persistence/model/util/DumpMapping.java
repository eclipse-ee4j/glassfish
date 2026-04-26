/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.jdo.api.persistence.model.util;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.api.persistence.model.jdo.ConcurrencyGroupElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceClassElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;
import com.sun.jdo.api.persistence.model.jdo.RelationshipElement;
import com.sun.jdo.api.persistence.model.mapping.MappingClassElement;
import com.sun.jdo.api.persistence.model.mapping.MappingFieldElement;
import com.sun.jdo.api.persistence.model.mapping.MappingReferenceKeyElement;
import com.sun.jdo.api.persistence.model.mapping.MappingRelationshipElement;
import com.sun.jdo.api.persistence.model.mapping.MappingTableElement;
import com.sun.jdo.api.persistence.model.mapping.impl.MappingClassElementImpl;
import com.sun.jdo.api.persistence.model.mapping.impl.MappingTableElementImpl;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.netbeans.modules.dbschema.ColumnElement;
import org.netbeans.modules.dbschema.ColumnPairElement;

public class DumpMapping {

    private static Model model;

    static {
        // initialize the model reference
        setModel(Model.RUNTIME);
    }

    /** Print out the cache of MappingClassElements to the specified PrintStream.
     * @param stream PrintStream used to dump the info
     */
    public static void dumpMappingCache(PrintStream stream) {
        stream.println("Mapping cache (class names -> MappingClassElements)");
        for (Iterator<Entry<String, MappingClassElement>> i = model.getMappingCache().entrySet().iterator(); i.hasNext();) {
            Entry<String, MappingClassElement> entry = i.next();
            String className = entry.getKey();
            MappingClassElement mce = entry.getValue();
            String mceRepr = mce.getClass() + "@" + Integer.toHexString(System.identityHashCode(mce));
            stream.println("\t" + className + " ->\t" + mceRepr);
        }
    }

    /** Print out the cache of classLoaders to the specified PrintStream.
     * @param stream PrintStream used to dump the info
     */
    public static void dumpClassLoaderCache(PrintStream stream) {
        stream.println("ClassLoader cache (class names -> ClassLoaders)");
        for (Entry<String, ClassLoader> entry : model.getClassLoaderCache().entrySet()) {
            String className = entry.getKey();
            ClassLoader classLoader = entry.getValue();
            stream.println("\t" + className + " ->\t" + classLoader);
        }
    }

    public static void main(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            String className = args[i];
            println(0, "\nClass " + className + ":");

            try
            {
                MappingClassElementImpl mce = (MappingClassElementImpl)model.getMappingClass(className);
                if (mce != null)
                {
                    printPersistenceClassElement(mce.getPersistenceElement());
                    printMappingClassElement(mce);
                }
                else
                {
                    println(0, "Cannot find mapping info for class " + className + " (getMappingClass returns null)");
                }
            }
            catch (Exception e)
            {
                println(0, "Problems during accessing mapping info for class " + className);
                e.printStackTrace();
            }
        }
    }

    /** Sets the internal model reference used by the DumpMapping methods
     * to the specified Model instance.
     * @param newModel the Model instance to be used by DumpMapping
     */
    public static void setModel(Model newModel)
    {
        model = newModel;
    }

    // ----- JDO model ------

    public static void printPersistenceClassElement(PersistenceClassElement pce)
    {
        println(0, "\n--> PersistenceClassElement ");
        println(1, "package  = " + pce.getPackage());
        println(1, "name     = " + pce.getName());
        println(1, "identity = " + getObjectIdentityTypeRepr(pce.getObjectIdentityType()));
        println(1, "keyClass = " + pce.getKeyClass());

        printPersistenceFieldElements(1, pce.getFields());
        printConcurrencyGroupElements(1, pce.getConcurrencyGroups());

        println(0, "<-- PersistenceClassElement\n ");
    }


    public static void printPersistenceFieldElements(int tabs, PersistenceFieldElement[] fields)
    {
        if ((fields != null) && (fields.length > 0))
        {
            println(tabs, "--> fields ");
            for (int i = 0; i < fields.length; i++)
            {
                PersistenceFieldElement pfe = fields[i];

                println(tabs, "[" + i + "] " + pfe.getClass());
                println(tabs+1, "name             = " + pfe.getName());
                println(tabs+1, "declaringClass   = " + pfe.getDeclaringClass());
                println(tabs+1, "fieldNumber      = " + pfe.getFieldNumber());
                println(tabs+1, "persistenceType  = " + getPersistenceTypeRepr(pfe.getPersistenceType()));
                println(tabs+1, "read / write     = " + pfe.isReadSensitive() + " / " + pfe.isWriteSensitive());
                println(tabs+1, "isKey            = " + pfe.isKey());

                if (pfe instanceof RelationshipElement)
                {
                    RelationshipElement re = (RelationshipElement) pfe;

                    println(tabs+1, "bounds          = " + re.getLowerBound() + " / " +  re.getUpperBound());
                    println(tabs+1, "deleteAction    = " + re.getDeleteAction());
                    println(tabs+1, "updateAction    = " + re.getUpdateAction());
                    println(tabs+1, "collectionClass = " + re.getCollectionClass());
                    println(tabs+1, "elementClass     = " + re.getElementClass());
                    println(tabs+1, "isPrefetch      = " + re.isPrefetch());
                }
                printConcurrencyGroupElements(tabs+1, pfe.getConcurrencyGroups());
            }
            println(tabs, "<-- fields ");
        }
    }

    public static void printConcurrencyGroupElements(int tabs, ConcurrencyGroupElement[] groups)
    {
        if ((groups != null) && (groups.length > 0))
        {
            println(tabs, "--> concurrency groups");
            for (int i = 0; i < groups.length; i++)
            {
                ConcurrencyGroupElement cg = groups[i];
                println(tabs, "[" + i + "] " + cg.getClass());
                println(tabs+1, "name           = " + cg.getName());
                println(tabs+1, "declaringClass = " + cg.getDeclaringClass());
            }
            println(tabs, "<-- concurrency groups");
        }
    }

    // ----- Mapping model ------

    public static void printMappingClassElement(MappingClassElement mce)
    {
        println(0, "\n--> MappingClassElement");

        println(1, "databaseRoot = " + mce.getDatabaseRoot());
        printMappingTableElements(1, mce.getTables());
        printMappingFieldElements(1, mce.getFields());

        println(0, "<-- MappingClassElement");
    }

    public static void printMappingTableElements(int tabs, List<MappingTableElement> tables)
    {
        final int count = ((tables != null) ? tables.size() : 0);

        if (count > 0)
        {
            println(tabs, "--> tables ");
            for (int i = 0; i < count; i++)
            {
                MappingTableElementImpl mte = (MappingTableElementImpl) tables.get(i);

                println(tabs, "[" + i + "] " + mte.getClass());

                println(tabs+1, "table           = " + mte.getTable());
                println(tabs+1, "tableObject     = " + mte.getTableObject());
                println(tabs+1, "key             = " + mte.getKey());
                println(tabs+1, "keyObjects      = " + mte.getKeyObjects());
                printMappingRefKeyElements(tabs+1, mte.getReferencingKeys());
            }
            println(tabs, "<-- tables ");
        }
     }


     public static void printMappingRefKeyElements(int tabs, List<MappingReferenceKeyElement> refKeys) {
         final int count = ((refKeys != null) ? refKeys.size() : 0);

         if (count > 0) {
             println(tabs, "--> tables ");
             for (int i = 0; i < count; i++) {
                 MappingReferenceKeyElement mrke = refKeys.get(i);

                 println(tabs, "[" + i + "] " + mrke.getClass());

                 println(tabs + 1, "table           = " + mrke.getDeclaringTable());
                 println(tabs + 1, "pairs           = " + mrke.getColumnPairNames());
             }
             println(tabs, "<-- tables ");
         }
     }

     public static void printMappingFieldElements(int tabs, List<MappingFieldElement> fields) {
         final int count = fields == null ? 0 : fields.size();
         if (count == 0) {
             return;
         }
         println(tabs, "--> fields ");
         for (int i = 0; i < count; i++) {
             MappingFieldElement mfe = fields.get(i);

             println(tabs, "[" + i + "] " + mfe.getClass());
             println(tabs + 1, "name            = " + mfe.getName());
             println(tabs + 1, "fetchGroup      = " + mfe.getFetchGroup());
             println(tabs + 1, "columns         = " + mfe.getColumns());

             if (!(mfe instanceof MappingRelationshipElement)) {
                 println(tabs + 1, "columnObjects     = " + mfe.getColumnObjects());
             } else {
                 MappingRelationshipElement mre = (MappingRelationshipElement) mfe;
                 List<ColumnPairElement> columnObjects = mre.getColumnObjects();
                 int colCount = columnObjects == null ? 0 : columnObjects.size();
                 if (colCount > 0) {
                     println(tabs + 1, "--> columnsObjects ");
                     for (int j = 0; j < colCount; j++) {
                         ColumnPairElement fce = columnObjects.get(j);
                         ColumnElement rce = fce == null ? null : fce.getReferencedColumn();
                         println(tabs + 1, "[" + j + "] " + fce + " -> " + rce);
                     }
                     println(tabs + 1, "<-- columnsObjects ");
                 }

                 println(tabs + 1, "associatedColumns = " + mre.getAssociatedColumns());

                 List<ColumnPairElement> associatedColumnObjects = mre.getAssociatedColumnObjects();
                 colCount = associatedColumnObjects == null ? 0 : associatedColumnObjects.size();
                 if (colCount > 0) {
                     println(tabs + 1, "--> associatedColumnObjects ");
                     for (int j = 0; j < colCount; j++) {
                         ColumnPairElement fce = associatedColumnObjects.get(j);
                         ColumnElement rce = (fce != null) ? fce.getReferencedColumn() : null;
                         println(tabs + 1, "[" + j + "] " + fce + " -> " + rce);
                     }
                     println(tabs + 1, "<-- associatedColumnObjects ");
                 }
             }
         }
         println(tabs, "<-- fields ");
    }

   // ----- helper methods -----

    static String getObjectIdentityTypeRepr(int objectIdentityType)
    {
        switch (objectIdentityType)
        {
        case PersistenceClassElement.APPLICATION_IDENTITY:
            return "APPLICATION_IDENTITY";
        case PersistenceClassElement.DATABASE_IDENTITY:
            return "DATABASE_IDENTITY_IDENTITY";
        case PersistenceClassElement.UNMANAGED_IDENTITY:
            return "UNMANAGED_IDENTITY";
        default:
            return "UNKNOWN";
        }
    }

    static String getPersistenceTypeRepr(int persistenceType)
    {
        switch (persistenceType)
        {
        case PersistenceFieldElement.PERSISTENT:
            return "PERSISTENT";
        case PersistenceFieldElement.DERIVED:
            return "DERIVED";
        case PersistenceFieldElement.TRANSIENT:
            return "TRANSIENT";
        default:
            return "UNKNOWN";
        }
    }

    static void println(int indent, String text)
    {
        for (int i = 0; i < indent; i++)
        {
            System.out.print("\t");
        }

        System.out.println(text);
    }
}
