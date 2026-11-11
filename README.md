# Insta Scroll

Android widget that counts how many times you scroll in the **Instagram app** and shows the total on a floating bubble + home-screen widget.

## What you get

- **Floating overlay** over Instagram with today’s scroll count (draggable, minimizable)
- **Home screen widget** with the same daily count
- **In-app dashboard** with today / all-time / last 7 days
- Counts only while `com.instagram.android` is in use (via Accessibility)

Data stays on your phone. Nothing is uploaded.

## Requirements

- Android 8.0+ (API 26)
- Instagram app installed
- Two permissions you turn on once:
  1. **Accessibility** → Insta Scroll (so it can see scroll events in Instagram)
  2. **Display over other apps** (for the floating bubble)

> iOS cannot host this kind of counter inside the Instagram app without jailbreak APIs, so this project targets Android.

## Setup (Android Studio)

1. Clone this repo and open the project root in Android Studio.
2. Let Gradle sync, then Run on a phone or emulator with Instagram installed.
3. Open **Insta Scroll** and tap:
   - **Turn on Accessibility** → enable Insta Scroll
   - **Allow floating widget** → allow overlay
4. Open Instagram and scroll — the bubble should tick up.

### Home screen widget

Long-press your home screen → **Widgets** → **Insta Scroll** → add the scrolls-today widget.

## How counting works

The accessibility service listens for `TYPE_VIEW_SCROLLED` events from Instagram, groups them into gestures (~220ms gap), and also converts larger scroll distances into extra units (~280px ≈ 1 scroll). Totals are stored in local `SharedPreferences` and mirrored to the overlay + widget.

## Project layout

```
app/src/main/java/com/synectron/instascroll/
  data/ScrollRepository.kt      # daily + lifetime stats
  service/ScrollAccessibilityService.kt
  service/OverlayService.kt     # floating bubble
  ui/MainActivity.kt            # setup + stats
  widget/ScrollAppWidgetProvider.kt
```

## Privacy

- No network calls
- No account login
- Accessibility is scoped to the Instagram package only
