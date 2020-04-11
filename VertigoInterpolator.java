/*
 * Copyright © 2019-2020 sinu <cpu344@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

/**
 * Simple interpolator for general use.<br/>
 * Interpolates linear value of 0 to 1 with logarithmic concave or power convex interpolation.<br/>
 * <br/>
 * To preview interpolated value, type this into WolframAlpha:<br/>
 * <blockquote><code>plot y=((log2((a-1)x + 1)) / (log2(a))) and y=((a^x - 1)/(a-1)) where a=24 from x=0 to x=1</code></blockquote>
 * Upper graph will show concave interpolation, and below graph will show convex interpolation. Change <code>a</code> to modify strength.
 * @author sinu
 */
public class VertigoInterpolator {
    /** stop updating value when it reaches its final value. */
    public static final int REPEATMODE_NOREPEAT = 0;
    /** reset value to its initial state when it reaches its final value. */
    public static final int REPEATMODE_REPEAT = 1;
    /** make value go back to its origin when it reaches its final value. */
    public static final int REPEATMODE_PINGPONG = 2;

    private boolean isIncremental;
    private boolean isConcave;
    private float interval;
    private float valueRaw;
    private float valueInterpolated;
    private int strength;
    private int repeatMode;

    /**
     * Initialize VertigoInterpolator with default strength of 24 and repeatMode of {@link #REPEATMODE_NOREPEAT}.
     * @param isIncremental Set to true if value should be increased upon update (from 0 to 1). If false, value will be decreased (from 1 to 0).
     * @param isConcave Set to true if the interpolator should use concave function for interpolation. If false, interpolator will use convex function.
     * @param interval The interval in seconds.
     */
    public VertigoInterpolator(boolean isIncremental, boolean isConcave, float interval) {
        this(isIncremental, isConcave, interval, 24, REPEATMODE_NOREPEAT);
    }

    /**
     * Initialize VertigoInterpolator with specified strength and default repeatMode of {@link #REPEATMODE_NOREPEAT}.
     * @param isIncremental Set to true if value should be increased upon update (from 0 to 1). If false, value will be decreased (from 1 to 0).
     * @param isConcave Set to true if the interpolator should use concave function for interpolation. If false, interpolator will use convex function.
     * @param interval The interval in seconds.
     * @param strength The strength. Must be bigger than 1.
     */
    public VertigoInterpolator(boolean isIncremental, boolean isConcave, float interval, int strength) {
        this(isIncremental, isConcave, interval, strength, REPEATMODE_NOREPEAT);
    }

    /**
     * Initialize VertigoInterpolator with specified strength and repeatMode.
     * @param isIncremental Set to true if value should be increased upon update (from 0 to 1). If false, value will be decreased (from 1 to 0).
     * @param isConcave Set to true if the interpolator should use concave function for interpolation. If false, interpolator will use convex function.
     * @param interval The interval in seconds.
     * @param strength The strength. Must be bigger than 1.
     * @param repeatMode {@link #REPEATMODE_NOREPEAT}, {@link #REPEATMODE_REPEAT}, or {@link #REPEATMODE_PINGPONG}.
     */
    public VertigoInterpolator(boolean isIncremental, boolean isConcave, float interval, int strength, int repeatMode) {
        if (interval <= 0) throw new IllegalArgumentException("interval must be positive");
        if (strength <= 1) throw new IllegalArgumentException("strength must be bigger than 1");
        if (repeatMode < 0 || repeatMode > 2) throw new IllegalArgumentException("repeatMode must be one of the REPEATMODE values");

        this.isIncremental = isIncremental;
        this.interval = interval;
        this.isConcave = isConcave;
        this.strength = strength;
        this.repeatMode = repeatMode;

        reset();
    }

    /**
     * Sets the both raw and interpolated values into initial state.
     */
    public void reset() {
        if (isIncremental) {
            valueRaw = 0f;
            valueInterpolated = 0f;
        } else {
            valueRaw = 1f;
            valueInterpolated = 1f;
        }
    }

    /**
     * Update the key value and calculate interpolated value.
     * @param delta delta-time to progress (in seconds)
     */
    public void update(float delta) {
        if (repeatMode == REPEATMODE_NOREPEAT) {
            if ((isIncremental && valueRaw == 1f) || (!isIncremental && valueRaw == 0f)) return;
        }
        if (isIncremental) {
            valueRaw += delta / interval;
            if (valueRaw > 1f) {
                switch (repeatMode) {
                    case REPEATMODE_NOREPEAT:
                        valueRaw = 1f;
                        break;
                    case REPEATMODE_REPEAT:
                        valueRaw -= 1f;
                        break;
                    case REPEATMODE_PINGPONG:
                        valueRaw = 1f - (valueRaw - 1f);
                        isIncremental = false;
                        break;
                }
            }
        } else {
            valueRaw -= delta / interval;
            if (valueRaw < 0f) {
                switch (repeatMode) {
                    case REPEATMODE_NOREPEAT:
                        valueRaw = 0f;
                        break;
                    case REPEATMODE_REPEAT:
                        valueRaw += 1f;
                        break;
                    case REPEATMODE_PINGPONG:
                        valueRaw = -valueRaw;
                        isIncremental = true;
                        break;
                }
            }
        }
        valueInterpolated = interpolateValue(valueRaw, isConcave);
    }

    /**
     * Set raw value manually and calculate interpolated value.
     * @param newRawValue new raw value
     */
    public void setValueRaw(float newRawValue) {
        if (newRawValue < 0f || newRawValue > 1f) throw new IllegalArgumentException("Key value must be between 0 to 1");
        valueRaw = newRawValue;
        valueInterpolated = interpolateValue(valueRaw, isConcave);
    }

    private float interpolateValue(float rawValue, boolean concave) {
        if (concave) return (log2(((strength - 1) * rawValue) + 1) / log2(strength));
        else return (float)(((Math.pow(strength, rawValue)) - 1) / (strength - 1));
    }

    private float log2 (float value) {
        return (float)(Math.log(value) / Math.log(2));
    }

    /** Get the strength of the interpolation. */
    public int getStrength() { return strength; }
    /** Get the interval of the interpolation. */
    public float getInterval() { return interval; }
    /** Get the raw value. */
    public float getValueRaw() { return valueRaw; }
    /** Get the interpolated value. */
    public float getValueInterpolated() { return valueInterpolated; }

    /** Get if the value increases on update. If true, value will go from 0 to 1. */
    public boolean isIncremental() { return isIncremental; }
    /** Get if the value is interpolated with concave function. If false, the value will be interpolated with convex function. */
    public boolean isConcave() { return isConcave; }
    /** Get the current repeat mode. */
    public int getRepeatMode() { return repeatMode; }

    // below functions are likely to screw something up - use at your own risk
    /** Set interval. */
    public void setInterval(float newInterval) { interval = newInterval; }
    /** Set isIncremental. */
    public void setIncremental(boolean newIncremental) { isIncremental = newIncremental; }
    /** Set isConcave. */
    public void setConcave(boolean newConcave) { isConcave = newConcave; }
}
