package com.flipkart.perf.inmemorydata;

import java.util.ArrayList;
import java.util.List;

/**
 * Show the shared object details on ui.
 */

public class SharedDataInfo {
    public static enum SharedDataType {
        LIST, MAP, QUEUE, STACK, SET, COUNTER;
    }
    private String name;
    private boolean acrossAgents = false;
    private SharedDataType sharedDataType;
    private List<Class<?>> sharedDataValueType;

    private SharedDataInfo() {
        sharedDataValueType = new ArrayList<Class<?>>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAcrossAgents() {
        return acrossAgents;
    }

    public void setAcrossAgents(boolean acrossAgents) {
        this.acrossAgents = acrossAgents;
    }

    public SharedDataType getSharedDataType() {
        return sharedDataType;
    }

    public void setSharedDataType(SharedDataType sharedDataType) {
        this.sharedDataType = sharedDataType;
    }

    public List<Class<?>> getSharedDataValueType() {
        return sharedDataValueType;
    }

    public void setSharedDataValueType(List<Class<?>> sharedDataValueType) {
        this.sharedDataValueType = sharedDataValueType;
    }

    public static SharedDataInfo sharedList(String name, Class<?> collectionType) {
        SharedDataInfo sc = new SharedDataInfo();
        sc.name = name;
        sc.sharedDataType = SharedDataType.LIST;
        sc.sharedDataValueType.add(collectionType);
        return sc;
    }

    public static SharedDataInfo sharedSet(String name, Class<?> collectionType) {
        SharedDataInfo sc = new SharedDataInfo();
        sc.name = name;
        sc.sharedDataType = SharedDataType.SET;
        sc.sharedDataValueType.add(collectionType);
        return sc;
    }

    public static SharedDataInfo sharedMap(String name, Class<?> keyType, Class<?> valueType) {
        SharedDataInfo sc = new SharedDataInfo();
        sc.name = name;
        sc.sharedDataType = SharedDataType.MAP;
        sc.sharedDataValueType.add(keyType);
        sc.sharedDataValueType.add(valueType);
        return sc;
    }

    public static SharedDataInfo sharedStack(String name, Class<?> collectionType) {
        SharedDataInfo sc = new SharedDataInfo();
        sc.name = name;
        sc.sharedDataType = SharedDataType.STACK;
        sc.sharedDataValueType.add(collectionType);
        return sc;
    }

    public static SharedDataInfo sharedQueue(String name, Class<?> collectionType) {
        SharedDataInfo sc = new SharedDataInfo();
        sc.name = name;
        sc.sharedDataType = SharedDataType.QUEUE;
        sc.sharedDataValueType.add(collectionType);
        return sc;
    }

    public static SharedDataInfo sharedCounter(String name) {
        SharedDataInfo sc = new SharedDataInfo();
        sc.name = name;
        sc.sharedDataType = SharedDataType.COUNTER;
        return sc;
    }
}
