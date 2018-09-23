package com.tim.usong;

import com.tim.usong.core.SongbeamerSettings;
import com.tim.usong.core.ui.PreviewFrame;
import com.tim.usong.core.SongParser;
import com.tim.usong.core.SongbeamerListener;
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
import java.net.BindException;
import java.util.ResourceBundle;

public class USongApplication extends Application<Configuration> {
    public static final String APP_NAME = USongApplication.class.getPackage().getImplementationTitle();
    public static final String APP_VERSION = USongApplication.class.getPackage().getImplementationVersion();
    public static final String LOCAL_DIR = System.getenv("APPDATA") + "\\uSongServer\\";
    private static final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
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
        try {
            SongbeamerSettings sbSettings = new SongbeamerSettings();
            SongParser songParser = new SongParser(sbSettings.songDir);

            PreviewFrame previewFrame = new PreviewFrame();
            SongResource songResource = new SongResource(songParser);
            SongbeamerListener sBListener = new SongbeamerListener(songResource);
            UsongTray usongTray = new UsongTray(previewFrame);
            StatusResource statusResource = new StatusResource(sBListener, songResource,
                    songParser, previewFrame, sbSettings.version);

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
        } catch (SongbeamerSettings.NoSongDirFoundException e) {
            logger.error("Could not find Songs directory", e);
            showErrorDialog(messages.getString("songsDirNotFoundError"), e);
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
}
