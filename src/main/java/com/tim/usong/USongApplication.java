package com.tim.usong;

import com.google.common.base.Strings;
import com.tim.usong.core.SongParser;
import com.tim.usong.core.SongbeamerListener;
import com.tim.usong.core.StatusTray;
import com.tim.usong.resource.RootResource;
import com.tim.usong.resource.SongResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class USongApplication extends Application<USongConfiguration> {

    public static final String APP_NAME = USongApplication.class.getPackage().getImplementationTitle();
    public static final String APP_VERSION = USongApplication.class.getPackage().getImplementationVersion();
    public static final String LOCAL_DIR = System.getenv("APPDATA") + "\\uSongServer\\";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Server server;

    public static void main(String[] args) throws Exception {
        Setup.setUpEverything(true);
        if (args.length == 0) args = new String[]{"server", LOCAL_DIR + "usong.yml"};
        new USongApplication().run(args);
    }

    public static void showErrorDialog(Object msg, boolean async) {
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
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor())
        );
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new ViewBundle<>());
    }

    @Override
    public void run(USongConfiguration config, Environment environment) throws Exception {
        SongBeamerSettings SBSettings = readSongBeamerSettings();

        String songDir = config.getAppConfig().songDir;
        if (Strings.isNullOrEmpty(songDir)) {
            songDir = SBSettings.songDir; //no songDir in app yml config provided
        }
        if (songDir != null && (!songDir.endsWith("\\"))) {
            songDir = songDir + "\\";
        }
        logger.info("Using songs directory: " + songDir);

        try {
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
            logger.error("Failed to start server", e);
            throw e;
        }
    }

    @Override
    protected void onFatalError() {
        logger.error("Fatal error");
        showErrorDialog("Fataler Fehler", false);
        super.onFatalError();
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
            //Apparently the file can be UTF_16LE or UTF_8 encoeded. yay
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
                        //"%My Documents%" is a Songbeamer variable which points to users documents folder
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

    public class SongBeamerSettings {
        public final String version;
        public final String songDir;

        SongBeamerSettings(String version, String songDir) {
            this.version = version;
            this.songDir = songDir;
        }
    }
}
