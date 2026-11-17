# CourtUp mobile

Native Kotlin / Jetpack Compose Android app and the existing CourtUp Express backend, included together so one clone is enough.

## Run on another laptop

Prerequisites: Git, Node.js with npm, Android Studio, Android SDK components requested by Gradle, and JDK 21 selected as Android Studio's Gradle JDK.

1. Clone https://github.com/Vidit-001/Courtup_mobile.git.
2. Open the cloned repository's root directory in Android Studio and let Gradle sync.
3. Open a terminal in the cloned repository and start the backend:

   ```powershell
   cd server
   npm ci
   node index.js
   ```

   Keep this terminal open. It should print "Server running on port 5000". Run npm ci on the first setup or after the dependency lockfile changes.
4. Start an Android emulator, select the app configuration and click Run (debug).
5. The default API URL is http://10.0.2.2:5000/, which reaches the backend on the same laptop. The backend must run while using room features.

The backend source and dependency lockfile are in server/. Dependencies are installed locally; node_modules is excluded from Git. The original web project is unchanged and is not needed to run the mobile app.

## Physical phone

Connect the phone and laptop to the same Wi-Fi, set ApiConfig.BASE_URL in app/src/main/java/com/example/courtup/CourtUpViewModel.kt to http://YOUR_LAPTOP_LAN_IP:5000/, and allow Node/port 5000 through the laptop firewall on private networks. Connect the phone with USB debugging enabled, select it in Android Studio and run the debug app.

HTTP is enabled only in debug builds. Release builds require an HTTPS backend.

## Behavior

The display name is an identity gate, not authentication. Name and navigation survive configuration changes and saved-state restoration. Rooms are stored in memory and reset when the backend restarts. Refresh fetches current room data; successful mutations also refresh. Failed requests show errors.

The included backend preserves the original endpoints and behavior: GET/POST /api/rooms and POST /api/rooms/{id}/request, /accept, /reject, /remove.

## Verification

- Android debug APK assembly and JVM unit tests passed with JDK 21.
- State tests cover host, member, pending, full and joinable precedence.
- Live Express calls exercised create, list, request, reject, accept and remove using the original payloads.
- APK installed and launched on the API 36.1 emulator; welcome screen, disabled empty-name button and keyboard layout were visually inspected.
- Full UI flows and rotation/state restoration remain unverified after the emulator input channel disconnected.
- Android Studio sync previously timed out locally; command-line Gradle builds succeeded with JDK 21.
