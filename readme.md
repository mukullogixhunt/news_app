# NewsFlow App 📰

A modern Android news reader application built entirely with Kotlin and leveraging the latest Jetpack libraries and best practices. This project demonstrates a clean architecture approach (MVVM) for building scalable and maintainable Android apps.

**(Optional: Add Screenshots/GIF Here)**
<!-- ![App Screenshot 1](link/to/screenshot1.png) -->
<!-- ![App GIF](link/to/demo.gif) -->
*Screenshots/GIF demonstrating the app's UI and features would go here.*

---

## ✨ Features

*   **Top Headlines:** Browse the latest news headlines, filterable by country (Default: US).
*   **Search:** Find articles based on keywords.
*   **Article Details:** View article details including image, source, published date, and content. Option to open the full article in a browser.
*   **Bookmarking:** Save interesting articles for later reading.
*   **Offline Caching:** Fetched articles are stored locally using Room, allowing offline access to previously viewed headlines and bookmarks.
*   **Background Sync:** WorkManager periodically fetches new top headlines in the background (configurable interval via Settings).
*   **Notifications:** Receive notifications when new headlines are fetched during background sync (requires notification permission on Android 13+).
*   **Theme Support:** Seamless Light and Dark mode support, configurable via Settings (System Default, Light, Dark).
*   **Efficient Loading:** Uses Paging 3 library with `RemoteMediator` to efficiently load headlines from the network with local caching, and handle large lists smoothly. Search and Bookmarks also use Paging 3.
*   **Modern UI:** Built with Material Design 3 components using XML Views and ViewBinding.

---

## 🛠️ Tech Stack & Architecture

This project showcases a robust set of modern Android development tools and patterns:

*   **Language:** [Kotlin](https://kotlinlang.org/) (100%)
*   **Architecture:** Clean Architecture (Layered: Data, Domain, Presentation), MVVM (Model-View-ViewModel)
*   **UI:**
    *   XML Layouts with [ViewBinding](https://developer.android.com/topic/libraries/view-binding)
    *   [Material Design 3](https://m3.material.io/) Components
    *   [Navigation Component](https://developer.android.com/guide/navigation) (Single Activity architecture)
    *   [RecyclerView](https://developer.android.com/guide/topics/ui/layout/recyclerview)
*   **Asynchronous Programming:**
    *   [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
    *   [Flow](https://kotlinlang.org/docs/flow.html) (for reactive data streams)
*   **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) (for managing dependencies across the app, including ViewModels and Workers)
*   **Networking:**
    *   [Retrofit 2](https://square.github.io/retrofit/) (for type-safe HTTP requests)
    *   [OkHttp 3](https://square.github.io/okhttp/) (as the HTTP client, used for interceptors)
    *   [Gson](https://github.com/google/gson) (for JSON parsing)
*   **Database:** [Room Persistence Library](https://developer.android.com/training/data-storage/room) (for offline caching)
*   **Paging:** [Paging 3 Library](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) (for loading large datasets gradually from network/database)
    *   `RemoteMediator` (for Network + Database pagination)
    *   `PagingSource` (for Database-only or Network-only pagination)
*   **Background Processing:** [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) (for reliable background sync tasks)
    *   Hilt Integration for WorkManager (`@HiltWorker`)
*   **Data Persistence (Settings):** [Jetpack DataStore (Preferences)](https://developer.android.com/topic/libraries/architecture/datastore) (for saving user preferences like theme and sync interval)
*   **Image Loading:** [Glide](https://github.com/bumptech/glide) *(or Coil, depending on your final choice)*
*   **Date/Time Handling:** [ThreeTenABP](https://github.com/JakeWharton/ThreeTenABP) (Android backport of JSR-310)
*   **Build System:** Gradle with Kotlin DSL (`build.gradle.kts`) and Version Catalog (`libs.versions.toml`)

---

## 🌐 API

This application uses the [NewsAPI.org](https://newsapi.org/) service to fetch news articles.

**⚠️ Important:** You need to obtain your own API key from [NewsAPI.org](https://newsapi.org/) to build and run this project. The free tier is usually sufficient for development purposes but has request limitations.

---

## 🚀 Setup and Installation

1.  **Clone the repository:**
    ```bash
    git clone https://your-repository-url/NewsFlowApp.git
    cd NewsFlowApp
    ```
2.  **API Key Configuration:**
    *   Create a file named `local.properties` in the **root** directory of the project (the same level as `settings.gradle.kts`).
    *   Add your NewsAPI key to this file in the following format:
        ```properties
        NEWS_API_KEY=YOUR_ACTUAL_API_KEY_HERE
        ```
    *   **Important:** Ensure `local.properties` is included in your `.gitignore` file to prevent accidentally committing your key.
3.  **Open in Android Studio:**
    *   Open Android Studio (latest stable version recommended).
    *   Select "Open" and navigate to the cloned project directory.
    *   Let Android Studio sync the Gradle files and download dependencies.
4.  **Build and Run:**
    *   Select a target device (emulator or physical device).
    *   Click the "Run" button (▶️) in Android Studio.

---

## 🔧 Configuration

*   **API Key:** Must be provided in `local.properties` as described in the Setup section.
*   **Theme:** Can be changed in the app's Settings screen (System, Light, Dark).
*   **Sync Interval:** The frequency for background headline checks can be adjusted in the Settings screen (1-24 hours).

---

## 🔮 Future Enhancements (Ideas)

*   Implement robust Unit, Integration, and UI Tests.
*   Add filtering options for headlines (e.g., specific categories).
*   Improve error handling and user feedback for network issues.
*   Add pull-to-refresh functionality on headline/bookmark lists.
*   Implement article sharing functionality from the detail screen.
*   Explore using Jetpack Compose for parts or all of the UI.
*   Add ability to add custom RSS feeds.

---

## 📄 License

*This project was created as a learning exercise to demonstrate modern Android development techniques.*