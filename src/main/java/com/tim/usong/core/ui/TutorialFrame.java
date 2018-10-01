package com.tim.usong.core.ui;

import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class TutorialFrame extends WebFrame {

    public TutorialFrame() {
        super("", "http://localhost/tutorial",
                Preferences.userNodeForPackage(TutorialFrame.class).node("tutorial5"),
                1024, 800, 1);
        ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
        setTitle(messages.getString("tutorial"));
    }
}
