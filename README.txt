T.A.N.K. Android Companion
===========================

WHAT THIS IS
------------
An Android Studio project for a phone companion that calculates the same
Layer 1/2/3/4/7/8/9 chain used by the T.A.N.K. v2.2 package.

It shows:
  Layer 1 - Wordle
  Layer 2 - Daily coordinate
  Layer 3 - Kalebration 3301
  Layer 4 - Cicada transform
  Layer 7 - Distorted coordinate
  Layer 8 - Waypoint token
  Layer 9 - final T.A.N.K. password

The app intentionally asks you to type the 5-letter Wordle instead of using
network permissions.

BUILDING THE APK
----------------
1. Install Android Studio on Windows.
2. Open this folder as a project.
3. Let Gradle sync.
4. Build > Build APK(s).
5. Android Studio will create an APK under:
   app/build/outputs/apk/debug/app-debug.apk

QR DOWNLOAD
-----------
A QR code can only download an APK from a URL your phone can reach.
After you build the APK, upload it to OneDrive, Google Drive, Dropbox,
GitHub Releases, or another HTTPS location. Send that download URL back to
ChatGPT and a QR code can be generated for it.

IMPORTANT
---------
This project contains the current protected-build local root material required to reproduce the
same deterministic layer results. Like the desktop launcher, that material
can be reverse-engineered by a determined attacker with the APK/source.

This phone app does NOT yet approve/deny Rhino runs. It calculates the layer
password. The Rhino password-gate integration is a separate next step.
