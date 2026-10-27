# Zordyx Client

Zordyx Client is a free, open source configuration library for Minecraft: Java Edition. It gives developers a config API, and it gives players one screen where they change the settings of every mod that uses it.

Zordyx Client is a fork of [OneConfig](https://github.com/Polyfrost/OneConfig) by [Polyfrost](https://polyfrost.org). The Java packages and the `assets/oneconfig` resource namespace keep their original names, so mods, translations and resource packs written against OneConfig keep working.

The mod itself is identified as `zordyx_client`, with `zordyx_client_v1` as the platform module nested in the jar, so launchers and Minecraft list Zordyx Client as its own mod instead of as OneConfig. It declares that it provides `oneconfig` (and `oneconfigv1`), which is what keeps a `depends: oneconfig` in a mod's metadata resolving instead of failing to load.

Zordyx Client keeps its own state: its settings and themes live in `zordyx_client/`, its preferences are `zordyx-client.json` and its themes are `zordyx-themes.json`. Nothing in this mod reads or writes OneConfig's own configuration, and settings from earlier Zordyx Client versions are copied over the first time it runs.

## ❔ General information

### Is it a client?
Zordyx Client is a standalone mod, not a full game client. It ships no mods of its own, so you decide which mods you use with it.

### What do players get?
- A settings screen with one card per mod, search, and a keybinds page. Right Control opens it, and the key is configurable in Preferences, so it can be changed if another mod already uses it.
- A HUD editor for moving, scaling and restyling the HUD elements of your mods.
- Profiles, which each keep a separate copy of your mod settings.

### Which versions does it support?
- Minecraft 1.8.9 on Ornithe.
- Minecraft 1.21.1-26.3 on Fabric.

Zordyx Client needs [Compose Multiplatform](https://modrinth.com/mod/compose-multiplatform) and Fabric Language Kotlin. It also needs Fabric API on Fabric and OSL on Ornithe.

## ⚡️ Getting started

For questions and bug reports, use the [issue tracker](https://github.com/notdeathlyatall/Zordyx-Client/issues). Upstream OneConfig documentation and support are available at [docsv1.polyfrost.org](https://docsv1.polyfrost.org).

To build Zordyx Client yourself, run the command below with JDK 25 installed. Replace `26.3-fabric` with any supported version, such as `1.8.9-ornithe`.

```sh
./gradlew ":bootstrap:26.3-fabric:build"
```

## 🎨 Adding your own theme
Themes are JSON files in `zordyx_client/themes` (next to your `config` folder). Drop a file in, restart the game or reopen the menu, and it appears in the Themes page; no code or rebuild needed. Zordyx Dark is the default theme, and any theme that is missing, invalid or deleted falls back to it.

The interface reads only the values below, treats the file as untrusted input and never runs code from it. A file that fails validation is skipped and reported in the log with the reason, and the rest keep working.

```json
{
  "id": "midnight-teal",
  "name": "Midnight Teal",
  "description": "A cooler take on the default dark interface.",

  "preview": "assets/oneconfig/images/zordyx-dark.svg",
  "logo": "assets/oneconfig/brand/oneconfig.svg",
  "font": "poppins",
  "cornerRadius": 10.0,
  "iconShapeRadius": 4.0,
  "scrollbarRadius": 8.0,
  "shadowEnabled": true,

  "pageBackground": "#FF0A0E11",
  "sidebarBackground": "#FF070A0C",
  "panelBackground": "#FF121A1E",
  "componentBackground": "#FF141C21",
  "chipBackground": "#FF101619",
  "popupBackground": "#FF101619",
  "border": "#1FFFFFFF",
  "primaryText": "#FFE9F1F2",
  "secondaryText": "#FF828D93",
  "accentText": "#FFFFFFFF",
  "shadow": "#FF000000",
  "controlThumb": "#FFF3F7F8",
  "controlTrack": "#FF74777F",
  "favorite": "#FFE8B84B",
  "accent": "#FF0A7C6F"
}
```

- `id` is required, unique, and lowercase (`a-z`, `0-9`, `.`, `_`, `-`). It is how the theme is stored in your settings, so changing it later switches you back to the default theme.
- `name` is required, and `description` is shown under the name in the Themes page.
- Colours are `#RRGGBB` or `#AARRGGBB` strings. The surfaces and text colours above are required; everything else has the default shown here.
- `preview` and `logo` must be a path inside this mod's own assets, and `font` is either `poppins` or `minecraft`.
- `accent` is optional: the theme's accent colour is applied when you select the theme, on top of the interface's normal accent setting.
- Hover, selected, enabled and disabled states are derived from the tokens above (`componentBackground`, `chipBackground`, `controlThumb`, `controlTrack` and `accentText`) rather than being separate settings, so a theme only has to describe its surfaces and text.

## ℹ️ Example mod
Mods built against OneConfig's API work with Zordyx Client. See the [example mod](https://github.com/Polyfrost/OneConfigExampleMod/).

## 📄 Licenses and Supporting Projects
<details>
  <summary>License</summary>
  Zordyx Client is a fork of OneConfig and is free and open-source. Except where noted otherwise (below and/or in individual files), all code is licensed under BOTH
- version 3 of the GNU Lesser General Public License, **AND**
- the Additional Terms Applicable to OneConfig, version 1.0 of the Additional Terms or (at your option) any later version.

Copies of version 3 of the GNU Lesser General Public License, version 1.0 of the Additional Terms Applicable to OneConfig, and version 3 of the GNU General Public License (which the GNU LGPLv3 supplements) can be found in the [**LICENSE**](LICENSE) file.

Polyfrost acts as the specified proxy when deciding whether future versions of the GNU Lesser General Public License shall apply. If Polyfrost publicly states the acceptance of a later version of the GNU Lesser General Public License, this is permanent authorization for you to choose that version for OneConfig. However, you **must still** be compliant with the Additional Terms Applicable to OneConfig.
</details>

<details>
  <summary>Credits</summary>
  Zordyx Client is based on OneConfig, created by Polyfrost and its contributors. The in-game credits screen retains the upstream project's credits, and all original copyright notices and license headers are preserved.
</details>

<details>
  <summary>Supporting Projects</summary>

![YourKitLogo|50](https://www.yourkit.com/images/yklogo.png)

YourKit supports open source projects with innovative and intelligent tools
for monitoring and profiling Java and .NET applications. <br>
YourKit is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/),
[YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/),
and [YourKit YouMonitor](https://www.yourkit.com/youmonitor/).
</details>
