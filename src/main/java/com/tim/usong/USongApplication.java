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

public class USongApplication extends Application<USongConfiguration> {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = new String[]{"server", "usong.yml"};
        }
        try {
            Runtime.getRuntime().exec("taskkill /F /IM SBRemoteClient.exe");
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
}
