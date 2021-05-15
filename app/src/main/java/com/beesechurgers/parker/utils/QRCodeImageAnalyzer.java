/*
 * Copyright (c) 2021, Beesechurgers <https://github.com/Beesechurgers>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package com.beesechurgers.parker.utils;

import android.graphics.ImageFormat;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Credits: https://github.com/learntodroid/AndroidQRCodeScanner
 */
public class QRCodeImageAnalyzer implements ImageAnalysis.Analyzer {

    public interface QRCodeListener {
        void onFound(String data);
    }

    private final QRCodeListener listener;

    public QRCodeImageAnalyzer(QRCodeListener listener) {
        this.listener = listener;
    }

    @Override
    public void analyze(@NonNull @NotNull ImageProxy image) {
        if (image.getFormat() == ImageFormat.YUV_420_888 ||
            image.getFormat() == ImageFormat.YUV_422_888 ||
            image.getFormat() == ImageFormat.YUV_444_888) {

            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            byte[] imageData = new byte[byteBuffer.capacity()];
            byteBuffer.get(imageData);

            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(imageData, image.getWidth(),
                image.getHeight(), 0, 0, image.getWidth(), image.getHeight(), false);

            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            try {
                listener.onFound(new QRCodeMultiReader().decode(binaryBitmap).getText());
            } catch (Exception ignored) {
            }
        }

        image.close();
    }
}
