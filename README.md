# Nukepad

<img width="160" height="220" alt="nukepadlogo" src="https://github.com/user-attachments/assets/3efb7d56-06d3-4e39-b540-cb47c4196587" />

> A no-nonsense text editor built from scratch in Java Swing — because it just works.

---

## What is Nukepad?

Nukepad is an alpha-stage desktop text editor written in Java, built on top of the **TEDitor** architecture and progressively expanded with real cool features. 

It's not trying to replace your IDE (and it won't, really). It's a passion (vibecoded) project that's getting better and better.

---

## Features

- 📄 **Tabbed editing** — open multiple files at once with closable tabs
- 🎨 **Syntax highlighting** — powered by RSyntaxTextArea, supporting Java, Python, JavaScript, C, C++, C#, XML, HTML and more
- 🧠 **Language-based autocomplete** — context-aware completions for common keywords
- 🌳 **File tree** — browse your project structure from the sidebar
- 🔍 **Search** — search files from the home directory
- 📁 **Project / folder opening** — open entire directories as projects
- 🕐 **Recent files** — quick access to your last 8 opened files/folders
- 🗂️ **Categories panel** — organize files into custom named groups
- 🌙 **Dark & Light theme** — toggle between FlatDarcula and FlatIntelliJ themes, persisted across sessions
- 🔔 **Intro screen** — welcome screen with chime sound and quick-open buttons
- ✏️ **Compile & Run** — basic Java compile and run support
- 🖨️ **Print** — print the current file directly

---

## Updates

### v0.1.3

- Fixed intro screen delay — chime sound now loads on a background thread instead of blocking the UI
- Fixed editor startup delay — FileTree, SearchPanel and buildCategoriesPane` now load asynchronously via SwingWorker, showing the editor instantly
- Fixed hardcoded C:\Users path in SearchPanel, now uses the system home directory (cross-platform fix)
- Migrated to FlatLaf (FlatDarcula / FlatIntelliJ) for a modern look and feel
- Added dark/light theme switcher to the editor View menu
- Added Monokai syntax theme for dark mode and IntelliJ syntax theme for light mode via RSyntaxTextArea's built-in theme system
- Added theme toggle button to the intro screen
- Theme choice is now persisted across sessions via ThemeManager (saved to ~/.nukepad_theme.txt)
- Fixed SearchPanel colors to follow the active theme
- Fixed IntroScreen hardcoded white backgrounds to respect the active theme

### v0.1.2
- Reorganized the three sidebar categories into irremovable tabs on the left
- Added an intro screen with chimes and functions to open files and projects
- Optimized file loading
- Added search function (searches home directory)
- Added opening projects
- Buttons now look more glassy
- Added dark/light theme switcher with session persistence

### v0.1.0
- Updated file tree to sort folders and files uniformly
- Added closable tabs
- Added language-based autocomplete & syntax highlighting (limited language support)
- Changed the placeholder button to an "Author's Signature" button linking to the author's GitHub
- Separated the Compile and Run buttons

---

## Additional Info

- Built on top of the existing **TEDitor** architecture — parts of pre-alpha code are still present
- Still in **alpha** — far from a release, don't expect one anytime soon
- Want to contribute? Leave a suggestion in the issues tab

---

## Running the Project

1. Clone the repo
2. Open in your IDE (IntelliJ, NetBeans, Eclipse, etc.)
3. Make sure you're using a **JDK** (not a JRE) — required for the Compile feature
4. Build and run `Nukepad.java` (entry point is `main` in the `Nukepad` class)

---

## Dependencies

- [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) — syntax highlighting & editor core
- [AutoComplete](https://github.com/bobbylight/AutoComplete) — autocomplete support
- [FlatLaf](https://github.com/JFormDesigner/FlatLaf) — modern Swing look and feel

---

## Author

Made by [@alexandru-andoni](https://github.com/alexandru-andoni)


