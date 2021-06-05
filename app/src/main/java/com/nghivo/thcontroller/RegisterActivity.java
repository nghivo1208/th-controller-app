package com.nghivo.thcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.nghivo.thcontroller.model.BaseRequest;
import com.nghivo.thcontroller.model.LoginResponse;
import com.nghivo.thcontroller.shared.ComkotSharedPref;
import com.nghivo.thcontroller.shared.ComkotSharedPrefKt;
import com.nghivo.thcontroller.socket.TcpClient;
import com.nghivo.thcontroller.socket.TcpClientTask;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

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
        setContentView(R.layout.activity_register);
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

        TextInputEditText edtPassword2 = findViewById(R.id.edtPassword2);
        edtPassword2.setError(null);

        Editable valuePassword2 = edtPassword2.getText();
        String password2 = valuePassword2 != null ? valuePassword2.toString() : "";

        if (password2.equals("")) {
            edtPassword2.setError(getString(R.string.confirm_password_empty));
            return;
        } else if (!password.equals(password2)) {
            edtPassword2.setError(getString(R.string.confirm_password_wrong));
            return;
        }

        String request = BaseRequest.getRegisterRequest(username, password);
        tcpClientTask.sendData(request);

    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void onClickBack(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        tcpClientTask.closeSocket();
        tcpClientTask.cancel(true);
        super.onDestroy();
    }

}
