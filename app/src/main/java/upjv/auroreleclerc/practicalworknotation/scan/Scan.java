package upjv.auroreleclerc.practicalworknotation.scan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

abstract class Scan extends AppCompatActivity {

    // https://medium.com/analytics-vidhya/creating-a-barcode-scanner-using-android-studio-71cff11800a2
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private ToneGenerator toneGen1;
    private String barcodeData;
    private int format;

    public abstract void dataExtractionToView(String barcodeData, SurfaceView surfaceView);

    private void initialiseDetectorsAndSources(SurfaceView surfaceView) {
        this.initialiseDetectorsAndSources(surfaceView, Barcode.ALL_FORMATS);
    }
    void initialiseDetectorsAndSources(SurfaceView surfaceView, int format) {
        this.format = format;
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        this.surfaceView = surfaceView;

        //Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(format)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(Scan.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(Scan.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                // Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    barcodes.valueAt(0);
                    dataExtractionToView(barcodeData, surfaceView);
                    barcodeData = barcodes.valueAt(0).email.address;
                    dataExtractionToView(barcodeData, surfaceView);
                        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSupportActionBar().hide();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().hide();
        initialiseDetectorsAndSources(this.surfaceView, this.format);
    }

    // https://github.com/googlesamples/mlkit/blob/master/android/codescanner/app/src/main/java/com/google/mlkit/samples/codescanner/java/MainActivity.java
    /* public void addStudent(View view) {
        GmsBarcodeScannerOptions.Builder optionsBuilder = new GmsBarcodeScannerOptions.Builder();
        optionsBuilder.setBarcodeFormats(Barcode.FORMAT_ITF);
        optionsBuilder.allowManualInput();
        GmsBarcodeScanner gmsBarcodeScanner =
                GmsBarcodeScanning.getClient(this, optionsBuilder.build());
        gmsBarcodeScanner
                .startScan()
                .addOnSuccessListener(barcode -> {
                    Intent intention = new Intent(this,
                            AddStudent.class);
                    intention.putExtra("id", barcode.getRawValue());
                    startActivityForResult(intention, REQUEST_CODE);
                })
                .addOnFailureListener(
                        error -> Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show())
                .addOnCanceledListener(
                        () -> Toast.makeText(MainActivity.this, "Action annulée", Toast.LENGTH_SHORT).show());
    } */
}
