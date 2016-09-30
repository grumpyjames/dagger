package net.digihippo;

interface Task<T> {
    T execute();

    String description();
}
