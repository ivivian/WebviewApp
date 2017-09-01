package cn.zzv.push.yqqwebview;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;


public class zxscservice extends Service {
    private static final String TAG = "Test";

    @Override
    //Service时被调用  
    public void onCreate() {
        Log.i(TAG, "Service onCreate--->");
        super.onCreate();
        //启动百度推送服务，避开主窗口等待时机过长
        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, Utils.getMetaValue(zxscservice.this, "api_key"));
    }

    @Override
    //当调用者使用startService()方法启动Service时，该方法被调用  
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "Service onStart--->");
        //super.onStart(intent, startId);  

    }

    @Override
    //当Service不在使用时调用  
    public void onDestroy() {
        Log.i(TAG, "Service onDestroy--->");
        super.onDestroy();
    }

    @Override
    //当使用startService()方法启动Service时，方法体内只需写return null  
    public IBinder onBind(Intent intent) {
        return null;
    }

}
