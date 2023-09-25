package com.cominatyou.skip;

import android.graphics.drawable.Icon;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.NonNull;

public class QuickSettingsTileService extends TileService {
    private String cameraId;
    private CameraManager cameraManager;
    private boolean isTorchOn = false;
    private final CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
        @Override
        public void onTorchModeUnavailable(@NonNull String cameraId) {
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
            getQsTile().setSubtitle(getString(R.string.qs_title_subtitle_unavailable));
            getQsTile().setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_flashlight_off));
            getQsTile().updateTile();
        }

        @Override
        public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
            isTorchOn = enabled;
            getQsTile().setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            getQsTile().setSubtitle(getString(enabled ? R.string.qs_tile_subtitle_on : R.string.qs_tile_subtitle_off));
            getQsTile().setIcon(Icon.createWithResource(getApplicationContext(), enabled ? R.drawable.ic_flashlight_on : R.drawable.ic_flashlight_off));
            getQsTile().updateTile();
        }
    };

    @Override
    public void onClick() {
        super.onClick();
        isTorchOn = !isTorchOn;
        getQsTile().setState(isTorchOn ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        getQsTile().setIcon(Icon.createWithResource(getApplicationContext(), isTorchOn ? R.drawable.ic_flashlight_on : R.drawable.ic_flashlight_off));
        getQsTile().updateTile();
        try {
            cameraManager.setTorchMode(cameraId, isTorchOn);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        cameraManager = getSystemService(CameraManager.class);
        cameraManager.registerTorchCallback(torchCallback, null);

        try {
            final String[] cameraIds = cameraManager.getCameraIdList();
            cameraId = cameraIds[0];
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        cameraManager.unregisterTorchCallback(torchCallback);
    }
}
