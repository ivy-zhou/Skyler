package com.ivyzhou.tutorial.skyler;

/**
 * Simple 3D vector class.  Handles basic vector math for 3D vectors.
 */
public final class Vector3 {
    public double x;
    public double y;
    public double z;

    public static final Vector3 ZERO = new Vector3(0, 0, 0);

    public Vector3() {
    }

    public Vector3(double xValue, double yValue, double zValue) {
        set(xValue, yValue, zValue);
    }

    public Vector3(Vector3 other) {
        set(other);
    }

    public final void add(Vector3 other) {
        x += other.x;
        y += other.y;
        z += other.z;
    }

    public final void add(double otherX, double otherY, double otherZ) {
        x += otherX;
        y += otherY;
        z += otherZ;
    }

    public final void subtract(Vector3 other) {
        x -= other.x;
        y -= other.y;
        z -= other.z;
    }

    public final void multiply(double magnitude) {
        x *= magnitude;
        y *= magnitude;
        z *= magnitude;
    }

    public final void multiply(Vector3 other) {
        x *= other.x;
        y *= other.y;
        z *= other.z;
    }

    public final void divide(double magnitude) {
        if (magnitude != 0.0f) {
            x /= magnitude;
            y /= magnitude;
            z /= magnitude;
        }
    }

    public final void set(Vector3 other) {
        x = other.x;
        y = other.y;
        z = other.z;
    }

    public final void set(double xValue, double yValue, double zValue) {
        x = xValue;
        y = yValue;
        z = zValue;
    }

    public final double dot(Vector3 other) {
        return (x * other.x) + (y * other.y) + (z * other.z);
    }

    public final double length() {
        return (float) Math.sqrt(length2());
    }

    public final double length2() {
        return (x * x) + (y * y) + (z * z);
    }

    public final double distance2(Vector3 other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    public final double normalize() {
        final double magnitude = length();

        // TODO: I'm choosing safety over speed here.
        if (magnitude != 0.0f) {
            x /= magnitude;
            y /= magnitude;
            z /= magnitude;
        }

        return magnitude;
    }

    public final void zero() {
        set(0.0f, 0.0f, 0.0f);
    }

}