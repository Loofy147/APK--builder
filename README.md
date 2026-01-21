# HelloWorldApp

A complete, "cloud-native" Android application setup for GitHub Actions. Designed to be built and managed entirely from your phone.

## 📱 How to Build and Install (Phone Only)

1. **Edit Code**: Use any code editor app on your phone to make changes.
2. **Push to GitHub**: Commit and push your changes to the `main` branch.
3. **GitHub Actions**:
   - Go to your repo on GitHub.
   - Click the **Actions** tab.
   - Click on the latest "Build APK" run.
4. **Download**:
   - Scroll down to **Artifacts**.
   - Download `debug-apk` (or `release-apk` if configured).
5. **Install**:
   - Open the downloaded file on your phone.
   - Allow "Install unknown apps" if prompted.

---

## 🔐 Release APK Setup (Production)

To build a signed Release APK, you need to add GitHub Secrets.

### 1. Create a Keystore
Run this once (on a PC or cloud shell) to generate a keystore and encode it:
```bash
keytool -genkeypair -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
base64 release.jks > release.jks.base64
```

### 2. Add GitHub Secrets
Go to **Settings → Secrets and variables → Actions** and add:

| Name | Value |
| :--- | :--- |
| `KEYSTORE_BASE64` | Contents of `release.jks.base64` |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | `release` (or your chosen alias) |
| `KEY_PASSWORD` | Your key password |

---

## 🛠 Project Structure
- **SDK**: 34
- **Gradle**: 8.8
- **Language**: Java 17
- **Tests**: Included unit and instrumented tests.

## 🏗 Local Development (Optional)
If you have a PC with Android SDK:
```bash
./gradlew assembleDebug
./gradlew test
```
