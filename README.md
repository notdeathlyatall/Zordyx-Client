# Zordyx Client

**A customizable configuration and HUD interface for Minecraft: Java Edition.**

Zordyx Client is a free, open-source fork of [OneConfig](https://github.com/Polyfrost/OneConfig), originally developed by [Polyfrost](https://polyfrost.org). It provides a centralized interface for managing compatible mods, customizing HUD elements, and organizing mod settings.

Zordyx Client is a standalone mod, not a complete Minecraft client. It does not bundle gameplay mods, allowing you to choose which compatible mods you want to use.

## ✨ Features

- **Centralized settings:** Manage compatible mods through a unified interface with individual mod cards and search.
- **Keybind management:** View and manage supported mod keybinds.
- **HUD editor:** Move, resize, and customize supported HUD elements.
- **Profiles:** Maintain separate copies of your mod settings for different configurations.
- **Dark theme:** A dark interface designed for the Zordyx Client experience.
- **OneConfig API compatibility:** Support for mods built against the OneConfig API.

## 🎮 Compatibility

Zordyx Client currently targets the following Minecraft versions and mod loaders:

- **Minecraft 1.8.9:** Ornithe
- **Minecraft 1.21.1–26.3:** Fabric

### Required dependencies

Zordyx Client requires the following dependencies, depending on your Minecraft version and mod loader:

- [Compose Multiplatform](https://modrinth.com/mod/compose-multiplatform)
- Fabric Language Kotlin
- Fabric API for Fabric
- OSL for Ornithe

Install dependency versions compatible with your Minecraft version and mod loader.

## 🚀 Getting Started

### Installation

1. Download a Zordyx Client build that matches your Minecraft version and mod loader.
2. Install the required dependencies.
3. Place the mod and its dependencies in the appropriate mods directory.
4. Launch Minecraft using the matching mod loader.

### Opening the interface

Press **Right Shift** to open the configuration interface using the default keybind.

### Building from source

To build Zordyx Client yourself, install **JDK 25** and clone this repository.

Run the following command from the repository root:

```sh
./gradlew ":bootstrap:26.3-fabric:build"
```

Replace `26.3-fabric` with your intended build target, such as `1.8.9-ornithe`.

On Windows, use:

```powershell
.\gradlew.bat ":bootstrap:26.3-fabric:build"
```

## 🧩 Developer Information

Zordyx Client is based on OneConfig and is intended to support mods built against its API.

For an example of a mod built using the OneConfig API, see the [OneConfig Example Mod](https://github.com/Polyfrost/OneConfigExampleMod/).

For compatibility, certain Java package names, mod IDs, and on-disk configuration paths retain their original `oneconfig` identifiers. These internal identifiers do not mean Zordyx Client is an official OneConfig release.

## 🐛 Support and Bug Reports

Found a bug or have a suggestion?

- **Report issues:** [Zordyx Client Issue Tracker](https://github.com/notdeathlyatall/Zordyx-Client/issues)
- **Upstream documentation:** [OneConfig Documentation](https://docsv1.polyfrost.org)
- **Original project:** [Polyfrost/OneConfig](https://github.com/Polyfrost/OneConfig)

When reporting a bug, include your Minecraft version, mod loader, Zordyx Client version, relevant dependencies, and a crash log when applicable.

## 📄 License

Zordyx Client is a fork of OneConfig. The applicable license terms depend on the original code, any modifications, and the notices accompanying individual files.

The upstream OneConfig licensing notice specifies:

- **GNU Lesser General Public License, version 3 (LGPL-3.0)**; and
- **Additional Terms Applicable to OneConfig**, version 1.0 or a later version where permitted by those terms.

The upstream licensing notice also references the GNU General Public License, version 3, which supplements the LGPL.

Refer to the repository's [`LICENSE`](LICENSE) file and applicable source-file notices for the complete terms. Preserve required copyright notices, license headers, attribution, and other applicable notices when redistributing the project.

New code, assets, and third-party dependencies may have separate licensing requirements. Their licensing status should be determined from the relevant files and permissions rather than assumed from this README.

**This section is an informational summary, not a replacement for the actual license documents.** Do not interpret it as a declaration that every file in Zordyx Client is licensed solely under GPL-3.0.

## 🙏 Credits

Zordyx Client is based on [OneConfig](https://github.com/Polyfrost/OneConfig), created by Polyfrost and its contributors.

Applicable upstream copyright notices, license headers, and credits are retained. Thank you to the original developers and contributors whose work made this project possible.

## 🛠️ Supporting Projects

[YourKit](https://www.yourkit.com/) supports open-source projects with tools for monitoring and profiling Java and .NET applications.

Learn more about its products:

- [YourKit Java Profiler](https://www.yourkit.com/java/profiler/)
- [YourKit .NET Profiler](https://www.yourkit.com/.net-profiler/)
- [YourKit YouMonitor](https://www.yourkit.com/youmonitor/)

## ⚖️ Disclaimer

Zordyx Client is an independent fork and is **not affiliated with, endorsed by, or an official product of Polyfrost or OneConfig**.

Minecraft is a trademark of Mojang Studios. Zordyx Client is an independent modification and is **not affiliated with or endorsed by Mojang Studios or Microsoft**.
