# Menu opening and cursor effects

Socket ClickGUI now reproduces two Astra Visual menu effects:
- a 250 ms sine-out opening flash expanding from the screen centre;
- a halftone grid with 6 GUI-unit spacing, dot radii 0.7–3 and a 100-unit cursor reach.

The effects render behind panels with a single full-screen shader pass.
The theme accent supplies the opening glow. Menu reopening resets the flash;
window resize preserves the current animation. Existing menu sounds and portrait remain.

Reference behaviour inspected in Astra Menu.renderOverlay and HalftoneDotsPipeline.
Implementation adapted to Minecraft 1.21.4; Astra's 1.21.11 rendering pipeline is not used.

Runtime check: tools/menu-effects-smoke.init.gradle opens, renders and reopens
ClickGUI, checks shader loading and OpenGL errors, and captures screenshots.
Run the QA build only in an isolated instance. The production JAR has no QA entrypoint.
