```markdown
# Krishna Foods - Namkeen Ordering App

**Krishna Foods** is a native Android application designed for a **Namkeen Manufacturing Company based in Vita**. This B2B (Business-to-Business) platform enables retailers and shop owners to place bulk orders directly with the manufacturer, streamlining the supply chain.

The project contains two distinct modules within a single app:
1.  **Retailer App:** For shop owners to browse the catalog and place bulk orders.
2.  **Admin Dashboard:** For the manufacturer to manage stock, orders, and advertisements.

## 📱 Features

### For Retailers (User Side)
* **Bulk Ordering:** Browse a categorized catalog of Namkeen products tailored for wholesale.
* **Cash/Pay on Delivery:** Simplified payment flow focused on cash or payment upon delivery (No complex payment gateway integration).
* **Order Tracking:** Real-time status updates (Pending, Accepted, Delivered, Rejected).
* **Profile Management:** Retailers can manage their shop details and contact info.
* **Enquiries & Feedback:** Direct communication channel with the manufacturer.

### For Manufacturer (Admin Side)
* **Order Management:** View, Accept, Reject, and mark orders as Delivered.
* **Inventory Management:** Add, update, or remove Namkeen items and adjust prices.
* **Advertisement Banner:** Update the promotional banner on the user's home screen.
* **User Management:** View retailer details and handle enquiries.
* **Push Notifications:** Send updates to retailers using Firebase Cloud Messaging.

## 🛠 Tech Stack

* **Language:** Kotlin
* **UI:** XML (Layouts & Styles)
* **Backend:** Firebase (Firestore, Authentication, Storage)
* **Notifications:** Firebase Cloud Messaging (FCM)
* **Architecture:** MVVM (Model-View-ViewModel)

## 📂 Project Structure

The code is organized into specific packages for the User and Admin flows:

```text
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

## 🚀 Setup & Installation

1. **Clone the repository**
```bash
git clone [https://github.com/yashajaykadav/FoodOrderingApp.git](https://github.com/yashajaykadav/FoodOrderingApp.git)

```

2. **Firebase Setup (Critical)**
* This app relies on Firebase. You must provide your own `google-services.json`.
* Create a project in the [Firebase Console](https://console.firebase.google.com/).
* Enable **Authentication** (Email/Password), **Firestore Database**, and **Storage**.
* Download `google-services.json` and place it in the `app/` folder.


3. **Build and Run**
* Open the project in **Android Studio**.
* Sync Gradle files.
* Run on an Emulator or Physical Device.
* *Note:* The `LoginActivity` is the entry point. You may need to manually set a flag in the database or code to designate an "Admin" user if you haven't built a separate Admin Login screen.



## 📸 Usage

* **Retailers** simply sign up/login, add items to their cart, and checkout. The payment method is strictly "Pay on Delivery".
* **Admins** use the dedicated Admin Activities to manage the incoming flow of orders.

## Contact

Developed for **Namkeen Manufacturer, Vita**.
For technical support or contributions, please open an issue.

```
