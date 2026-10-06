# MakeUp Ultra Fast

Socket Loader installs original, unmodified releases from Modrinth:

- MakeUp Ultra Fast 9.5g: https://modrinth.com/shader/makeup-ultra-fast-shaders (LGPL-3.0-or-later)
- Iris 1.8.8 for Minecraft 1.21.4: https://modrinth.com/mod/iris (LGPL-3.0-only)
- Sodium 0.6.13 for Minecraft 1.21.4: https://modrinth.com/mod/sodium (Polyform-Shield-1.0.0)

The default preset uses the author's Low profile, including shadows, volumetric
clouds, water reflections, ambient occlusion and bloom. Depth of field and motion
blur are disabled. The old Miniature selection is migrated to MakeUp; other
existing selections and shader settings are preserved during installation.

The **Shaders** module in Visual toggles MakeUp; its settings button
opens Iris. While an Iris shader pack is active, Socket skips its own Bloom,
Saturation and procedural sky so the shader owns those effects. Camera-based
Motion Blur runs as a separate pass and remains available with Iris enabled.
ChunkAnimator's vanilla terrain hook is disabled when Sodium is installed.

The shader ZIP is kept in `instance/shaderpacks`; Iris and Sodium remain separate
mods. Third-party source code is not merged into the Socket JAR.

Runtime verification uses `tools/miniature-smoke.init.gradle`; its test entrypoint
must not be included in the installed release build.

Verified on AMD Radeon RX 580 2048SP: local world renders with the Low profile,
Glass GUI renders, and toggling shaders off/on succeeds. Screenshots are saved as
`makeup-world-qa.png` and `makeup-glass-qa.png` in the instance screenshots.
Performance has not been benchmarked.
