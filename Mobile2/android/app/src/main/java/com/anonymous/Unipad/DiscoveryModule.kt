package com.anonymous.Unipad

import android.content.Context
import android.net.wifi.WifiManager
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketTimeoutException

/*DiscoveryModule.kt
Listens for the PC's UDP announcement (UNIPAD|<server port>|<PC name>)

JS usage: NativeModules.Discovery.find(timeoutMs)
    - resolves { ip, port, name } when a PC is found
    - resolves null when nothing is heard before the timeout
*/
class DiscoveryModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

  companion object {
    private const val DISCOVERY_PORT = 8089
  }

  override fun getName(): String = "Discovery"

  @ReactMethod
  fun find(timeoutMs: Int, promise: Promise) {
    Thread {
      var socket: DatagramSocket? = null
      var lock: WifiManager.MulticastLock? = null

      try {
        val wifi = reactContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        lock = wifi.createMulticastLock("unipad-discovery")
        lock.setReferenceCounted(false)
        lock.acquire()

        socket = DatagramSocket(null)
        socket.reuseAddress = true
        socket.bind(InetSocketAddress(DISCOVERY_PORT))

        val buffer = ByteArray(256)
        val deadline = System.currentTimeMillis() + timeoutMs
        var result = Arguments.createMap()
        var found = false

        while (!found) {
          val remaining = (deadline - System.currentTimeMillis()).toInt()
          if (remaining <= 0) break

          socket.soTimeout = remaining
          val packet = DatagramPacket(buffer, buffer.size)
          socket.receive(packet)

          val parts = String(packet.data, 0, packet.length, Charsets.UTF_8).split("|")
          if (parts.size >= 2 && parts[0] == "UNIPAD") {
            result.putString("ip", packet.address.hostAddress)
            result.putInt("port", parts[1].toInt())
            result.putString("name", parts.getOrElse(2) { "" })
            found = true
          }
        }

        if (found) promise.resolve(result) else promise.resolve(null)
      } catch (e: SocketTimeoutException) {
        promise.resolve(null)
      } catch (e: Exception) {
        promise.reject("E_DISCOVERY", e.message, e)
      } finally {
        socket?.close()
        if (lock?.isHeld == true) lock.release()
      }
    }.start()
  }
}
