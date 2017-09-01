package cn.zzv.push.yqqwebview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.zzv.yqqwebview.R;


/*
 * 云推送Demo主Activity。
 * 代码中，注释以Push标注开头的，表示接下来的代码块是Push接口调用示例
 */
public class PushDemoActivity extends Activity implements View.OnClickListener {

    //private static final String TAG = PushDemoActivity.class.getSimpleName();
    RelativeLayout mainLayout = null;
    private Boolean IsCreate=true;
    
    int homebuttonid = 0;
    int companybuttonid = 0;
    int brandbuttonid = 0;
    int cartbuttonid = 0;
    int userbuttonid = 0;
    
    Button homebutton = null;
    Button companybutton = null;
    Button brandbutton = null;
    Button cartbutton = null;
    Button userbutton = null;
    //TextView logText=null;
    
	String url,urlhome,urlcompany,urlmobile,urlcart,urluser,urlnotice;
	String baidu_userid="";
	private UpdateManager mUpdateManager;

	//about webview==================
	WebView webview;
	private ValueCallback<Uri> mUploadMessage;
	private ProgressDialog dialog;
	private Uri imageUri;
	public final static int REQ_CANCEL = 0;
	public final static int REQ_CHOOSER = 1;
	private final static int REQ_CAMERA = 2;	
	//---------------------------------------------
	public String locinfo="";
    
    public static int initialCnt = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources resource = this.getResources();
        String pkgName = this.getPackageName();

        setContentView(resource.getIdentifier("main", "layout", pkgName));
        homebuttonid = resource.getIdentifier("button_home", "id", pkgName);
        companybuttonid = resource.getIdentifier("button_company", "id", pkgName);
        brandbuttonid = resource.getIdentifier("button_brand", "id", pkgName);
        cartbuttonid = resource.getIdentifier("button_cart", "id", pkgName);
        userbuttonid = resource.getIdentifier("button_user", "id", pkgName);

        homebutton = (Button) findViewById(homebuttonid);
        companybutton = (Button) findViewById(companybuttonid);
        brandbutton = (Button) findViewById(brandbuttonid);
        cartbutton = (Button) findViewById(cartbuttonid);
        userbutton = (Button) findViewById(userbuttonid);

        homebutton.setOnClickListener(this);
        companybutton.setOnClickListener(this);
        brandbutton.setOnClickListener(this);
        cartbutton.setOnClickListener(this);
        userbutton.setOnClickListener(this);
        
		//----add by yhj --------------------------------------
		 urlhome = this.getString(R.string.url_home)+"&ftag=app";//"http://192.168.1.2";//AssetsManage.getText(this, "URL");
		 urlcompany = this.getString(R.string.url_company);
		 urlmobile = this.getString(R.string.url_mobile);
		 urlcart= this.getString(R.string.url_cart);
		 urluser = this.getString(R.string.url_user);
		 urlnotice= this.getString(R.string.url_notice);
		 //初始化webview控件
		 initWebview();
		 //创建快捷方式
         //addShortCut(); 
         Utils.fversion=this.getString(R.string.app_versioncode);//当前安装版本
         
		 if(NetWorkStatus()==false)
		 {
				Toast.makeText(this, "当前网络不可用！", Toast.LENGTH_LONG).show();
				webview.loadUrl("file:///android_asset/index.html");				
		 }
		 else
		 {
			 
			//百度初始化
	        PushManager.startWork(getApplicationContext(),
	                    PushConstants.LOGIN_TYPE_API_KEY,
	                    Utils.getMetaValue(PushDemoActivity.this, "api_key"));
	        
	         //加载中鑫商城首页
			 webview.loadUrl(urlhome);
			 //这里来检测版本是否需要更新
			 String fupdateapk=this.getString(R.string.app_updateapk);//最新版本的下载文件路径
			 String fupdateurl=this.getString(R.string.app_updateurl);//最新版本号码的地址
			 String fversion=this.getString(R.string.app_versioncode);//当前安装版本
			 mUpdateManager = new UpdateManager(this);
			 mUpdateManager.checkUpdateInfo(fupdateurl,fupdateapk,fversion);
		 }		 


    }

    @Override
    public void onClick(View v) {
    	if(NetWorkStatus()==false)
			webview.loadUrl("file:///android_asset/index.html");				
		else
		{
	        if (v.getId() == homebuttonid) {
		    	webview.loadUrl(urlhome);	        	
	        } else if (v.getId() == companybuttonid) {
	        	webview.loadUrl(urlcompany);
	        } else if (v.getId() == brandbuttonid) {
	        	webview.loadUrl(urlmobile);//openRichMediaList();
	        } else if (v.getId() == cartbuttonid) {
	        	webview.loadUrl(urlcart);
	        } else if (v.getId() == userbuttonid) {
	        	String fuserid=Utils.fuserid;
				md5 mmd5=new md5();
				String fid=mmd5.getMD5Str(fuserid); 
	        	webview.loadUrl(urluser+"?ftag=app&ftype=baidu&uid="+fuserid+"&fid="+fid);
	        } else {
	        	webview.loadUrl(urlhome);
	        }
		}

    }
   
    
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        //Log.d(TAG, "onResume");
        //updateDisplay();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        updateDisplay();
    	countuserinfo("open");
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

    // 更新界面显示内容
    private void updateDisplay() {
        //Log.d(TAG, "updateDisplay, logText:" + logText + " cache: " + Utils.logStringCache);
        //if (logText != null) {logText.setText(Utils.fuserid);
        //}
    	//logText.setText(Utils.logStringCache);
    	if(Utils.ftype=="notice")
    	{
    		//add by yhj 2014.04.022 根据fmsgid请求服务器来跟踪用户是否查看收到
            String fcustomer_content = Utils.fcustomcontent;
    		String fmsgid="0";
    		String fdetail="";
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
            String fversion=Utils.fversion;
            
    		md5 mmd5=new md5();
    		String fid=mmd5.getMD5Str(fmsgid); 
    		
    		webview.loadUrl(urlnotice+"?aid="+appid+"&cid="+channelid+"&uid="+userid+"&fid="+fid+"&fmsgid="+fmsgid+"&fdetail="+fdetail+"&fv="+fversion);
    		//String myurl=urlnotice+"?aid="+appid+"&cid="+channelid+"&uid="+userid+"&fid="+fid+"&fmsgid="+fmsgid+"&fdetail="+fdetail+"&fv="+fversion;
    		Utils.ftype="";
    	}
    	else if(Utils.ftype=="message")
    	{
    		//add by yhj 2014.04.022 根据fmsgid请求服务器来跟踪用户是否查看收到
            String fcustomer_content = Utils.fcontent;
            openDialog(fcustomer_content,"中鑫商城信息");
            Utils.ftype="";
    	}    	
    }
    
    private void openDialog(String strMsg,String strTitle){
        new AlertDialog.Builder(this)
            .setTitle(strTitle)
            .setMessage(strMsg)
            .setPositiveButton("确认",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int which) {
                            // TODO Auto-generated method stub
                        }
            })
            .show();
    }
    
    //add by yhj -----------------
	//--start--add by yhj----------------------------------
	@SuppressLint({ "NewApi"})
	private void initWebview() {
		webview = (WebView) findViewById(R.id.webView1);
		//传达信息给web页面获取
		webview.setTag(null);
		WebSettings settings = webview.getSettings();
		settings.setJavaScriptEnabled(true);
		webview.requestFocus();
		settings.setBuiltInZoomControls(true);
		webview.setInitialScale(100);// 设置最小缩放等级
		settings.setDomStorageEnabled(true);
		//settings.setBlockNetworkImage(false);
		//settings.setBlockNetworkLoads(false);		
		//webview.setScrollBarStyle(WebView.SCROLLBAR_POSITION_DEFAULT);
		//webview.setHorizontalScrollbarOverlay(true);
		//settings.setUserAgentString("0"); // 0为手机默认, 1为PC台机，2为IPHONE
        //settings.setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.2.1; zh-cn; MB525 Build/3.4.2-117) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");		
		
		// 无限缩放,设置此属性，可任意比例缩放。
		webview.getSettings().setUserAgentString("zxscbrowser");
		webview.getSettings().setUseWideViewPort(true);
		webview.setWebViewClient(new WebViewClient() {
			// 加载页面的URL
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				if(url.startsWith("http:")){
					view.loadUrl(url);
				}else if (url.startsWith("tel:")) {
					view.loadUrl(webview.getUrl());
					Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
					startActivity(intent);
	            }else if(url.startsWith("mailto:")){
	            	view.loadUrl(webview.getUrl());
	            	Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
	            	startActivity(intent);
	            }
				return true;
			}

			// 拦截URL，必须的
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

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * android.webkit.WebViewClient#onLoadResource(android.webkit.WebView
			 * , java.lang.String)
			 */
			@Override
			public void onLoadResource(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onLoadResource(view, url);
			}
			
			@Override  
			public void onPageStarted(WebView view, String url,Bitmap favicon) {  
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
			
			
		});
		webview.setWebChromeClient(new WebChromeClient() {
			//add by yhj 2014.04.30 for upload file
			/***************** android中使用WebView来打开本机的文件选择器 *************************/  
			// js上传文件的<input type="file" name="fileField" id="fileField" />事件捕获
			// Android > 4.1.1 调用这个方法
			public void openFileChooser(ValueCallback<Uri> uploadMsg,String acceptType, String capture) {
				openFileChooser(uploadMsg, acceptType);
			}
			// 3.0 + 调用这个方法
			public void openFileChooser(ValueCallback<Uri> uploadMsg,String acceptType) {
				if (mUploadMessage != null) return;
	               mUploadMessage = uploadMsg;  
	               selectImage();
			}

			// Android < 3.0 调用这个方法
			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				openFileChooser(uploadMsg, "" );
			}
			//------------------------------------------			

		});
		
		//设置下载文件监听
		webview.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                            String contentDisposition, String mimetype,
                            long contentLength) {
                    //实现下载的代码
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    startActivity(intent);
            }
		});
		
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
		//Log.d(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        switch (requestCode) {
	                case REQ_CHOOSER:
	                        if (null == mUploadMessage)
	                               return;
	                        Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
	                        mUploadMessage.onReceiveValue(result);
	                        mUploadMessage = null;
	                        break;
	                case REQ_CAMERA:
	                        if (resultCode == Activity.RESULT_OK) {
	                                mUploadMessage.onReceiveValue(imageUri);
	                                mUploadMessage = null;
	                        }	
	                case REQ_CANCEL:
                        if (null == mUploadMessage)
                            return;
                        mUploadMessage.onReceiveValue(null);
                        mUploadMessage = null;	                        
        }
	}
	
	protected final void selectImage() {
			                AlertDialog.Builder builder = new Builder(PushDemoActivity.this);
			                //builder.setTitle("选择类型");
			                final String[] items = { "照相机", "相册" };
			                builder.setItems(items, new DialogInterface.OnClickListener() {
			 
			                        @SuppressLint("SdCardPath")
			                        public void onClick(DialogInterface dialog, int which) {
			                                Toast.makeText(getApplicationContext(), items[which], Toast.LENGTH_SHORT).show();
			                                Intent intent = null;
			                                switch (which) {
			                                case 0:
			                                        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			                                        // 必须确保文件夹路径存在，否则拍照后无法完成回调
			                                        File vFile = new File(Environment.getExternalStorageDirectory().getPath() + "/fangwangtong/Images/" + (System.currentTimeMillis() + ".jpg"));
			                                        if (!vFile.exists()) {
			                                                File vDirPath = vFile.getParentFile();
			                                                vDirPath.mkdirs();
			                                        } else {
			                                                if (vFile.exists()) {
			                                                        vFile.delete();
			                                                }
			                                        }
			                                        imageUri = Uri.fromFile(vFile);
			                                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			                                        PushDemoActivity.this.startActivityForResult(intent, REQ_CAMERA);
			                                        break;
			                                case 1:
			                                        intent = new Intent(Intent.ACTION_PICK, null);
			                                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
			                                        PushDemoActivity.this.startActivityForResult(Intent.createChooser(intent, "选择图片"), REQ_CHOOSER);
			                                        break;
			                                default:  
		                                		dialog.cancel();
		                                		onActivityResult(1,0,intent);
		                                		break;
		                                }
		                        }
		                });
		                
		                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
		                	@Override
	                        public void onClick(DialogInterface dialog, int which) {
		                		Intent intent = null;
                        		dialog.cancel();
                        		onActivityResult(1,0,intent);
		                        }
		                });
		                
		                builder.setCancelable(false);
			                builder.create().show();
			}	
	
	@Override  
	protected Dialog onCreateDialog(int id) {  
	        //实例化进度条对话框  
	        dialog=new ProgressDialog(this);  
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
		//add by yhj 2014.4.11 用于处理案件menu菜单的动作
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			 super.openOptionsMenu(); 
			}		
		return true;
	}	
	//--end------------------------------------
	//判定网络情况
	private boolean NetWorkStatus() {
	       ConnectivityManager conManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE );  
	       NetworkInfo networkInfo = conManager.getActiveNetworkInfo();  
	       if (networkInfo != null ){  
	            return networkInfo.isAvailable();  
	       }  
	       return false ;  
    }	
	//弹出菜单处理------------------------------
	  private static final int EXIT = 6;
	  private static final int ABOUT = 5;
	  private static final int UPDATE = 4;
	  private static final int NOTICE = 3;
	  private static final int CLEARCACHE = 2;
	  private static final int RELOAD = 1;
	  private static final int flocation = 0;
	  
	  
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
	        		webview.loadUrl("http://www.zzv.cn");
					break;
	        case EXIT: //同上
	        	moveTaskToBack(true);
	        	break;
	            //android.os.Process.killProcess(android.os.Process.myPid());
	        case ABOUT: //如果使用xml方式，这里可以使用R.id.about  
	            Toast.makeText(this, "中鑫商城"+fversionname+"版 客服：4000200088", Toast.LENGTH_LONG).show();
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
	   
	   private  void countuserinfo(String ftype)
	    {
	        //add
	        String userId = Utils.fuserid;
	        String appid = Utils.fappid;
	        String channelId = Utils.fchannelid;
	        String fversion = Utils.fversion;
	        //获取登录用户的id
	        String fviewurl = webview.getUrl();
	        String ecsuserid = "0";
	        if(fviewurl!=null&&fviewurl.length()>5) {//当fviewurl为null的时候会有问题
	            CookieManager cookieManager = CookieManager.getInstance();
	            String CookieStr = cookieManager.getCookie(fviewurl);

	            if (CookieStr!= null&&CookieStr.length() > 5) {
	                int fpos = CookieStr.indexOf("ECS[user_id]=");
	                if (fpos > 0)//已经登录
	                {
	                    String[] strarray = CookieStr.split(";");
	                    for (int i = 0; i < strarray.length; i++) {
	                        if (strarray[i].indexOf("ECS[user_id]=") > 0)
	                            ecsuserid = strarray[i].substring(strarray[i].indexOf("ECS[user_id]=") + 13);
	                    }
	                } else
	                    ecsuserid = "0";
	            }
	        }
	        md5 mmd5 = new md5();
	        String fid = mmd5.getMD5Str("zzv");
	        String fgeturl="http://www.zzv.cn/demo/webview/user.php?aid=" + appid + "&cid=" + channelId + "&uid=" + userId + "&fid=" + fid + "&fv=" + fversion + "&euid=" + ecsuserid + "&ftype=" + ftype;
	        //Log.w("ivivian", fgeturl);	        
	        try
	        {executeHttpGet(fgeturl);}
	        catch (Exception e) {}
	    }	   
	   
	   //http请求
		public String executeHttpGet(String furl) {
			String result = "9999";
			URL url = null;
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build()); 
			HttpURLConnection connection = null;
			InputStreamReader in = null;
			try {
				url = new URL(furl);
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(10000);
				connection.setReadTimeout(8000);				
				in = new InputStreamReader(connection.getInputStream());
				BufferedReader bufferedReader = new BufferedReader(in);
				StringBuffer strBuffer = new StringBuffer();
				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					strBuffer.append(line);
				}
				result = strBuffer.toString();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
			return result;
		}		   
}

