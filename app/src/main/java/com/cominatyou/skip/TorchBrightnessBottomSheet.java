package com.cominatyou.skip;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.cominatyou.skip.databinding.BottomSheetTorchBrightnessBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TorchBrightnessBottomSheet extends BottomSheetDialogFragment {
    private final MainActivity parent;
    private final CameraManager cameraManager;
    private CameraManager.TorchCallback torchCallback;
    private boolean shouldRunSwitchChangedHandler = true;
    boolean shouldRunBrightnessSliderInit = true;

    public TorchBrightnessBottomSheet(MainActivity parent, CameraManager cameraManager) {
        this.parent = parent;
        this.cameraManager = cameraManager;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final BottomSheetTorchBrightnessBinding binding = BottomSheetTorchBrightnessBinding.inflate(inflater);
        final String[] cameraIds;

        try {
            cameraIds = cameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }

        torchCallback = new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeUnavailable(@NonNull String cameraId) {
                super.onTorchModeUnavailable(cameraId);
                binding.torchLayout.setEnabled(false);
                binding.torchLayout.setAlpha(0.4f);
                binding.torchSwitch.setChecked(false);
                binding.torchSwitch.setEnabled(false);
                binding.brightnessSlider.setEnabled(false);
                binding.brightnessSlider.setValue(0);
                binding.brightnessLayout.setAlpha(0.4f);
            }

            @Override
            public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
                super.onTorchModeChanged(cameraId, enabled);
                binding.torchLayout.setEnabled(true);
                binding.torchLayout.setAlpha(1f);
                binding.torchSwitch.setEnabled(true);
                shouldRunSwitchChangedHandler = false;
                binding.torchSwitch.setChecked(enabled);
                shouldRunSwitchChangedHandler = true;
                binding.brightnessSlider.setEnabled(true);
                binding.brightnessLayout.setAlpha(1f);

                try {
                    if (!shouldRunBrightnessSliderInit) return;

                    final CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraIds[0]);
                    final int maxBrightnessLevel = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL);

                    if (enabled) {
                        Log.d("TorchBrightnessSliderInit", "Torch is currently ON");
                        Log.d("TorchBrightnessSliderInit", "Torch has a max strength level of " + maxBrightnessLevel);
                        final int currentBrightnessLevel = cameraManager.getTorchStrengthLevel(cameraIds[0]);
                        Log.d("TorchBrightnessSliderInit", "Torch is currently at strength level of " + currentBrightnessLevel);

                        final float newSliderValue = (float) Math.floor(currentBrightnessLevel / (maxBrightnessLevel / 5.0));

                        getContext().getSharedPreferences("brightness_data", Context.MODE_PRIVATE).edit().putFloat("last_strength", newSliderValue).apply();
                        binding.brightnessSlider.setValue(newSliderValue);
                    }
                    shouldRunBrightnessSliderInit = false;
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        cameraManager.registerTorchCallback(torchCallback, null);

        binding.torchLayout.setOnClickListener(v -> binding.torchSwitch.toggle());
        binding.torchSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!shouldRunSwitchChangedHandler) return;

            try {
                final CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraIds[0]);
                final int maxStrengthLevel = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL);
                final int defaultStrengthLevel = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL);

                final float lastStrengthLevel = getContext().getSharedPreferences("brightness_data", Context.MODE_PRIVATE).getFloat("last_strength", (float) Math.floor(defaultStrengthLevel / (maxStrengthLevel / 5.0)));
                binding.brightnessSlider.setValue(isChecked ? lastStrengthLevel : 0);

                if (!isChecked) {
                    Log.d("TorchSwitch", "Turning off torch");
                    cameraManager.setTorchMode(cameraIds[0], false);
                }
                else {
                    Log.d("TorchSwitch", "Turning on torch with strength level of " + (int) lastStrengthLevel);
                    cameraManager.turnOnTorchWithStrengthLevel(cameraIds[0], (int) Math.floor(lastStrengthLevel * maxStrengthLevel / 5.0));
                }
            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            final CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraIds[0]);
            final int maxBrightnessLevel = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL);

            final double incrementValue = maxBrightnessLevel / 5.0;

            binding.brightnessSlider.addOnChangeListener((self, value, fromUser) -> {
                if (!fromUser) return;

                if (value == 0) {
                    shouldRunSwitchChangedHandler = false;
                    binding.torchSwitch.setChecked(false);
                    shouldRunSwitchChangedHandler = true;
                    try {
                        Log.d("BrightnessSlider", "Turning off torch");
                        cameraManager.setTorchMode(cameraIds[0], false);
                    } catch (CameraAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    shouldRunSwitchChangedHandler = false;
                    binding.torchSwitch.setChecked(true);
                    shouldRunSwitchChangedHandler = true;
                    final int brightness = (int) Math.floor(value * incrementValue);
                    getContext().getSharedPreferences("brightness_data", Context.MODE_PRIVATE).edit().putFloat("last_strength", value).apply();

                    try {
                        Log.d("BrightnessSlider", "Turning on torch with strength level of " + brightness);
                        cameraManager.turnOnTorchWithStrengthLevel(cameraIds[0], brightness);
                    } catch (CameraAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        }
        catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }

        return binding.getRoot();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        cameraManager.unregisterTorchCallback(torchCallback);
        parent.finish();
    }
}
