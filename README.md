# Zordyx Client

Zordyx Client is a free, open source configuration library for Minecraft: Java Edition. It gives developers a config API, and it gives players one screen where they change the settings of every mod that uses it.

Zordyx Client is a fork of [OneConfig](https://github.com/Polyfrost/OneConfig) by [Polyfrost](https://polyfrost.org). The Java packages, mod IDs and on-disk config paths keep their original `oneconfig` names so existing mods and settings keep working after the rebrand.

## ❔ General information

### Is it a client?
Zordyx Client is a standalone mod, not a full game client. It ships no mods of its own, so you decide which mods you use with it.

### What do players get?
- A settings screen with one card per mod, search, and a keybinds page. Right Shift opens it.
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
