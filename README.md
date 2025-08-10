# Lidar’s Studio – Appointment Management App

**Where nails meet ink** – Android application for managing appointments in a tattoo and gel nails studio.  
Developed as part of the Mobile Apps course at Afeka College.

---

## 📋 Overview
Lidar’s Studio replaces manual appointment scheduling with a smart, real-time management system.  
The app allows customers to browse available times, book or cancel appointments, and view a gallery for inspiration.  
Managers can approve/decline bookings, manage availability, and update the gallery – all in one place.

---

## 📄 Main Pages
- **Home** – Branded header, image slider, quick link to booking and gallery.
- **Login / Signup** – Firebase Auth for authentication, Firestore for profile storage.
- **Book Appointment** – Choose service, date, and time; load real-time availability; sync with server time.
- **Customer Profile** – Personal details, upcoming appointment, appointment history.
- **Manager Profile** – Add/remove availability, cancel or mark appointments as completed.
- **Gallery** – Filter by Nails / Tattoos, load images from Firebase Storage with Glide.
- **Price List** – Full service price list.
- **Tattoo Request** – Form with up to 3 images, upload to Storage, automatic email to owner.
- **About** – Business details, navigation links, and social media.

---

## ✨ Features

### Customer
- 📅 View real-time availability and book appointments
- 💅 Choose between tattoo or gel nails services
- ↔ Cancel or modify existing appointments
- 🖼 Browse categorized image gallery for inspiration
- 👤 View active and past appointments in profile

### Manager
- ✅ Approve or decline appointment requests
- ⛔ Block times/days (permanent or temporary)
- 🖼 Upload images to the gallery
- 📅 Manage all studio appointments from one interface

### Unique Implementations
- **Role-based navigation** – Different flows for customers and managers
- **Smart availability management** – Add/block time slots, auto-remove taken slots
- **Server Time Synchronization** – All bookings use `serverTimestamp` for accurate timing
- **Firebase Functions** – Automatic email sent to manager when a tattoo request is submitted
- **Glide Image Loading** – Efficient image loading with caching and placeholders
- **Material Design** – Modern UI components for a polished look
- **User Authentication** – Secure login and registration via Firebase Authentication
- **Real-time Data** – Uses Cloud Firestore for instant updates across devices
- **Gallery Management** – Upload and categorize images for easy browsing

---

## 🛠 Technologies
- **Kotlin / Android**, ViewBinding, Material Components
- **Firebase**: Authentication, Firestore, Storage, Functions
- **Glide** – Efficient image loading with placeholders and caching
- **OkHttp** – Cloud Run HTTP calls for server logic
- **Lottie** – JSON animations for status screens
- **Material Design** – Modern UI components

---

## 🚀 Possible Issues
- No internet – booking or gallery loading may fail
- Slot already taken – blocked server-side with an error message
- Wrong login details – Firebase Auth error displayed

---


## 📂 Project Structure
```
app/
 ├── adapters/        # RecyclerView adapters
 ├── utils/           # Data classes and helper utilities
 ├── activities/      # All app screens (Activities)
 ├── res/             # Layouts, drawables, values
 └── functions/       # Firebase Cloud Functions (Node.js)
```

---

## 🚀 Installation
1. Clone this repository:
   ```bash
   git clone https://github.com/Tura2/LidarsStudio.git
   ```
2. Open in **Android Studio**.
3. Add your own `google-services.json` from Firebase Console.
4. Run the project on an emulator or physical device.


This project was developed for academic purposes as part of a Mobile Apps course at Afeka College.
