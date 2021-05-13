package com.beesechurgers.parker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;

import org.jetbrains.annotations.NotNull;

public class App extends Application implements CameraXConfig.Provider {

    @NonNull
    @NotNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }
}
