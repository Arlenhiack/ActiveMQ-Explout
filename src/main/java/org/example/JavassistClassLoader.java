package org.example;

public class JavassistClassLoader extends ClassLoader {
    public JavassistClassLoader() {
        super(Thread.currentThread().getContextClassLoader());
    }
}
