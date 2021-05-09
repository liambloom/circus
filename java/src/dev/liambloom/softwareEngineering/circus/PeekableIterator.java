package dev.liambloom.softwareEngineering.circus;

import java.util.Iterator;
import java.util.NoSuchElementException;

public interface PeekableIterator<T> extends Iterator<T> {
    T peek();

    static <T> PeekableIterator<T> from(Iterator<T> inner) {
        return new PeekableIterator<>() {
            private T next;

            {
                if (inner.hasNext())
                    next = inner.next();
            }

            @Override
            public T peek() {
                return next;
            }

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                T c = next;
                next = inner.hasNext() ? inner.next() : null;
                return c;
            }
        };
    }
}
