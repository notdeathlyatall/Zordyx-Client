/*
 * A metadata-only Fabric mod that carries the OneConfig mod ids.
 *
 * Fabric Loader resolves `depends: oneconfig` and `depends: oneconfigv1` against the version of the mod
 * that owns the id, and a `provides` entry is always compared against the providing mod's own version.
 * Zordyx Client's release version therefore cannot satisfy the constraints third-party mods are built
 * with, no matter which ids it provides. This mod is the id carrier instead: it is nested inside the
 * bootstrap jar, holds no classes at all (every OneConfig API lives in the platform module, so nothing
 * is duplicated), and its version is the OneConfig API level this fork implements rather than the
 * Zordyx release, which is kept as semver build metadata and ignored by version comparisons.
 */

// 1.5.0 is the OneConfig platform release line this fork implements and the last version it shipped
version = "1.5.0+zordyx.${rootProject.version}"

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
}

// the jar is only ever nested inside the bootstrap, so it has nothing to publish on its own
tasks.named("publish") { setDependsOn(emptyList<Any>()) }
