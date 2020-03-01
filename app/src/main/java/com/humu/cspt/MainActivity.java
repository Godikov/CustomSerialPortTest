package com.humu.cspt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kongqw.serialportlibrary.Device;
import com.kongqw.serialportlibrary.SerialPortFinder;
import com.kongqw.serialportlibrary.SerialPortManager;
import com.kongqw.serialportlibrary.listener.OnOpenSerialPortListener;
import com.kongqw.serialportlibrary.listener.OnSerialPortDataListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "MainActivity";
    private RecyclerView rv;
    private EditText etSp;
    private EditText etBaudRate;
    private TextView tvSpState;

    private SerialPortManager serialPortManager;
    private Device currentDevice;
    private boolean isSpOpen = false;

    private final byte[] OPEN = {0x01, 0x05, 0x00, 0x00, (byte) 0xFF, 0x00, (byte) 0x8C, 0x3A};
    private final byte[] CLOSE = {0x01, 0x05, 0x00, 0x00, 0x00, 0x00, (byte) 0xCD, (byte) 0xCA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        showDevices();

    }

    private void initViews() {
        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        etSp = findViewById(R.id.et_sp);
        etBaudRate = findViewById(R.id.et_baudRate);
        tvSpState = findViewById(R.id.tv_sp_state);
        Button btnConnect = findViewById(R.id.btn_connect);
        Button btnDisconnect = findViewById(R.id.btn_disconnect);
        Button btnOpen = findViewById(R.id.btn_open);
        Button btnClose = findViewById(R.id.btn_close);
        Button btnRefreshSpList = findViewById(R.id.btn_refresh_sp_list);
        btnConnect.setOnClickListener(this);
        btnDisconnect.setOnClickListener(this);
        btnOpen.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnRefreshSpList.setOnClickListener(this);

        serialPortManager = new SerialPortManager();
    }

    private void showDevices() {
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        ArrayList<Device> devices = serialPortFinder.getDevices();
        for (Device device : devices) {
            Log.d(TAG, device.getFile().getAbsolutePath() + " " + device.getName());
        }
        SerialPortDeviceAdapter adapter = new SerialPortDeviceAdapter(this, devices);
        adapter.setOnSerialportDeviceClickListener(new OnSerialportDeviceClickListener() {
            @Override
            public void onDeviceClick(Device device) {
                etSp.setText(device.getFile().getAbsolutePath());
                currentDevice = device;
            }
        });
        rv.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_connect:
                String baudRateStr = etBaudRate.getText().toString();
                if (currentDevice != null && !TextUtils.isEmpty(baudRateStr)) {
                    int baudRate = Integer.parseInt(baudRateStr);
                    serialPortManager.setOnOpenSerialPortListener(new OnOpenSerialPortListener() {
                        @Override
                        public void onSuccess(File file) {
                            Toast.makeText(MainActivity.this, "串口连接成功！",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFail(File file, Status status) {
                            Toast.makeText(MainActivity.this, "串口连接失败！" + status.name(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    serialPortManager.setOnSerialPortDataListener(new OnSerialPortDataListener() {
                        @Override
                        public void onDataReceived(final byte[] bytes) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,
                                            "收到指令：" + Arrays.toString(bytes),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onDataSent(final byte[] bytes) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,
                                            "发送指令：" + Arrays.toString(bytes),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    boolean isOpenSuccess = serialPortManager.openSerialPort(currentDevice.getFile(),
                            baudRate);
                    if (isOpenSuccess) {
                        tvSpState.setText("串口连接成功!");
                        isSpOpen = true;
                    } else {
                        tvSpState.setText("串口连接失败!");
                        isSpOpen = false;
                    }
                }else{
                    Toast.makeText(this, "请选择串口并填写波特率", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_disconnect:
                serialPortManager.closeSerialPort();
                etSp.setText("");
                currentDevice = null;
                isSpOpen = false;
                tvSpState.setText("串口连接已断开");
                break;
            case R.id.btn_open:
                if (isSpOpen) {
                    if (serialPortManager.sendBytes(OPEN)) {
                        Toast.makeText(this, "发送指令成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "发送指令失败！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "串口未连接！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_close:
                if (isSpOpen) {
                    if (serialPortManager.sendBytes(CLOSE)) {
                        Toast.makeText(this, "发送指令成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "发送指令失败！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "串口未连接！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_refresh_sp_list:
                showDevices();
                break;
        }
    }
}
