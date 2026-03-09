# EduQuiz 🎓

EduQuiz is a cross-platform, local-network assessment system built with Kotlin Multiplatform (KMP) and Compose Multiplatform. It is designed to modernize classroom quizzes by bridging a powerful desktop management system with a lightweight, interactive mobile experience for students.

The application operates entirely on your local network, meaning no internet connection or external cloud database is required to run live quizzes!

## ✨ Key Features

👨‍🏫 Desktop App (Teacher / Admin Portal)
The desktop application serves as both the teacher's dashboard and the central backend server for the classroom.

  Embedded Ktor Server:* Automatically runs a local backend server to securely handle all student requests and database operations.
	
  Student Management:* Register and manage enrolled students in a local SQLite database.
	
  Question Bank Builder:* Create multiple-choice questions with customized options, correct answers, and individual time limits.

  Activity Hosting:* Bundle questions into live "Activities" (quizzes), assign specific students, and launch them in real-time.
	
  QR Code Integration:* Automatically generates large QR codes on the screen for students to scan and connect instantly.

#📱 Android App (Student Companion)

The Android application is a lightweight client designed for speed and ease of use.
	
  QR Code Scanner:* Built-in scanner allows students to simply point their camera at the teacher's screen to join the local network session instantly.

  Live Assessments:* Students pull quiz data from the teacher's desktop server, answer questions within the allotted time, and securely submit their results back to the desktop app.

## 🛠️ Tech Stack
  Framework:* Kotlin Multiplatform (KMP) & Compose Multiplatform
	UI:* Jetpack Compose (Android) / Compose for Desktop
	Networking & Backend:* Ktor (Embedded Server & Client)
	Database:* SQLDelight (SQLite)
	Architecture:* MVVM



## 💻 Getting Started (Installation & Setup)

To run this project locally, you will need a basic Kotlin Multiplatform development environment set up.

### Prerequisites
* [Android Studio](https://developer.android.com/studio) (Recommended) or IntelliJ IDEA (with the Android plugin installed).
* **JDK 17** or higher.
* An Android Emulator or physical Android device (to test the student app).

### 1. Clone the Repository
Open your terminal and run the following command to download the project to your computer:

``bash
git clone [https://github.com/Sualden/Eduquiz.git](https://github.com/Sualden/Eduquiz.git)
cd Eduquiz

2. Open the Project
    1. Open Android Studio or IntelliJ IDEA.
    2. Click Open and select the Eduquiz folder you just cloned.
    3. Wait a few minutes for the IDE to sync and Gradle to download all the necessary dependencies.
       
3. Running the Desktop App (Admin / Server)
The Desktop app automatically starts the embedded Ktor backend, so you must run this first!
    • Option A (Via IDE): Open the project panel on the left, navigate to src/jvmMain/kotlin/com/dens/eduquiz/main.kt. Click the green Play button next to the main() function.
    • Option B (Via Terminal): Run the following Gradle command in your IDE's terminal:

   ``bash
      ./gradlew run
(Note: On the very first launch, it will automatically create the AppDatabase.db file to store your local data).

5. Running the Android App (Student)
Make sure the Desktop app is running and your Android device/emulator is connected to the same local network (Wi-Fi) as your computer.
    • Via IDE: In the top toolbar of Android Studio, select the androidApp or composeApp run configuration, choose your connected device/emulator, and click the green Play button.
    • Via Terminal: ```bash ./gradlew installDebug
      
🔑 Default Admin Login
When you run the Desktop application for the first time, use these default credentials to log in:
    • Username: admin
    • Password: admin123
