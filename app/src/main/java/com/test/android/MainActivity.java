package com.test.android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * @author feifei.zhan
 */
public class MainActivity extends AppCompatActivity {
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;
    private File outputDirectory;
    private ExecutorService cameraExecutor;
    private String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private int REQUEST_CODE_PERMISSIONS = 10;
    private PreviewView previewView;
    private String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);

        // 请求相机权限
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeVideo();
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analysis();
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(MainActivity.this,MainActivity2.class);
               startActivity(intent);
            }
        });
    }

    private void analysis() {
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {

                Toast.makeText(MainActivity.this,
                        image.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e("MainActivity",image.toString());
            }
        });
    }

    private void takeVideo() {

    }

    private void takePhoto() {
        File photoFile = new File(getOutputDirectory(), new SimpleDateFormat(FILENAME_FORMAT,
                Locale.US).format(System.currentTimeMillis()) + ".jpg");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Toast.makeText(MainActivity.this,
                        "保存图片成功",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(MainActivity.this,
                        exception.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getOutputDirectory() {
        File mediaDir = new File(getExternalMediaDirs()[0], getString(R.string.app_name));
        mediaDir.mkdirs();
        if (mediaDir == null) {
            return getFilesDir();
        }
        return mediaDir;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> instance = ProcessCameraProvider.getInstance(this);
        instance.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = instance.get();
                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                    imageCapture = new ImageCapture.Builder().build();
                    imageAnalysis = new ImageAnalysis.Builder().build();
                    // 默认选择后置摄像头，这边可以设置方法选择摄像头
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                    cameraProvider.unbindAll();
                    // 将用例绑定到相机
                    cameraProvider.bindToLifecycle(MainActivity.this, cameraSelector, preview,
                            imageCapture,imageAnalysis);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
    }

    private boolean allPermissionsGranted() {
        for (String required_permission : REQUIRED_PERMISSIONS) {
            int status = ContextCompat.checkSelfPermission(this, required_permission);
            if (status != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}