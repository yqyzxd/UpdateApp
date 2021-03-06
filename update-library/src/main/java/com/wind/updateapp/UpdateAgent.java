package com.wind.updateapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.io.File;

/**
 * Created by wind on 16/5/9.
 */
public class UpdateAgent {
    private static UpdateAgent updateAgent;

    private UpdateAgent(){

    }

    public synchronized static UpdateAgent getInstance(){
        if (updateAgent==null){
            updateAgent=new UpdateAgent();
        }
        return updateAgent;
    }

    private UpdateApi.UpdateListener updateListener;
    public void setUpdateListener(UpdateApi.UpdateListener updateListener){
        this.updateListener=updateListener;
    }
    /**
     * 强制检查更新,不管用户是否忽略此更新
     * @param context
     * @param channelName
     */
    public  void forceUpdate(final FragmentActivity context, final String channelName){
        update(context,channelName,true);
    }

    /**
     * 自动检查更新(非强制)
     * @param context
     * @param channelName
     */
    public void update(final FragmentActivity context, final String channelName){
        update(context,channelName,false);
    }
    private   void update(final FragmentActivity context, final String channelName,final boolean forceUpdate){
        //获取应用版本号
        final int versionCode=getAppVersion(context);
        //应用渠道
        //包名
        final String packageName=context.getPackageName();

        if (!forceUpdate){
            //是否忽略更新
            SharedPreferences sp=context.getSharedPreferences("update",FragmentActivity.MODE_PRIVATE);
            boolean isIgnoreUpadte=sp.getBoolean(getSpKey(context),false);
            if (isIgnoreUpadte){
                return;
            }
        }

        if (updateListener==null){
            updateListener=new UpdateApi.UpdateListener() {
                @Override
                public void onUpdateReturned(int updateStatus, UpdateInfo updateInfo) {
                    switch (updateStatus) {
                        case UpdateStatus.Yes: // has update
                            UpdateAgent.showUpdateDialog(context,
                                    updateInfo,forceUpdate,mDialogStyle);
                            break;
                        case UpdateStatus.No: // has no update
                            //  ToastUtil.showToast(SettingActivity.this,"没有更新");
                            break;
                          /*  case UpdateStatus.NoneWifi: // none wifi
                                break;*/
                        case UpdateStatus.Timeout: // time out
                            //  ToastUtil.showToast(SettingActivity.this, "超时");
                            break;
                    }
                }
            };
        }

        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                new UpdateApi().update(packageName, versionCode+"", channelName, updateListener);
            }
        });
        thread.start();



    }

    private static String getSpKey(Context context) {

        return UpdateDialogFragment.SP_KEY_IGNORE_UPDATE+(getAppVersion(context)+1);
    }

    private DialogStyle mDialogStyle;
    public void setDialogStyle(DialogStyle style){
        this.mDialogStyle=style;
    }
    public static void showUpdateDialog(final FragmentActivity context, final UpdateInfo updateInfo,
                                        final boolean forceUpdate,final DialogStyle dialogStyle) {
        if (!context.isFinishing()){

            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try{
                        UpdateDialogFragment dialogFragment=new UpdateDialogFragment();
                        Bundle args=new Bundle();
                        args.putSerializable(UpdateDialogFragment.ARG_KEY_DIALOG_STYLE,dialogStyle);
                        args.putBoolean(UpdateDialogFragment.ARG_KEY_DOWNLOADED,isDownloaded(updateInfo.getLatestAppUrl()));
                        args.putBoolean(UpdateDialogFragment.ARG_KEY_FORCEUPDATE,forceUpdate);
                        args.putSerializable(UpdateDialogFragment.ARG_KEY_UPDATE_INFO,updateInfo);
                        dialogFragment.setArguments(args);
                        dialogFragment.setUpdateCallback(new UpdateDialogFragment.UpdateCallback() {
                            @Override
                            public void update(boolean isDownloaded) {
                                if (isDownloaded){
                                    AutoInstall.install(context,getDownloadApkPath(updateInfo.getLatestAppUrl()));
                                }else {
                                    //启动下载service
                                    Intent intent = new Intent(context,DownloadService.class);
                                    intent.putExtra(DownloadService.EXTRA_KEY_DOWNLOAD_URL,updateInfo.getLatestAppUrl());
                                    context.startService(intent);
                                }

                            }

                            @Override
                            public void ignoreUpdate(boolean isIgnore) {
                                SharedPreferences sp=context.getSharedPreferences("update",FragmentActivity.MODE_PRIVATE);
                                sp.edit().putBoolean(getSpKey(context),isIgnore).apply();
                            }
                        });
                        dialogFragment.show(context.getSupportFragmentManager(),"UpdateDialogFragment");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });

        }
    }

    public  static  boolean isDownloaded(String latestAppUrl) {
        return new File(getDownloadApkPath(latestAppUrl)).exists();
    }
    public static String getDownloadApkPath(String latestAppUrl) {
        return getDownloadPath() + getFilenName(latestAppUrl);
    }
    public static String getFilenName(String downloadUrl) {
        return downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
    }
    public static  String getDownloadPath() {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        String parentPath = dir + "/download" + "/.wind/";
        File parentDir = new File(parentPath);
        if (!parentDir.exists()) {
            boolean mkFlag = parentDir.mkdirs();
            Log.e("dir", "mkFlag:" + mkFlag);
        }
        return parentPath;
    }



    public static int getAppVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            int version = info.versionCode;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
