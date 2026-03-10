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

## Form Interface

![Form1](https://github.com/user-attachments/assets/4da33107-1b63-4923-9fda-32dc7343bb04)

![Form2](https://github.com/user-attachments/assets/80c1ae63-1782-4e62-b5ee-7e7e3532046d)

![Form3](https://github.com/user-attachments/assets/38d4ad5b-198e-40ff-8f1c-2df41a1363e6)

![Form4](https://github.com/user-attachments/assets/d9b60aa5-875a-4a2d-913e-23d98a6b4a08)

## Table View

![Table1](https://github.com/user-attachments/assets/5f05316d-107a-407d-938c-60518a88c012)

![Table2](https://github.com/user-attachments/assets/aed8d9fb-b7a6-4d96-8b85-6c386460ee94)

![Table3](https://github.com/user-attachments/assets/22c95715-1ea0-4bd5-bec0-70d31def43bc)

## Settings

![Settings1](https://github.com/user-attachments/assets/a80c343a-7092-4cf8-ad78-c02493ec7f1a)

![Settings2](https://github.com/user-attachments/assets/7b365e0b-673f-4f4c-913f-8caafb475be6)

![Settings3](https://github.com/user-attachments/assets/2588c8dc-4833-4777-8e3e-0658d76fed8c)

![Settings4](https://github.com/user-attachments/assets/5e1eb1a4-7c3c-4f8f-8371-b3d0031a3632)

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

# Project Structure

src/main/java/org/cubicode

Auditacity.java  
SettingsDialog.java  
FieldEditorDialog.java  
DynamicTableModel.java  
EntryRecord.java  
FieldDefinition.java  

ImportData.java  
ExportService.java  
ProfileService.java  
PreferencesService.java  

LanguageManager.java  
ThemeManager.java  
ValidationService.java  

---

# Adding new languages

Auditacity uses Java resource bundles for translations and automatically detects available languages.

Auditacity scans the resources folder for all `messages_*.properties` files and loads them dynamically.

To add a new language:

1. Copy an existing file such as `messages_en.properties`
2. Rename it to `messages_xx.properties` (for example `messages_it.properties`)
3. Translate the values in the file
4. Set the language name using `language.self`

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

Created by Cubicode

GitHub  
https://github.com/Cubiljub
