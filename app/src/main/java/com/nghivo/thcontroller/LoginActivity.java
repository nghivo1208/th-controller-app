package com.nghivo.thcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.nghivo.thcontroller.model.BaseRequest;
import com.nghivo.thcontroller.model.BaseResponse;
import com.nghivo.thcontroller.model.LoginResponse;
import com.nghivo.thcontroller.shared.ComkotSharedPref;
import com.nghivo.thcontroller.shared.ComkotSharedPrefKt;
import com.nghivo.thcontroller.socket.TcpClient;
import com.nghivo.thcontroller.socket.TcpClientTask;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    ComkotSharedPref sharedPref;
    TextInputEditText edtUsername, edtPassword;
    String username;

    private TcpClientTask tcpClientTask;
    private final Gson gson = new Gson();

    private final TcpClient.OnMessageReceived onMessageReceived = message -> {
        Log.e(TAG, "Response : " + message);
        try {
            if (message.isEmpty()) return;
            LoginResponse loginResponse = gson.fromJson(message, LoginResponse.class);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (loginResponse.getStatus().equalsIgnoreCase("ON")) {
                        sharedPref.saveString("username", username);
                        openMainActivity();
                    } else {
                        edtUsername.setError(getString(R.string.account_incorrect));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_login);
        sharedPref = ComkotSharedPrefKt.createBaseSharedPref(getApplicationContext());
        tcpClientTask = new TcpClientTask(onMessageReceived);
        tcpClientTask.execute("");
    }

    public void onClickButtonNext(View view) {
        edtUsername = findViewById(R.id.edtUserName);
        edtUsername.setError(null);

        Editable valueUsername = edtUsername.getText();
        username = valueUsername != null ? valueUsername.toString() : "";

        if (username.equals("")) {
            edtUsername.setError(getString(R.string.username_empty));
            return;
        }

        // Handle password
        edtPassword = findViewById(R.id.edtPassword);
        edtPassword.setError(null);

        Editable valuePassword = edtPassword.getText();
        String password = valuePassword != null ? valuePassword.toString() : "";

        if (password.equals("")) {
            edtPassword.setError(getString(R.string.password_empty));
            return;
        }

        String request = BaseRequest.getLoginRequest(username, password);
        tcpClientTask.sendData(request);

    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void onClickButtonRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        tcpClientTask.closeSocket();
        tcpClientTask.cancel(true);
        super.onDestroy();
    }

}
