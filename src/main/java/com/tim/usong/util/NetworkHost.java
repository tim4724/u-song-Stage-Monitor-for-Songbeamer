package com.tim.usong.util;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetworkHost {
    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static String getHostAddress() {
        try {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                return socket.getLocalAddress().getHostAddress();
            } catch (SocketException e) {
                return InetAddress.getLocalHost().getHostAddress();
            }
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
