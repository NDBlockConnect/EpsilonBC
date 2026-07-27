package com.github.epsilon.events.impl;

public class MoveEvent {

    private double x;
    private double y;
    private double z;
    private int horizontalPriority = Integer.MIN_VALUE;
    private int verticalPriority = Integer.MIN_VALUE;

    public MoveEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public boolean setHorizontal(double x, double z, int priority) {
        if (priority <= horizontalPriority) return false;
        this.x = x;
        this.z = z;
        horizontalPriority = priority;
        return true;
    }

    public void finalizeHorizontal(double x, double z) {
        this.x = x;
        this.z = z;
    }

    public boolean setVertical(double y, int priority) {
        if (priority <= verticalPriority) return false;
        this.y = y;
        verticalPriority = priority;
        return true;
    }

}
