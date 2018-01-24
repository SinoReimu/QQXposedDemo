package cn.tecotaku.cn.bluetoothkeytest

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import de.robv.android.xposed.XposedHelpers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text.setOnClickListener{
            v ->
            var intent = Intent()
            intent.action = "cn.tecotaku,send"
            intent.putExtra("message", "123")
            sendBroadcast(intent)
        }
    }


}
