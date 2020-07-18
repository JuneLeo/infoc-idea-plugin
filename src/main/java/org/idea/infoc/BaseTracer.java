package org.idea.infoc;

public abstract class BaseTracer {
    public BaseTracer(String name) {
        throw new RuntimeException("you need extends infoc parent class , but now is extends" + getClass().getCanonicalName());
    }
    /**
     * 设置
     *
     * @param key
     * @param value
     */
    public void set(String key, long value) {

    }

    /**
     * 设置
     *
     * @param key
     * @param value
     */
    public void set(String key, int value) {

    }

    /**
     * 设置
     *
     * @param key
     * @param value
     */
    public void set(String key, short value) {
    }

    /**
     * 设置
     *
     * @param key
     * @param value
     */
    public void set(String key, byte value) {


    }

    /**
     * 设置
     * @param key
     * @param b
     */
    public void set(String key, boolean b) {

    }

    /**
     * 设置
     *
     * @param key
     * @param value
     */
    public void set(String key, String value) {


    }

}
