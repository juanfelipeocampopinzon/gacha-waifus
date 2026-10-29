package com.gachawaifus.registry;

import java.util.function.Supplier;

public class RegistrySupplier<T> implements Supplier<T> {
    private final T value;

    public RegistrySupplier(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }
}
