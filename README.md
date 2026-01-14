# Krishna Foods - Namkeen Ordering App

Krishna Foods is a native Android application built for a Namkeen manufacturer in Vita. This B2B (business-to-business) app lets retailers and shop owners place bulk orders directly with the manufacturer, simplifying wholesale ordering and inventory management.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Firebase Setup (Required)](#firebase-setup-required)
- [Build & Run](#build--run)
- [Usage](#usage)
- [Admin Setup](#admin-setup)
- [Contributing](#contributing)
- [License & Contact](#license--contact)

## Features

### Retailer (User) side
- Bulk ordering: Browse categorized Namkeen catalog for wholesale.
- Pay on Delivery: Simple cash/Pay on Delivery flow (no payment gateway integration).
- Order status updates: Pending, Accepted, Delivered, Rejected.
- Profile management: Update shop details and contact info.
- Enquiries & feedback: Contact manufacturer from the app.

### Manufacturer (Admin) side
- Order management: View, accept/reject, and mark orders as delivered.
- Inventory management: Add, update, or remove items and manage prices.
- Promotional banner: Update advertisement banner shown on the user home screen.
- User & enquiry management: View retailer details and respond to enquiries.
- Push notifications: Notify retailers using Firebase Cloud Messaging (FCM).

## Tech Stack

- Language: Kotlin
- UI: Android XML layouts
- Backend: Firebase (Cloud Firestore, Authentication, Storage)
- Push Notifications: Firebase Cloud Messaging (FCM)
- Architecture: MVVM

## Project Structure

```
com.foodordering.krishnafoods
├── admin             # Manufacturer/Admin specific code
│   ├── activity      # ManageItems, OrdersActivity, etc.
│   ├── adapter       # Admin-specific list adapters
│   └── message       # Firebase Messaging Service
├── user              # Retailer specific code
│   ├── activity      # MainActivity, Cart, OrderConfirmation
│   ├── fragment      # Menu, Cart, Orders fragments
│   └── adapter       # User-specific list adapters
└── core              # Shared utilities and repositories
```

## Prerequisites

- Android Studio (latest stable) with Android SDK
- Kotlin support
- A Firebase project with Firestore, Authentication and Storage enabled

## Firebase Setup (Required)

1. Create a project in the Firebase Console: https://console.firebase.google.com/
2. Enable Authentication (Email/Password), Cloud Firestore, and Storage.
3. Register your Android app in the Firebase project and download `google-services.json`.
4. Place `google-services.json` in the `app/` directory of the project.
5. If you use FCM, include the required setup for Firebase Cloud Messaging.

Important: The app expects certain collections/documents in Firestore (orders, products, users, etc.). Seed data or specific flags (for example an Admin user flag) may be required for initial testing — see the Admin Setup section.

## Build & Run

1. Clone the repository:

```bash
git clone https://github.com/yashajaykadav/FoodOrderingApp.git
cd FoodOrderingApp
```

2. Open the project in Android Studio and let Gradle sync.
3. Ensure `google-services.json` is present in the `app/` folder.
4. Build and run the app on an emulator or physical device.

Entry point: `LoginActivity` (the app currently opens to the login screen). Some admin functionality relies on a database flag to identify Admin users.

## Usage

- Retailers: Sign up or log in, browse the catalog, add items to the cart, and place bulk orders with Pay on Delivery as the default payment option.
- Admin: Use the Admin activities to manage products, view incoming orders, update order statuses, and update the promotional banner.

## Admin Setup

If an Admin login screen is not provided, mark the appropriate user document in Firestore with an `isAdmin` (or similarly named) boolean flag to grant admin privileges. Inspect the code (`admin` and `core` packages) to find the exact field name expected by the app.

## Contributing

Contributions, bug reports, and feature requests are welcome. Please open an issue describing the change you'd like to make. If you submit code changes, please include a clear description and test steps.

## License & Contact

This project was developed for a Namkeen Manufacturer in Vita.

For technical support or to contribute, please open an issue on the repository: https://github.com/yashajaykadav/FoodOrderingApp/issues

---

(README updated to improve clarity, structure, and instructions.)
