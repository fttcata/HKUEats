# HKUEats

HKUEats is an Android app concept for HKU students and staff to discover
student-friendly restaurants and food spots around campus. Users can browse
nearby recommendations, view ratings and reviews, and contribute places that
are difficult to find through general-purpose food platforms.

## Current status

This repository contains the Android application MVP. It provides a clickable
Compose prototype with:

- A home screen with seeded nearby restaurant recommendations
- Search across restaurant name, cuisine, and location
- Independent cuisine and location filters
- Restaurant details with ratings and seeded student reviews
- An add-restaurant form (local UI only)
- A profile/settings screen
- A demo login screen that checks the email domain; Microsoft authentication
  and Supabase profile syncing are not connected

The current data is primarily local mock data. Supabase restaurant/review
integration is optional; Microsoft OAuth, GPS, image uploads, likes, and
comments are not connected.

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
│       ├── data/RestaurantRepository.kt
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

## Wireframe and navigation

The low-fidelity navigation is deliberately shallow for the MVP:

1. Login prototype → Explore
2. Explore → search/filter seeded places → restaurant card
3. Restaurant card → restaurant details → back to Explore
4. Bottom navigation provides Explore, Add place, and Account destinations

The Explore screen keeps search and filter controls above a vertically scrolling
restaurant list. Restaurant details prioritise the name, cuisine/price, rating,
location, review action, and recent student reviews.

## Manual browse-flow validation

Use an HKU student tester on an emulator or device and record whether each task
is completed without explanation:

1. Continue with an email ending in `@connect.hku.hk` or `@hku.hk` (the
   prototype checks the domain only; it does not authenticate the account).
2. Search for `cafe` and confirm The Caffeine Laboratory appears.
3. Clear the query, select `HKU Main Campus`, and confirm Wang Fu appears.
4. Open a restaurant card and confirm its rating, location, and two reviews.
5. Press back and confirm the Explore list is visible again.

The current repository does not include participant data or automated UI test
fixtures, so this checklist is the handoff for the requested student
validation rather than a claim that a user study has already been completed.

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