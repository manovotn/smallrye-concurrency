package io.smallrye.context.impl;

import io.smallrye.context.SmallRyeContextManager;
import io.smallrye.context.api.ManagedExecutorConfig;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Holds default values for {@code ManagedExecutor} and {@code ThreadContext}. It firstly looks into MP Config
 * for any user-specified defaults and if not defined, then it uses SmallRye defaults which propagate everything.
 *
 * @author Matej Novotny
 */
public class DefaultValues {

    private static Map<ClassLoader, DefaultValues> mapOfDefaults = new HashMap<>();

    // constants defined by spec for MP Config
    private static final String EXEC_ASYNC = "ManagedExecutor/maxAsync";
    private static final String EXEC_QUEUE = "ManagedExecutor/maxQueued";
    private static final String EXEC_PROPAGATED = "ManagedExecutor/propagated";
    private static final String EXEC_CLEARED = "ManagedExecutor/cleared";
    private static final String THREAD_CLEARED = "ThreadContext/cleared";
    private static final String THREAD_PROPAGATED = "ThreadContext/propagated";
    private static final String THREAD_UNCHANGED = "ThreadContext/unchanged";

    // actual defaults
    private String[] executorPropagated;
    private String[] executorCleared;
    private int executorAsync;
    private int executorQueue;
    private String[] threadPropagated;
    private String[] threadCleared;
    private String[] threadUnchanged;

    private DefaultValues() {
        // NOTE: we do not perform sanity check here, that's done in SmallRyeContextManager
        Config config = ConfigProvider.getConfig();
        this.executorAsync = config.getOptionalValue(EXEC_ASYNC, Integer.class)
                .orElse(ManagedExecutorConfig.Literal.DEFAULT_INSTANCE.maxAsync());
        this.executorQueue = config.getOptionalValue(EXEC_QUEUE, Integer.class)
                .orElse(ManagedExecutorConfig.Literal.DEFAULT_INSTANCE.maxQueued());
        // remaining values have to be done via try-catch block as a workaround for
        // https://github.com/smallrye/smallrye-config/issues/83
        // once fixed, rewrite this to getOptionalValue()
        this.executorPropagated = resolveConfiguration(config, EXEC_PROPAGATED, SmallRyeContextManager.ALL_REMAINING_ARRAY);
        this.executorCleared = resolveConfiguration(config, EXEC_CLEARED, SmallRyeContextManager.NO_STRING);
        this.threadCleared = resolveConfiguration(config, THREAD_CLEARED, SmallRyeContextManager.NO_STRING);
        this.threadPropagated = resolveConfiguration(config, THREAD_PROPAGATED, SmallRyeContextManager.ALL_REMAINING_ARRAY);
        this.threadUnchanged = resolveConfiguration(config, THREAD_UNCHANGED, SmallRyeContextManager.NO_STRING);
    }

    private static String[] resolveConfiguration(Config mpConfig, String key, String[] originalValue) {
        try {
            return mpConfig.getValue(key, String[].class);
        } catch (NoSuchElementException e) {
            return originalValue;
        }
    }

    public static DefaultValues getDefaults(ClassLoader cl) {
        DefaultValues defaultValues = mapOfDefaults.get(cl);
        if (defaultValues == null) {
            defaultValues = new DefaultValues();
            mapOfDefaults.put(cl, defaultValues);
        }
        return defaultValues;
    }

    public String[] getExecutorPropagated() {
        return executorPropagated;
    }

    public String[] getExecutorCleared() {
        return executorCleared;
    }

    public int getExecutorAsync() {
        return executorAsync;
    }

    public int getExecutorQueue() {
        return executorQueue;
    }

    public String[] getThreadPropagated() {
        return threadPropagated;
    }

    public String[] getThreadCleared() {
        return threadCleared;
    }

    public String[] getThreadUnchanged() {
        return threadUnchanged;
    }
}
