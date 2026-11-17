# CourtUp Android

Native Kotlin / Jetpack Compose client for the existing CourtUp Express server. No web source changes or separate backend.

## Run
1. In PowerShell, open the existing Court-up_V0 web project's server directory.
2. Run `npm install` if dependencies are missing, then `node index.js`.
3. In Android Studio, sync Gradle, select the app configuration and an emulator, then Run (debug).
4. Default API URL is http://10.0.2.2:5000/. Change ApiConfig.BASE_URL in app/src/main/java/com/example/courtup/CourtUpViewModel.kt for another server.
5. Physical phone: use http://YOUR_COMPUTER_LAN_IP:5000/, connect both devices to the same network, and allow Node/port 5000 through Windows Firewall. Rebuild and run the debug app.

HTTP is enabled only in the debug manifest. Release builds require an HTTPS backend.
Use JDK 21 for Gradle (Android Studio's Gradle JDK setting); the system Java 25 installation is incompatible with this project's current tooling.

## Behavior
The display name is an identity gate, not authentication. Name and navigation survive configuration changes and saved-state restoration. Rooms remain in memory on the existing server and reset when it restarts. Refresh fetches current room data; successful mutations also refresh. Failed requests show errors.

All original endpoints are used: GET/POST /api/rooms and POST /api/rooms/{id}/request, /accept, /reject, /remove.

## Verification
- Debug APK assembly and JVM unit tests passed with JDK 21.
- State tests cover host, member, pending, full and joinable precedence.
- Live Express calls exercised create, list, request, reject, accept and remove using the original payloads.
- APK installed and launched on the API 36.1 emulator; welcome screen, disabled empty-name button and keyboard layout were visually inspected.
- Full UI flows, rotation/state restoration and physical phone connectivity remain unverified: the emulator input channel disconnected and the emulator exited.
- Android Studio Gradle sync timed out after updating its local Gradle JDK; command-line Gradle works with the JDK noted above.

