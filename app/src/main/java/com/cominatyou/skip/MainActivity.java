package com.cominatyou.skip;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.color.DynamicColors;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        final CameraManager cameraManager = getSystemService(CameraManager.class);
        String[] cameraIds;

        boolean hasFlash;
        int maximumStrength;

        try {
            cameraIds = cameraManager.getCameraIdList();
            final CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraIds[0]);
            hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            maximumStrength = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }

        Log.d("SheetRouter", "Device has a flash: " + hasFlash);
        if (hasFlash) {
            Log.d("SheetRouter", "Maximum strength of flash: " + maximumStrength);
        }

        if (!hasFlash || maximumStrength == 1) {
            new NotSupportedBottomSheet(this)
                    .show(getSupportFragmentManager(), "NotSupportedBottomSheet");
        }
        else {
            new TorchBrightnessBottomSheet(this, cameraManager)
                    .show(getSupportFragmentManager(), "FlashlightBrightnessBottomSheet");
        }
    }
}