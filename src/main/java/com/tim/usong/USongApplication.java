package com.tim.usong;

import com.tim.usong.core.SongParser;
import com.tim.usong.core.SongbeamerActionListener;
import com.tim.usong.core.SongbeamerSettings;
import com.tim.usong.resource.RootResource;
import com.tim.usong.resource.SettingsResource;
import com.tim.usong.resource.SongResource;
import com.tim.usong.resource.StatusResource;
import com.tim.usong.ui.*;
import com.tim.usong.util.AutoStart;
import com.tim.usong.util.Setup;
import com.tim.usong.util.SongbeamerUpdateChecker;
import com.tim.usong.util.UpdateChecker;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.dropwizard.websockets.WebsocketBundle;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.URLDecoder;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class USongApplication extends Application<Configuration> implements ServerLifecycleListener {
    public static final String APP_NAME = USongApplication.class.getPackage().getImplementationTitle();
    public static final String APP_VERSION = USongApplication.class.getPackage().getImplementationVersion();
    public static final String LOCAL_DIR = System.getenv("APPDATA") + "\\uSongServer\\";
    private final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String songbeamerVersion = null;

    public static void main(String[] args) throws Exception {
        Setup.setUpUI();
        if (GlobalPreferences.isShowSplashScreen()) {
            SplashWindow.showSplash();
        }
        Setup.setUpRequiredExternalFiles();
        if (args.length == 0) {
            args = new String[]{"server", LOCAL_DIR + "usong.yml"};
        }
        new USongApplication().run(args);
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
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor())
        );
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new ViewBundle<>());
    }

    @Override
    public void run(Configuration config, Environment environment) throws Exception {
        JerseyEnvironment jersey = environment.jersey();
        LifecycleEnvironment lifecycle = environment.lifecycle();

        // Assume that the current executed application is the latest version
        // Update Autostart registry entry
        try {
            if (AutoStart.isAutostartEnabled()) {
                AutoStart.enableAutoStart(getCurrentJarPath());
            }
        } catch (Exception e) {
            logger.error("Failed to ensure autostart jar path is correct", e);
        }

        SongbeamerSettings sbSettings = SongbeamerSettings.readSongbeamerSettings();
        File songDir = sbSettings.songDir;
        Boolean titleHasOwnPage = sbSettings.titleHasOwnPage;
        Integer maxLinesPerPage = sbSettings.maxLinesPerPage;

        songbeamerVersion = sbSettings.version;
        if (songDir == null) {
            String songDirString = GlobalPreferences.getSongDir();
            if (songDirString == null || !(songDir = new File(songDirString)).exists()) {
                songDir = new SelectSongDirectoryDialog().getDirectory();
            }
            if (songDir == null) {
                logger.error("Could not find Songs directory");
                showErrorDialog(messages.getString("songsDirNotFoundError"));
                System.exit(-1);
                return;
            }
        }
        GlobalPreferences.setSongDir(songDir.getAbsolutePath());
        if (titleHasOwnPage == null) {
            titleHasOwnPage = GlobalPreferences.hasTitlePage();
        } else {
            GlobalPreferences.setTitleHasPage(titleHasOwnPage);
        }
        if (maxLinesPerPage == null) {
            maxLinesPerPage = GlobalPreferences.getMaxLinesPage();
        } else {
            GlobalPreferences.setMaxLinesPage(maxLinesPerPage);
        }
        if (GlobalPreferences.getShowChordsOrNull() == null) {
            // if show chords pref is not initialized, initialize it with the songbeamer settings value
            GlobalPreferences.setShowChords(sbSettings.showChords != null && sbSettings.showChords);
        }
        if (sbSettings.chordsUseBNatural != null) {
            GlobalPreferences.setChordsUseBNatural(sbSettings.chordsUseBNatural);
        }

        SongParser songParser = new SongParser(songDir, titleHasOwnPage, maxLinesPerPage);
        SongResource songResource = new SongResource(songParser);
        SongbeamerActionListener songbeamerActionListener;
        try {
            songbeamerActionListener = new SongbeamerActionListener(songResource);
        } catch (BindException e) {
            logger.error("Application already running", e);
            showErrorDialog(messages.getString("alreadyRunningError"), e);
            System.exit(-1);
            return;
        }
        PreviewFrame previewFrame = new PreviewFrame();
        jersey.register(new RootResource());
        jersey.register(songResource);
        jersey.register(new SettingsResource(sbSettings, songParser, songResource));
        jersey.register(new StatusResource(songbeamerActionListener, songResource, songParser, previewFrame,
                sbSettings.version));
        lifecycle.manage(songbeamerActionListener);
        lifecycle.manage(previewFrame);
        lifecycle.manage(new UsongTray(previewFrame));
        lifecycle.addServerLifecycleListener(this);
    }

    @Override
    public void serverStarted(Server server) {
        SplashWindow.started();
        Preferences prefs = Preferences.userNodeForPackage(USongApplication.class);
        if (prefs.getBoolean("first_run", true)) {
            Object tutorialFrame = new TutorialFrame();
            // bring to front, because of any reason the window is not
            //tutorialFrame.setAlwaysOnTop(true);
            //tutorialFrame.setVisible(true);
            //tutorialFrame.setAlwaysOnTop(false);
            prefs.putBoolean("first_run", false);
        }
        if (GlobalPreferences.isNotifyUpdates()) {
            UpdateChecker.checkForUpdateAsync();
        }
        if (GlobalPreferences.isNotifySongbeamerUpdates() && songbeamerVersion != null) {
            SongbeamerUpdateChecker.checkForUpdateAsync(songbeamerVersion);
        }
        int showOnDisplay = GlobalPreferences.getFullscreenDisplay();
        if (showOnDisplay != -1) {
            try {
                FullScreenStageMonitor.showOnDisplay(showOnDisplay);
            } catch (Exception e) {
                logger.error("Failed to display in fullscreen mode " + showOnDisplay, e);
            }
        }
    }

    @Override
    protected void onFatalError() {
        logger.error("Fatal error");
        showErrorDialog(messages.getString("fatalError"));
        super.onFatalError();
    }

    public static String getCurrentJarPath() {
        String path = USongApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path.replace("/", "\\");
    }
}
