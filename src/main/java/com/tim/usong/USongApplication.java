package com.tim.usong;

import com.tim.usong.core.ui.PreviewFrame;
import com.tim.usong.core.SongParser;
import com.tim.usong.core.SongbeamerListener;
import com.tim.usong.core.ui.SelectSongDirectoryDialog;
import com.tim.usong.core.ui.UsongTray;
import com.tim.usong.core.ui.SplashWindow;
import com.tim.usong.resource.RootResource;
import com.tim.usong.resource.SongResource;
import com.tim.usong.resource.StatusResource;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.dropwizard.websockets.WebsocketBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.net.BindException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class USongApplication extends Application<Configuration> {
    public static final String APP_NAME = USongApplication.class.getPackage().getImplementationTitle();
    public static final String APP_VERSION = USongApplication.class.getPackage().getImplementationVersion();
    public static final String LOCAL_DIR = System.getenv("APPDATA") + "\\uSongServer\\";
    private static final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
    private static final Preferences prefs = Preferences.userNodeForPackage(USongApplication.class);
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws Exception {
        Setup.setUpRequiredExternalFiles();
        Setup.setUpUI();
        SplashWindow.showSplash();
        if (args.length == 0) {
            args = new String[]{"server", LOCAL_DIR + "usong.yml"};
        }
        new USongApplication().run(args);
    }

    public static void saveSongDir(File songDir) {
        if (songDir != null) {
            prefs.put("songDir", songDir.getAbsolutePath());
        } else {
            prefs.remove("songDir");
        }
    }

    public static void showErrorDialogAsync(String text, Object extra) {
        new Thread(() -> showErrorDialog(text, extra)).start();
    }

    private static void showErrorDialog(String text, Object extra) {
        showErrorDialog(extra != null ? text + "\n" + extra : text);
    }

    private static void showErrorDialog(String body) {
        SplashWindow.error();
        JOptionPane.showMessageDialog(null, body, APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new WebsocketBundle(SongResource.SongWebSocket.class));
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor())
        );
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new ViewBundle<>());
    }

    @Override
    public void run(Configuration config, Environment environment) {
        SongBeamerSettings sBSettings = readSongBeamerSettings();

        String prefsSongDir = prefs.get("songDir", null);
        File prefsSongDirFile = prefsSongDir != null ? new File(prefsSongDir) : null;
        String sbSongDir = sBSettings.songDir;
        File sbSongDirFile = sbSongDir != null ? new File(sbSongDir) : null;

        if (prefsSongDirFile != null && sbSongDirFile != null
                && prefsSongDirFile.exists() && sbSongDirFile.exists()) {

            if (!prefsSongDirFile.equals(sbSongDirFile)) {
                String body = messages.getString("whichSongDir");
                body = MessageFormat.format(body, prefsSongDir, sbSongDir);
                int result = JOptionPane.showConfirmDialog(null, body, APP_NAME,
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    prefsSongDir = null;
                    prefsSongDirFile = null;
                    saveSongDir(null);
                }
            }
        }

        SongParser songParser;
        if (prefsSongDirFile != null && prefsSongDirFile.exists()) {
            songParser = new SongParser(prefsSongDirFile);
            logger.info("Using songs directory: " + prefsSongDir);
        } else if (sbSongDirFile != null && sbSongDirFile.exists()) {
            songParser = new SongParser(sbSongDirFile);
            logger.info("Using songs directory: " + sbSongDir);
        } else {
            prefsSongDirFile = new SelectSongDirectoryDialog().getDirectory();
            if (prefsSongDirFile != null && prefsSongDirFile.exists()) {
                saveSongDir(prefsSongDirFile);
                prefsSongDir = prefsSongDirFile.getAbsolutePath();
                songParser = new SongParser(prefsSongDirFile);
            } else {
                logger.error("No directory for songs found.");
                showErrorDialog(messages.getString("songsDirNotFoundError"));
                System.exit(-1);
                return;
            }
        }

        try {
            PreviewFrame previewFrame = new PreviewFrame();
            SongResource songResource = new SongResource(songParser);
            SongbeamerListener sBListener = new SongbeamerListener(songResource);
            UsongTray usongTray = new UsongTray(previewFrame);
            StatusResource statusResource = new StatusResource(sBListener, songResource,
                    songParser, previewFrame, sBSettings);

            environment.jersey().register(new RootResource());
            environment.jersey().register(songResource);
            environment.jersey().register(statusResource);
            environment.lifecycle().manage(sBListener);
            environment.lifecycle().manage(previewFrame);
            environment.lifecycle().manage(usongTray);
            environment.lifecycle().addServerLifecycleListener(server -> {
                SplashWindow.started();
            });
        } catch (BindException e) {
            logger.error("Application already running", e);
            showErrorDialog(messages.getString("alreadyRunningError"), e);
            System.exit(-1);
        } catch (Exception e) {
            logger.error("Failed to start server", e);
            showErrorDialog(messages.getString("fatalError"), e);
            System.exit(-1);
        }
    }

    @Override
    protected void onFatalError() {
        logger.error("Fatal error");
        showErrorDialog(messages.getString("fatalError"));
        super.onFatalError();
    }

    /**
     * Read the directory of the songs from the SongBeamer.ini file
     * Read the installed songbeamer version string from Songbeamer.ini file
     *
     * @return data object with the values
     */
    private SongBeamerSettings readSongBeamerSettings() {
        String songDir = null;
        String version = null;
        try {
            String path = System.getenv("APPDATA") + "\\SongBeamer\\SongBeamer.ini";
            // Apparently the file can be UTF_16LE or UTF_8 encoeded. yay
            List<String> lines;
            try {
                lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_16LE);
            } catch (CharacterCodingException e) {
                lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
            }

            for (String l : lines) {
                if (l.startsWith("FolienBaseDir=")) {
                    songDir = l.replaceFirst("FolienBaseDir=", "");
                    if (songDir.startsWith("%My Documents%")) {
                        // "%My Documents%" is a Songbeamer variable which points to users documents folder
                        String myDocuments = System.getenv("USERPROFILE") + "\\Documents";
                        songDir = songDir.replace("%My Documents%", myDocuments);
                    }
                } else if (l.startsWith("Version=")) {
                    version = l.replace("Version=", "");
                }
            }
        } catch (Exception e) {
            logger.error("Error while reading Songbeamer.ini", e);
        }
        return new SongBeamerSettings(version, songDir);
    }

    public static class SongBeamerSettings {
        public final String version;
        final String songDir;

        SongBeamerSettings(String version, String songDir) {
            this.version = version;
            this.songDir = songDir;
        }
    }
}
