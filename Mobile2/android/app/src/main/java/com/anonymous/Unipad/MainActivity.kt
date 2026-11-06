package com.anonymous.Unipad

import android.os.Bundle

import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

  //Leaves the splash theme and switches to the normal app theme
  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.AppTheme)
    super.onCreate(null)
  }

  //Must match the "name" in app.json
  override fun getMainComponentName(): String = "Unipad"

  override fun createReactActivityDelegate(): ReactActivityDelegate =
      DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)
}
