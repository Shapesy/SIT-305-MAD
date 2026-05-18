# Event Planner

Android app for creating and managing personal events. Built with Kotlin and Jetpack.

## Features

- Add, edit, and delete events (title, category, location, date/time)
- Events persist locally with Room
- Bottom navigation between the event list and add/edit screen
- Validation on save — title and date are required, past dates blocked for new events
- Delete snackbar includes an undo option

## Stack

- Kotlin, MVVM
- Room + Repository
- Jetpack Navigation, Safe Args
- Material Components, ViewBinding

## Setup

1. Open the project in Android Studio
2. Wait for Gradle to sync
3. Run on a device or emulator (Android 8.0+)
