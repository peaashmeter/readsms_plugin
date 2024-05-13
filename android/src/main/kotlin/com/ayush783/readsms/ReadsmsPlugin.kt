package com.ayush783.readsms

import android.app.Activity
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

class ReadsmsPlugin: FlutterPlugin, EventChannel.StreamHandler,BroadcastReceiver(), ActivityAware {
  private var channel : EventChannel? = null

  private var eventSink: EventChannel.EventSink? = null
  /**
   * context object to get the current context and register
   * the broadcast receiver
   */
  private lateinit var context: Context
  private lateinit var activity: Activity


  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext
    context.registerReceiver(this,IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    channel = EventChannel(flutterPluginBinding.binaryMessenger,"readsms")
    channel!!.setStreamHandler(this)
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    eventSink = events
  }

  override fun onCancel(arguments: Any?) {
    eventSink = null
  }

  override fun onReceive(context: Context?, intent: Intent?) {
    /**
     * Get the messages through the broadcast receiver
     * using the Telephony.Sms.Intent
     */
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
      val body: StringBuilder = StringBuilder()
        var number = ""
        val bundle: Bundle? = intent!!.extras
        val messages: Array<SmsMessage?>
        var timestamp = ""
        if (bundle != null) {
            val msgObjects: Array<*>? = bundle.get("pdus") as Array<*>?
            messages = arrayOfNulls(msgObjects!!.size)
            for (i in messages.indices) {
                messages[i] = SmsMessage.createFromPdu(msgObjects!![i] as ByteArray?)
                body.append(messages[i]!!.messageBody)
                number = messages[i]!!.originatingAddress.toString()
                timestamp = messages[i]!!.timestampMillis.toString()
            }
          var data = listOf(body.toString(), number, timestamp,)
          eventSink?.success(data)
        }
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel = null
    eventSink = null
  }

  override fun onAttachedToActivity(p0: ActivityPluginBinding) {
    activity = p0.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
  }

  override fun onReattachedToActivityForConfigChanges(p0: ActivityPluginBinding) {
  }

  override fun onDetachedFromActivity() {
  }
}
