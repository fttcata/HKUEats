# HKUEats

HKUEats is an Android app concept for HKU students and staff to discover
student-friendly restaurants and food spots around campus. Users can browse
nearby recommendations, view ratings and reviews, and contribute places that
are difficult to find through general-purpose food platforms.

## Current status

This repository contains the initial Android application skeleton. It provides
a clickable UI prototype with:

- A home screen with mock nearby restaurant recommendations
- Search and cuisine filter controls (UI only)
- Restaurant details with ratings and sample reviews
- An add-restaurant form (local UI only)
- A profile/settings screen
- A simple login screen for the planned HKU Microsoft sign-in flow

The current data is intentionally local mock data. Supabase, Ktor, Microsoft
OAuth, GPS, image uploads, likes, and comments are planned integration work
and are not yet connected.

## Technology

- Kotlin
- Android Studio
- Jetpack Compose and Material 3
- Gradle

## Project structure

```text
HKUEats/
├── app/
│   └── src/main/java/com/example/hkueats/
│       ├── MainActivity.kt
│       └── ui/theme/Theme.kt
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Running the skeleton

1. Open the `HKUEats` folder in Android Studio.
2. Allow Gradle to sync and install any requested Android SDK components.
3. Run the `app` configuration on an emulator or Android device.

The project targets Android API 35 and requires a JDK version supported by the
installed Android Gradle Plugin (JDK 17 is recommended).

## Compressed 3-week delivery plan

Because only two to three weeks remain, the plan prioritises a demonstrable
core user journey over a full production backend.

### Week 1 — Foundation and core browsing

- Finalise low-fidelity wireframes and navigation
- Set up the Android project, shared repository, and basic Compose UI
- Implement the home screen with seeded restaurant data
- Implement restaurant search and simple cuisine/location filtering
- Create the restaurant details page with ratings and sample reviews
- Validate the browse-to-details flow with HKU students

### Week 2 — Contributions and account flow

- Implement the add-restaurant form and local validation
- Implement review submission with rating, comment, visit date, and optional
  image selection
- Add the account/settings screen
- Add the login screen and define the Microsoft OAuth integration boundary
- Connect a small Supabase schema or repository abstraction if time permits;
  otherwise keep the seeded local data for the final demo

### Week 3 — Integration, testing, and presentation

- Connect the highest-priority backend features: restaurants, reviews, and
  user profiles
- Add location-based recommendations if device permissions and time allow
- Test the complete flow: login → browse/search → restaurant details → review
- Fix usability and stability issues found during testing
- Prepare screenshots, demo data, presentation, and final documentation

### De-scoped or stretch features

The review feed, likes, comments, restaurant verification workflow, table
reservations, takeout ordering, rewards, and full Microsoft OAuth production
configuration are stretch goals. They should only be attempted after the core
flow is stable.

## MVP acceptance criteria

The final demo should allow a user to:

1. Open the app and see nearby/student-friendly restaurants.
2. Search or filter the seeded restaurants.
3. Open a restaurant and read its rating and reviews.
4. Submit a new restaurant or review through validated forms.
5. View a basic account/settings page.

## References

- [Kotlin and Android documentation](https://developer.android.com/kotlin)
- [Jetpack Compose documentation](https://developer.android.com/jetpack/compose)
- [Ktor documentation](https://ktor.io/)
- [Supabase documentation](https://supabase.com/docs)
- [Microsoft identity platform documentation](https://learn.microsoft.com/en-us/entra/identity-platform/)
- [GitHub documentation](https://docs.github.com/en)