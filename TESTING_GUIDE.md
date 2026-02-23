# SafeVault - Complete Testing Guide

## App Overview
SafeVault is a security-focused vault app that encrypts and stores sensitive data such as ID cards, passwords, secure notes, receipts, and photos. It features biometric/password authentication, a document scanner with OCR, AES-256-GCM encryption via Android Keystore, a panic/stealth mode, and an auto-destruct option.

---

## Pre-requisites
1. Install the app on an Android device or emulator (API 34+)
2. Grant camera permission when prompted (required for document scanning)
3. Device with biometric hardware (fingerprint/face) is recommended but not required

---

## Feature Testing Guide

### 1. 🔐 First Launch & Password Setup

**How to test:**
1. Install and open the app for the first time
2. You'll see the **Authentication Screen** (AuthActivity)
3. The subtitle reads "Create a password to get started"
4. The fingerprint icon is hidden during setup
5. Enter a master password (minimum 4 characters) in the password field
6. Tap **Set Up**

**Expected Result:**
- Password is hashed with SHA-256 and stored in EncryptedSharedPreferences
- First-launch flag is set to false
- You are navigated to the **Vault Screen** (VaultActivity)
- On next launch, you'll see the returning user screen instead

---

### 2. 🔑 Returning User Authentication

**How to test:**
1. Close and reopen the app after initial setup
2. You'll see the **Authentication Screen** with biometric prompt (if device supports it)

**Biometric Authentication:**
1. The fingerprint icon and hint text are visible
2. Biometric prompt appears automatically on launch
3. Touch the fingerprint sensor or use face unlock
4. If biometric fails, tap "Use Password" on the prompt

**Password Authentication:**
1. Enter your master password in the text field
2. Tap **Unlock**
3. If password is wrong, error message "Authentication failed" appears and field is cleared

**Expected Result:**
- Successful biometric → navigates to Vault
- Correct password → navigates to Vault
- Wrong password → error message shown, field cleared
- Tapping fingerprint icon re-triggers biometric prompt

---

### 3. 🏦 Vault List (Main Screen)

**How to test:**
1. After authentication, you land on the **Vault List** screen
2. Bottom navigation has three tabs: **Vault**, **Scan**, **Settings**

**Empty State:**
1. On first use, you'll see "Your vault is empty" with subtitle "Tap + to add your first secure item"
2. The extended FAB button shows at the bottom

**With Items:**
1. Items appear in a RecyclerView sorted by most recently updated
2. Each item card shows:
   - Category icon with color-coded background
   - Title
   - Category label
   - Last updated date (format: "MMM dd, yyyy • HH:mm")
3. Tap any item to open its **Detail** page

**Search:**
1. Tap the search box at the top
2. Type part of a title
3. Results filter in real-time as you type

**Category Filter:**
1. Use the horizontal chip row below the search box
2. Tap a category chip (ID Card, Password, Secure Note, Receipt)
3. List filters to show only that category
4. Tap the chip again or select another to change filter

**FAB Scroll Behavior:**
1. Scroll the list down → FAB shrinks
2. Scroll the list up → FAB extends back

**Expected Result:**
- Empty state toggles based on item count
- Search filters items by title in real-time
- Category chips filter by category
- Tapping an item navigates to VaultDetailFragment

---

### 4. ➕ Add New Vault Item (Manual)

**How to test:**
1. From the Vault List, tap the **+ FAB** button
2. **Add Item Screen** (AddItemFragment) opens
3. Fill in:
   - **Title** — name for the item (required)
   - **Category** — dropdown with options: ID Card, Password, Secure Note, Receipt, Photo, Other
   - **Sensitive content** — the text to be encrypted (required)
4. Tap **Encrypt & Save**

**Validation:**
1. Leave title or content empty
2. Tap **Encrypt & Save**
3. Toast: "Title and content are required"

**Expected Result:**
- Content is encrypted with AES-256-GCM via Android Keystore
- Item is saved to Room database
- Toast: "Item encrypted and saved"
- You are navigated back to the **Vault List**
- New item appears at the top of the list

---

### 5. 📸 Document Scanner & OCR

**How to test:**
1. Tap the **Scan** tab in bottom navigation
2. **Scanner Screen** opens with camera preview
3. Grant camera permission if prompted
4. Align a document within the scan frame
5. Tap the **capture button** (center of control panel)
6. Wait for processing (progress indicator appears)

**After OCR Processing:**
1. A **Bottom Sheet** slides up showing:
   - "Scan Result" title
   - Extracted text in a scrollable card
   - If the document contains structured data, "Extracted Fields" section shows detected:
     - Name, ID Number, Date, Amount, Email, Phone
   - Raw text follows below
2. Two buttons: **Discard** and **Save to Vault**

**Save to Vault:**
1. Tap **Save to Vault**
2. You're taken to the **Add Item Screen** with the scanned text pre-filled in the content field
3. The original scanned image is saved to internal storage
4. Fill in a title and pick a category
5. Tap **Encrypt & Save**

**Discard:**
1. Tap **Discard** to close the bottom sheet and return to camera

**Expected Result:**
- Camera preview shows with overlay frame
- OCR extracts text using ML Kit Text Recognition
- Bottom sheet shows extracted text and fields
- Saving navigates: Scanner → (scanner popped from back stack) → Add Item → Save → Vault List
- After saving, you land on the **Vault List** (not back on the scanner)
- The scanned image path is stored with the vault item

---

### 6. 🔍 View Item Details & Original Scanned Image

**How to test:**
1. From the Vault List, tap on any saved item
2. **Detail Screen** (VaultDetailFragment) opens showing:
   - Category icon with color-coded background
   - Item title
   - Category label chip
   - "Decrypted Content" section with the decrypted text
   - Created date and Updated date (format: "MMM dd, yyyy • HH:mm")
   - If item was created from a scan: **"Original Scanned Image"** card with the photo
3. Two action buttons at the bottom: **Copy** and **Delete**

**Copy:**
1. Tap **Copy**
2. Toast: "Copied to clipboard"
3. Decrypted content is now in the clipboard

**Original Scanned Image:**
1. If the item was saved via the scanner, an image card appears below the content
2. The image shows exactly as captured (correctly oriented, not rotated)
3. If the item was added manually (no scan), this card is hidden

**Expected Result:**
- Content is decrypted and displayed correctly
- Dates are formatted properly
- Category icon and colors match the item's category
- Scanned image displays in correct orientation
- Copy button copies decrypted text to clipboard

---

### 7. ✏️ Edit Existing Item

**How to test:**
1. Open an item's detail page
2. Navigate to edit (via navigation that passes `editItemId`)
3. Title, category, and content are pre-filled from the existing item
4. Modify any fields
5. Tap **Encrypt & Save**

**Expected Result:**
- Existing item is updated (not duplicated)
- Content is re-encrypted with fresh IV
- `updatedAt` timestamp changes
- Navigates back to Vault List

---

### 8. 🗑️ Delete Items

**Swipe to Delete (from Vault List):**
1. Go to the **Vault List**
2. **Swipe left** on any item
3. Item is deleted
4. **Snackbar** appears at bottom: "Item deleted" with **Undo** button
5. Tap **Undo** within the snackbar timeout to restore the item

**Delete from Detail Screen:**
1. Open an item's detail page
2. Tap the **Delete** button
3. Confirmation dialog appears: "Delete this item permanently?"
4. Tap **Delete** to confirm or **Cancel** to abort

**Expected Result:**
- Swipe delete removes item with undo option
- Undo restores the item (re-encrypts and re-inserts)
- Detail delete shows confirmation dialog first
- After deletion, navigates back to Vault List
- Vault List updates immediately

---

### 9. 🚨 Panic Password & Stealth Mode

**Setting Up Panic Password:**
1. Go to **Settings** tab (bottom navigation)
2. Tap **Set Panic Password**
3. Dialog appears with two fields:
   - Panic Password
   - Confirm Panic Password
4. Enter matching passwords
5. Tap **Save**

**Validation:**
- Empty password → Toast: "Password cannot be empty"
- Mismatched passwords → Toast: "Passwords don't match"
- Success → Toast: "Panic password set"

**Using Panic Password at Login:**
1. Close the app and reopen
2. On the **Authentication Screen**, enter the panic password instead of the real password
3. Tap **Unlock**

**Expected Result (Stealth Mode):**
- App opens the **Stealth Activity** — a fake "Calculator" screen
- The back button exits the app entirely (`finishAffinity`)
- No access to vault data whatsoever
- If Auto-Destruct is disabled: data remains intact but hidden
- If Auto-Destruct is enabled: **all vault items are permanently wiped**

---

### 10. 💣 Auto-Destruct Mode

**How to test:**
1. Go to **Settings**
2. Toggle **Auto-Destruct Mode** ON
3. Summary: "Panic password will wipe all data instead of showing fake vault"
4. Set a panic password if not already set (see Feature 9)
5. Add some test items to the vault
6. Close the app
7. Reopen and enter the **panic password**

**Expected Result:**
- All vault items are permanently deleted from the database
- Toast: "Data wiped"
- Stealth Calculator screen is shown
- Reopening with the real password shows an empty vault
- This action is **irreversible**

**Testing Auto-Destruct:**
1. Add 3-4 test items with different categories
2. Enable Auto-Destruct in Settings
3. Set panic password to something like "000"
4. Close app → Reopen → Enter "000"
5. Verify stealth screen opens
6. Close app → Reopen → Enter real password
7. Vault should be completely empty

---

### 11. 🧹 Wipe All Data

**How to test:**
1. Go to **Settings**
2. Scroll to **Danger Zone** section
3. Tap **Wipe All Data**
4. Confirmation dialog: "This will permanently delete ALL items. This cannot be undone."
5. Tap **Wipe All Data** to confirm or **Cancel** to abort

**Expected Result:**
- All vault items are deleted from the database
- Toast: "All data wiped"
- Vault List shows empty state
- This does NOT reset the master password or panic password

---

### 12. ⚙️ Settings Overview

**How to test:**
1. Tap **Settings** in bottom navigation
2. Review all sections:

**Security:**
- **Set Panic Password** — opens dialog to set/change panic password
- **Auto-Destruct Mode** — toggle switch, persisted in EncryptedSharedPreferences

**Danger Zone:**
- **Wipe All Data** — permanently deletes all vault items

**About:**
- **Version** — displays current app version (e.g., "Version 1.0")

**Expected Result:**
- Auto-Destruct toggle state persists across app restarts
- Panic password is stored as SHA-256 hash in EncryptedSharedPreferences
- Version number matches build.gradle versionName

---

## Troubleshooting

### App Crashes on Camera
- Ensure camera permission is granted in device Settings > Apps > SafeVault
- Check if device has a rear camera
- Restart the app and try again

### OCR Not Working / No Text Detected
- Ensure the document text is clearly visible and in focus
- Good lighting significantly improves accuracy
- Keep the camera steady when capturing
- ML Kit works best with Latin script text
- Toast "No text detected" means OCR found nothing — try repositioning

### Biometric Not Working
- Ensure fingerprint/face is enrolled in device settings
- "Biometric not available. Use password." means the device doesn't support it
- Some emulators don't support biometric — use password authentication instead

### Scanned Image Appears Rotated
- This was fixed — the app reads `rotationDegrees` from the camera sensor and rotates the bitmap before saving
- If still occurring, ensure you're running the latest build

### Decryption Failed
- "[Decryption failed]" means the Keystore key has changed (e.g., after factory reset or app data clear)
- Items encrypted with a previous key cannot be recovered
- This is by design for security

### After Save, Still on Scanner
- This was fixed — the scanner is now popped from the back stack before navigating to Add Item
- After saving, you should land on the Vault List

---

## Database Information

The app uses **Room** database with the following entity:

**VaultItemEntity** (`vault_items` table):
| Column | Type | Description |
|--------|------|-------------|
| `id` | Long (PK, auto-generate) | Unique item ID |
| `title` | String | Item display name |
| `encryptedContent` | String | AES-256-GCM encrypted content (Base64) |
| `category` | ItemCategory (enum) | ID_CARD, PASSWORD, NOTE, RECEIPT, PHOTO, OTHER |
| `scannedImagePath` | String? (nullable) | Absolute path to original scanned image |
| `createdAt` | Long | Timestamp of creation |
| `updatedAt` | Long | Timestamp of last update |

---

## Encryption Details

- **Algorithm:** AES-256-GCM (128-bit authentication tag)
- **Key Storage:** Android Keystore (hardware-backed when available)
- **Key Alias:** `safevault_master_key`
- **IV:** Random, stored alongside ciphertext in Base64-encoded payload
- **Format:** `[4-byte IV length][IV bytes][ciphertext bytes]` → Base64
- **Password Storage:** SHA-256 hash in EncryptedSharedPreferences

---

## Categories Available
- 🪪 ID Card
- 🔑 Password
- 📝 Secure Note
- 🧾 Receipt
- 📷 Photo
- 📦 Other

---

## Navigation Flow
```
AuthActivity (launcher)
  ├── [correct password / biometric] → VaultActivity
  │     ├── Vault Tab → VaultListFragment
  │     │     ├── Tap item → VaultDetailFragment
  │     │     │     ├── Copy → clipboard
  │     │     │     └── Delete → confirm → pop back
  │     │     └── FAB (+) → AddItemFragment → save → pop to VaultList
  │     ├── Scan Tab → ScannerFragment
  │     │     └── Capture → BottomSheet → Save to Vault
  │     │           └── AddItemFragment (scanner popped) → save → pop to VaultList
  │     └── Settings Tab → SettingsFragment
  │           ├── Panic Password → dialog
  │           ├── Auto-Destruct → toggle
  │           └── Wipe All → confirm dialog
  └── [panic password] → StealthActivity (fake calculator)
        └── Back button → finishAffinity (exit app)
```

---

## File Locations
- **APK Output:** `app/build/outputs/apk/debug/app-debug.apk`
- **Scanned Images:** `{app internal storage}/files/scanned_images/`
- **Database:** `{app internal storage}/databases/safevault_database`
- **Encrypted Preferences:** `{app internal storage}/shared_prefs/safevault_secure_prefs.xml`

