# ScalableApp

## How to run
Requires Android Studio Rabbit 2 (2026.2.2) or newer (AGP 9.5).
Open in Android Studio and run `app`. Unit tests: `./gradlew testDebugUnitTest`.

## Not done
- Pagination: only the first API page is loaded (up to 100 repos and 100 commits).
- Error handling: generic message only, no retry or rate-limit handling.
- UI tests.
- Pixel-perfect design: the focus was on functionality, UI uses default Material 3 styling.
