# 🏨 StaySwift

> A native Android hotel booking application built with Java and Firebase, featuring separate experiences for guests and hotel administrators.

StaySwift is a full-stack Android application that streamlines hotel discovery, room reservations, and hotel management. The project demonstrates modern Android development practices while leveraging Firebase as a complete backend solution for authentication, real-time data synchronization, cloud storage, and push notifications.

---

## ✨ Features

### 👤 Guest Experience

- Secure authentication with Firebase Authentication
- Browse hotels by city
- Search hotels and explore detailed listings
- View amenities, images, and hotel locations
- Browse available room categories
- Book hotel rooms
- View booking history
- Save favourite hotels
- Receive real-time booking notifications
- Submit customer support requests
- Manage user profile

### 🛠 Admin Dashboard

- Manage hotel listings
- Upload hotel images
- Configure hotel amenities
- Add and manage room categories
- Monitor bookings
- Handle customer support requests
- Receive administrative notifications

---

## 🏗 Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java |
| Platform | Android (Min SDK 24) |
| Database | Firebase Realtime Database |
| Authentication | Firebase Authentication |
| Storage | Firebase Storage |
| Notifications | Firebase Cloud Messaging |
| Backend | Firebase Cloud Functions (Node.js) |
| Maps | Google Maps SDK |
| Places | Google Places SDK |
| Location | Google Play Services Location |
| Image Loading | Glide |
| Build System | Gradle (Kotlin DSL) |

---

## 📂 Project Structure

```text
StaySwift/
│
├── app/
│   ├── admin/
│   ├── user/
│   ├── models/
│   ├── LoginActivity.java
│   ├── SignupActivity.java
│   ├── SplashActivity.java
│   ├── GuestHomeActivity.java
│   ├── AdminDashboardActivity.java
│   └── MyFirebaseMessagingService.java
│
├── functions/
│   └── index.js
│
├── firebase.json
├── build.gradle.kts
└── .firebaserc
```

---

## 🔔 Push Notification Flow

Firebase Cloud Functions automatically trigger notifications whenever new records are created.

- **User Notifications**
  - Trigger: `/notifications/{uid}/{notificationId}`
  - Delivered to: `user_{uid}` topic

- **Admin Notifications**
  - Trigger: `/admin_notifications/{notificationId}`
  - Delivered to: `admin` topic

---

## 🚀 Getting Started

### Prerequisites

- Android Studio
- JDK 11
- Firebase Project
- Google Maps API Key
- Google Places API

### Installation

```bash
git clone https://github.com/coderwahaj/StaySwift-Android.git
```

Place your `google-services.json` file inside:

```
app/
```

Add your Google Maps API key.

Sync Gradle and run the application.

To deploy Cloud Functions:

```bash
cd functions
npm install
firebase deploy --only functions
```

---

## 🔐 Permissions

- INTERNET
- ACCESS_NETWORK_STATE
- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION
- POST_NOTIFICATIONS

---

## 📌 Future Improvements

- Online payment integration
- Hotel reviews and ratings
- Booking cancellation policies
- Dark mode
- Offline caching
- Multi-language support

---

## 👨‍💻 Author

**Wahaj Asif**

Software Engineer

If you found this project interesting, consider giving it a ⭐.
