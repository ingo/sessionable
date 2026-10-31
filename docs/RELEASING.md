# Releasing

`.github/workflows/release.yml` builds the iOS and Android apps and sends them to your testers whenever app code
changes on `main` (or when you start it by hand from the Actions tab). The web app is published to GitHub Pages by
`web-deploy.yml`. Until the settings below exist, the release jobs say what is missing and finish green.

Set them under Settings > Secrets and variables > Actions.

## What you need once

**Which app.** Variables `RELEASE_EVENT` (an id from `EventProfiles.kt`, or empty for the generic app) and
`APP_DISPLAY_NAME` (default `Sessionable`).

### iOS to TestFlight

1. In App Store Connect, create an app record with your bundle id.
2. Create an App Store Connect API key (Users and Access > Integrations > App Store Connect API, role *App Manager*).
3. Secrets `ASC_KEY_ID`, `ASC_ISSUER_ID`, and `ASC_KEY_P8` (the contents of the `.p8` file).
   Variables `APPLE_TEAM_ID` and `IOS_BUNDLE_ID`.

The build is signed automatically with that key; no certificates are stored. The build number is managed by Apple, and
the build appears for your internal testers when processing finishes. External testers need a TestFlight beta review.

### Android to Google Play (internal testing)

1. Create the app in the Play Console and upload the first build by hand (Play requires it before the API works).
2. Create an upload key: `keytool -genkeypair -v -keystore release.keystore -alias upload -keyalg RSA -keysize 2048 -validity 10000`
   and keep it somewhere safe. Enroll in Play App Signing when asked.
3. In Google Cloud, create a service account with a JSON key, invite it in the Play Console (Users and permissions) with
   release rights on the app.
4. Secrets `ANDROID_KEYSTORE_BASE64` (`base64 -i release.keystore`), `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`,
   optionally `ANDROID_KEY_PASSWORD`, and `PLAY_SERVICE_ACCOUNT_JSON`. Variable `ANDROID_APP_ID` (the application id).

Each upload gets the version code `1000 + run number`. Testers join the internal track from a link in the Play Console.

## Store listing

- Support URL: https://thunderclatter.com/
- Privacy policy URL: the policy page (`docs/site/privacy.html`) published under thunderclatter.com once its DNS is live.
  The page must return 200 before it is entered in App Store Connect and Play Console.
- Say in the description that the app is unofficial and not affiliated with Sessionize.

## By hand

```bash
scripts/ios-testflight.sh "" <bundle-id> Sessionable <team-id> --upload     # needs ASC_KEY_ID and ASC_ISSUER_ID
ANDROID_KEYSTORE_PATH=... ANDROID_KEYSTORE_PASSWORD=... ANDROID_KEY_ALIAS=... ./gradlew -PappId=<id> :app:androidApp:bundleRelease
```
