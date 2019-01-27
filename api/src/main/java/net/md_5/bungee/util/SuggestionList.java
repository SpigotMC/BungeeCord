package net.md_5.bungee.util;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SuggestionList implements List<String> {
    private Suggestions suggestions;
    public SuggestionList(Suggestions suggestions) {
        Objects.requireNonNull(suggestions);
        this.suggestions = suggestions;
    }

    private Suggestions getSuggestions() {
        return this.suggestions;
    }

    @Override
    public int size() {
        return suggestions.getList().size();
    }

    @Override
    public boolean isEmpty() {
        return suggestions.getList().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Iterator<String> iterator() {
        return new SuggestionListIterator(0);
    }

    @Override
    public Object[] toArray() {
        String[] obj = new String[size()];
        for(int i = 0; i < size(); i++) {
            obj[i] = suggestions.getList().get(i).getText();
        }
        return obj;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        return (T[])toArray();
    }

    @Override
    public boolean add(String s) {
        Objects.requireNonNull(s);
        return suggestions.getList().add(new Suggestion(suggestions.getRange(), s));
    }

    @Override
    public boolean remove(Object o) {
        if(o instanceof Suggestion) {
            return suggestions.getList().remove(o);
        } else if(o instanceof String) {
            for (Suggestion suggestion : suggestions.getList()) {
                if(suggestion.getText().equals(o)) {
                    return suggestions.getList().remove(suggestion);
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> coll) {
        Objects.requireNonNull(coll);
        for (Suggestion suggestion : suggestions.getList()) {
            for(Object c : coll) {
                if(!contains(c)) return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        Objects.requireNonNull(c);
        for(String string : c) {
            if(!add(string)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends String> c) {
        Objects.requireNonNull(c);
        return addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        for(Object o : c) {
            if(!remove(o)) return false;
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        for(Object o : c) {
            if(!contains(o)) remove(o);
        }
        return true;
    }

    @Override
    public boolean removeIf(Predicate<? super String> filter) {
        Objects.requireNonNull(filter);
        for (Suggestion suggestion : suggestions.getList()) {
            if(filter.test(suggestion.getText()))
                suggestions.getList().remove(suggestion);
        }
        return false;
    }

    @Override
    public void sort(Comparator<? super String> c) {
        // Do nothing. There is no need to sort the Suggestion list. The client will sort the list for his own.
    }

    @Override
    public void clear() {
        suggestions.getList().clear();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof SuggestionList) {
            return suggestions.equals(((SuggestionList) o).getSuggestions());
        } else if(o instanceof Suggestions) {
            return suggestions.equals(o);
        }
        return false;
    }

    @Override
    public String get(int index) {
        return suggestions.getList().get(index).getText();
    }

    @Override
    public String set(int index, String element) {
        Objects.requireNonNull(element);
        Suggestion suggestion = suggestions.getList().get(index);
        suggestions.getList().set(index, new Suggestion(suggestion.getRange(), element));
        return element;
    }

    @Override
    public void add(int index, String element) {
        add(element);
    }

    @Override
    public String remove(int index) {
        Suggestion suggestion = suggestions.getList().get(index);
        suggestions.getList().remove(suggestion);
        return suggestion.getText();
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            return -1;
        }
        for(int i = 0; i < size(); i++) {
            if (o.equals(suggestions.getList().get(i)))
                return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null)
            return -1;
        for(int i = size()-1; i >= 0; i--) {
            if (o.equals(suggestions.getList().get(i)))
                return i;
        }
        return -1;
    }

    @Override
    public ListIterator<String> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<String> listIterator(int index) {
        return new SuggestionListIterator(index);
    }

    @Override
    public List<String> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");
        List<String> list = new ArrayList<>(size());
        for(Suggestion suggestion : suggestions.getList())
            list.add(suggestion.getText());
        return list;
    }

    @Override
    public void forEach(Consumer<? super String> action) {
        Objects.requireNonNull(action);
        for (Suggestion suggestion : suggestions.getList()) {
            action.accept(suggestion.getText());
        }
    }

    @Override
    public Spliterator<String> spliterator() {
        ArrayList<String> list = new ArrayList<>();
        for (Suggestion suggestion : suggestions.getList())
            list.add(suggestion.getText());
        return list.spliterator();
    }

    @Override
    public Stream<String> stream() {
        return suggestions.getList().stream().map(Suggestion::getText);
    }

    @Override
    public Stream<String> parallelStream() {
        return suggestions.getList().parallelStream().map(Suggestion::getText);
    }

    final class SuggestionListIterator implements ListIterator<String> {
        int currIndex;

        SuggestionListIterator(int index) {
            this.currIndex = index;
        }

        @Override
        public boolean hasNext() {
            return size() > currIndex;
        }

        @Override
        public String next() {
            currIndex++;
            return get(currIndex);
        }

        @Override
        public boolean hasPrevious() {
            return currIndex < 0;
        }

        @Override
        public String previous() {
            currIndex--;
            return get(currIndex);
        }

        @Override
        public int nextIndex() {
            return currIndex + 1;
        }

        @Override
        public int previousIndex() {
            return currIndex - 1;
        }

        @Override
        public void remove() {
            suggestions.getList().remove(currIndex);
        }

        @Override
        public void forEachRemaining(Consumer<? super String> action) {

        }

        @Override
        public void set(String s) {
            SuggestionList.this.set(currIndex, s);
        }

        @Override
        public void add(String s) {
            SuggestionList.this.add(currIndex, s);
        }
    }
}
