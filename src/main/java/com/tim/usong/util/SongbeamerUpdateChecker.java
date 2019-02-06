package com.tim.usong.util;

import com.tim.usong.USongApplication;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class SongbeamerUpdateChecker {
    private static final String url = "https://www.songbeamer.de/download.htm";

    private SongbeamerUpdateChecker() {
    }

    public static void checkForUpdateAsync(final String songbeamerVersion) {
        new Thread(() -> checkForUpdate(songbeamerVersion)).start();
    }

    private static void checkForUpdate(String currentVersion) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements versionCandidates = doc.select("td > p > strong");
            String newestVersion = "0.0.0";
            for (Element element : versionCandidates) {
                if (element.hasText() && element.children().size() == 0) {
                    String version = element.text().trim();
                    if (version.matches("\\d.\\d\\d?\\D?")) {
                        newestVersion = version;
                        break;
                    }
                }
            }

            Preferences prefs = Preferences.userNodeForPackage(USongApplication.class)
                    .node("updateSongbeamer");
            if (newestVersion.equals(prefs.get("do_not_ask_update", null))) {
                return;
            }

            if (newestVersion.compareTo(currentVersion) > 0) {
                ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");

                String title = messages.getString("songbeamer");
                String body = messages.getString("updateAvailableSongbeamer");
                body = String.format(body, newestVersion, currentVersion);

                Object[] options = {messages.getString("yes"), messages.getString("notNow"),
                        messages.getString("never"),};

                int result = JOptionPane.showOptionDialog(null, body, title, JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                if (result == 0) {
                    Browser.open(url);
                } else if (result == 2) {
                    prefs.put("do_not_ask_update", newestVersion);
                }
            }
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(SongbeamerUpdateChecker.class);
            logger.error("Failed to check on songbeamer.de for updates", e);
        }
    }
}
