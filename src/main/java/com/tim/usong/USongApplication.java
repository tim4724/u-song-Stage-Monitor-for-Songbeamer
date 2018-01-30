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
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

public class USongApplication extends Application<USongConfiguration> {
    static {
        UIManager.put("OptionPane.messageFont", new Font("Helvetica Neue", Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", new Font("Helvetica Neue", Font.PLAIN, 12));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("OptionPane.background", Color.decode("0x111111"));
        UIManager.put("Panel.background", Color.decode("0x111111"));
        UIManager.put("TextArea.foreground", Color.WHITE);
        UIManager.put("TextArea.margin", new Insets(4, 4, 4, 4));
        UIManager.put("TextArea.background", Color.decode("0x111111"));
        UIManager.put("TextArea.font", new Font("Helvetica Neue", Font.PLAIN, 14));
        UIManager.put("Panel.background", Color.decode("0x111111"));
        UIManager.put("Button.background", Color.decode("0x008cff"));
        UIManager.put("Button.border", BorderFactory.createEmptyBorder(5, 20, 5, 20));
        UIManager.put("Button.foreground", Color.WHITE);
    }

    public static final String appName = USongApplication.class.getPackage().getImplementationTitle();
    public static final String appVersion = USongApplication.class.getPackage().getImplementationVersion();
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private Server server;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = new String[]{"server"};
        }
        new USongApplication().run(args);
    }

    public static void showErrorDialog(Object msg) {
        new Thread(() -> {
            JFrame jf = new JFrame();
            jf.setAlwaysOnTop(true);
            JOptionPane.showMessageDialog(jf, msg, appName, JOptionPane.ERROR_MESSAGE);
        }).start();
    }

    @Override
    public void initialize(Bootstrap<USongConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new ViewBundle<>());
    }

    @Override
    public void run(USongConfiguration config, Environment environment) throws Exception {
        try {
            SongBeamerSettings songBeamerSettings = readSongBeamerSettings();
            String songDir = config.getAppConfig().songDir;
            if ((songDir == null || songDir.isEmpty())) {
                //no songDir in app yml config provided
                songDir = songBeamerSettings.songDir;
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

            SongbeamerListener songbeamerListener = new SongbeamerListener(songResource);
            environment.lifecycle().manage(songbeamerListener);
            environment.lifecycle().manage(new StatusTray(this, songBeamerSettings, songbeamerListener, songParser, songResource));
            environment.lifecycle().addServerLifecycleListener(server -> this.server = server);
        } catch (Exception e) {
            logger.error(e.getMessage());
            JFrame jf = new JFrame();
            jf.setAlwaysOnTop(true);
            JOptionPane.showMessageDialog(jf, e, appName, JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    public void shutdown() {
        try {
            if (server != null) server.stop();
        } catch (Exception ignore) {
        }
    }

    private SongBeamerSettings readSongBeamerSettings() {
        String songDir = "unbekannt";
        String version = "unbekannt";
        try {
            String path = System.getenv("APPDATA") + "\\SongBeamer\\SongBeamer.ini";
            Set<String> lines = Files.lines(Paths.get(path), StandardCharsets.UTF_16LE).collect(Collectors.toSet());
            for (String l : lines) {
                if (l.startsWith("FolienBaseDir=")) {
                    songDir = l.replaceFirst("FolienBaseDir=", "");
                    if (songDir.startsWith("%My Documents%")) {
                        //%My Documents% is a Songbeamer variable which points to users documents folder
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
