package com.nghivo.thcontroller.socket;

import android.util.Log;

import com.koushikdutta.async.util.Charsets;
import com.nghivo.thcontroller.model.BaseRequest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class TcpClient {

    public static final String TAG = TcpClient.class.getSimpleName();
    public static final String SERVER_IP = "tkvm.ddns.net"; //server IP address
    public static final int SERVER_PORT = 2000;
    // message to send to the server
    private String mServerMessage = "";
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private DataInputStream mDataIn;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = () -> {
            if (mBufferOut != null) {
                Log.d(TAG, "Sending: " + message);
                mBufferOut.println(message);
                mBufferOut.flush();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void readIn() {
        try {
            if (mDataIn.available() > 0) return;

            Log.d(TAG, "C: reading...");

            byte[] messageByte = new byte[1024];
            StringBuilder dataString = new StringBuilder();
            int bytesRead = mDataIn.read(messageByte);
            dataString.append(new String(messageByte, 0, bytesRead));
            mServerMessage = dataString.toString();

            if (mServerMessage.isEmpty()) return;

            if (mMessageListener != null) {
                //call the method messageReceived from MyActivity class
                mMessageListener.messageReceived(mServerMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }
        try {
            mDataIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMessageListener = null;
        mDataIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    Socket socket;

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.d(TAG, "C: Connecting...");

            //create a socket to make the connection with the server

            try {
                socket = new Socket(serverAddr, SERVER_PORT);

                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                mDataIn = new DataInputStream(socket.getInputStream());

//                InputStream inputStream = socket.getInputStream();
//                byte[] buffer = new byte[1024];
                while (mRun) {
                    readIn();
                }

            } catch (Exception e) {
                Log.e(TAG, "S: Error", e);
            }
            //the socket must be closed. It is not possible to reconnect to this socket
            // after it is closed, which means a new socket instance has to be created.

        } catch (Exception e) {
            Log.e(TAG, "C: Error", e);
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}
