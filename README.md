# SafeVault

A security-focused Android vault app that encrypts and stores sensitive data using AES-256-GCM encryption via Android Keystore. Features a built-in document scanner with OCR, biometric authentication, panic/stealth mode, and auto-destruct capabilities.

## Features

- **AES-256-GCM Encryption** — All sensitive content is encrypted using hardware-backed Android Keystore with unique IVs per entry
- **Biometric & Password Authentication** — Unlock with fingerprint, face recognition, or a master password
- **Document Scanner with OCR** — Use your phone's camera to scan documents and automatically extract text, names, IDs, dates, amounts, emails, and phone numbers
- **Vault Management** — Store, search, filter, edit, and delete encrypted items across multiple categories
- **Panic Password & Stealth Mode** — Enter a panic password at login to display a fake calculator screen, hiding all vault data
- **Auto-Destruct Mode** — Optionally wipe all vault data permanently when the panic password is entered
- **Category Organization** — Organize items as ID Card, Password, Secure Note, Receipt, Photo, or Other
- **Original Scan Image Storage** — Scanned document images are saved and viewable from the item detail screen
- **Swipe to Delete with Undo** — Quickly remove items with a swipe gesture and restore them if needed
- **Wipe All Data** — One-tap option to permanently delete all vault items

## Screenshots

*Coming soon*

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: 34 (Android 14)
- **Target SDK**: 36
- **Architecture**: MVVM (Model-View-ViewModel)

### Libraries Used

| Library | Purpose |
|---------|---------|
| [Google ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition) | OCR for document scanning |
| [CameraX](https://developer.android.com/training/camerax) | Modern camera API for document capture |
| [Room Database](https://developer.android.com/training/data-storage/room) | Local encrypted data persistence |
| [AndroidX Security Crypto](https://developer.android.com/topic/security/data) | EncryptedSharedPreferences for secure settings |
| [AndroidX Biometric](https://developer.android.com/jetpack/androidx/releases/biometric) | Fingerprint and face authentication |
| [AndroidX Navigation](https://developer.android.com/guide/navigation) | Single-activity navigation with fragments |
| [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle) | ViewModel, LiveData, and coroutines integration |
| [Material Components](https://material.io/develop/android) | Material Design 3 UI components |

## Project Structure

```
app/src/main/java/project/safevault/
├── database/             # Room database, DAO, entity, and converters
│   ├── SafeVaultDatabase.kt
│   ├── VaultDao.kt
│   ├── VaultItemEntity.kt
│   └── Converters.kt
├── mlkit/                # Document scanning and OCR processing
│   ├── TextRecognitionHelper.kt
│   └── DocumentProcessor.kt
├── models/               # Data models and enums
│   ├── ItemCategory.kt
│   └── ScannedDocument.kt
├── security/             # Encryption, keystore, biometrics, and stealth
│   ├── CryptoUtils.kt
│   ├── KeystoreManager.kt
│   ├── BiometricHelper.kt
│   └── StealthManager.kt
├── ui/
│   ├── auth/             # Authentication screen
│   │   └── AuthActivity.kt
│   ├── scanner/          # Document scanner with camera
│   │   ├── ScannerFragment.kt
│   │   └── ScanResultBottomSheet.kt
│   ├── settings/         # App settings (panic password, auto-destruct, wipe)
│   │   └── SettingsFragment.kt
│   ├── stealth/          # Fake calculator (stealth mode)
│   │   └── StealthActivity.kt
│   └── vault/            # Main vault screens
│       ├── VaultActivity.kt
│       ├── VaultListFragment.kt
│       ├── VaultDetailFragment.kt
│       ├── AddItemFragment.kt
│       ├── VaultItemAdapter.kt
│       └── VaultViewModel.kt
├── MainActivity.kt
└── SafeVaultApp.kt
```

## Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2.1) or newer
- JDK 11 or higher
- Android device or emulator (API 34+) with camera support (for document scanning)
- Device with biometric hardware (recommended but not required)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/M-Mehdi-M/SafeVault.git
   ```

2. Open the project in Android Studio

3. Sync Gradle and build the project

4. Run on a physical device (recommended for camera and biometric features) or emulator

### Permissions

The app requires the following permissions:
- **Camera** — For scanning documents with OCR
- **Biometric** — For fingerprint/face unlock authentication

## Usage

1. **Set Up** — On first launch, create a master password to protect your vault
2. **Unlock** — Use biometrics or your master password to access the vault
3. **Add Items** — Tap the FAB (+) to manually add encrypted items, or use the Scan tab to capture documents
4. **Scan Documents** — Point the camera at a document, capture, review the extracted text, and save to vault
5. **Browse & Search** — Filter items by category or search by title from the vault list
6. **View Details** — Tap any item to see decrypted content, metadata, and the original scanned image (if applicable)
7. **Stay Safe** — Set a panic password in Settings to enable stealth mode or auto-destruct

## Security

- **Encryption**: AES-256-GCM with 128-bit authentication tag
- **Key Storage**: Android Keystore (hardware-backed when available)
- **Password Storage**: SHA-256 hash in EncryptedSharedPreferences
- **IV Handling**: Random IV per encryption, stored alongside ciphertext
- **Panic Mode**: Decoy calculator screen with optional data wipe

## Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Google ML Kit](https://developers.google.com/ml-kit) for OCR capabilities
- [Android Keystore System](https://developer.android.com/training/articles/keystore) for hardware-backed encryption
- [Material Design 3](https://m3.material.io) for design guidelines
- [CameraX](https://developer.android.com/training/camerax) for modern camera integration
