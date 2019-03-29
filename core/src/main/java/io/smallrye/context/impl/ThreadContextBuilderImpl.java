package io.smallrye.context.impl;

import org.eclipse.microprofile.context.ThreadContext;

import io.smallrye.context.SmallRyeContextManager;

public class ThreadContextBuilderImpl implements ThreadContext.Builder {

    private String[] propagated;
    private String[] unchanged;
    private String[] cleared;
    private SmallRyeContextManager manager;
    private String injectionPointName = null;

    public ThreadContextBuilderImpl(SmallRyeContextManager manager) {
        this.manager = manager;
        DefaultValues defaults = DefaultValues.getDefaults(Thread.currentThread().getContextClassLoader());
        this.propagated = defaults.getThreadPropagated();
        this.unchanged = defaults.getThreadUnchanged();
        this.cleared = defaults.getThreadCleared();
    }

    @Override
    public ThreadContext build() {
        return new ThreadContextImpl(manager, propagated, unchanged, cleared, injectionPointName);
    }

    @Override
    public ThreadContext.Builder propagated(String... types) {
        propagated = types;
        return this;
    }

    @Override
    public ThreadContext.Builder unchanged(String... types) {
        unchanged = types;
        return this;
    }

    @Override
    public ThreadContext.Builder cleared(String... types) {
        cleared = types;
        return this;
    }

    public ThreadContext.Builder injectionPointName(String name) {
        this.injectionPointName = name;
        return this;
    }

}
