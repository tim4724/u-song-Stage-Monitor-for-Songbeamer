package com.tim.usong;

import com.tim.usong.core.SongParser;
import com.tim.usong.core.SongbeamerListener;
import com.tim.usong.resource.RootResource;
import com.tim.usong.resource.SongResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.server.session.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class USongApplication extends Application<USongConfiguration> {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = new String[]{"server"};
        }
        try {
            Runtime.getRuntime().exec("taskkill /F /IM SBRemoteSender.exe");
        } catch (Exception ignore) {
        }
        new USongApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<USongConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new ViewBundle<>());
    }

    @Override
    public void run(USongConfiguration config, Environment environment) throws Exception {
        String songDir = config.getAppConfig().songDir;
        if (songDir == null || songDir.isEmpty()) {
            //no songDir provided
            songDir = extractSongDirFromSBSettings();
        }
        if (songDir != null && (!songDir.endsWith("\\"))) {
            songDir = songDir + "\\";
        }
        logger.info("Songs directory: " + songDir);

        SongParser parser = new SongParser(songDir);

        RootResource rootResource = new RootResource();
        SongResource songResource = new SongResource(parser);
        environment.jersey().register(rootResource);
        environment.jersey().register(songResource);
        environment.servlets().setSessionHandler(new SessionHandler());

        SongbeamerListener songBeamerListener = new SongbeamerListener(songResource);
        environment.lifecycle().manage(songBeamerListener);
    }

    private String extractSongDirFromSBSettings() {
        try {
            String path = System.getenv("APPDATA") + "\\SongBeamer\\SongBeamer.ini";
            Stream<String> lines = Files.lines(Paths.get(path), StandardCharsets.UTF_16LE);
            String songDir = lines.filter((l) -> l.startsWith("FolienBaseDir=")).findFirst().orElse(null);
            if (songDir != null) {
                songDir = songDir.replaceFirst("FolienBaseDir=", "");
                if (songDir.startsWith("%My Documents%")) {
                    //%My Documents% is a Songbeamer variable which points to users documents folder
                    String myDocuments = System.getenv("USERPROFILE") + "\\Documents";
                    songDir = songDir.replace("%My Documents%", myDocuments);
                }
                return songDir;
            }
        } catch (Exception ingore) {
        }
        return null;
    }
}
