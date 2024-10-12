# **University Attendance Management System**
## _A Firebase-powered Android app for managing university attendance_

The University Attendance Management System is an Android application designed to simplify and digitize attendance tracking within a university faculty. It uses Firebase Realtime Database to store and manage attendance data securely, while providing lecturers and students with a smooth and efficient experience.This project was created to replace the traditional manual attendance process with a modern, automated solution. By integrating student ID scanning, personalized notifications, and real-time cloud storage, it ensures that attendance is accurate, transparent, and easily accessible.
This project was developed as part of a university requirement to provide a digital attendance management solution for lecturers and students. It eliminates manual errors, speeds up the process of taking attendance, and ensures records are securely stored and easily retrievable. By allowing CSV file uploads for custom student messages, integrating ID scanning with the front camera, and enabling CSV exports of attendance lists, this app ensures both lecturers and students have a seamless experience.
* ### Features –
 #### 📚 Class Creation
Lecturers can create new classes by entering essential details such as class name, class date, start time, end time, and lecturer name. Once created, the app generates a unique Class ID for the session. This ID is later used for managing or updating class records.
#### 📩 Custom Student Messages
A unique feature of this app is the ability for lecturers to upload a CSV file containing student IDs and personalized messages. During the attendance process, when a student scans their ID, any special message linked to their ID will be displayed on the screen. This helps lecturers provide important instructions or feedback directly to specific students.
### 📷 ID Scanning with Front Camera
The app uses the device’s front camera to scan student university IDs. This makes the attendance marking process fast and eliminates the need for manual entry.
#### ✅ Attendance Marking
Once the ID is scanned and verified, students can click the “Mark Attendance” button. This instantly records their attendance in the Firebase Realtime Database, ensuring that all records are stored securely and in real time.
#### 📤 Export and View Attendance
Lecturers can download the attendance list as a CSV file for record-keeping or analysis. They can also view the full attendance list directly from the app’s interface without needing to export it.
#### 🔐 Lecturer Accounts
Every lecturer must create an account to use the system. The account creation process requires the use of a university email address, ensuring only authorized faculty members gain access. The dashboard displays the lecturer’s name and email for verification and personalization.
#### ✏️ Data Management with Class ID
If a lecturer needs to modify any class details (such as date, time, or name), they can do so using the unique Class ID. This ensures data integrity and prevents accidental changes to unrelated classes.

* ### Project Demo captures -
  
<img width="856" height="592" alt="Screenshot 2025-09-10 204648" src="https://github.com/user-attachments/assets/96de50b7-eeb2-4081-89e2-18ef87cfb7bb" />
<img width="837" height="582" alt="Screenshot 2025-09-10 204737" src="https://github.com/user-attachments/assets/67fae666-b54d-49c3-aa6f-bbe14b5d78ff" />
<img width="832" height="582" alt="Screenshot 2025-09-10 204813" src="https://github.com/user-attachments/assets/6bf54836-4a5d-4fe7-97ac-f13f7786c3a4" />

* ### Project Smaple CSV file and Features -
[Attendance_Management_System.csv](https://github.com/user-attachments/files/22258665/Attendance_Management_System.csv)

[Attendance_Management_System.pdf](https://github.com/user-attachments/files/22258704/Attendance_Management_System.pdf)