# Engineering stack and pinned baseline

Versions are deliberately pinned instead of using dynamic dependency selectors. Revalidate them during controlled upgrades.

| Component | Selected version / target | Reason |
|---|---:|---|
| Android Gradle Plugin | 9.5.1 | Current stable AGP reference at foundation time |
| Gradle | 9.6.0 | Minimum documented Gradle line for AGP 9.5 |
| Kotlin | 2.4.10 | Kotlin plugin baseline used by Android's AGP 9.5 setup example |
| Jetpack Compose BOM | 2026.09.00 | Stable Compose library alignment |
| Ktor | 3.6.1 | Current Ktor 3.x patch release |
| JDK | 17 | Android build toolchain baseline |
| Android compile SDK | 37 | Required by Compose 1.12; supported by AGP 9.5 |\n| Android target SDK | 36 | Keep runtime behavior unchanged until target API migration is planned |
| Android min SDK | 26 | Initial compatibility floor; validate against feature needs |
| App UI | Jetpack Compose + Material 3 | Native Android UI |
| Backend | Kotlin + Ktor / Netty | Typed, coroutine-oriented service |
| Database (planned) | PostgreSQL | Durable source of truth |
| Ephemeral state (planned) | Redis | Presence leases and rate limiting |
| Media (planned) | LiveKit / WebRTC | SFU-based voice and streaming |

## Version sources

- Android Gradle Plugin reference: https://developer.android.com/reference/tools/gradle-api
- AGP version and Gradle compatibility: https://developer.android.com/build/releases/about-agp
- Kotlin releases: https://kotlinlang.org/docs/releases.html
- Compose BOM: https://developer.android.com/develop/ui/compose/bom
- Ktor releases: https://ktor.io/docs/releases.html
- Google Play target API policy: https://support.google.com/googleplay/android-developer/answer/11926878

## Upgrade policy

- Do not use dynamic versions such as 9.+ or 3.+.
- Upgrade one toolchain dimension at a time.
- Run Android assemble, backend tests, and relevant instrumentation tests after changes.
- Record compatibility constraints and the reason for each upgrade.
