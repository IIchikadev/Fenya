# Optimization

The existing Misc / Optimization module now has four profiles. The default is
Balanced. The FPS limit remains configurable for every profile (260 is Minecraft's
unlimited setting); legacy saved limits are retained.

| Profile | View distance | Simulation | Entity distance | Particles | Effects |
| --- | --- | --- | --- | --- | --- |
| Maximum FPS | 6 | 5 | 50% | Minimal | Iris temporarily disabled; Socket postprocessing and procedural sky skipped |
| Balanced | 10 | 6 | 75% | Decreased | Iris preserved; Motion Blur capped at 16 samples |
| Quality | 14 | 8 | 100% | All | Effects preserved |
| Manual | User setting | User setting | User setting | User setting | Effects preserved |

Effect savings and shader suspension can be disabled independently. Skipping an
effect preserves its enabled state and settings. Shader suspension changes the
Iris state for the session without changing the selected pack. Iris reload requires
a temporary config write; the user's exact file is restored immediately afterward.

The module captures all eleven affected Minecraft options when enabled and
restores them on disable. Switching profiles applies a complete target state, so
Maximum FPS restrictions do not leak into Quality or Manual. In multiplayer,
simulation distance on the server remains controlled by that server.

Changes are checked once per second and options are written only when different;
render distance uses the engine callback. Terrain rebuilds are not forced every
tick. No FPS improvement percentage is claimed without a controlled benchmark.

Runtime QA: tools/optimization-smoke.init.gradle checks all four profiles, shader
suspension/restoration, Motion Blur suppression/sample cap, all eleven restored
options, and an OpenGL error checkpoint. Test entrypoints must be excluded from
the release JAR.
