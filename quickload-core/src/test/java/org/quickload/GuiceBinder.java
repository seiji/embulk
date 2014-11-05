package org.quickload;

import java.util.List;
import java.util.ArrayList;
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;

public class GuiceBinder
        implements TestRule
{
    private final List<Module> baseModules;
    private List<Module> extraModules;
    private Injector injector;

    public GuiceBinder(Module... baseModules)
    {
        this.baseModules = ImmutableList.copyOf(baseModules);
        reset();
    }

    private void reset()
    {
        extraModules = new ArrayList<Module>();
        injector = null;
    }

    public synchronized void addModule(Module module)
    {
        if (injector != null) {
            throw new IllegalStateException("Injector is already initialized. Call addModule before getInjector or getInstance");
        }
        extraModules.add(module);
    }

    public synchronized Injector getInjector()
    {
        if (injector == null) {
            ImmutableList.Builder modules = ImmutableList.builder();
            modules.addAll(baseModules);
            modules.addAll(extraModules);
            injector = Guice.createInjector(modules.build());
        }
        return injector;
    }

    public <T> T getInstance(Class<T> klass)
    {
        return getInjector().getInstance(klass);
    }

    @Override
    public final Statement apply(Statement base, Description description)
    {
        return new GuceBinderWatcher().apply(base, description);
    }

    private class GuceBinderWatcher
            extends TestWatcher
    {
        @Override
        protected void starting(Description description)
        {
            reset();
        }
    }
}
