# Neo Launcher — Redesign 01

A futuristic Android home launcher with a dark midnight palette, mint/cyan accents, a prominent live clock, date, quick app grid, and searchable app library.

## Redesign highlights

- **New home screen:** live 24-hour clock, localized date, system status panels, quick-launch app grid, and app-library entry point.
- **App library:** searchable installed-app grid with app icons and one-tap launch.
- **Visual system:** midnight gradients, glass-like panels, rounded surfaces, monospace micro-labels, and mint/cyan highlights.
- **Android integration:** retains the Android Home intent and opens installed apps through the package manager.

## Build

Open the project in Android Studio and run the `app` configuration. The project uses Kotlin and Jetpack Compose.

## Status

This branch is the first UI redesign pass. The supplied custom PNG icon pack and clock/date cutouts still need to be mapped into Android drawable resources in a follow-up asset-integration pass; the current app grid uses each installed app's system icon.

## Branch

`redesign/neo-home-v1` — preview the redesign without replacing `main`.
