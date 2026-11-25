# Rylune Utils 1.4.0
Client-only Fabric mod for Minecraft Java 1.21.11, Java 21. All five modules are disabled initially. Uses a custom dark in-game menu; no server installation or account authentication required by the mod itself.

## Controls
- Right Shift: open/close the menu (Escape backs out of settings or closes the menu).
- C: hold to zoom, or press to toggle when Toggle mode is enabled.
- R: latch sprint on/off. Vanilla movement, hunger and collision rules still apply.
- Rebind all three in Options > Controls > Key Binds > Rylune Utils. If another mod claims the same key, rebind one of them.

## Features
Monochrome black/charcoal theme, rounded panels, responsive sidebar or compact category tabs, animated switches and hover states, search, independent toggles, paginated settings, color hex input, adjustable values, previews, reset buttons and JSON persistence (config/rylune-utils.json). Zoom: 2–12x, hold/toggle, smoothing and speed. Sprint: optional status HUD (off by default), position, color and death reset. Crosshair: plus/dot/T/square, gap, size, thickness, outline, dot, RGB and opacity. Block overlay: outline RGB, width, opacity and outline opacity.

## 1.4.0 performance and customization
- Crosshair editor: draw or erase on a 15×15 grid, with a single selectable RGB color and live preview. Custom shape is saved in JSON.
- HSV color wheel and brightness control for crosshair and block outline. Existing RGB controls and hex field remain.
- Crosshair presets and drawings are rasterized into one cached 65×65 GPU texture; only regenerate on changes. Only the vanilla crosshair drawing call is intercepted, not every GUI sprite.
- Idle zoom bypasses smoothing. Sprint key handling is scoped to keyboard input. Sprint HUD exits before accessing the client when disabled.
- Removed the block fill rendering event and all fill geometry; colored outlines are preserved.
- No new background threads, network requests or shader blur. The new editors only render when open.
- Actual FPS improvement has not been benchmarked in Minecraft; compare the same world and settings.

## Lite rendering changes
- Removed all block-fill callbacks and geometry; the outline remains customizable.
- Crosshair uses a cropped cached texture and conditional sprite hook, without WrapOperation dispatch. The drawing editor and color wheels remain.
- Sprint uses one keyboard-tick injection; zoom input is read on client ticks, not each frame. Smoothing is optional and off by default.
- Static menu cards and switches; one-click Disable all for isolating module-specific FPS issues.
- Existing configurations are migrated once: preserve colors, designs and module states; turn off smoothing and sprint HUD, reset outline width to one and remove fill.
- No FPS benchmark is claimed. For diagnosis compare identical world/settings with mod removed, installed with all modules disabled, and one module enabled at a time.

## Motion Blur (1.4.0)
Disabled by default. Open Right Shift > Motion Blur settings. Lite (3 taps), Balanced (5), High (7). Strength, sensitivity, maximum radius, deadzone, automatic/fixed direction, adaptive quality, configurable minimum FPS, first-person-only and zoom exclusion. Camera rotation drives a spatial directional effect; moving objects or pure translation alone do not blur. No frame accumulation, history buffers or depth pass. One directional sampling pass and one copy into the main framebuffer using Minecraft's existing pooled targets. Skips idle camera, open screens, world/camera changes, other vanilla post effects, and (conservatively) any Iris or VulkanMod installation. Intended for vanilla OpenGL/Fabric; Sodium compatibility needs testing. On shader load/render error it disables itself for the session and logs a warning. FPS protection is based on reported FPS and frame duration and does not guarantee zero FPS cost. Presets quantize radius to six values. Use Lite, strength 35%, minimum FPS 60 and adaptive quality on for low-end GPUs.

## Build locally
Install Java 21 and Gradle 9.2.1. From this folder run gradle clean build. The distributable (not the sources JAR) is build/libs/rylune-utils-1.21.11-1.4.0.jar. No Gradle wrapper is included; use an installed Gradle or the supplied GitHub Actions workflow.

## Build on GitHub
The supplied workflow builds the project and publishes the versioned JAR after a successful build. In Actions select Build Rylune Utils > Run workflow.

## Install in Rylune Client
Open the game directory of your Fabric 1.21.11 profile (the folder containing mods, e.g. rylune/profiles/1.21.11/ if configured that way). Place the built JAR and Fabric API for 1.21.11 in mods. Launch with Fabric Loader 0.18.0 or newer and Java 21. Avoid duplicate versions. This does not modify the launcher itself.

## Verification status
Compilation is handled by GitHub Actions; Minecraft runtime testing has not been performed. The custom hooks may conflict with other mods that replace the same rendering or input methods. Test on a clean Fabric profile first, then your usual Rylune profile. This release targets exactly 1.21.11; newer 26.x versions need separate ports. Check multiplayer server rules before using any client-side utility.

## Smoke-test checklist
1. Build succeeds; launch a fresh Fabric profile with Fabric API and this JAR.
2. Open a world; Right Shift opens menu; search filters all four modules; Esc and Right Shift close.
3. Enable Zoom and test C hold and toggle; ensure normal FOV returns when disabled.
4. Enable Sprint; press R and move forward, toggle off; test hunger, death and HUD visibility.
5. Enable Crosshair; try all shapes/colors/opacity and ensure vanilla attack indicator remains.
6. Enable Block Overlay; change width/color/opacity; test full blocks, slabs, stairs and transparent blocks.
7. Close/reopen the game and check JSON settings persist; repeat with other client mods installed.
