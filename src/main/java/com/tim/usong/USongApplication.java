package com.tim.usong;

import com.tim.usong.core.SongParser;
import com.tim.usong.core.SongbeamerListener;
import com.tim.usong.core.StatusTray;
import com.tim.usong.resource.RootResource;
import com.tim.usong.resource.SongResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class USongApplication extends Application<USongConfiguration> {
    static {
        Font font = new Font("Helvetica Neue", Font.PLAIN, 14);
        Color darkGrayBg = Color.decode("0x111111");
        Color accent = Color.decode("0x008cff");
        UIManager.put("OptionPane.messageFont", font);
        UIManager.put("OptionPane.buttonFont", font);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("OptionPane.background", darkGrayBg);
        UIManager.put("Panel.background", darkGrayBg);
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("Label.font", font);
        UIManager.put("TextArea.foreground", Color.WHITE);
        UIManager.put("TextArea.margin", font);
        UIManager.put("TextArea.background", darkGrayBg);
        UIManager.put("TextArea.font", font);
        UIManager.put("Panel.background", darkGrayBg);
        UIManager.put("Panel.foregrount", Color.WHITE);
        UIManager.put("Button.background", accent);
        UIManager.put("Button.border", BorderFactory.createEmptyBorder(5, 20, 5, 20));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("ProgressBar.background", darkGrayBg);
        UIManager.put("ProgressBar.foreground", accent);
    }

    public static final String APP_NAME = USongApplication.class.getPackage().getImplementationTitle();
    public static final String APP_VERSION = USongApplication.class.getPackage().getImplementationVersion();
    public static final String LOCAL_DIR = System.getenv("APPDATA") + "\\uSongServer\\";
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private Server server;

    public static void main(String[] args) throws Exception {
        SplashScreen.showSplashScreen();

        Files.createDirectories(Paths.get(LOCAL_DIR));
        Path songControlPath = Paths.get(LOCAL_DIR, "uSongControl.jar");
        if (!Files.exists(songControlPath)) {
            //write the uSongControl.jar to the appdata directory
            Files.copy(USongApplication.class.getResourceAsStream("/uSongControl.jar"), songControlPath);
        }

        if (args.length == 0) args = new String[]{"server"};
        new USongApplication().run(args);
    }

    public static void showErrorDialogAsync(Object msg, boolean async) {
        JFrame jf = new JFrame();
        jf.setAlwaysOnTop(true);
        if (async) {
            new Thread(() -> JOptionPane.showMessageDialog(jf, msg, APP_NAME, JOptionPane.ERROR_MESSAGE)).start();
        } else {
            JOptionPane.showMessageDialog(jf, msg, APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void initialize(Bootstrap<USongConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new ViewBundle<>());
    }

    @Override
    public void run(USongConfiguration config, Environment environment) throws Exception {
        try {
            SongBeamerSettings SBSettings = readSongBeamerSettings();
            String songDir = config.getAppConfig().songDir;
            if ((songDir == null || songDir.isEmpty())) {
                songDir = SBSettings.songDir; //no songDir in app yml config provided
            }
            if (songDir != null && (!songDir.endsWith("\\"))) {
                songDir = songDir + "\\";
            }
            logger.info("Songs directory: " + songDir);

            SongParser songParser = new SongParser(songDir);
            SongResource songResource = new SongResource(songParser);
            environment.jersey().register(new RootResource());
            environment.jersey().register(songResource);
            environment.servlets().setSessionHandler(new SessionHandler());

            SongbeamerListener SBListener = new SongbeamerListener(songResource);
            environment.lifecycle().manage(SBListener);
            StatusTray statusTray = new StatusTray(this, SBSettings, SBListener, songParser, songResource);
            environment.lifecycle().manage(statusTray);
            environment.lifecycle().addServerLifecycleListener(server -> {
                this.server = server;
            });
        } catch (Exception e) {
            logger.error(e.getMessage());
            showErrorDialogAsync(e, false);
            throw e;
        }
    }

    public void shutdown() {
        try {
            if (server != null) server.stop();
        } catch (Exception e) {
            System.exit(0);
        }
    }

    private SongBeamerSettings readSongBeamerSettings() {
        String songDir = "unbekannt";
        String version = "unbekannt";
        try {
            String path = System.getenv("APPDATA") + "\\SongBeamer\\SongBeamer.ini";
            List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_16LE);
            for (String l : lines) {
                if (l.startsWith("FolienBaseDir=")) {
                    songDir = l.replaceFirst("FolienBaseDir=", "");
                    if (songDir.startsWith("%My Documents%")) {
                        //"%My Documents%" is a Songbeamer variable which points to users documents folder
                        String myDocuments = System.getenv("USERPROFILE") + "\\Documents";
                        songDir = songDir.replace("%My Documents%", myDocuments);
                    }
                } else if (l.startsWith("Version=")) {
                    version = l.replace("Version=", "");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return new SongBeamerSettings(version, songDir);
    }

    public class SongBeamerSettings {
        public final String version;
        public final String songDir;

        SongBeamerSettings(String version, String songDir) {
            this.version = version;
            this.songDir = songDir;
        }
    }
}
