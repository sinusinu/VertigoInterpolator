# VertigoInterpolator
VertigoInterpolator is single-file Java library-thing that simplifies process of interpolating linear value of 0 to 1 to logarithmic concave or power convex interpolated *smooth-curved* value on time basis.<br/>
To preview interpolated values, type this into WolframAlpha:<br/>
```
plot y=((log2((a-1)x + 1)) / (log2(a))) and y=((a^x - 1)/(a-1)) where a=24 from x=0 to x=1
```
Upper graph will show concave interpolation, and below graph will show convex interpolation. Change *a* to modify strength.

## Basic usage

```java
// creates new VertigoInterpolator with incremental, concave, 3-second interval, strength of 36, no repeat.
VertigoInterpolator interpolator = new VertigoInterpolator(true, true, 3f, 36, VertigoInterpolator.REPEATMODE_NOREPEAT);

// in rendering loop or something:
interpolator.update(deltaTime);
float interpolatedValue = interpolator.getValueInterpolated();

// interpolatedValue will go from 0 to 1 (incremental) with fast acceleration in beginning (concave) in 3 seconds (interval).
// after 3 seconds, calling interpolator.update will do nothing since we set its repeat mode as REPEATMODE_NOREPEAT.
// if it's set to anything else, you can keep calling interpolator.update and get the result you want.
```

## License
VertigoInterpolator is distributed under the terms of the Do What The Fuck You Want To Public License, Version 2.