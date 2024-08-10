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

package org.glassfish.admin.amx.util.jmx;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import javax.management.ObjectName;

import org.glassfish.admin.amx.util.EnumerationIterator;

public class ObjectNameQueryImpl implements ObjectNameQuery
{
    public ObjectNameQueryImpl()
    {
    }

    /**
    Return true if one (or more) of the properties match the regular expressions
    for both name and value.   Return false if no property/value combinations match.

    A null pattern matches anything.
     */
    boolean match(Hashtable properties, Pattern propertyPattern, Pattern valuePattern)
    {
        final Iterator keys = new EnumerationIterator(properties.keys());
        boolean matches = false;

        while (keys.hasNext())
        {
            final String key = (String) keys.next();

            if (propertyPattern == null || propertyPattern.matcher(key).matches())
            {
                if (valuePattern == null)
                {
                    matches = true;
                    break;
                }

                // see if value matches
                final String value = (String) properties.get(key);

                if (valuePattern.matcher(value).matches())
                {
                    matches = true;
                    break;
                }
            }
        }

        return (matches);
    }

    /**
    Match all property/value expressions against the ObjectName.

    Return true if for each property/value regular expression pair there is at least one
    property within the ObjectName whose property name and value match their respective
    patterns.

    A null regex indicates "match anything".
     */
    boolean matchAll(ObjectName name,
                     Pattern[] propertyPatterns,
                     Pattern[] valuePatterns)
    {
        boolean matches = true;

        final Hashtable properties = name.getKeyPropertyList();

        for (int i = 0; i < propertyPatterns.length; ++i)
        {
            if (!match(properties, propertyPatterns[i], valuePatterns[i]))
            {
                matches = false;
                break;
            }
        }

        return (matches);
    }

    /**
    Match all property/value expressions against the ObjectName.

    Return true if there is at least one property/value regular expression pair that
    matches a property/value pair within the ObjectName.

    A null regex indicates "match anything".
     */
    boolean matchAny(ObjectName name,
                     Pattern[] propertyPatterns,
                     Pattern[] valuePatterns)
    {
        boolean matches = false;

        final Hashtable properties = name.getKeyPropertyList();

        for (int i = 0; i < propertyPatterns.length; ++i)
        {
            if (match(properties, propertyPatterns[i], valuePatterns[i]))
            {
                matches = true;
                break;
            }
        }

        return (matches);
    }

    Pattern[] createPatterns(final String[] patternStrings, int numItems)
    {
        final Pattern[] patterns = new Pattern[numItems];

        if (patternStrings == null)
        {
            for (int i = 0; i < numItems; ++i)
            {
                patterns[i] = null;
            }

            return (patterns);
        }


        for (int i = 0; i < numItems; ++i)
        {
            // consider null to match anything

            if (patternStrings[i] == null)
            {
                patterns[i] = null;
            }
            else
            {
                patterns[i] = Pattern.compile(patternStrings[i]);
            }
        }

        return (patterns);
    }

    private interface Matcher
    {
        boolean match(ObjectName name, Pattern[] names, Pattern[] values);

    }

    private class MatchAnyMatcher implements Matcher
    {
        public MatchAnyMatcher()
        {
        }

        public boolean match(ObjectName name, Pattern[] names, Pattern[] values)
        {
            return (matchAny(name, names, values));
        }

    }

    private class MatchAllMatcher implements Matcher
    {
        public MatchAllMatcher()
        {
        }

        public boolean match(ObjectName name, Pattern[] names, Pattern[] values)
        {
            return (matchAll(name, names, values));
        }

    }

    Set<ObjectName> matchEither(
            Matcher matcher,
            Set<ObjectName> startingSet,
            String[] regexNames,
            String[] regexValues)
    {
        if (regexNames == null && regexValues == null)
        {
            // both null => matches entire original set
            return (startingSet);
        }

        final Set<ObjectName> results = new HashSet<ObjectName>();

        int numMatches = 0;
        if (regexNames != null)
        {
            numMatches = regexNames.length;
        }
        else
        {
            numMatches = regexValues.length;
        }

        final Pattern[] namePatterns = createPatterns(regexNames, numMatches);
        final Pattern[] valuePatterns = createPatterns(regexValues, numMatches);

        for (final ObjectName name : startingSet)
        {
            if (matcher.match(name, namePatterns, valuePatterns))
            {
                results.add(name);
            }
        }

        return (results);
    }

    public Set<ObjectName> matchAll(Set<ObjectName> startingSet, String[] regexNames, String[] regexValues)
    {
        return (matchEither(new MatchAllMatcher(), startingSet, regexNames, regexValues));
    }

    public Set<ObjectName> matchAny(Set<ObjectName> startingSet, String[] regexNames, String[] regexValues)
    {
        return (matchEither(new MatchAnyMatcher(), startingSet, regexNames, regexValues));
    }

}






