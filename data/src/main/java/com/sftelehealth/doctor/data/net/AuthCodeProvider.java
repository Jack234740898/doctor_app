package com.sftelehealth.doctor.data.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.securepreferences.SecurePreferences;
import com.sftelehealth.doctor.data.BuildConfig;

/**
 * Created by Rahul on 27/12/17.
 */

public class AuthCodeProvider {

    private static String authCode = null;

    public static String getAuthCode(Context context) {
        if(authCode == null) {
            authCode = getSharedPreference(context).getString("auth_code", "");
        }
        return authCode;
    }

    public static String setAuthCode(Context context) {
        if(authCode != null) {
            getSharedPreference(context).edit().putString("auth_code", null).apply();
            authCode = null;
        }
        return authCode;
    }

    public static SharedPreferences getSharedPreference(Context context) {

        return context.getSharedPreferences(BuildConfig.APP_ENVIRONMENT.replace(".data","")+".user_prefs", Context.MODE_PRIVATE);
    }


    // Old SharedPreference for transition
    public static SharedPreferences getOldSharedPreference(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return new SecurePreferences(context, "@h>wPJ_P;YW48/z3", BuildConfig.APP_ENVIRONMENT.replace(".data","") + ".user_prefs_encrypted");
        } else {
            return context.getSharedPreferences(context + ".user_prefs_encrypted", Context.MODE_PRIVATE);
        }
    }
}
