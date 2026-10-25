package com.bilboldev.pixeldungeonskills.online;

import android.content.Context;
import android.content.Intent;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.utils.GLog;

import org.json.JSONObject;

public class OnlineSync implements ServerConnection.ServerListener {

    private static OnlineSync mInstance = null;
    private ServerConnection mServerConnection;
    private OnlineSync()
    {
        Init();
    }

    public static OnlineSync GetSingleton()
    {
        if(mInstance == null)
            mInstance = new OnlineSync();

        return  mInstance;
    }

    public static void Reset()
    {
        mInstance = null;
    }

    private void Init()
    {
        ((Game)Game.mContext).mService.startSignalR();

    }

    @Override
    public void onStatusChange(ServerConnection.ConnectionStatus status) {

    }

    @Override
    public void onNewMessage(String message) {
      OnlineMessageHepler.DecodeMessage(message);
    }

    public void SendMessage(String message)
    {
        ((Game)Game.mContext).mService.sendMessage(message);

    }
}
