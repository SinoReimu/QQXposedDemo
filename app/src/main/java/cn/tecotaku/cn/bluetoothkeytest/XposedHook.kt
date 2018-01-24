package cn.tecotaku.cn.bluetoothkeytest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Modifier
import okhttp3.*
import java.io.IOException
import okhttp3.RequestBody
import org.json.JSONObject


/**
 * Created by HoshikawaShiro on 2018/1/12.
 */

class XposedHook: BroadcastReceiver(), IXposedHookLoadPackage {

    override fun onReceive(context: Context?, intent: Intent?) {
        XposedBridge.log("receive message"+intent!!.getStringExtra("message"))
        XposedHelpers.callStaticMethod(findClass("com.tencent.mobileqq.activity.ChatActivityFacade", packageParam.classLoader), "a",
                qqInter, context, getSessionInfo(0, "814092898"),
                intent!!.getStringExtra("message"),  null, getMsgParamter())
    }

    lateinit var packageParam: XC_LoadPackage.LoadPackageParam
    lateinit var qqInter: Any
    lateinit var context: Any
    var lastMsgUid: Long = 0
    var messageTime = HashMap<String, Long>()

    fun getMsgParamter(): Any {
        var cls = XposedHelpers.findClass("com.tencent.mobileqq.activity.ChatActivityFacade\$SendMsgParams", packageParam.classLoader)
        var param = cls.newInstance()
        //setFieldValueIndex()
        return param
    }
    fun getSessionInfo(isTroop: Int, friendUin: String): Any {
        var cls = XposedHelpers.findClass("com.tencent.mobileqq.activity.aio.SessionInfo", packageParam.classLoader)
        var session = cls.newInstance()
        setFieldValueIndex(0, session, isTroop)
        setFieldValueIndex(1, session, System.currentTimeMillis())
        setFieldValueIndex(3, session, friendUin)
        return session
    }

    fun setFieldValueIndex(index: Int, instant: Any, value: Any) {
        val fields = instant::class.java.declaredFields
        fields[index].set(instant, value)
    }

    fun getFieldValueIndex(index: Int, instant: Any): Any {
        val fields = instant::class.java.declaredFields
        return fields[index].get(instant)
    }


    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam?) {
        if(loadPackageParam!!.packageName.equals("com.tencent.mobileqq")) {
            packageParam = loadPackageParam


            findAndHookConstructor("com.tencent.mobileqq.activity.BaseChatPie", loadPackageParam.classLoader,
            "com.tencent.mobileqq.app.QQAppInterface", "android.view.ViewGroup" ,"android.support.v4.app.FragmentActivity","android.content.Context"
            , object:XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    XposedBridge.log("init the qq inter and context")
                    qqInter = param!!.args[0]
                    context = param!!.args[3]
                    var a = IntentFilter()
                    a.addAction("cn.tecotaku,send")
                    (context as Context).registerReceiver(this@XposedHook, a)
                }
            })


            findAndHookMethod("com.tencent.mobileqq.app.message.BaseMessageManager", loadPackageParam.classLoader, "b",
                    ArrayList::class.java,
                    object:XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    var msg = (param!!.args[0] as ArrayList<Any>)[0]
                    var isTroop = getFieldValueIndex(0, msg)
                    var chatUin = getFieldValueIndex(2, msg)
                    var shmsgseq = getFieldValueIndex(1, msg)
                    var senderUin = getFieldValueIndex(5, msg)
                    log(printFields(msg))
                    log("撤回hook isTroop：$isTroop chatUin:$chatUin senderUid:$senderUin msgUid:$shmsgseq")
                    if(isTroop == 0)
                        replyRevoke(senderUin as String, isTroop as Int, shmsgseq as Long)
                    else
                        replyRevoke(chatUin as String, isTroop as Int, shmsgseq as Long)
                }
            })

            findAndHookMethod("com.tencent.mobileqq.app.MessageHandlerUtils", loadPackageParam.classLoader, "a",
                    "com.tencent.mobileqq.app.QQAppInterface",  "com.tencent.mobileqq.data.MessageRecord",Boolean::class.java,
                    object:XC_MethodHook(){

                        override fun beforeHookedMethod(param: MethodHookParam?) {
                            var friendUin = XposedHelpers.getObjectField(param!!.args[1], "frienduin") as String
                            var senderUin = XposedHelpers.getObjectField(param!!.args[1], "senderuin") as String
                            var time = XposedHelpers.getLongField(param!!.args[1], "time")
                           // log(" lastMsgUid:$lastMsgUid")
                            if (newMessage(senderUin, time)) {
                                if (senderUin != "1605301169") {
                                    var message: String? = XposedHelpers.getObjectField(param!!.args[1], "msg") as String?
                                    if(message==null) return
                                    var isTroop = XposedHelpers.getIntField(param!!.args[1], "istroop")
                                    if (isTroop == 0) replyFromInternet(message, friendUin, isTroop)
                                    else if (isTroop == 1) {
                                        if (message.contains("王凤燕")||message.contains("凤燕")||message.contains("燕燕"))
                                            reply("就是那个不老实的王凤燕吗?", friendUin, isTroop)
                                        else if (message.contains("心怡")||message.contains("李心怡"))
                                            reply("就是那个权限狗群主李心怡吗?", friendUin, isTroop)
                                        else if (message.contains("妞妞")||message.contains("小草莓"))
                                            reply("就是那个脱发至今未痊愈的小草莓吗?", friendUin, isTroop)
                                        else if (message.contains("兔兔")||message.contains("紫荆")||message.contains("兔叽")||message.contains("兔子"))
                                            reply("兔兔最可爱了，你们不能凶凶兔兔", friendUin, isTroop)
                                        else if (message.contains("亚萍")||message.contains("朱亚萍"))
                                            reply("刘宇涵？", friendUin, isTroop)
                                        else if(message.contains("%c")) {

                                        } else if(message.contains("%%")) replyFromInternet(message.replace("%%", ""), friendUin, isTroop)
                                    }
                                }
                            }
                        }
                    })

        }
    }

    fun replyRevoke(chatUin: String, isTroop: Int, msgUid: Long) {
        var list: List<*>? = query(qqInter, " where shmsgseq=$msgUid", chatUin, isTroop) ?: return
        var msgRecord = list!![0]
        var msgContent = XposedHelpers.getObjectField(msgRecord, "msg") as String?

        if(msgContent == "对方撤回了一条消息") return
        reply("你为什么要撤回"+msgContent, chatUin, isTroop)
    }

    fun replyFromInternet(message: String, toId: String, isTroop: Int) {

        val json = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(json, "{key: 'bd51a1ca1bf6448a954e527d394409ac', info: '$message', userid: '$toId'}")
        var url = "http://www.tuling123.com/openapi/api"
        var okHttpClient = OkHttpClient()
        var request = Request.Builder().url(url)
                .post(body).build()
        var call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                var json = JSONObject(response!!.body().string())
                reply(json.get("text") as String, toId, isTroop)
            }

            override fun onFailure(call: Call?, e: IOException?) {
               reply("error", toId, isTroop)
            }
        })
    }

    fun query(qinterface: Any, param: String, who: String, isTroop: Int): List<*>? {
        var tableName = XposedHelpers.callStaticMethod(findClass("com.tencent.mobileqq.data.MessageRecord", packageParam.classLoader),
                "getTableName", who, isTroop)
        var sql = "SELECT * FROM " + tableName + param
        var entityFactory = XposedHelpers.callMethod(qinterface, "getEntityManagerFactory")
        var manager = XposedHelpers.callMethod(entityFactory, "createMessageRecordEntityManager")
        return callMethod(manager, "a", sql, null, qinterface) as List<*>?
    }

    fun reply(message: String, toId: String, isTroop: Int) {
        XposedHelpers.callStaticMethod(findClass("com.tencent.mobileqq.activity.ChatActivityFacade", packageParam.classLoader), "a",
                qqInter, context, getSessionInfo(isTroop, toId),
                message, null, getMsgParamter())
    }

    fun printFields(instant: Any): String {
        var fieldString = StringBuilder()

        val fields = instant::class.java.declaredFields
        for (f in fields) {
            val type = f.type
            val name = f.name
            fieldString.append("  ")
            val modifiers = Modifier.toString(f.modifiers)
            if (modifiers.isNotEmpty())
                fieldString.append(modifiers + " ")
            fieldString.append(type.name + " " + name + "="+f.get(instant)+";" + "\n")
        }
        return fieldString.toString()
    }

    fun newMessage(senderUin: String, time: Long): Boolean {
        if(messageTime.containsKey(senderUin)) {
            if(time > messageTime[senderUin]!!) {
                messageTime[senderUin] = time
                return true
            } else
                return false
        } else {
            messageTime.put(senderUin, time)
            return true
        }
    }

    fun log(conteng: String) {
        XposedBridge.log(conteng)
        //Log.i("XposedQQ", conteng)
    }
}