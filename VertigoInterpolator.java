/*
 * Copyright © 2019-2022 Woohyun Shin (sinusinu)
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

 /*
  * Changelog
  * ===
  * v1.1
  * - Changed REPEATMODE_*, isIncremental, isConcave to RepeatMode, Direction, Curvature enums.
  */

/**
 * Simple interpolator for general use.<br/>
 * Interpolates linear value of 0 to 1 with logarithmic concave or power convex interpolation.<br/>
 * <br/>
 * To preview interpolated value, type this into WolframAlpha:<br/>
 * <blockquote><code>plot y=((log2((a-1)x + 1)) / (log2(a))) and y=((a^x - 1)/(a-1)) where a=24 from x=0 to x=1</code></blockquote>
 * Upper graph will show concave interpolation, and below graph will show convex interpolation. Change <code>a</code> to modify strength.
 */
public class VertigoInterpolator {
    public enum RepeatMode {
        /** stop updating value when it reaches its final value. */
        NoRepeat,
        /** reset value to its initial state when it reaches its final value. */
        Repeat,
        /** make value go backwards when it reaches its final value. */
        PingPong
    };

    public enum Direction {
        /** value go from 0 to 1 on update. */
        Incremental,
        /** value go from 1 to 0 on update. */
        Decremental
    };

    public enum Curvature {
        /** value is interpolated with concave function. */
        Concave,
        /** value is interpolated with convex function. */
        Convex
    }

    private Direction direction;
    private Curvature curvature;
    private float interval;
    private float valueRaw;
    private float valueInterpolated;
    private int strength;
    private RepeatMode repeatMode;

    /**
     * Initialize VertigoInterpolator with default strength of 24 and repeatMode of NoRepeat.
     * @param direction Set whether value should be increased (from 0 to 1) or decreased (from 1 to 0) upon update.
     * @param curvature Set whether the interpolator should use concave function or convex function for interpolation.
     * @param interval The interval in seconds.
     */
    public VertigoInterpolator(Direction direction, Curvature curvature, float interval) {
        this(direction, curvature, interval, 24, RepeatMode.NoRepeat);
    }

    /**
     * Initialize VertigoInterpolator with specified strength and default repeatMode of NoRepeat.
     * @param direction Set whether value should be increased (from 0 to 1) or decreased (from 1 to 0) upon update.
     * @param curvature Set whether the interpolator should use concave function or convex function for interpolation.
     * @param interval The interval in seconds.
     * @param strength The strength. Must be bigger than 1.
     */
    public VertigoInterpolator(Direction direction, Curvature curvature, float interval, int strength) {
        this(direction, curvature, interval, strength, RepeatMode.NoRepeat);
    }

    /**
     * Initialize VertigoInterpolator with specified strength and repeatMode.
     * @param direction Set whether value should be increased (from 0 to 1) or decreased (from 1 to 0) upon update.
     * @param curvature Set whether the interpolator should use concave function or convex function for interpolation.
     * @param interval The interval in seconds.
     * @param strength The strength. Must be bigger than 1.
     * @param repeatMode One of RepeatMode.
     */
    public VertigoInterpolator(Direction direction, Curvature curvature, float interval, int strength, RepeatMode repeatMode) {
        if (interval <= 0) throw new IllegalArgumentException("interval must be positive");
        if (strength <= 1) throw new IllegalArgumentException("strength must be bigger than 1");

        this.direction = direction;
        this.curvature = curvature;
        this.interval = interval;
        this.strength = strength;
        this.repeatMode = repeatMode;

        reset();
    }

    /**
     * Sets the both raw and interpolated values into initial state.
     */
    public void reset() {
        if (direction == Direction.Incremental) {
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
        if (repeatMode == RepeatMode.NoRepeat) {
            if ((direction == Direction.Incremental && valueRaw == 1f) || (direction == Direction.Decremental && valueRaw == 0f)) return;
        }
        if (direction == Direction.Incremental) {
            valueRaw += delta / interval;
            if (valueRaw > 1f) {
                switch (repeatMode) {
                    case NoRepeat:
                        valueRaw = 1f;
                        break;
                    case Repeat:
                        valueRaw -= 1f;
                        break;
                    case PingPong:
                        valueRaw = 1f - (valueRaw - 1f);
                        direction = Direction.Decremental;
                        break;
                }
            }
        } else {
            valueRaw -= delta / interval;
            if (valueRaw < 0f) {
                switch (repeatMode) {
                    case NoRepeat:
                        valueRaw = 0f;
                        break;
                    case Repeat:
                        valueRaw += 1f;
                        break;
                    case PingPong:
                        valueRaw = -valueRaw;
                        direction = Direction.Incremental;
                        break;
                }
            }
        }
        valueInterpolated = interpolateValue(valueRaw, curvature);
    }

    /**
     * Set raw value manually and calculate interpolated value.
     * @param newRawValue new raw value
     */
    public void setValueRaw(float newRawValue) {
        if (newRawValue < 0f || newRawValue > 1f) throw new IllegalArgumentException("Key value must be between 0 to 1");
        valueRaw = newRawValue;
        valueInterpolated = interpolateValue(valueRaw, curvature);
    }

    private float interpolateValue(float rawValue, Curvature curvature) {
        if (curvature == Curvature.Concave) return (log2(((strength - 1) * rawValue) + 1) / log2(strength));
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

    /** Get whether the value increases on update (go from 0 to 1) or the opposite. */
    public Direction getDirection() { return direction; }
    /** Get whether the value is interpolated with concave function or convex function. */
    public Curvature getCurvature() { return curvature; }
    /** Get the current repeat mode. */
    public RepeatMode getRepeatMode() { return repeatMode; }

    // below functions are likely to screw something up - use at your own risk
    /** Set interval. */
    public void setInterval(float newInterval) { interval = newInterval; }
    /** Set direction. */
    public void setDirection(Direction newDirection) { direction = newDirection; }
    /** Set curvature. */
    public void setCurvature(Curvature newCurvature) { curvature = newCurvature; }
}
