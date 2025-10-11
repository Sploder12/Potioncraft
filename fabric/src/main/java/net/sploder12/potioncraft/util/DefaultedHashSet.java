package net.sploder12.potioncraft.util;

import java.util.HashSet;
import java.util.function.Consumer;

public class DefaultedHashSet<T> extends HashSet<T> {
    protected T defaultElement;

    public DefaultedHashSet(T defaultElement) {
        this.defaultElement = defaultElement;
    }

    @Override
    public boolean add(T element) {
        if (element.equals(defaultElement)) {
            return false;
        }

        return super.add(element);
    }

    @Override
    public boolean remove(Object element) {
        return super.remove(element);
    }

    @Override
    public boolean contains(Object element) {
        return super.contains(element) || element.equals(defaultElement);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        super.forEach(action);
        action.accept(defaultElement);
    }

    public boolean setDefault(T newDefault, boolean keep) {
        if (newDefault.equals(defaultElement)) {
            return false;
        }

        remove(newDefault);

        if (keep) {
            add(defaultElement);
        }

        this.defaultElement = newDefault;
        return true;
    }

    public boolean setDefault(T newDefault) {
        return setDefault(newDefault, true);
    }

    public T getDefaultElement() {
        return defaultElement;
    }
}
