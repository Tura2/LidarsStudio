# Lidar’s Studio – Appointment Management App

**Where nails meet ink** – Android application for managing appointments in a tattoo and gel nails studio.  
Developed as part of the Mobile Apps course.

---

## 📋 Overview
Lidar’s Studio replaces manual appointment scheduling with a smart, real-time management system.  
The app allows customers to browse available times, book or cancel appointments, and view a gallery for inspiration.  
Managers can approve/decline bookings, manage availability, and update the gallery – all in one place.

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
- **Server Time Synchronization** – All bookings use `serverTimestamp` for accurate timing
- **Firebase Functions** – Automatic email sent to manager when a tattoo request is submitted
- **Glide Image Loading** – Efficiently loads images with caching and placeholders
- **Material Design** – Modern UI components for a polished look
- **User Authentication** – Secure login and registration via Firebase Authentication
- **Real-time Data** – Uses Cloud Firestore for instant updates across devices
- **Gallery Management** – Upload and categorize images for easy browsing


## 🛠 Technologies
- **Firebase Authentication** – User login and registration
- **Cloud Firestore** – Real-time data storage for users, appointments, and gallery
- **Firebase Storage** – Storing and loading gallery images
- **Firebase Functions** – Server-side logic for emails and server time
- **Glide** – Efficient image loading with placeholders and caching
- **Material Design** – Modern UI components

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
