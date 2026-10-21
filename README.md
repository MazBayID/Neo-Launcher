# Neo Launcher v0.2

Dark terminal-style Android launcher.

## New features
- Touch-driven alphabet fast scroller with wave scaling and haptic ticks
- Notification previews for apps that post notifications
- Swipe app rows right for Open / App Info / Close actions
- Spring-based Compose row motion; AndroidX DynamicAnimation dependency included
- TextClock, next calendar event within one hour, and media-notification title fallback

## Permissions / setup
- Grant Calendar permission when prompted for upcoming-event header.
- Open Settings > Notifications > Device & app notifications (Notification access) and enable Neo Launcher for inline previews.
- Media title fallback reads media-style notifications; it is not a full MediaSession controller.
