# Auditacity

![Java](https://img.shields.io/badge/Java-25-blue)
![Platform](https://img.shields.io/badge/platform-Windows-lightgrey)
![License](https://img.shields.io/badge/license-MIT-green)
![Status](https://img.shields.io/badge/status-active-success)
![Download](https://img.shields.io/github/v/release/Cubiljub/Auditacity?label=Download)

Auditacity is a dynamic ticket tracking and statistics tool built with Java Swing.

The application allows users to create custom fields, track entries in a table, and export data to CSV or Excel. Profiles can be saved and loaded, making it easy to adapt the tool to different workflows.

Auditacity was designed as a lightweight, flexible desktop tool for tracking structured information without requiring a full database system.

---

# Features

- Dynamic field system (create your own fields)
- Field profiles (save and load configurations)
- Drag & drop field ordering
- CSV and Excel import
- CSV and Excel export
- Database-friendly export mode
- Table search and sorting
- Light / Dark / System theme
- Multi-language support  
  - English  
  - German  
  - Serbian
  - French
  - Russian
- Executable Windows build (.exe)

---

# Download

You can download the latest executable here:

Latest Release  
https://github.com/Cubiljub/Auditacity/releases/latest

Download the file:

Auditacity.exe

(Note: Windows may show a security warning because the application is not signed with a commercial code-signing certificate. This is normal for independent open-source software. You can proceed by clicking **More info → Run anyway**.)

No installation required.

---

# Screenshots


![Form4](https://github.com/user-attachments/assets/d9b60aa5-875a-4a2d-913e-23d98a6b4a08)

![Table1](https://github.com/user-attachments/assets/5f05316d-107a-407d-938c-60518a88c012)

![Settings3](https://github.com/user-attachments/assets/2588c8dc-4833-4777-8e3e-0658d76fed8c)

---

# How It Works

Auditacity uses a dynamic data model:

1. Fields define the structure of the data
2. Entries store the values
3. Profiles save the field configuration

This allows the tool to adapt to different use cases such as:

- Call center ticket tracking
- CRM notes
- Support tracking
- Task logging
- Structured data collection 

---

# Adding new languages

Auditacity uses Java resource bundles for translations.

Available languages are defined in the `languages.properties` file located in the resources folder.

To add a new language:

1. Copy an existing translation file such as `messages_en.properties`
2. Rename it to `messages_xx.properties` (for example `messages_it.properties`)
3. Translate the values in the file
4. Add the language code to `languages.properties`

Example:

languages=en,de,sr,fr,ru,it

Each language file must contain a `language.self` entry which defines how the language appears in the user interface.

Example:

language.self=Italiano

After restarting the application, the new language will automatically appear in the language selection menu.

---

# Building from Source

Requirements:

Java 25  
Maven

Build the project:

mvn clean package

The executable JAR will be created in:

target/Auditacity.jar

---

# Creating the Windows EXE

Auditacity can be packaged into a Windows executable using Launch4j.

Launch4j is an open-source tool that wraps a Java application into a native Windows executable.

Download Launch4j here:  
https://launch4j.sourceforge.net/

Steps:

1. Build the JAR with Maven

mvn clean package

2. Open Launch4j and select the generated JAR file

target/Auditacity.jar

3. Set the header type to:

GUI

4. Build the executable

Output:

Auditacity.exe

---

# Roadmap

Future improvements may include:

- Advanced filtering
- Additional field types (number, date)
- Plugin system
- Automatic updates
- Linux and macOS builds

---

# License

This project is licensed under the MIT License.

See the LICENSE file for details.

---

# Author

Created by CubiCode

GitHub  
https://github.com/Cubiljub
