package com.tim.usong.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.stream.Collectors;

public class AutoStartUtil {
    private static final Logger logger = LoggerFactory.getLogger(AutoStartUtil.class);
    private static final String REG_RUN_KEY = "HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String VALUE = "uSongStageMonitor";

    public static void enableAutoStart(String path) throws IOException {
        File f = new File(path);
        if (f.exists() && f.getName().endsWith(".jar")) {
            String cmd = "reg add %s /v \"%s\" /d \"%s\" /t REG_SZ /f";
            String startApplicationCmd = "cmd /c START javaw -jar \"" + path + "\"";
            cmd = String.format(cmd, REG_RUN_KEY, VALUE, startApplicationCmd);
            Runtime.getRuntime().exec(cmd);
        } else {
            throw new FileNotFoundException(path);
        }
    }

    public static void disableAutoStart() throws IOException {
        String cmd = String.format("reg delete %s /v \"%s\" /f", REG_RUN_KEY, VALUE);
        Runtime.getRuntime().exec(cmd);
    }

    public static boolean isAutostartEnabled() {
        try {
            String cmd = String.format("reg query \"%s\" /v \"%s\"", REG_RUN_KEY, VALUE);
            Process process = Runtime.getRuntime().exec(cmd);
            String result = new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines().collect(Collectors.joining(""));
            return result != null && result.contains(VALUE) && result.contains(".jar");
        } catch (Exception e) {
            logger.error("Failed to query registry for autostart entry", e);
            return false;
        }
    }
}
