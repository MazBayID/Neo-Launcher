# Neo Launcher v0.2

Dark terminal-style Android launcher built with Kotlin and Jetpack Compose.

## Features
- Terminal-inspired interface with a live clock
- App drawer with an alphabet fast-scroller
- Notification previews for apps that post notifications
- Swipe app rows for Open, App Info, and Close actions
- Animated Compose row motion
- Upcoming calendar event header
- Media-notification title fallback

## Permissions and setup
- Grant Calendar permission when prompted to display upcoming events.
- Enable Neo Launcher under Android Settings → Notifications → Device & app notifications (Notification access) to show notification previews.
- The media title fallback reads media-style notifications; it is not a full MediaSession controller.

## Build
The repository includes a GitHub Actions workflow for building the Android APK. Check the Actions tab for the latest workflow run and its result.

## Development notes
- Android project uses Kotlin and Jetpack Compose.
- Review changes on the development branch before merging them into `main`.
