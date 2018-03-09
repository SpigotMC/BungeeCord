package io.github.waterfallmc.waterfall.utils;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A set that uses a <a href=>binary search</a> to find objects in a <a href=https://en.wikipedia.org/wiki/Sorted_array>sorted array</a>.
 * Avoids the memory cost of {@link java.util.HashSet}, while maintaining reasonable {@link Set#contains}
 * <b>Insertions are O(N)!</b>
 */
public class LowMemorySet<T extends Comparable<T>> extends AbstractSet<T> implements Set<T> {
    private final List<T> backing;

    private LowMemorySet(List<T> list) {
        this.backing = checkNotNull(list, "Null list");
        this.sort(); // We have to sort any initial elements
    }

    public static <T extends Comparable<T>> LowMemorySet<T> create() {
        return new LowMemorySet<>(new ArrayList<T>());
    }

    public static <T extends Comparable<T>> LowMemorySet<T> copyOf(Collection<T> c) {
        return new LowMemorySet<>(new ArrayList<>(c));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private int indexOf(Object o) {
        return Collections.binarySearch((List) backing, o);
    }

    private void sort() {
        backing.sort(null);
    }

    @Override
    public int size() {
        return backing.size();
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> backing = this.backing.iterator();
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return backing.hasNext();
            }

            @Override
            public T next() {
                return backing.next();
            }

            @Override
            public void remove() {
                backing.remove();
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                backing.forEachRemaining(action);
            }
        };
    }

    @Override
    public Object[] toArray() {
        return backing.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return backing.toArray(a);
    }

    @Override
    public boolean add(T t) {
        if (contains(t)) return false;
        backing.add(t);
        this.sort();
        return true;
    }

    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        if (index < 0) return false;
        T old = backing.remove(index);
        assert old == o;
        return old != null;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return backing.removeIf(c::contains);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return backing.removeIf((o) -> !c.contains(o));
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if (containsAll(c)) return false;
        backing.addAll(c);
        this.sort();
        return true;
    }

    @Override
    public void clear() {
        backing.clear();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        backing.forEach(action);
    }

    @Override
    public Stream<T> stream() {
        return backing.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return backing.parallelStream();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return backing.removeIf(filter);
    }
}
