package com.anonymous.Unipad

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.FileProvider
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import java.io.File

/**
 * Puts an image file on the Android clipboard.
 *
 * Replaces expo-clipboard's setImageAsync(). The community clipboard library
 * (@react-native-clipboard/clipboard) does NOT support setImage on Android.
 *
 * JS usage:  NativeModules.ClipboardImage.setImage("/path/to/image.png")
 */
class ClipboardImageModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String = "ClipboardImage"

  @ReactMethod
  fun setImage(path: String, promise: Promise) {
    try {
      val source = File(path.removePrefix("file://"))
      require(source.exists()) { "Image file not found: $path" }

      // FileProvider only exposes this sub-folder (see res/xml/clipboard_image_paths.xml).
      // Only the most recent image is kept.
      val dir = File(reactContext.cacheDir, "clipboard_images")
      dir.deleteRecursively()
      dir.mkdirs()

      val target = File(dir, "clip_${System.currentTimeMillis()}.${extensionFor(source)}")
      source.copyTo(target, overwrite = true)

      val uri = FileProvider.getUriForFile(
        reactContext,
        "${reactContext.packageName}.clipboardimage",
        target
      )
      // The MIME type (image/png, image/jpeg) is derived from the file extension.
      val clip = ClipData.newUri(reactContext.contentResolver, "image", uri)
      val clipboard = reactContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      clipboard.setPrimaryClip(clip)

      promise.resolve(null)
    } catch (e: Exception) {
      promise.reject("E_CLIPBOARD_IMAGE", e.message, e)
    }
  }

  /** The PC may send PNG (screenshots) or the raw bytes of a copied file (e.g. JPEG). */
  private fun extensionFor(file: File): String {
    val header = ByteArray(2)
    file.inputStream().use { it.read(header) }
    return if (header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte()) "jpg" else "png"
  }
}
