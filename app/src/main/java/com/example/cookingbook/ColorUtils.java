package com.example.cookingbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class ColorUtils {

    public interface OnColorExtractedListener {
        void onColorExtracted(int startColor, int endColor);
    }

    public static void extractColorsFromImage(Context context, Uri imageUri, OnColorExtractedListener listener) {
        Glide.with(context)
                .asBitmap()
                .load(imageUri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        Palette.Builder builder = Palette.from(resource);
                        builder.generate(palette -> {
                            if (palette != null) {
                                int dominantColor = palette.getDominantColor(0xFF6B6B);
                                int vibrantColor = palette.getVibrantColor(0xFFD93D);

                                // Create a lighter version for complementary gradient
                                int startColor = dominantColor;
                                int endColor = vibrantColor;

                                listener.onColorExtracted(startColor, endColor);
                            }
                        });
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                    }
                });
    }

    public static int adjustBrightness(int color, float factor) {
        int a = color >> 24 & 0xff;
        int r = color >> 16 & 0xff;
        int g = color >> 8 & 0xff;
        int b = color & 0xff;

        r = Math.min(255, (int) (r * factor));
        g = Math.min(255, (int) (g * factor));
        b = Math.min(255, (int) (b * factor));

        return a << 24 | r << 16 | g << 8 | b;
    }
}