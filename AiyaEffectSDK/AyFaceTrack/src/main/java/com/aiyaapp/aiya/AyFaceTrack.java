package com.aiyaapp.aiya;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class AyFaceTrack {

    static {
        System.loadLibrary("AyFaceTrackJni");
    }

    /**
     * 初始化人脸识别
     */
    public static void init(Context context) {
        File folder = context.getExternalCacheDir();
        if (folder == null) {
            folder = context.getCacheDir();
        }

        if (folder != null) {
            String dstPath = folder.getAbsolutePath() + "/aiya/config";
            deleteFile(new File(dstPath));
            copyFileFromAssets("config", dstPath, context.getAssets());

            init(dstPath);
        }
    }

    private static native void init(String dstPath);

    /**
     * 释放人脸识别
     */
    public static native void deinit();

    /**
     * 人脸数据指针的指针
     */
    public static native long faceData();

    /**
     * 人脸识别
     */
    public static native void trackWithBGRABuffer(ByteBuffer pixelBuffer, int width, int height);


    private static void deleteFile(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    deleteFile(f);
                }
            } else {
                boolean _ = file.delete();
            }
        }
    }

    private static boolean copyFileFromAssets(String src, String dst, AssetManager manager) {
        try {
            String[] files = manager.list(src);
            if (files.length > 0) {     //如果是文件夹
                File folder = new File(dst);
                if (!folder.exists()) {
                    boolean b = folder.mkdirs();
                    if (!b) {
                        return false;
                    }
                }
                for (String fileName : files) {
                    if (!copyFileFromAssets(src + File.separator + fileName, dst +
                            File.separator + fileName, manager)) {
                        return false;
                    }
                }
            } else {  //如果是文件
                if (!copyAssetsFile(src, dst, manager)) {
                    return false;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private static boolean copyAssetsFile(String src, String dst, AssetManager manager) {
        InputStream in;
        OutputStream out;
        try {
            File file = new File(dst);
            if (!file.exists()) {
                in = manager.open(src);
                out = new FileOutputStream(dst);
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
                out.close();
                in.close();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
