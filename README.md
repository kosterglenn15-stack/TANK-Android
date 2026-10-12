# T.A.N.K. Companion — single-app integrated prototype

This project is derived from the user-provided original `TANK-Android-password.zip`. The **original `TankCore.java` file is byte-for-byte unchanged**.

## What is integrated
- Original seven named calculation outputs (layers 1, 2, 3, 4, 7, 8, 9) and copy button.
- A nine-screen owner-entry demonstration inside the **same** Android app. No second APK or launcher required.
- Steps 5 and 6 are **new** checkpoints (date confirmation and SHA-256 chain checksum), not original T.A.N.K. stages: they are labeled as such in the UI.
- Owner state clears when app goes to background. No background service is added.
- GitHub Actions debug APK builder (repository-root Gradle project).

## Security warning
**This does not implement a secure owner authorization mechanism.** `TankCore.java` contains root-secret shares distributed to every user. Anyone with the APK could reproduce the answers. The nine-step screen is only a prototype and MUST NOT be used to guard important operations, proprietary source, licenses, or Rhino execution. For real owner authentication, use a server-side verifier or public-key signed authorization, move master secrets out of public APKs, and distribute a compiled Rhino component instead of a readable Python file.

## Build
Upload extracted contents to the root of a GitHub repository. Actions > Build TANK Companion Integrated APK > Run workflow. The debug artifact is `TANK-Companion-integrated-debug`. This app has the same `applicationId` as the original, but GitHub's debug signing key differs from a release build, so it generally **cannot be installed as an update over your signed release APK**. Back up any data you need first; test on a spare device or separate installation environment.

Android build not executed in the current environment; GitHub CI is expected to validate it.
