package cu.axel.smartdock.utils;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import com.google.android.material.color.DynamicColors;

import java.util.ArrayList;

import cu.axel.smartdock.R;

public class ColorUtils {

    public static ArrayList<String> getWallpaperColors(Context context) {

        ArrayList<String> wallpaperColors = new ArrayList<>();
		/*
		 Generate Wallpaper colors based on light and dark variation
		 Accomplished by inspecting pixels in Wallpaper Bitmap without AndroidX palette library
		 You will want to use a factor less than 1.0f to darken. try 0.8f.
		
		 */

        if (DeviceUtils.hasStoragePermission(context)) {

            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            //noinspection all
            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            wallpaperDrawable.mutate();
            wallpaperDrawable.invalidateSelf();
            Bitmap wallpaperBitmap = drawableToBitmap(wallpaperDrawable);

            int color = wallpaperBitmap.getPixel(wallpaperBitmap.getWidth() / 4, wallpaperBitmap.getHeight() / 4);

            wallpaperColors.add(toHexColor(color));
            wallpaperColors.add(toHexColor(manipulateColor(color, .8f)));
            wallpaperColors.add(toHexColor(manipulateColor(color, .5f)));

        }

        return wallpaperColors;
    }

    //noinspection ResourceType
    public static int[] getThemeColors(Context context, boolean forceDark) {
        int[] colors = new int[3];

        int variant = forceDark ? R.style.ThemeOverlay_Material3_DynamicColors_Dark
                : R.style.ThemeOverlay_Material3_DynamicColors_DayNight;
        Context styledContext = DynamicColors.wrapContextIfAvailable(context, variant);

        int[] attrsToResolve = {R.attr.colorPrimary, R.attr.colorSurface, R.attr.colorError};

        TypedArray attrs = styledContext.obtainStyledAttributes(attrsToResolve);
        colors[0] = attrs.getColor(0, 0);
        colors[1] = attrs.getColor(1, 0);
        colors[2] = attrs.getColor(2, 0);
        attrs.recycle();

        return colors;
    }

    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }

    public static int getBitmapDominantColor(Bitmap bitmap) {
        int color = bitmap.getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 9);
        if (color == Color.TRANSPARENT)
            color = bitmap.getPixel(bitmap.getWidth() / 4, bitmap.getHeight() / 2);
        if (color == Color.TRANSPARENT)
            color = bitmap.getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        return color;
    }

    public static int getDrawableDominantColor(Drawable drawable) {
        return getBitmapDominantColor(drawableToBitmap(drawable));
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void applyColor(View view, int color) {
        view.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public static int[] getMainColors(SharedPreferences sp, Context context) {
        String theme = sp.getString("theme", "dark");
        int mainColor = 0;
        int secondaryColor = 0;
        int alpha = 255;

        int[] colors = new int[5];
        switch (theme) {
            case "dark":
                mainColor = Color.parseColor("#212121");
                secondaryColor = ColorUtils.manipulateColor(mainColor, 1.35f);
                break;
            case "black":
                mainColor = Color.parseColor("#060606");
                secondaryColor = ColorUtils.manipulateColor(mainColor, 2.2f);
                break;
            case "transparent":
                mainColor = Color.parseColor("#050505");
                secondaryColor = ColorUtils.manipulateColor(mainColor, 2f);
                alpha = 225;
                break;
            case "material_u":
                if (DynamicColors.isDynamicColorAvailable()) {
                    int surfaceColor = getThemeColors(context, true)[1];
                    mainColor = ColorUtils.manipulateColor(surfaceColor, 0.9f);
                    secondaryColor = ColorUtils.manipulateColor(surfaceColor, 1.2f);
                } else {
                    mainColor = Color.parseColor(getWallpaperColors(context).get(2));
                    secondaryColor = Color.parseColor(getWallpaperColors(context).get(1));
                }
                break;
            case "custom":
                mainColor = Color.parseColor(sp.getString("theme_main_color", "#212121"));
                secondaryColor = ColorUtils.manipulateColor(mainColor, 1.2f);
                alpha = sp.getInt("theme_main_alpha", 255);
        }
        colors[0] = mainColor;
        colors[1] = alpha;
        colors[2] = secondaryColor;
        if (alpha < 255)
            alpha -= alpha * 0.60;
        //secondary color alpha
        colors[3] = alpha;
        //separator color
        colors[4] = theme.equals("black") ? colors[2] : ColorUtils.manipulateColor(colors[0], 0.8f);
        return colors;
    }

    public static void applyMainColor(Context context, SharedPreferences sp, View view) {
        int[] colors = getMainColors(sp, context);
        applyColor(view, colors[0]);
        view.getBackground().setAlpha(colors[1]);
    }

    public static void applySecondaryColor(Context context, SharedPreferences sp, View view) {
        int[] colors = getMainColors(sp, context);
        applyColor(view, colors[2]);
        int alpha = colors[3];

        view.getBackground().setAlpha(alpha);
    }

    public static int toColor(String color) {
        try {
            return Color.parseColor(color);
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    public static String toHexColor(int color) {
        return "#" + Integer.toHexString(color).substring(2);
    }

}
