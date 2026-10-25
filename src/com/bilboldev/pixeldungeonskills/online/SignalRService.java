package com.bilboldev.pixeldungeonskills.online;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class SignalRService extends Service {
    public final Connection mHubConnection = new Connection();
    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void sendMessage(String message) {
    }

    public void startSignalR() {
    }

    public class LocalBinder extends Binder {
        public SignalRService getService() {
            return SignalRService.this;
        }
    }

    public static class Connection {
        public void stop() {
        }
    }
}
