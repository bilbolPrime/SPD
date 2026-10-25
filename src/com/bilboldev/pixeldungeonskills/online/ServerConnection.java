package com.bilboldev.pixeldungeonskills.online;

import android.os.Handler;
import android.os.Message;

import com.bilboldev.pixeldungeonskills.utils.GLog;

import java.util.concurrent.TimeUnit;

public class ServerConnection {
    public enum ConnectionStatus {
        DISCONNECTED,
        CONNECTED
    }

    public interface ServerListener {
        void onNewMessage(String message);
        void onStatusChange(ConnectionStatus status);
    }

    private String mServerUrl;
    private Handler mMessageHandler;
    private Handler mStatusHandler;
    private ServerListener mListener;

    public ServerConnection(String url) {

    }

    public void connect(ServerListener listener) {

    }

    public void disconnect() {

    }

    public void sendMessage(String message) {

    }
}
