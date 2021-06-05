package com.nghivo.thcontroller.socket;

import android.os.AsyncTask;
import android.util.Log;

public class TcpClientTask extends AsyncTask<String, String, TcpClient> {

    private TcpClient.OnMessageReceived mCallback;
    public TcpClient mTcpClient;

    public TcpClientTask(TcpClient.OnMessageReceived callback) {
        mCallback = callback;
    }

    public void closeSocket() {
        mTcpClient.stopClient();
    }

    public void sendData(String data) {
        if (mTcpClient == null) return;
        mTcpClient.sendMessage(data);
    }

    @Override
    protected TcpClient doInBackground(String... message) {

        //we create a TCPClient object
        mTcpClient = new TcpClient(mCallback);
        mTcpClient.run();

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        //response received from server
        Log.d("test", "response " + values[0]);
        //process server response here....
    }

}
