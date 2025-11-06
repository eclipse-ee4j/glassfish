package org.glassfish.internal.api;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Lazy enumeration that returns only distinct elements while iterating through
 * multiple enumerations.
 */
class LazyEnumeration<T> implements Enumeration<T> {

    private final Iterator<Enumeration<T>> enumerationIterator;
    private final Set<T> seenElements = new HashSet<>();
    private Enumeration<T> currentEnumeration;
    private T nextElement;

    public LazyEnumeration(List<Enumeration<T>> enumerations) {
        this.enumerationIterator = enumerations.iterator();
        advance();
    }

    @Override
    public boolean hasMoreElements() {
        return nextElement != null;
    }

    @Override
    public T nextElement() {
        if (nextElement == null) {
            throw new NoSuchElementException();
        }
        T result = nextElement;
        advance();
        return result;
    }

    private void advance() {
        nextElement = null;

        while (nextElement == null) {
            // Find next unique element in current enumeration
            while (currentEnumeration != null && currentEnumeration.hasMoreElements()) {
                T candidate = currentEnumeration.nextElement();
                if (seenElements.add(candidate)) {
                    nextElement = candidate;
                    return;
                }
            }

            // Move to next enumeration if available
            if (enumerationIterator.hasNext()) {
                currentEnumeration = enumerationIterator.next();
            } else {
                break;
            }
        }
    }
}
