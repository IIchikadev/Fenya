# Motion Blur

Adapted from the user-supplied MotionBlur.java (Telegram Desktop). The supplied
file depends on another client's classes and an absent motion_blur shader. Socket
uses the same camera reprojection inputs with its own fullscreen shader instead
of that client's PostEffectProcessor and resource hooks.

Settings: strength 0–3 (default 1), samples 4–128 (default 32), depth, centered,
third-person and monitor refresh compensation. Camera history resets when the
module is disabled, the world changes, a menu opens or the camera teleports more
than four blocks. Both depth and color are sampled before the hand pass, and the
blur is completed before the vanilla hand is drawn. Iris draws its hand earlier;
its compressed near depth is excluded both at the output pixel and along the
sample path, preventing hand distortion and smearing into the world. The old
temporal color blend has been removed.

Runtime smoke verification on RX 580 2048SP: at least 20 rendered blur passes,
rotation with depth on/off and centered sampling, no OpenGL error at the test
checkpoint, Glass GUI, and rendering with MakeUp enabled and disabled. The test
entrypoint is excluded from the installed release. Screenshots are saved in the
instance as motionblur-moving-qa.png, motionblur-world-qa.png and
motionblur-glass-qa.png. Performance and every setting combination are not
benchmarked.
