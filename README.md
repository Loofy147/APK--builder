# HelloWorldApp

A simple Android application prepared for CI/CD with GitHub Actions.

## Features
- Android SDK 34
- Gradle 8.8
- Unit tests and Instrumented tests included.
- GitHub Actions workflow for building Debug and Release APKs.

## How to build
To build the debug APK:
```bash
./gradlew assembleDebug
```

To run unit tests:
```bash
./gradlew test
```

## GitHub Actions
The workflow is configured to:
1. Build the Debug APK on every push to `main`.
2. Build a Signed Release APK if the necessary secrets are provided.

### Secrets for Release Build
- `KEYSTORE_BASE64`: Base64 encoded `.jks` or `.keystore` file.
- `KEYSTORE_PASSWORD`: Password for the keystore.
- `KEY_ALIAS`: Alias for the key.
- `KEY_PASSWORD`: Password for the key.
