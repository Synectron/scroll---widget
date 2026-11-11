# Insta Scroll (Expo)

Mobile Expo app that opens Instagram in a WebView and counts every scroll. A floating badge shows today’s total; Stats keeps a 7-day history.

## Install on your phone (easiest)

1. Install **Expo Go** on your phone  
   - [iOS App Store](https://apps.apple.com/app/expo-go/id982107779)  
   - [Google Play](https://play.google.com/store/apps/details?id=host.exp.exponent)
2. On your computer, in this repo:

```bash
npm install
npx expo start
```

3. Scan the QR code with:
   - **iPhone**: Camera app → open in Expo Go  
   - **Android**: Expo Go → Scan QR code  
4. Log into Instagram inside the app and scroll — the bubble counts.

Phone and computer must be on the same Wi‑Fi (or use tunnel mode: `npx expo start --tunnel`).

## What it tracks

- Scroll gestures while you browse Instagram **inside this app**
- Today / all-time totals + last 7 days (stored on device with AsyncStorage)
- Floating badge (tap for stats, minimize with – / +)

> This does **not** hook into the Instagram native app. Browse Instagram in Insta Scroll so scrolls can be counted. For a system overlay on the Instagram app itself, you’d need a custom native Android build.

## Scripts

```bash
npm start          # same as npx expo start
npm run android    # open on Android emulator / device
npm run ios        # open on iOS simulator (macOS)
npm run typecheck  # tsc --noEmit
```

## Project layout

```
src/app/           # Expo Router screens
src/components/    # Scroll badge + stats sheet
src/lib/           # storage + WebView scroll bridge
```
