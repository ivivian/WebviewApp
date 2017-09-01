package cn.zzv.push.yqqwebview;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cn.zzv.yqqwebview.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

public class UpdateManager {

	private Context mContext;
	
	private String updateMsg = "有新版本发布，请下载！";
	
	private String apkUrl = "http://www.zzv.cn/demo/webview/zxsc.apk";
	
	private Dialog noticeDialog;
	
	private Dialog downloadDialog;

    private String savePath = "/sdcard/updatedemo/";
    
    private String saveFileName = savePath + "Updatezxsj.apk";

    private ProgressBar mProgress;
    
    private static final int DOWN_UPDATE = 1;
    
    private static final int DOWN_OVER = 2;
    
    private int progress;
    
    private Thread downLoadThread;
    
    private boolean interceptFlag = false;
    
    private Handler mHandler = new Handler(){
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
			case DOWN_UPDATE:
				mProgress.setProgress(progress);
				break;
			case DOWN_OVER:
				installApk();
				break;
			default:
				break;
			}
    	};
    };
    
	public UpdateManager(Context context) {
		this.mContext = context;
	}
	
	public void checkUpdateInfo(String fupdateurl,String fupdateapk,String fversion){
		// 获得存储卡的路径
         String sdpath = Environment.getExternalStorageDirectory() + "/";
         saveFileName = sdpath + "download/updatezxsj.apk";
        
		 apkUrl=fupdateapk;
		 String fstr="";
		 try {
			fstr=executeHttpGet(fupdateurl);
	 	 } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fstr="9999";
		 }
		 Integer fserververion=Integer.valueOf(fstr);//Integer.valueOf((UtilHttp.getHTTPUtil().queryStringForPost(url)));
		 Integer fversionint=Integer.valueOf(fversion);
		//当前安装的版本号码
		 if(fserververion>fversionint&&fserververion!=9999)
			 showNoticeDialog();
	}
	
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
	
	
	
	private void showNoticeDialog(){
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("软件版本更新");
		builder.setMessage(updateMsg);
		builder.setPositiveButton("手动下载", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
	        	   Uri uri = Uri.parse(apkUrl);  
	        	   Intent it = new Intent(Intent.ACTION_VIEW, uri);  
	               mContext.startActivity(it);      	   
			}
		});
		builder.setNegativeButton("以后再说", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();				
			}
		});
		builder.setNeutralButton("下载", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showDownloadDialog();			
			}
		});		
		noticeDialog = builder.create();
		noticeDialog.show();
	}
	
	private void showDownloadDialog(){
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("软件版本有更新");
		
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.progress, null);
		mProgress = (ProgressBar)v.findViewById(R.id.progress);
		
		builder.setView(v);
		builder.setNegativeButton("取消", new OnClickListener() {	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				interceptFlag = true;
			}
		});
		
		downloadDialog = builder.create();
		downloadDialog.show();
		
		downloadApk();
	}
	
	private Runnable mdownApkRunnable = new Runnable() {	
		@Override
		public void run() {
			try {
				URL url = new URL(apkUrl);
			
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				InputStream is = conn.getInputStream();
				
				File file = new File(savePath);
				if(!file.exists()){
					file.mkdir();
				}
				String apkFile = saveFileName;
				File ApkFile = new File(apkFile);
				FileOutputStream fos = new FileOutputStream(ApkFile);
				
				int count = 0;
				byte buf[] = new byte[1024];
				
				do{   		   		
		    		int numread = is.read(buf);
		    		count += numread;
		    	    progress =(int)(((float)count / length) * 100);
		    	    //���½��
		    	    mHandler.sendEmptyMessage(DOWN_UPDATE);
		    		if(numread <= 0){	
		    			//�������֪ͨ��װ
		    			mHandler.sendEmptyMessage(DOWN_OVER);
		    			break;
		    		}
		    		fos.write(buf,0,numread);
		    	}while(!interceptFlag);//���ȡ���ֹͣ����.
				
				fos.close();
				is.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch(IOException e){
				e.printStackTrace();
			}
			
		}
	};
	
	 /**
     * ����apk
     * @param url
     */
	
	private void downloadApk(){
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}
	 /**
     * ��װapk
     * @param url
     */
	private void installApk(){
		File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }    
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive"); 
        mContext.startActivity(i);
	
	}
}
