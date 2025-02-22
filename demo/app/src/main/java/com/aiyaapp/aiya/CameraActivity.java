package com.aiyaapp.aiya;

import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;

import com.aiyaapp.aiya.cameraTool.AYCameraPreviewListener;
import com.aiyaapp.aiya.cameraTool.AYCameraPreviewWrap;
import com.aiyaapp.aiya.cameraTool.AYPreviewView;
import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;

import java.io.IOException;

import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageContentMode.kAYGPUImageScaleAspectFill;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageRotateRight;

public class CameraActivity extends AppCompatActivity implements AYCameraPreviewListener, SurfaceHolder.Callback {

    Camera camera;
    AYCameraPreviewWrap cameraPreviewWrap;
    AYPreviewView surfaceView;

    AYEffectHandler effectHandler;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        surfaceView = findViewById(R.id.camera_preview);

        surfaceView.getHolder().addCallback(this);
        surfaceView.setContentMode(kAYGPUImageScaleAspectFill);
    }

    /**
     * 打开硬件设备
     */
    private void openHardware() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        camera = Camera.open(1);

        cameraPreviewWrap = new AYCameraPreviewWrap(camera);
        cameraPreviewWrap.setPreviewListener(this);
        cameraPreviewWrap.setRotateMode(kAYGPUImageRotateRight);
        cameraPreviewWrap.startPreview(surfaceView.eglContext);
    }

    /**
     * 关闭硬件设备
     */
    private void closeHardware() {
        // 关闭相机
        if (camera != null) {
            cameraPreviewWrap.stopPreview();
            cameraPreviewWrap = null;
            camera.release();
            camera = null;
        }
    }

    @Override
    public void cameraCrateGLEnvironment() {

        effectHandler = new AYEffectHandler(this);
        effectHandler.setRotateMode(AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageFlipVertical);
        // 设置特效
        effectHandler.setEffectPath(getExternalCacheDir() + "/aiya/effect/xiaohongmao/meta.json");
        effectHandler.setEffectPlayCount(2);
        // 设置美颜程度
        effectHandler.setBeautyType(AyBeauty.AY_BEAUTY_TYPE.AY_BEAUTY_TYPE_3);
        effectHandler.setIntensityOfSmooth(0.8f);
        effectHandler.setIntensityOfSaturation(0.2f);
        effectHandler.setIntensityOfWhite(0f);

        // 设置大眼瘦脸
        effectHandler.setIntensityOfBigEye(0.2f);
        effectHandler.setIntensityOfSlimFace(0.8f);

        try {
            // 添加滤镜
            effectHandler.setStyle(BitmapFactory.decodeStream(getApplicationContext().getAssets().open("FilterResources/filter/03桃花.JPG")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cameraVideoOutput(int texture, int width, int height, long timeStamp) {
        // 渲染特效美颜
        if (effectHandler != null) {
            effectHandler.processWithTexture(texture, width, height);
        }

        // 渲染到surfaceView
        if (surfaceView != null) {
            surfaceView.render(texture, width, height);
        }
    }

    @Override
    public void cameraDestroyGLEnvironment() {
        if (effectHandler != null) {
            effectHandler.destroy();
            effectHandler = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openHardware();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        closeHardware();
    }
}
