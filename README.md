# MatuleMe

![GitHub release (latest by date)](https://img.shields.io/github/v/release/takeshikodev/MatuleMe)
![Downloads](https://img.shields.io/github/downloads/takeshikodev/MatuleMe/total)

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technologies](#technologies)
- [Architecture](#architecture)
- [Installation](#installation)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Performance Optimizations](#performance-optimizations)
- [Testing](#testing)
- [Contributing](#contributing)
- [Roadmap](#roadmap)
- [License](#license)
- [Contact](#contact)

## Overview

**MatuleMe** is a modern non-commerce Android application built with Kotlin. It provides a seamless shopping experience with features like product browsing, cart management, checkout process, user authentication, and order tracking.

The application follows clean architecture principles with a focus on performance, user experience, and maintainability. It integrates with Supabase backend services for data management and authentication.

### Target Audience
- Fashion enthusiasts looking for a streamlined shopping experience
- Customers who value intuitive UX and responsive design
- Users seeking a reliable platform for tracking orders and managing payments

## Features

### User Management

- **Authentication**: Complete login and registration flows with email/password
- **Social Authentication**: Integration with Google and Facebook login (coming soon)
- **Profile Management**: Update personal information, preferences, and avatar
- **OTP Verification**: Secure authentication through one-time passwords sent via email
- **Password Reset**: Self-service password recovery flow

### Shopping Experience

- **Product Discovery**: Browse products by categories, featured collections, and trending items
- **Search Functionality**: Find products quickly with robust search including filters for:
  - Price range
  - Category
  - Brand
  - Size
  - Color
  - Availability
- **Product Details**: Comprehensive product information including:
  - High-resolution images with zoom capability
  - Size charts
  - Material information
  - Care instructions
  - Related products
- **Best Seller Tags**: Highlighting popular products with visual indicators
- **Wishlist**: Save favorite items for future reference

### Shopping Cart & Checkout

- **Cart Management**: Add, remove, and update quantities with real-time price updates
- **Guest Checkout**: Complete purchases without registration
- **Multiple Payment Methods**: Support for various payment cards including:
  - Credit/Debit cards
  - Saved cards for registered users
  - Card validation with secure entry
- **Address Management**: Save and select from multiple delivery addresses with mapping integration
- **Order Tracking**: Monitor orders from placement to delivery with status updates
- **Order History**: View past purchases and reorder functionality
- **Discount Codes**: Apply promotional codes at checkout

### User Interface

- **Onboarding Experience**: Introduction screens for first-time users with feature highlights
- **Intuitive Navigation**: Well-organized menus and navigation patterns using bottom navigation and tabs
- **Responsive Design**: Adapts to different screen sizes and orientations
- **Custom Animations**: Smooth transitions between screens, loading states, and interactive elements
- **Dark Mode Support**: Automatic and manual theme switching

## Technologies

### Development

- **Kotlin 1.8**: Modern language for Android development
- **Gradle 8.0**: Build automation tool with Kotlin DSL for build scripts
- **Android Jetpack**:
  - AppCompat: Backward compatibility
  - ConstraintLayout: Complex layouts with flat hierarchy
  - RecyclerView: Efficient list rendering
  - ViewPager2: Swipeable views
  - ViewModel/LiveData: UI state management with lifecycle awareness
  - Room 2.5**: Local database storage for offline capabilities
  - Navigation Component 2.6**: In-app navigation with safe args
  - DataStore: Preferences storage with type safety
  - WorkManager: Background processing for order sync

### Backend Integration

- **Supabase**: Backend-as-a-Service platform for:
  - User authentication and management
  - PostgreSQL database integration
  - Storage for product images
  - Real-time updates via websockets
- **Retrofit 2.9**: HTTP client for API communication
- **OkHttp 4.10**: HTTP client for low-level operations
- **Kotlin Serialization**: JSON parsing and serialization
- **Kotlin Coroutines**: Asynchronous programming
- **Kotlin Flow**: Data stream processing with reactive programming

### UI Components

- **Material Components 1.8**: Google's Material Design implementation
- **Coil 2.2**: Image loading and caching
- **Custom Views**: Enhanced user experience with specialized components:
  - Rating view
  - Quantity selector
  - Price display with discount calculation
  - Expandable text areas
- **Animations**: 
  - Lottie: Vector animations for loading and empty states
  - Motion Layout: Complex transitions between UI states
  - Property Animations: Element transformations

### Testing

- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework for dependencies
- **Espresso**: UI testing
- **Robolectric**: Unit tests that run on JVM
- **Turbine**: Testing for Flow
- **MockWebServer**: Testing HTTP requests

## Architecture

MatuleMe follows the Clean Architecture pattern with three main layers:

1. **Presentation Layer** (UI): 
   - UI components (Activities, Fragments)
   - ViewModels with UI state
   - UI models and mappers

2. **Domain Layer** (Business Logic):
   - Use cases (user stories)
   - Domain models (entities)
   - Repository interfaces
   - Business rules

3. **Data Layer** (Data Access):
   - Repository implementations
   - Remote data sources (API clients)
   - Local data sources (Room database, DataStore)
   - Data models and mappers

This architecture ensures:
- **Separation of Concerns**: Each component has a specific responsibility
- **Testability**: Easier to write unit and integration tests with clear boundaries
- **Maintainability**: Easier to understand and modify code with clear dependencies
- **Scalability**: Flexibility to add new features without affecting existing code
- **Dependency Rule**: Inner layers don't depend on outer layers, dependencies point inward

### Dependency Injection

The application uses Koin for dependency injection, which provides:
- Constructor injection for ViewModels
- Singleton management for repositories
- Module organization by feature
- Testability through module overrides

## Installation

### Prerequisites

- Android Studio Arctic Fox (2020.3.1) or newer
- JDK 11 or higher
- Android SDK API 33 (minimum API 24)
- Supabase account for backend services

### Setup

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/takeshikodev/MatuleMe.git
   cd MatuleMe
   ```

2. **Configure Supabase:**
   - Create a `.env` file in the project root
   - Add your Supabase credentials:
     ```
     SUPABASE_URL=your_supabase_url
     SUPABASE_KEY=your_supabase_key
     ```

3. **Build the Project:**
   ```bash
   ./gradlew build
   ```

4. **Run the App:**
   - Open the project in Android Studio
   - Connect a device or use an emulator
   - Click the "Run" button

## Configuration

### Supabase Setup

1. Create a Supabase project
2. Set up the following tables:
   - Users (managed by Supabase Auth)
   - Products
   - Categories
   - Orders
   - OrderItems
   - Addresses
   - PaymentMethods

### Environment Variables

Create a `local.properties` file with:

```properties
supabase.url=your_supabase_url
supabase.key=your_supabase_key
```

### Build Variants

The app includes:
- **debug**: Development build with logging
- **staging**: Testing environment with mock data
- **release**: Production-ready build with optimizations

## Project Structure

```
matuleme/
├── app/                     # Main application module
│   ├── src/                 # Source code
│   │   ├── main/            # Main source set
│   │   │   ├── java/        # Kotlin/Java code
│   │   │   │   └── ru/takeshiko/matuleme/
│   │   │   │       ├── data/          # Data layer
│   │   │   │       │   ├── remote/     # API clients
│   │   │   │       │   ├── local/      # Local storage
│   │   │   │       │   ├── model/      # Data models
│   │   │   │       │   └── repository/ # Repository implementations
│   │   │   │       ├── domain/        # Domain layer
│   │   │   │       │   ├── model/      # Entity models
│   │   │   │       │   ├── usecase/    # Use cases
│   │   │   │       │   └── repository/ # Repository interfaces
│   │   │   │       ├── presentation/  # UI layer
│   │   │   │       │   ├── cart/       # Cart functionality
│   │   │   │       │   ├── category/   # Category browsing
│   │   │   │       │   ├── checkout/   # Checkout process
│   │   │   │       │   ├── login/      # Authentication
│   │   │   │       │   ├── main/       # Main screen
│   │   │   │       │   ├── product/    # Product details
│   │   │   │       │   └── ...         # Other UI components
│   │   │   │       ├── di/            # Dependency injection
│   │   │   │       ├── util/          # Utilities
│   │   │   │       └── MatuleMeApp.kt # Application class
│   │   │   ├── res/         # Resources
│   │   │   │   ├── drawable/  # Images and drawables
│   │   │   │   ├── layout/    # XML layouts
│   │   │   │   ├── values/    # Strings, colors, styles
│   │   │   │   └── ...        # Other resources
│   │   │   └── AndroidManifest.xml # App manifest
│   │   ├── test/             # Unit tests
│   │   └── androidTest/      # Instrumentation tests
│   ├── build.gradle.kts      # App module build script
├── build.gradle.kts          # Project build script
├── settings.gradle.kts       # Project settings
└── README.md                 # Project documentation
```

## API Documentation

### Supabase Endpoints

The application interacts with the following Supabase resources:

| Endpoint | Description |
|----------|-------------|
| `/auth/v1` | Authentication APIs |
| `/rest/v1/products` | Product management |
| `/rest/v1/categories` | Categories management |
| `/rest/v1/orders` | Orders management |

### Sample API Calls

```kotlin
// Fetch products by category
suspend fun getProductsByCategory(categoryId: String): List<Product> {
    return supabaseClient
        .from("products")
        .select("*") { filter { eq("category_id", categoryId) }
        .order("created_at", OrderDirection.DESCENDING)
        .execute()
        .data
}
```

## Performance Optimizations

- **Image Caching**: Coil image loader with memory and disk cache
- **View Recycling**: Efficient RecyclerView implementations
- **Lazy Loading**: Load content as needed with paging
- **Offline Support**: Room database for local data persistence
- **Background Processing**: WorkManager for long-running tasks
- **Memory Management**: Weak references for disposable resources

## Testing

### Unit Tests

Run unit tests with:
```bash
./gradlew test
```

### UI Tests

Run instrumentation tests with:
```bash
./gradlew connectedAndroidTest
```

### Code Coverage

Generate coverage reports with:
```bash
./gradlew jacocoTestReport
```

## Contributing

We welcome contributions to improve MatuleMe! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

Follow the project's code style:
- Kotlin style guide
- 100 character line limit
- Documentation for public APIs
- Use of extension functions for utility methods

## Roadmap

### Upcoming Features

- **v1.1**: Social authentication integration
- **v1.2**: Wishlist functionality
- **v1.3**: Push notifications for order updates
- **v2.0**: AR try-on for select products

## License

This project is licensed under the MIT License - see the LICENSE file for details.
