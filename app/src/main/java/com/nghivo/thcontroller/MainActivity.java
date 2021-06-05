package com.nghivo.thcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;
import com.google.gson.Gson;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.nghivo.thcontroller.dialog.UpdateBottomSheet;
import com.nghivo.thcontroller.model.BaseRequest;
import com.nghivo.thcontroller.model.BaseResponse;
import com.nghivo.thcontroller.shared.ComkotSharedPref;
import com.nghivo.thcontroller.shared.ComkotSharedPrefKt;
import com.nghivo.thcontroller.socket.TcpClient;
import com.nghivo.thcontroller.socket.TcpClientTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";

    ComkotSharedPref sharedPref;

    private AppCompatImageView ivFan1, ivFan2;
    private LabeledSwitch labeledSwitch;
    private TextView tvCurrentTemp, tvCurrentHumidity, tvMaxTemp, tvMinTemp, tvMaxHumidity, tvMinHumidity, tvStatusFan1, tvStatusFan2, tvStatusHeater, tvStatusSpray;

    private void findView() {
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        tvCurrentHumidity = findViewById(R.id.tvCurrentHumidity);
        tvMaxTemp = findViewById(R.id.tvMaxTemp);
        tvMinTemp = findViewById(R.id.tvMinTemp);
        tvMaxHumidity = findViewById(R.id.tvMaxHumidity);
        tvMinHumidity = findViewById(R.id.tvMinHumidity);

        tvStatusFan1 = findViewById(R.id.tvStatusFan1);
        ivFan1 = findViewById(R.id.ivFan1);

        tvStatusFan2 = findViewById(R.id.tvStatusFan2);
        ivFan2 = findViewById(R.id.ivFan2);

        tvStatusHeater = findViewById(R.id.tvStatusHeater);
        tvStatusSpray = findViewById(R.id.tvStatusSpray);
    }

    private TcpClientTask tcpClientTask;

    private final Gson gson = new Gson();
    private BaseResponse baseResponse;

    private final TcpClient.OnMessageReceived onMessageReceived = message -> {
        Log.e(TAG, "Response : " + message);
        try {
            if (message.isEmpty()) return;
            baseResponse = gson.fromJson(message, BaseResponse.class);
            fillData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    @SuppressLint("SetTextI18n")
    private void fillData() {
        if (baseResponse == null) return;

        runOnUiThread(() -> {
            labeledSwitch.setOn(baseResponse.getModetemp() == 1);

            tvCurrentTemp.setText(baseResponse.getTemp() + "°");
            tvMaxTemp.setText(baseResponse.getTemptren() + "°");
            tvMinTemp.setText(baseResponse.getTempduoi() + "°");
            tvCurrentHumidity.setText(baseResponse.getHumi() + "%");
            tvMaxHumidity.setText(baseResponse.getHumitren() + "%");
            tvMinHumidity.setText(baseResponse.getHumiduoi() + "%");

            String onString = getString(R.string.on);
            String offString = getString(R.string.off);

            // Fan 1
            boolean isOnFan1 = baseResponse.getFan1() == 1;
            tvStatusFan1.setText(isOnFan1 ? onString : offString);
            Animation fanAnimation1 = new RotateAnimation(
                    0.0f, 360.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            // Set the animation's parameters
            fanAnimation1.setDuration(1500);
            fanAnimation1.setRepeatCount(Animation.INFINITE);
            ivFan1.clearAnimation();
            if (isOnFan1) ivFan1.setAnimation(fanAnimation1);

            // Fan 2
            boolean isOnFan2 = baseResponse.getFan2() == 1;
            tvStatusFan2.setText(isOnFan2 ? onString : offString);
            Animation fanAnimation2 = new RotateAnimation(
                    0.0f, 360.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            // Set the animation's parameters
            fanAnimation2.setDuration(1500);
            fanAnimation2.setRepeatCount(Animation.INFINITE);
            ivFan2.clearAnimation();
            if (isOnFan2) ivFan2.setAnimation(fanAnimation2);

            // Heater
            tvStatusHeater.setText(baseResponse.getGianhiet() == 0 ? offString : onString);

            // Spray
            tvStatusSpray.setText(baseResponse.getPhun() == 0 ? offString : onString);
        });
    }

    private final CountDownTimer countDownTimer = new CountDownTimer(Long.MAX_VALUE, 2000) {
        @Override
        public void onTick(long millisUntilFinished) {
            String req = BaseRequest.get411Request(baseResponse);
            tcpClientTask.sendData(req);
        }

        @Override
        public void onFinish() {
            countDownTimer.start();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_main);

        sharedPref = ComkotSharedPrefKt.createBaseSharedPref(getApplicationContext());

        String username = sharedPref.getString("username", "");
        TextView tvUsername = findViewById(R.id.tvUsername);
        tvUsername.setText(username);

        View btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            sharedPref.saveString("username", null);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        findView();

        tcpClientTask = new TcpClientTask(onMessageReceived);
        tcpClientTask.execute("");

        labeledSwitch = findViewById(R.id.switchMode);
        labeledSwitch.setOnToggledListener((toggleableView, isOn) -> {
            if (baseResponse != null) baseResponse.setModetemp(isOn ? 1 : 0);
            String request = BaseRequest.get402Request(isOn);
            tcpClientTask.sendData(request);
            fillData();
        });

        countDownTimer.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void onClickEditTemp(View view) {
        float maxTemp = baseResponse != null ? baseResponse.getTemptren() : 0f;
        float minTemp = baseResponse != null ? baseResponse.getTempduoi() : 0f;
        UpdateBottomSheet
                .newInstance(maxTemp, minTemp, (min, max) -> {
                    baseResponse.setTemptren(max);
                    baseResponse.setTempduoi(min);
                    String req = BaseRequest.get404Request(baseResponse);
                    tcpClientTask.sendData(req);
                    fillData();
                })
                .show(getSupportFragmentManager(), TAG);
    }

    public void onClickHumidity(View view) {
        float maxHumi = baseResponse != null ? baseResponse.getHumitren() : 0f;
        float minHumi = baseResponse != null ? baseResponse.getHumiduoi() : 0f;
        UpdateBottomSheet
                .newInstance(maxHumi, minHumi, (min, max) -> {
                    baseResponse.setHumitren(max);
                    baseResponse.setHumiduoi(min);
                    String req = BaseRequest.get404Request(baseResponse);
                    tcpClientTask.sendData(req);
                    fillData();
                })
                .show(getSupportFragmentManager(), TAG);
    }

    public void onClickFanTemp(View view) {
        if (baseResponse == null) return;

        boolean isOn = baseResponse.getFan1() == 1;
        baseResponse.setFan1(!isOn ? 1 : 0);
        String request = BaseRequest.get403Request(baseResponse);
        tcpClientTask.sendData(request);
        fillData();
    }

    public void onClickFanHumidity(View view) {
        if (baseResponse == null) return;

        boolean isOn = baseResponse.getFan2() == 1;
        baseResponse.setFan2(!isOn ? 1 : 0);
        String request = BaseRequest.get403Request(baseResponse);
        tcpClientTask.sendData(request);
        fillData();
    }

    public void onClickHeater(View view) {
        if (baseResponse == null) return;

        boolean isOn = baseResponse.getGianhiet() == 1;
        baseResponse.setGianhiet(!isOn ? 1 : 0);
        String request = BaseRequest.get403Request(baseResponse);
        tcpClientTask.sendData(request);
        fillData();
    }

    public void onClickSpray(View view) {
        if (baseResponse == null) return;

        boolean isOn = baseResponse.getPhun() == 1;
        baseResponse.setPhun(!isOn ? 1 : 0);
        String request = BaseRequest.get403Request(baseResponse);
        tcpClientTask.sendData(request);
        fillData();
    }

    @Override
    protected void onDestroy() {
        tcpClientTask.closeSocket();
        tcpClientTask.cancel(true);
        super.onDestroy();
    }
}
