package cn.huicheng.hc_101.util;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2015/12/21.
 */
    /*
 * 从服务器获取xml解析并进行比对版本号
 */
public class CheckVersionTask implements Runnable {
    private WifiManager mWifiManager;
    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock wakeLock = null;
    private Context context;
    private String TAG = "CheckVersion.";
    private UpdataInfo info;
    private DownloadManager downloadManager;
    private SharedPreferences url_sp;
    private SharedPreferences.Editor url_editor;

    public CheckVersionTask(Context context){
        this.context = context;
        url_sp = this.context.getSharedPreferences("url", Context.MODE_PRIVATE);
        url_editor = url_sp.edit();
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiLock =	mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,"wifilock");
    }

    public void run() {
        try {
            //包装成url的对象
            URL url = new URL(url_sp.getString("xmlUrl","http://www.hchchchc.com/JavaWeb/HC_101.xml"));
            HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            InputStream is =conn.getInputStream();
            info =  UpdataInfoParser.getUpdataInfo(is);

            if(Float.parseFloat(info.getVersion())<= Float.parseFloat(getVersionName())){
                Log.i(TAG, "版本号相同无需升级");
            }else{
                Log.i(TAG, "版本号不同 ,提示用户升级 ");
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
            url_editor.putString("xmlUrl",info.getXml_url());
            url_editor.commit();
        } catch (Exception e) {
            // 待处理
            Message msg = new Message();
            msg.what = 2;
            handler.sendMessage(msg);
            e.printStackTrace();
        }
    }

    /*
* 获取当前程序的版本号
*/
    private String getVersionName() throws Exception {
        //获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        return packInfo.versionName;
    }

    Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //对话框通知用户升级程序
                    showUpdataDialog();
                    break;
                case 2:
                    //服务器超时
//                    Toast.makeText(context.getApplicationContext(), "获取服务器更新信息失败", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    //下载apk失败
                    Toast.makeText(context.getApplicationContext(), "下载新版本失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /*
     *
     * 弹出对话框通知用户更新程序
     *
     * 弹出对话框的步骤：
     * 	1.创建alertDialog的builder.
     *	2.要给builder设置属性, 对话框的内容,样式,按钮
     *	3.通过builder 创建一个对话框
     *	4.对话框show()出来
     */
    protected void showUpdataDialog() {
        AlertDialog.Builder builer = new AlertDialog.Builder(context) ;
        builer.setTitle("版本升级");
        builer.setMessage(info.getTip());
        //当点确定按钮时从服务器上下载 新的apk 然后安装
        builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "下载apk,更新");
                downLoadApk();
            }
        });
        //当点取消按钮时进行登录
        builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        builer.setCancelable(false);
        builer.show();
    }

    /*
     * 从服务器中下载APK
     */
    protected void downLoadApk() {
        final ProgressDialog pd;	//进度条对话框
        pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在下载更新");
        pd.show();
        new Thread(){
            @Override
            public void run() {
                try {
                    if (!mWifiLock.isHeld())
                        mWifiLock.acquire();
                    downloadManager = (DownloadManager)context.getSystemService(context.DOWNLOAD_SERVICE);
                    downLoadFile(info.getApk_url(), pd);
                    sleep(1000);
                    if (null!=mWifiLock&&mWifiLock.isHeld())
                        mWifiLock.release();
                    installApk();
//                    pd.dismiss(); //结束掉进度条对话框
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                    try {
                        if (null!=mWifiLock&&mWifiLock.isHeld())
                            mWifiLock.release();
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }}.start();
    }

    public void downLoadFile(final String url,final ProgressDialog pDialog) {
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        HttpResponse response;
        try {
            response = client.execute(get);
            HttpEntity entity = response.getEntity();
            int length = (int) entity.getContentLength();   //获取文件大小
            pDialog.setMax(length);                            //设置进度条的总长度
            InputStream is = entity.getContent();
            FileOutputStream fileOutputStream = null;
            if (is != null) {
                File file = new File(
                        Environment.getExternalStorageDirectory(),
                        "HC_101.apk");
                fileOutputStream = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int ch = -1;
                int process = 0;
                while ((ch = is.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, ch);
                    process += ch;
                    pDialog.setProgress(process);       //这里就是关键的实时更新进度了！
                }

            }
            fileOutputStream.flush();
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            pDialog.cancel();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void installApk() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory(), "HC_101.apk")),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
