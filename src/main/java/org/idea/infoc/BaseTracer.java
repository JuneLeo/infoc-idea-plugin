package org.idea.infoc;

public abstract class BaseTracer {
    public BaseTracer() {
        throw new RuntimeException("you need extends infoc parent class , but now is extends" + getClass().getCanonicalName());
    }
}
