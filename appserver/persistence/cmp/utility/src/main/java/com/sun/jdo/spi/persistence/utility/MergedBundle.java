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
 * MappingContext.java
 *
 * Created on January 28, 2002, 6:30 PM
 */

package com.sun.jdo.spi.persistence.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/** Special resource bundle which delegates to two others.
 * Ideally could just set the parent on the first, but this is protected,
 * so it might not work.  It's still unclear whether that approach would work
 * in this subclass because it may break the localization fall through
 * mechanism if used.
 * Note: This code is copied from NbBundle in the openide sources with
 * the following modifications:
 * - reformatting
 * - making variables final
 * - renaming variables and some params
 * - removing locale code
 * - creating the merged set of keys using jdk classes and not nb utils
 *
 * @author Rochelle Raccah
 * @version %I%
 */
public class MergedBundle extends ResourceBundle
{
    private final ResourceBundle _mainBundle, _parentBundle;

    public MergedBundle (ResourceBundle mainBundle,
        ResourceBundle parentBundle)
    {
        _mainBundle = mainBundle;
        _parentBundle = parentBundle;
    }

    public Enumeration getKeys () { return mergeKeys(); }

    private Enumeration mergeKeys ()
    {
        Set noDuplicatesMerge =
            new HashSet(getCollection(_mainBundle.getKeys()));

        noDuplicatesMerge.addAll(getCollection(_parentBundle.getKeys()));

        return Collections.enumeration(noDuplicatesMerge);
    }

    private Collection getCollection (Enumeration enumeration)
    {
        List returnList = new ArrayList();

        if (enumeration != null)
        {
            while (enumeration.hasMoreElements())
                returnList.add(enumeration.nextElement());
        }

        return returnList;
    }

    protected Object handleGetObject (String key)
        throws MissingResourceException
    {
        try
        {
            return _mainBundle.getObject(key);
        }
        catch (MissingResourceException mre)    // try the other bundle
        {
            return _parentBundle.getObject(key);
        }
    }
}
