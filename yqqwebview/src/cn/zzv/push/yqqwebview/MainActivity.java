package cn.zzv.push.yqqwebview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.zzv.yqqwebview.R;

/*Activity */
public class MainActivity extends Activity implements MyWebChomeClient.OpenFileChooserCallBack {
    private String urlhome, urlmobile, urlfront, urlback, urluser, urlnotice;
    private UpdateManager mUpdateManager;
    protected static final String ACTIVITY_TAG="MyTraceivivian";
    private Boolean IsCreate=true;

    //about webview==================
    private FrameLayout mFullscreenContainer;
    private FrameLayout mContentView;
    private WebView webview;

    private static final String TAG = "MainAcivity";
    private static final int REQUEST_CODE_PICK_IMAGE = 0;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
    private Intent mSourceIntent;
    private ValueCallback<Uri> mUploadMsg;
    public ValueCallback<Uri[]> mUploadMsgForAndroid5;

    //permission Code
    private static final int P_CODE_PERMISSIONS = 101;

    private ProgressDialog dialog;

    //弹出菜单处理------------------------------
    private static final int EXIT = 6;
    private static final int ABOUT = 5;
    private static final int UPDATE = 4;
    private static final int NOTICE = 3;
    private static final int CLEARCACHE = 2;
    private static final int RELOAD = 1;
    private static final int flocation = 0;

    //底部菜单
    private GridView gridView;
    /*-- Toolbar底部菜单选项下标--*/
    private final int TOOLBAR_ITEM_HOME = 0;
    private final int TOOLBAR_ITEM_MOBILE = 1;
    private final int TOOLBAR_ITEM_COM = 2;
    private final int TOOLBAR_ITEM_CART = 3;
    private final int TOOLBAR_ITEM_USER = 4;
    //底部菜单图片
    int[] menu_toolbar_image_array = {R.drawable.controlbar_home, R.drawable.controlbar_mobile,R.drawable.controlbar_com,R.drawable.controlbar_cart, R.drawable.controlbar_user};
    //底部菜单文字
    String[] menu_toolbar_name_array = {"首页", "移动", "前端", "后端", "关于"};

    //启动界面----------------------------
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 1:
                    //切换到主页面
                    mFullscreenContainer.setVisibility(View.GONE);
                    mContentView.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };
    //----------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        long startTime=System.currentTimeMillis(); //获取开始时间
        //getWindow().requestFeature(Window.FEATURE_PROGRESS); //去标题栏
        mFullscreenContainer = (FrameLayout) findViewById(R.id.fullscreen_custom_content);
        mContentView = (FrameLayout) findViewById(R.id.main_content);
        requestPermissionsAndroidM();
        webview = (WebView)findViewById(R.id.webView1);
        //启动百度推送
        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, Utils.getMetaValue(MainActivity.this, "api_key"));

        urlhome = this.getString(R.string.url_home) + "&ftag=app";//"http://192.168.1.2";//AssetsManage.getText(this, "URL");
        urlmobile = this.getString(R.string.url_company);
        urlfront = this.getString(R.string.url_mobile);
        urlback = this.getString(R.string.url_cart);
        urluser = this.getString(R.string.url_user);
        urlnotice = this.getString(R.string.url_notice);
        //初始化webview控件
        initWebView();
        long endTime=System.currentTimeMillis(); //获取结束时间
        long waitTime=1000;
        if(endTime-startTime>1000)
            waitTime=1;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //底部主导航按钮
                initMenu();
                //升级检查
                if (Utils.NetWorkStatus(MainActivity.this) == true) {
                    //这里来检测版本是否需要更新
                    Utils.fversion = MainActivity.this.getString(R.string.app_versioncode);//获取当前安装版本，保存变量fversion中
                    String fupdateapk = MainActivity.this.getString(R.string.app_updateapk);//最新版本的下载文件路径
                    String fupdateurl = MainActivity.this.getString(R.string.app_updateurl);//最新版本号码的地址
                    String fversion = MainActivity.this.getString(R.string.app_versioncode);//当前安装版本
                    mUpdateManager = new UpdateManager(MainActivity.this);
                    mUpdateManager.checkUpdateInfo(fupdateurl, fupdateapk, fversion);
                }
                //4.0需打开硬件加速
                if (getPhoneAndroidSDK() >= 14) {
                    getWindow().setFlags(0x1000000, 0x1000000);
                }
                if (!Utils.NetWorkStatus(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, "当前网络不可用！", Toast.LENGTH_LONG).show();
                    webview.loadUrl("file:///android_asset/index.html");
                } else {
                    //加载商城首页
                    webview.loadUrl(urlhome);
                }
                handler.sendEmptyMessage(1); //给UI主线程发送消息
            }
        }, waitTime); //启动等待
        fixDirPath();
    }

    //底部菜单相关设置###############################
    private void initMenu() {
        gridView = (GridView) this.findViewById(R.id.gridView1);
        gridView.setBackgroundResource(R.drawable.controlbar_bg);
        gridView.setNumColumns(5);
        gridView.setGravity(Gravity.CENTER);
        gridView.setVerticalSpacing(5);
        gridView.setHorizontalSpacing(5);
        gridView.setAdapter(getMenuAdapter(menu_toolbar_name_array, menu_toolbar_image_array));
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (Utils.NetWorkStatus(MainActivity.this) == false)
                    webview.loadUrl("file:///android_asset/index.html");
                else {
                    setMenuSelect(arg1);
                    switch (arg2) {
                        case TOOLBAR_ITEM_HOME:
                            webview.loadUrl(urlhome);
                            break;
                        case TOOLBAR_ITEM_COM:
                            webview.loadUrl(urlmobile);
                            break;
                        case TOOLBAR_ITEM_MOBILE:
                            webview.loadUrl(urlfront);
                            break;
                        case TOOLBAR_ITEM_CART:
                            webview.loadUrl(urlback);
                            break;
                        case TOOLBAR_ITEM_USER:
                            String fuserid = Utils.fuserid;
                            md5 mmd5 = new md5();
                            String fid = mmd5.getMD5Str(fuserid);
                            webview.loadUrl(urluser + "?ftag=app&ftype=baidu&uid=" + fuserid + "&fid=" + fid);
                            break;
                    }
                }
            }
        });
    }
    private void setMenuSelect(View selectView) {
        for (int i = 0; i < gridView.getChildCount(); i++) {
            View view = gridView.getChildAt(i);
            if (view.equals(selectView)) {
                view.setBackgroundResource(R.drawable.controlbar_highlight_bg);
            } else {
                view.setBackgroundResource(0);
            }
        }
    }
    private SimpleAdapter getMenuAdapter(String[] menuNameArray, int[] imageResourceArray) {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < menuNameArray.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemImage", imageResourceArray[i]);
            map.put("itemText", menuNameArray[i]);
            data.add(map);
        }
        SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
                R.layout.item_menu, new String[]{"itemImage", "itemText"},
                new int[]{R.id.item_image, R.id.item_text});
        return simperAdapter;
    }



    //webview相关设置和初始化工作############################
    private void initWebView() {
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        //settings.setPluginsEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        //settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//这个会导致4.1.2版本的手机flash显示成一个个图片
        //不使用缓存：
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //传达信息给web页面获取
        webview.setTag(null);
        webview.requestFocus();
        settings.setBuiltInZoomControls(true);
        //webview.setInitialScale(68);// 设置最小缩放等级
        settings.setUserAgentString("zzvcnbrowser");
        settings.setUseWideViewPort(true);

        //webview.setWebChromeClient(new MyWebChromeClient());
        webview.setWebChromeClient(new MyWebChomeClient(MainActivity.this));
        webview.setWebViewClient(new MyWebViewClient());

        //设置下载文件监听
        webview.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                //实现下载的代码
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    //file upload start--------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            if (mUploadMsg != null) {
                mUploadMsg.onReceiveValue(null);
            }

            if (mUploadMsgForAndroid5 != null) {         // for android 5.0+
                mUploadMsgForAndroid5.onReceiveValue(null);
            }
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_IMAGE_CAPTURE:
            case REQUEST_CODE_PICK_IMAGE: {
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        if (mUploadMsg == null) {
                            return;
                        }

                        String sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);

                        if (TextUtils.isEmpty(sourcePath) || !new File(sourcePath).exists()) {
                            Log.e(TAG, "sourcePath empty or not exists.");
                            break;
                        }
                        Uri uri = Uri.fromFile(new File(sourcePath));
                        mUploadMsg.onReceiveValue(uri);

                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (mUploadMsgForAndroid5 == null) {        // for android 5.0+
                            return;
                        }

                        String sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);

                        if (TextUtils.isEmpty(sourcePath) || !new File(sourcePath).exists()) {
                            Log.e(TAG, "sourcePath empty or not exists.");
                            break;
                        }
                        Uri uri = Uri.fromFile(new File(sourcePath));
                        mUploadMsgForAndroid5.onReceiveValue(new Uri[]{uri});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    public void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType) {
        mUploadMsg = uploadMsg;
        showOptions();
    }

    @Override
    public boolean openFileChooserCallBackAndroid5
            (WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        mUploadMsgForAndroid5 = filePathCallback;
        showOptions();

        return true;
    }

    public void showOptions() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setOnCancelListener(new DialogOnCancelListener());

        alertDialog.setTitle("请选择操作");
        // gallery, camera.
        String[] options = {"相册", "拍照"};

        alertDialog.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            if (PermissionUtil.isOverMarshmallow()) {
                                if (!PermissionUtil.isPermissionValid(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    Toast.makeText(MainActivity.this,
                                            "请去\"设置\"中开启本应用的图片媒体访问权限2",
                                            Toast.LENGTH_SHORT).show();

                                    restoreUploadMsg();
                                    requestPermissionsAndroidM();
                                    return;
                                }

                            }

                            try {
                                mSourceIntent = ImageUtil.choosePicture();
                                startActivityForResult(mSourceIntent, REQUEST_CODE_PICK_IMAGE);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this,
                                        "请去\"设置\"中开启本应用的图片媒体访问权限3",
                                        Toast.LENGTH_SHORT).show();
                                restoreUploadMsg();
                            }

                        } else {
                            if (PermissionUtil.isOverMarshmallow()) {
                                if (!PermissionUtil.isPermissionValid(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    Toast.makeText(MainActivity.this,
                                            "请去\"设置\"中开启本应用的图片媒体访问权限1",
                                            Toast.LENGTH_SHORT).show();

                                    restoreUploadMsg();
                                    requestPermissionsAndroidM();
                                    return;
                                }

                                if (!PermissionUtil.isPermissionValid(MainActivity.this, Manifest.permission.CAMERA)) {
                                    Toast.makeText(MainActivity.this,
                                            "请去\"设置\"中开启本应用的相机权限",
                                            Toast.LENGTH_SHORT).show();

                                    restoreUploadMsg();
                                    requestPermissionsAndroidM();
                                    return;
                                }
                            }

                            try {
                                mSourceIntent = ImageUtil.takeBigPicture();
                                //startActivityForResult(mSourceIntent, REQUEST_CODE_IMAGE_CAPTURE);
                                startActivityForResult(mSourceIntent.createChooser(mSourceIntent, "Your title"),REQUEST_CODE_IMAGE_CAPTURE);

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this,
                                        "请去\"设置\"中开启本应用的相机和图片媒体访问权限6",
                                        Toast.LENGTH_SHORT).show();

                                restoreUploadMsg();
                            }
                        }
                    }
                }
        );

        alertDialog.show();
    }

    private void fixDirPath() {
        String path = ImageUtil.getDirPath();
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private class DialogOnCancelListener implements DialogInterface.OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            restoreUploadMsg();
        }
    }

    private void restoreUploadMsg() {
        if (mUploadMsg != null) {
            mUploadMsg.onReceiveValue(null);
            mUploadMsg = null;

        } else if (mUploadMsgForAndroid5 != null) {
            mUploadMsgForAndroid5.onReceiveValue(null);
            mUploadMsgForAndroid5 = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case P_CODE_PERMISSIONS:
                requestResult(permissions, grantResults);
                restoreUploadMsg();
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestPermissionsAndroidM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> needPermissionList = new ArrayList<>();
            needPermissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            needPermissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            needPermissionList.add(Manifest.permission.CAMERA);

            PermissionUtil.requestPermissions(MainActivity.this, P_CODE_PERMISSIONS, needPermissionList);

        } else {
            return;
        }
    }

    public void requestResult(String[] permissions, int[] grantResults) {
        ArrayList<String> needPermissions = new ArrayList<String>();

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (PermissionUtil.isOverMarshmallow()) {

                    needPermissions.add(permissions[i]);
                }
            }
        }

        if (needPermissions.size() > 0) {
            StringBuilder permissionsMsg = new StringBuilder();

            for (int i = 0; i < needPermissions.size(); i++) {
                String strPermissons = needPermissions.get(i);

                if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(strPermissons)) {
                    permissionsMsg.append("," + getString(R.string.permission_storage));

                } else if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(strPermissons)) {
                    permissionsMsg.append("," + getString(R.string.permission_storage));

                } else if (Manifest.permission.CAMERA.equals(strPermissons)) {
                    permissionsMsg.append("," + getString(R.string.permission_camera));

                }
            }

            String strMessage = "请允许使用\"" + permissionsMsg.substring(1).toString() + "\"权限, 以正常使用APP的所有功能.";

            Toast.makeText(MainActivity.this, strMessage, Toast.LENGTH_SHORT).show();

        } else {
            return;
        }
    }
    //file upload end ----------------------------

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            //if (url.indexOf(".3gp") != -1 || url.indexOf(".mp4") != -1 || url.indexOf(".flv") != -1 || url.indexOf(".swf") != -1){
            //            Intent intent = new Intent("android.intent.action.VIEW",
            //                            Uri.parse(url));
            //            startActivity(intent);
            //            return super.shouldOverrideUrlLoading(view, url);
            //}
            //else
            if (url.startsWith("http:")) {
                //友盟分享链接形成，这里增加fshareurl的形成，fsharetitle，fsharecontent的形成
                view.loadUrl(url);
                //return super.shouldOverrideUrlLoading(view, url);
            } else if (url.startsWith("tel:")) {
                view.loadUrl(webview.getUrl());
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(intent);
                //return super.shouldOverrideUrlLoading(view, url);
            } else if (url.startsWith("mailto:")) {
                view.loadUrl(webview.getUrl());
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                startActivity(intent);
                //return super.shouldOverrideUrlLoading(view, url);
            } else {
                view.loadUrl(url);
                //return super.shouldOverrideUrlLoading(view, url);
            }
            return true;
            //view.loadUrl(url);

        }

        @SuppressLint("NewApi")
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          String url) {
            // TODO Auto-generated method stub
            // String uu;
            // if (url.contains("http"))
            // uu = url;
            // else
            // uu = Config.getURL(WebMainActivity.this) + url;
            return super.shouldInterceptRequest(view, url);
        }


        @Override
        public void onLoadResource(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            showDialog(0);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if(IsCreate) {
                IsCreate=false;
                countuserinfo("create");
            }
            else
                countuserinfo("v");
            dialog.dismiss();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            // TODO Auto-generated method stub
            super.onReceivedError(view, errorCode, description, failingUrl);
            dialog.dismiss();
        }

    }
    public static int getPhoneAndroidSDK() {
        // TODO Auto-generated method stub
        int version = 0;
        try {
            version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return version;

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        //实例化进度条对话框
        dialog = new ProgressDialog(this);
            /*//可以不显示标题
	        dialog.setTitle("正在加载，请稍候！");*/
        dialog.setIndeterminate(true);
        dialog.setMessage("正在加载，请稍候！");
        dialog.setCancelable(true);
        return dialog;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (webview.canGoBack()) {
                webview.goBack();
                return true;
            }
            moveTaskToBack(true);
            //System.exit(0);
        }
        //用于处理案件menu菜单的动作
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            super.openOptionsMenu();
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        // 这里可以使用xml文件也可以使用代码方式，代码方式比较灵活一些~~~
        // MenuInflater inflater = new MenuInflater(getApplicationContext());
        // inflater.inflate(R.menu.options_menu, menu);

        menu.add(0, EXIT, 6, "退出系统");
        menu.add(0, ABOUT, 5, "关于系统");
        menu.add(0, UPDATE, 4, "更新软件");
        menu.add(0, NOTICE, 3, "消息历史");
        menu.add(0, CLEARCACHE, 2, "清除缓存");
        menu.add(0, RELOAD, 1, "刷新页面");
        menu.add(0, flocation, 0, "新客户注册");



        //setMenuBackgroud();
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String fversionname=this.getString(R.string.app_versionName);//当前安装版本
        // TODO Auto-generated method stub
        int id = item.getItemId();
        switch (id) {
            case flocation: //同上
                webview.loadUrl("http://www.zzv.cn/");
                break;
            case EXIT: //同上
                moveTaskToBack(true);
                break;
            //android.os.Process.killProcess(android.os.Process.myPid());
            case ABOUT: //如果使用xml方式，这里可以使用R.id.about
                Toast.makeText(this, "zzv.cn"+fversionname+"版 ", Toast.LENGTH_LONG).show();
                break;
            case RELOAD:
                webview.reload();
                break;
            case NOTICE:
                String appid = Utils.fappid;
                String channelid = Utils.fchannelid;
                String userid = Utils.fuserid;
                String fversion=Utils.fversion;

                md5 mmd5=new md5();
                String fid=mmd5.getMD5Str(userid);

                webview.loadUrl(urlnotice+"?aid="+appid+"&cid="+channelid+"&uid="+userid+"&fid="+fid+"&fmsgid=-1&fdetail=0&fv="+fversion);
                break;
            case UPDATE:
            {
                String furlapk=this.getString(R.string.app_updateapk);
                Uri uri = Uri.parse(furlapk);
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
                break;
            }
            case CLEARCACHE:
            {
                CookieSyncManager.createInstance(this);
                CookieSyncManager.getInstance().startSync();
                //CookieManager.getInstance().removeSessionCookie();
                CookieManager.getInstance().removeAllCookie();
                webview.clearCache(true);
                webview.clearHistory();
                webview.clearFormData();

                Toast.makeText(this, "缓存清除成功，页面自动刷新！", Toast.LENGTH_LONG).show();
                webview.reload();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public  void onStart()
    {
        super.onStart();
    }
    @Override
    public void onPause() {// 继承自Activity
        super.onPause();
        webview.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        webview.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        //Utils.setLogText(getApplicationContext(), Utils.logStringCache);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        countuserinfo("open");
        updateDisplay();
    }
    private  void countuserinfo(String ftype)
    {
        //--需要将客户的appid channel_id user_id 收集保存下来，将来用于实现指定客户的推送
        String userId = Utils.fuserid;
        String appid = Utils.fappid;
        String channelId = Utils.fchannelid;
        String fversion = Utils.fversion;
        //获取登录用户的id
        String fviewurl = webview.getUrl();
        md5 mmd5 = new md5();
        String fid = mmd5.getMD5Str("zzv");
        String fgeturl="http://www.zzv.cn/demo/webview/user.php?aid=" + appid + "&cid=" + channelId + "&uid=" + userId + "&fid=" + fid + "&fv=" + fversion + "&ftype=" + ftype;
        Log.w(ACTIVITY_TAG, fgeturl);
        /*如果要统计用户行为则去掉注释
        HttpUtils.doGetAsyn((fgeturl),
                new HttpUtils.CallBack() {
                    @Override
                    public void onRequestComplete(String result) {
                        //Toast.makeText(MainActivity.this, "结果：", Toast.LENGTH_LONG).show();
                    }
                }
        );
        */
    }
    // 更新界面显示内容
    private void updateDisplay() {
        if (Utils.ftype == "notice") {
            //根据fmsgid请求服务器来跟踪用户是否查看收到
            String fcustomer_content = Utils.fcustomcontent;
            String fmsgid = "0";
            String fdetail = "";
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(fcustomer_content);
                fmsgid = jsonObject.getString("fmsgid");
                fdetail = jsonObject.getString("fdetail");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String appid = Utils.fappid;
            String channelid = Utils.fchannelid;
            String userid = Utils.fuserid;
            String fversion = Utils.fversion;

            md5 mmd5 = new md5();
            String fid = mmd5.getMD5Str(fmsgid);

            webview.loadUrl(urlnotice + "?aid=" + appid + "&cid=" + channelid + "&uid=" + userid + "&fid=" + fid + "&fmsgid=" + fmsgid + "&fdetail=" + fdetail + "&fv=" + fversion);
            Utils.ftype = "";
        }
        else if (Utils.ftype == "loadurl") {
            webview.loadUrl(Utils.furl);
            Utils.ftype = "";
        }
        else if (Utils.ftype == "message") {
            //根据fmsgid请求服务器来跟踪用户是否查看收到
            String fcustomer_content = Utils.fcontent;
            openDialog(fcustomer_content, "微奇奇信息");
            Utils.ftype = "";
        }
    }
    //显示对话框
    private void openDialog(String strMsg, String strTitle) {
        new Builder(this)
                .setTitle(strTitle)
                .setMessage(strMsg)
                .setPositiveButton("确认",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                            }
                        })
                .show();
    }

}
