package com.tim.usong.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tim.usong.USongApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class UpdateChecker {
    private static final String releasesUrl =
            "https://api.github.com/repos/tim4724/u-song-Stage-Monitor-for-Songbeamer/releases";

    private UpdateChecker() {
    }

    public static void checkForUpdateAsync() {
        new Thread(UpdateChecker::checkForUpdate).start();
    }

    private static void checkForUpdate() {
        if (USongApplication.APP_VERSION == null) {
            return; // this is a debug build
        }
        try {
            URL url = new URL(releasesUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                String responseBody = readResponseBody(conn);
                JsonNode json = new ObjectMapper().readTree(responseBody);
                handleGithubResponse(json);
            }
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(UpdateChecker.class);
            logger.error("Failed to check on github for updates", e);
        }
    }

    private static String readResponseBody(HttpURLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    private static void handleGithubResponse(JsonNode json) {
        Preferences prefs = Preferences.userNodeForPackage(USongApplication.class).node("update");
        String currentVersion = USongApplication.APP_VERSION;
        JsonNode latestRelease = json.get(0);
        String tagName = latestRelease.get("tag_name").asText();
        String branch = latestRelease.get("target_commitish").asText();
        if (!"master".equals(branch) || tagName.compareTo(currentVersion) <= 0 ||
                tagName.equals(prefs.get("do_not_ask_update", null))) {
            return;
        }

        ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
        String title = USongApplication.APP_NAME;
        String body = messages.getString("updateAvailable");
        body = String.format(body, tagName, currentVersion);

        Object[] options = {messages.getString("yes"), messages.getString("notNow"),
                messages.getString("never"),};

        int result = JOptionPane.showOptionDialog(null, body, title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (result == 0) {
            Browse.open("https://github.com/tim4724/u-song-Stage-Monitor-for-Songbeamer/releases");
        } else if (result == 2) {
            prefs.put("do_not_ask_update", tagName);
        }
    }
}
