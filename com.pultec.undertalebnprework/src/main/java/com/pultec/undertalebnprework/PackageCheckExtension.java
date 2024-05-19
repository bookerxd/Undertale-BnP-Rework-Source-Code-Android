package com.pultec.undertalebnprework;

import android.util.Log;
import android.os.Build;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageCheckExtension {

    public double package_is_installed(String targetPackage) {
        PackageManager manager = RunnerActivity.CurrentActivity.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i("yoyo", "Package '" + targetPackage + "': NOT FOUND");
            return 0;
        }
        Log.i("yoyo", "Package '" + targetPackage + "': FOUND!");
        return 1;
    }

    public String getDeviceArchitecture() {
        String[] supportedAbis = Build.SUPPORTED_ABIS;
        if (supportedAbis == null || supportedAbis.length == 0) {
            return "Unknown";
        }

        String abi = supportedAbis[0];
        switch (abi) {
            case "arm64-v8a":
                return "ARM64";
            case "armeabi-v7a":
                return "ARMv7";
            case "armeabi":
                return "armebi";
            case "x86":
                return "x86";
            case "x86_64":
                return "x86_64";
            default:
                return "Unknown";
        }
    }

}
/*import android.util.Log;
import android.app.Activity;
import android.os.Build;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.content.Intent;

public class PackageCheckExtension {
    public double package_is_installed(String targetPackage) {
        PackageManager manager = RunnerActivity.CurrentActivity.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i("yoyo", "Package '" + targetPackage + "': NOT FOUND");
            return 0;
        }
        Log.i("yoyo", "Package '" + targetPackage + "': FOUND!");
        return 1;
    }



    public String getDeviceArchitecture() {
        String abi = Build.SUPPORTED_ABIS[0];
        if (abi.equals("arm64-v8a")) {
            return "ARM64";
        } else if (abi.equals("armeabi-v7a") || abi.equals("armeabi")) {
            return "ARMv7";
        } else if (abi.equals("x86")) {
            return "x86";
        } else if (abi.equals("x86_64")) {
            return "x86_64";
        } else {
            return "Unknown";
        }
    }

    public static int uninstallApp(String packageName) {
        PackageManager packageManager = RunnerActivity.CurrentActivity.getPackageManager();
        try {
            // Verifica si la aplicación está instalada
            packageManager.getPackageInfo(packageName, 0);

            // Inicia el proceso de desinstalación
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + packageName));
            RunnerActivity.CurrentActivity.startActivity(intent);

            return 1; // Retorna 1 indicando que la desinstalación se inició correctamente
        } catch (PackageManager.NameNotFoundException e) {
            // Maneja la situación donde la aplicación no se encuentra instalada
            e.printStackTrace();
            return 0; // Retorna 0 indicando que la aplicación no está instalada
        }
    }
}*/
