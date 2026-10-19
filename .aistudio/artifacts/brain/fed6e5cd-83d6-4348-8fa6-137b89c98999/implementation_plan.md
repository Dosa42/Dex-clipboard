# Implementation Plan - DeX-Optimized Floating Clipboard Manager

An advanced, Samsung DeX compatible floating clipboard app with persistent Room local storage, auto clipboard capture, favorite tagging, search/filtering, and quick-copy/drag-and-drop workflows.

## User Review Required

> [!IMPORTANT]
> - **Floating Window & DeX Compatibility**: The app is structured as a compact floating panel suited for Samsung DeX multitasking desktop mode.
> - **Clipboard Service / Monitoring**: Automatically captures copied text.
> - **Persistent Room Database**: Stores full clipboard history locally across device restarts.

## Proposed Architecture

### 1. Data Layer (Room Persistence)
- **Entity (`ClipItem`)**:
  - `id`: Long (PrimaryKey, autoGenerate)
  - `text`: String
  - `timestamp`: Long
  - `isFavorite`: Boolean
  - `tags`: String (comma separated)
  - `sourceApp`: String
- **DAO (`ClipDao`)**:
  - `getAllClips(): Flow<List<ClipItem>>`
  - `getFavorites(): Flow<List<ClipItem>>`
  - `searchClips(query: String): Flow<List<ClipItem>>`
  - `insertClip(item: ClipItem): suspend`
  - `updateClip(item: ClipItem): suspend`
  - `deleteClip(id: Long): suspend`
  - `clearAll(): suspend`
- **Database (`ClipboardDatabase`)**: Room database singleton.
- **Repository (`ClipRepository`)**: Abstracts DAO operations.

### 2. Clipboard Monitoring & ViewModel
- **ClipboardViewModel**: Manages clipboard state, search query, selected tab (All vs Favorites), insertion, toggling favorites, editing, and deleting.
- **Clipboard Monitor**: Periodically or via `ClipboardManager.OnPrimaryClipChangedListener` checks system clipboard when app is running to auto-capture new text clips.

### 3. UI Layer (Jetpack Compose - DeX Floating Panel Theme)
- **Theme (`Theme.kt`, `Color.kt`)**: Sleek dark/light DeX desktop-friendly color palette (Deep Navy, Slate, Electric Teal accents).
- **MainActivity**: Sets up EdgeToEdge, dynamic window sizing suited for DeX floating window mode.
- **Components**:
  - **Top Bar**: Search bar, filter chips (All, Favorites, Tags), clear history, minimize/close controls.
  - **Clip List (`LazyColumn`)**: Cards showing truncated text, timestamp, favorite heart toggle, copy button, tag pills, and long-click/quick copy action.
  - **Clip Detail / Edit Dialog**: View full text, edit tags, pin/favorite.

## Step-by-Step Implementation Plan

### Step 1: Update Metadata & Manifest
- Update `metadata.json` with app name ("DeX ClipVault") and description.
- Update `res/values/strings.xml` with `app_name`.

### Step 2: Room Database & Entities
- Create `ClipItem.kt`, `ClipDao.kt`, `ClipboardDatabase.kt`, and `ClipRepository.kt`.

### Step 3: ViewModel & Clipboard Monitor
- Create `ClipboardViewModel.kt` with StateFlow, coroutines, and integration with Android `ClipboardManager`.

### Step 4: Jetpack Compose UI Screens
- Create `Theme.kt` enhancements.
- Build `ClipboardScreen.kt` with search, filter tabs, favorite toggle, tag management, copy to clipboard action, and delete actions.

### Step 5: Verification & Testing
- Compile applet with `compile_applet` and verify build success.
