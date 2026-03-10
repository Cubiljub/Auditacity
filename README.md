# Auditacity

![Java](https://img.shields.io/badge/Java-25-blue)
![Platform](https://img.shields.io/badge/platform-Windows-lightgrey)
![License](https://img.shields.io/badge/license-MIT-green)
![Status](https://img.shields.io/badge/status-active-success)

Auditacity is a dynamic ticket tracking and statistics tool built with Java Swing.

The application allows users to create custom fields, track entries in a table, and export data to CSV or Excel. Profiles can be saved and loaded, making it easy to adapt the tool to different workflows.

Auditacity was designed as a lightweight, flexible desktop tool for tracking structured information without needing a full database system.

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

*(Note: Windows may show a security warning because the application is not signed with a commercial code-signing certificate. This is normal for independent open-source software. You can proceed by clicking **More info → Run anyway**.)*

No installation required.

---

# Screenshots

## Form Interface

![Form Screenshot](docs/screenshots/form_placeholder.png)

## Table View

![Table Screenshot](docs/screenshots/table_placeholder.png)

## Settings

![Settings Screenshot](docs/screenshots/settings_placeholder.png)

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

# Building from Source

Requirements:

Java 25  
Maven

Build the project:
