package com.tim.usong.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tim.usong.USongApplication;
import com.tim.usong.ui.UsongTray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class UpdateApplicationUtil {
    private static final String releasesUrl =
            "https://api.github.com/repos/tim4724/u-song-Stage-Monitor-for-Songbeamer/releases";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
    private final Preferences prefs = Preferences.userNodeForPackage(USongApplication.class).node("update");
    private final UsongTray usongTray;

    public UpdateApplicationUtil(UsongTray usongTray) {
        this.usongTray = usongTray;
    }

    public void checkForUpdateAsync() {
        new Thread(this::checkForUpdate).start();
    }

    private void checkForUpdate() {
        if (USongApplication.APP_VERSION == null) {
            return; // this is a debug build
        }

        try {
            URL url = new URL(releasesUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                String responseBody = readResponseBody(conn);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode json = mapper.readTree(responseBody);
                    handleGithubResponse(json);
                } catch (Exception e) {
                    logger.error("Failed to parse github api response", e);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to check on github for updates", e);
        }
    }

    private String readResponseBody(HttpURLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    private void handleGithubResponse(JsonNode json) {
        String currentVersion = USongApplication.APP_VERSION;

        String tagName = null;
        String downloadUrl = null;
        String fileName = null;
        for (JsonNode commit : json) {
            String tag = commit.get("tag_name").asText();
            String branch = commit.get("target_commitish").asText();
            if ("master".equals(branch)
                    && tag.compareTo(currentVersion) > 0
                    && !tag.equals(prefs.get("DoNotAskUpdate", null))) {
                tagName = tag;

                JsonNode assets = commit.get("assets");
                for (JsonNode asset : assets) {
                    String name = asset.get("name").asText();
                    if (name.endsWith(tagName + ".jar")) {
                        fileName = name;
                        downloadUrl = asset.get("browser_download_url").asText();
                        break;
                    }
                }
                break;
            }
        }

        if (tagName != null && downloadUrl != null) {
            String title = USongApplication.APP_NAME;
            String body = messages.getString("updateAvailable");
            body = String.format(body, tagName, currentVersion);

            Object[] options = {
                    messages.getString("no"),
                    messages.getString("never"),
                    messages.getString("yes")};

            int result = JOptionPane.showOptionDialog(
                    null, body, title, JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[2]);

            if (result == 2) {
                File currentJar = new File(USongApplication.getCurrentJarPath());
                File destination = new File(currentJar.getParentFile(), fileName);
                downloadFile(downloadUrl, destination);
            } else if (result == 1) {
                prefs.put("DoNotAskUpdate", tagName);
            }
        }
    }

    private void downloadFile(String url, File dest) {
        try {
            HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                InputStream in = conn.getInputStream();
                Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if (AutoStartUtil.isAutostartEnabled()) {
                    AutoStartUtil.enableAutoStart(dest.getAbsolutePath());
                }
                usongTray.showInfo(messages.getString("downloadSuccess"));
            }
        } catch (IOException e) {
            logger.error("Error downloading update", e);
            USongApplication.showErrorDialogAsync(messages.getString("downloadUpdateError"), e);
        }
    }
}
