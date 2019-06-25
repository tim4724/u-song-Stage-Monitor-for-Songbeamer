package com.tim.usong.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class TutorialFrame extends WebFrame {

    public TutorialFrame() {
        super("", "http://localhost/tutorial",
                Preferences.userNodeForPackage(TutorialFrame.class).node("tutorial"),
                1024, 800, 0.8);
        ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
        setTitle(messages.getString("tutorial"));

        Button closeButton = new Button(messages.getString("done"));
        closeButton.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
