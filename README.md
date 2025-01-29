# Crypto Painting Competition

## Table of Contents
1. [Project Overview](#project-overview)
2. [Technologies Used](#technologies-used)
3. [System Features](#system-features)
4. [Installation and Setup](#installation-and-setup)
5. [Usage Instructions](#usage-instructions)
6. [Database Structure](#database-structure)
7. [File Structure](#file-structure)
8. [Future Improvements](#future-improvements)

## Project Overview
The **Crypto Painting Competition** is a secure digital art competition platform where contestants submit encrypted paintings, a jury evaluates them using blind signatures, and the competition president verifies and announces the winners. This system ensures fair evaluation, cryptographic integrity, and security of submitted artworks.

## Technologies Used
- **Programming Language:** Java (JDK 22)
- **Graphical User Interface:** JavaFX
- **Cryptographic Libraries:** Bouncy Castle
- **Database Management:** MySQL
- **Security Mechanisms:**
  - RSA for encryption and blind signatures
  - AES-GCM for artwork encryption
  - ECDSA for digital signatures

## System Features
- **User Roles:**
  - **Contestants:** Encrypt and submit digital paintings.
  - **Judges:** Evaluate and rate the paintings using blind signatures.
  - **President:** Verifies the jury's evaluation and announces winners.
- **Secure Artwork Submission:** Paintings are encrypted before submission.
- **Blind Signature-Based Voting:** Judges evaluate paintings without revealing their identity to the president.
- **Winner Selection:** The president selects the top three winners based on verified evaluations.
- **Login System:** Users authenticate based on their assigned roles.

## Installation and Setup
### Prerequisites
- **Java 22 SDK** installed.
- **MySQL Database** setup.
- **Bouncy Castle JAR file** in the `lib/` directory.

### Steps
1. Clone the repository:
   ```sh
   git clone https://github.com/your-repo/Crypto-Painting-Competition.git
   ```
2. Add Bouncy Castle to the `lib/` directory.
3. Configure the database connection in `DatabaseManager.java`.
4. Compile and run the project:
   ```sh
   javac -cp lib/bcprov-jdk15on.jar:. src/*.java
   java -cp lib/bcprov-jdk15on.jar:. src.LoginScreen
   ```

## Usage Instructions
1. **Login** as a Contestant, Judge, or President.
2. **Contestants** generate key pairs, encrypt and upload their artwork.
3. **Judges** evaluate paintings and submit blind-signed ratings.
4. **The President** verifies ratings and selects winners.
5. **The system announces** the top three winners based on total scores.

## Database Structure
The database consists of the following key tables:
- **Users:** Stores login credentials and roles.
- **Paintings:** Stores encrypted paintings and metadata.
- **Evaluations:** Stores jury ratings and blind signatures.
- **VerifiedEvaluations:** Tracks validated evaluations.

## File Structure
```
Crypto-Painting-Competition/
│── src/
│   ├── LoginScreen.java
│   ├── DatabaseManager.java
│   ├── ArtistWindow.java
│   ├── JudgeWindow.java
│   ├── PresidentWindow.java
│   ├── BlindSignature.java
│   ├── AESGC.java
│   ├── RSA.java
│   ├── ECDSA.java
│   ├── StarRatingApp.java
│── lib/ (Bouncy Castle JAR file)
│── README.md
```

## Future Improvements
- Implement additional validation mechanisms for enhanced security.
- Improve the user interface with enhanced graphics and animations.
- Optimize database queries for better performance.

