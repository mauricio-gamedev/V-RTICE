# Test strategy

## Backend

- Unit tests for domain rules and permission evaluation.
- Integration tests for routes, persistence, WebSocket events, and migrations.
- Contract tests for event payload versioning and idempotency.
- Negative tests for unauthorized access and invalid input.

## Android

- Unit tests for ViewModels, reducers, validation, and repositories.
- Compose UI tests for primary navigation and critical states.
- Instrumented tests for permission prompts, lifecycle, and process recreation.
- Physical-device checks for audio routes, calls, MediaProjection, battery, and memory.

## CI gates

The foundation workflow must:
1. Compile the Android debug variant.
2. Run backend tests.
3. Preserve the debug APK as a workflow artifact.

Later gates should add Android lint, detekt/formatting, migration checks, dependency scanning, and API contract tests.

## Device validation

Use emulator coverage for standard behavior and a physical modest device for performance and hardware behavior. The Galaxy A06 is an early reference device.

Record Android version and build, device model and available RAM, app commit/variant, reproduction steps, measured RAM/CPU/jank/network values, and expected versus observed behavior.

A CI pass is necessary but does not replace manual device validation.
