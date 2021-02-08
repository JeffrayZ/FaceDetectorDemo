package com.test.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.FaceDetector;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
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

public class MainActivity2 extends AppCompatActivity {
    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private int REQUEST_CODE_PERMISSIONS = 10;
    private PreviewView previewView;
    private String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private FaceImageView imageView;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        previewView = findViewById(R.id.previewView);
        imageView = findViewById(R.id.iv_shibie);
        // 请求相机权限
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        findViewById(R.id.btn_shibie).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inSampleSize = 4;
                Bitmap mBitmap = BitmapFactory.decodeFile(path, options);
                imageView.setImageBitmap(mBitmap);
                FaceDetector faceDetector = new FaceDetector(mBitmap.getWidth(),
                        mBitmap.getHeight(), 5);
                FaceDetector.Face[] faces = new FaceDetector.Face[5];
                int face = faceDetector.findFaces(mBitmap, faces);
                Toast.makeText(MainActivity2.this, face + "", Toast.LENGTH_SHORT).show();
                imageView.setFace(faces[0]);
            }
        });
        findViewById(R.id.btn_paizhao).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        CountDownTimer timer = new CountDownTimer(30*1000*60,10*1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                takePhoto();
            }

            @Override
            public void onFinish() {

            }
        }.start();
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
                        Toast.makeText(MainActivity2.this,
                                "保存图片成功",
                                Toast.LENGTH_SHORT).show();
                        Uri savedUri;
                        if (outputFileResults.getSavedUri() == null) {
                            savedUri = Uri.fromFile(photoFile);
                        } else {
                            savedUri = outputFileResults.getSavedUri();
                        }
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                                "jpg");
                        MediaScannerConnection.scanFile(getApplicationContext(),
                                new String[]{new File(savedUri.getPath()).getAbsolutePath()},
                                new String[]{mimeType},
                                new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                final String threadName = Thread.currentThread().getName();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity2.this, path,
                                                Toast.LENGTH_SHORT).show();
                                        MainActivity2.this.path = path;


                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                                        options.inSampleSize = 4;
                                        Bitmap mBitmap = BitmapFactory.decodeFile(path, options);
                                        imageView.setImageBitmap(mBitmap);
                                        FaceDetector faceDetector = new FaceDetector(mBitmap.getWidth(),
                                                mBitmap.getHeight(), 5);
                                        FaceDetector.Face[] faces = new FaceDetector.Face[5];
                                        int face = faceDetector.findFaces(mBitmap, faces);
                                        Toast.makeText(MainActivity2.this, face + "", Toast.LENGTH_SHORT).show();
                                        imageView.setFace(faces[0]);
                                    }
                                });

                            }
                        });

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(MainActivity2.this,
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
                    // 默认选择后置摄像头，这边可以设置方法选择摄像头
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                    cameraProvider.unbindAll();
                    // 将用例绑定到相机
                    cameraProvider.bindToLifecycle(MainActivity2.this, cameraSelector, preview,
                            imageCapture);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
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