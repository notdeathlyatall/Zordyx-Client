# Zordyx Client OneConfig compatibility

A metadata-only Fabric mod. The jar contains `fabric.mod.json` and nothing else: no classes, no mixins,
no entrypoints.

## Why it exists

Fabric Loader resolves a dependency on a mod id against the **version of the mod that owns that id**.
A `provides` entry is only an alias for the providing mod; constraints attached to the alias are still
checked against the provider's own version. Zordyx Client's `provides: ["oneconfig", ...]` therefore
only helped mods that asked for `oneconfig` with `*` or a constraint the Zordyx release happened to
satisfy. Mods built against OneConfig ask for versions such as `>=1.5.0`, so they failed to resolve.

This mod owns the `oneconfig` id and provides `oneconfigv1`, so both dependency forms resolve against a
version that actually represents the OneConfig API level Zordyx Client implements.

## Versioning

The version is `1.5.0+zordyx.<zordyx release>`. `1.5.0` is the OneConfig platform release line this
fork implements; the `+zordyx...` part is semver build metadata, which Fabric ignores when comparing
versions, so the Zordyx release it belongs to stays visible without affecting dependency resolution.

Bump the leading `1.5.0` only when the OneConfig API level this fork implements changes, never to match
a Zordyx release number.

## Packaging

The mod is a normal module of this repository, so the platform module depends on it and
`oneconfig-bootstrap.gradle.kts` nests it inside the bootstrap jar. It is never published or uploaded on
its own, and it must stay out of `modules/compose-bundle` style standalone uploads: a user installs one
Zordyx Client jar and gets this metadata with it.
