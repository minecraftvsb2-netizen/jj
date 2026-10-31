# Power Wake Guard

An Android app (Android 9 or newer) that keeps your phone "asleep" unless you press the power
button a set number of times in a row (default: 3 presses, each within 2 seconds of the last).
One or two presses and the screen goes straight back to black.

iPhones cannot do this.

## How it works

Android does not let apps intercept the power button. So the app watches for the screen turning
on. Each screen-on counts as one press. If you have not reached your chosen number yet, the app
locks the screen again right away. When you reach it, the screen stays on.

## Get the APK (pick one)

**A. GitHub (no Android Studio needed)**
1. Create a free GitHub account and a new repository.
2. Upload everything in this folder, including the hidden `.github` folder.
3. Open the repository's **Actions** tab, run **Build APK**, and download `PowerWakeGuard-apk`
   from the finished run. Unzip it to get `app-debug.apk`.

**B. Android Studio**
Open this folder as a project, let it sync, then use Build > Build APK(s). Install the file from
`app/build/outputs/apk/debug/app-debug.apk`.

## Set it up on the phone

1. Install the APK (allow "install unknown apps" for your file manager or browser when asked).
2. Open the app. On Android 13 or newer: App info > three-dot menu > **Allow restricted
   settings**.
3. Tap **Turn on the service**, find **Power Wake Guard** in Accessibility, and switch it on.
4. Tap **Allow background use** and set the app to **Unrestricted** / **No restrictions**.
   Xiaomi, Oppo, Vivo, Realme and Samsung phones also have an "autostart" or "never sleeping
   apps" setting. Enable it, and lock the app in the recent apps list.
5. In your phone settings, turn off **Tap to wake**, **Lift to wake**, **Always-on display** and
   any "wake screen for notifications" option. Keep the phone on silent with vibration off.
6. Test it: lock the phone, press power once, then twice, then three times quickly.

## Limits

- The screen may light for a split second on each press. Android cannot block the button.
- If the phone's system kills the app (strict battery saving), the protection stops. Test it
  after a few hours and after a restart.
- A notification or alarm that lights the screen counts as one press.
- Calls are not interfered with.
- Phone must be on Android 9 (API 28) or newer.
