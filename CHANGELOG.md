## Unreleased
- Zordyx Client is now its own mod as far as Minecraft, Fabric Loader and launchers are concerned: it uses the ids `zordyx_client` and `zordyx_client_v1`, its own artifact name, mixin configs and access widener, and ships a small compatibility mod that keeps the OneConfig ids available for mods that depend on them
- Fix mods that depend on OneConfig failing to load with a missing dependency error. Fabric Loader checks a dependency's version against the mod that owns the id, so Zordyx Client's release version could never satisfy a constraint like `oneconfig >=1.5.0`. A metadata-only `oneconfig` compatibility mod, nested inside the Zordyx Client jar, now carries both OneConfig ids at version 1.5.0 and holds no classes of its own
- Add the AimAssist module to the Combat subsection, with a settings screen for its mode, target filters, speeds, range, item limits and targeting. It is a configuration screen only: no aiming behaviour is implemented yet
- The default accent colour is now green
- Zordyx Client's settings and themes moved into their own namespace (`zordyx_client/`, `zordyx-client.json`, `zordyx-themes.json`), so it no longer shares configuration or theme files with OneConfig; existing settings are copied across automatically
- The menu now opens on Right Control instead of Right Shift, so it does not collide with another mod opening on the same key, and its Minecraft keybind category is registered under Zordyx Client's own namespace
- Removed the PolyGlass themes and their previews, the Polyfrost and Atmofrost logos on the credits page, and the Polyfrost mod-list and category service
- Added custom themes: drop a JSON file into `zordyx_client/themes` to theme the interface, with validation, clear errors and a fallback to Zordyx Dark
- Rebranded the project as Zordyx Client
- New Zordyx interface: expandable sidebar with Modules, Overlays, Legit mods, Profiles, Friends, Macros, Online and Global settings, plus module lists with search, filters and visual toggles (interface preview, no module behaviour yet)

## 1.5.0
- Add community translations for Zordyx Client and many other mods
- Sodium is now translated too, using the Translations for Sodium resource pack
- Translators are now credited in the credits screen
- The accent color can now be a gradient. Turn on "Gradient" in the accent color picker and pick a second color
- Add keybinds for switching profiles. Set one on any profile card on the Profiles page. A notification confirms the switch, and you can turn it off in Preferences
- Add a sort control to the Mods page. Keep your own drag order, or sort mods A to Z
- Add a "Full Shadow" option for text HUDs, which outlines the text on every side instead of using a drop shadow
- Add a guided tour of the HUD editor. Replay it any time by right-clicking an empty spot and choosing "Editor Tour"
- Add a Search keybind that jumps to the search bar in the Zordyx Client menu and HUD editor. In the HUD editor it searches the selected HUD's settings, or the HUD library when nothing is selected
- Move the "Instant search" setting into the Search category
- Add "Select all" and "Deselect all" to dropdowns where you can pick several options
- Better Screens, Render Tweaks and Shake Tweaks now provide their own Zordyx Client on/off toggle, so Zordyx Client no longer adds one for them
- Fix the on/off toggle not turning off some mods
- Fix one broken line in your Minecraft options resetting all of your settings
- Fix copy and paste not working on 1.8.9 on Apple Silicon Macs, and copying images on Linux with Wayland
- Fix 1.8.9 settings such as clouds changing after playing a newer version
- Fix the theme and accent color not changing when you switch profiles
- Fix colored text showing without colors in HUDs and in Chat Patches and YACL config screens
- Fix YACL options not updating when another option changes them
- Fix the "HUD is locked" notice appearing on every key press while editing a locked HUD
- Fix a warning being logged for every chat message with transparent colors on newer versions
- Fix notification icons, the HUD editor search bar, item pickers and the mod card on/off indicator not following your theme
- Fix Zordyx Client's menus failing to show text on Windows when the game is installed in a deeply nested folder

### For mod developers
- Add an experimental public UI API (`api.ui.v1`) for pages, themes, components, search and keybinds, so mods no longer need Zordyx Client's internals
- Add an API for adding your own sort modes to the Mods page
- Add `SliderBounds` so sliders can change their minimum and maximum at runtime
- Add opt-in gradient colors with `@Color(gradient = true)`
- Add an `@EffectList` option for picking status effects
- Add item stack icons for item list options, for item variants such as potions
- Add a full width option for options that need the whole row
- Fix nested annotated commands and handler aliases not being registered
