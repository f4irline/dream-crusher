package com.github.f4irline.dreamcrusher.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.github.f4irline.dreamcrusher.BuildConfig;
import com.github.f4irline.dreamcrusher.R;

/**
 * Handles logging for the application.
 */
public class Debug {

    private static int DEBUG_LEVEL;
    private static boolean DEBUG_GUI;

    /**
     *
     * @param className - Class where the debugging is happening.
     * @param methodName - Method which is being debugged currently.
     * @param msg - Message.
     * @param level - Level of the debug.
     * @param host - The host app.
     */
    public static void print (String className, String methodName, String msg, int level, Context host) {
        if (BuildConfig.DEBUG) {
            if (level <= DEBUG_LEVEL) {
                if (DEBUG_GUI) {
                    Toast.makeText(host, className + ": " + methodName + ", "+ msg, Toast.LENGTH_LONG).show();
                } else {
                    Log.d(className, methodName+", "+ msg);
                }
            }
        }
    }

    /**
     * Initializes the debug class.
     *
     * Gets values for DEBUG_LEVEL and DEBUG_GUI from the Debug.xml resource.
     *
     * @param host - The host app.
     */
    public static void loadDebug (Context host) {
        DEBUG_LEVEL = host.getResources().getInteger(R.integer.debug_level);
        DEBUG_GUI = host.getResources().getBoolean(R.bool.debug_gui);
    }
}
