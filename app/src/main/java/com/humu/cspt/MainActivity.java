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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "MainActivity";
    private RecyclerView rv;
    private EditText etSp;
    private EditText etBaudRate;
    private TextView tvSpState;

    private SerialPortManager serialPortManager;
    private Device currentDevice;
    private boolean isSpOpen = false;
    private MessageAdapter messageAdapter;

    private final byte[] SEND_OPEN = {0x01, 0x05, 0x00, 0x00, (byte) 0xFF, 0x00, (byte) 0x8C, 0x3A}; //开门
    private final byte[] SEND_CLOSE = {0x01, 0x05, 0x00, 0x00, 0x00, 0x00, (byte) 0xCD, (byte) 0xCA}; //关门
    private final byte[] SEND_ALARM_STATE = {0x01, 0x02, 0x00, 0x00, 0x00, 0x04, 0x79, (byte) 0xC9}; //查询报警器状态

    private final byte[] GET_ALARM_STATE = {0x01, 0x02, 0x01, 0x05, 0x61, (byte) 0x8B};
    private long sendDataTime = 0L;
    private RecyclerView rvMsgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        showDevices();

        byte[] bytes = hexStr2BinArr("75");
        for (byte b : bytes) {
            Log.d("测试", "==== " + b);
        }

    }

    private void initViews() {
        rv = findViewById(R.id.rv);
        rvMsgs = findViewById(R.id.rv_msgs);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rvMsgs.setLayoutManager(new LinearLayoutManager(this));
        etSp = findViewById(R.id.et_sp);
        etBaudRate = findViewById(R.id.et_baudRate);
        tvSpState = findViewById(R.id.tv_sp_state);
        Button btnConnect = findViewById(R.id.btn_connect);
        Button btnDisconnect = findViewById(R.id.btn_disconnect);
        Button btnOpen = findViewById(R.id.btn_open);
        Button btnClose = findViewById(R.id.btn_close);
        Button btnRefreshSpList = findViewById(R.id.btn_refresh_sp_list);
        Button btnAlarmState = findViewById(R.id.btn_alarm_state);
        btnConnect.setOnClickListener(this);
        btnDisconnect.setOnClickListener(this);
        btnOpen.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnRefreshSpList.setOnClickListener(this);
        btnAlarmState.setOnClickListener(this);

        serialPortManager = new SerialPortManager();

        messageAdapter = new MessageAdapter(this, new ArrayList<String>());
        rvMsgs.setAdapter(messageAdapter);
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

    private void addMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.addMsg(msg);
                rvMsgs.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
            }
        });
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
                            addMsg("串口连接成功！");
                        }

                        @Override
                        public void onFail(File file, Status status) {
                            addMsg("串口连接失败！");
                        }
                    });
                    serialPortManager.setOnSerialPortDataListener(new OnSerialPortDataListener() {
                        @Override
                        public void onDataReceived(final byte[] bytes) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addMsg("收到指令：" + bytes2hex(bytes));
                                    addMsg("过程耗时：" + (System.currentTimeMillis() - sendDataTime) + "ms");
/*                                    if(Arrays.equals(bytes, GET_ALARM_STATE)){
                                        messageAdapter.addMsg("报警器状态正常 "+(System.currentTimeMillis() - sendDataTime));
                                    }else{
                                        messageAdapter.addMsg("收到指令：" + bytes2hex(bytes)+" "+(System.currentTimeMillis() - sendDataTime));
                                    }*/
                                }
                            });
                        }

                        @Override
                        public void onDataSent(final byte[] bytes) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    sendDataTime = System.currentTimeMillis();
                                    addMsg("发送指令：" + bytes2hex(bytes));
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
                } else {
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
                    if (serialPortManager.sendBytes(SEND_OPEN)) {
                        addMsg("发送指令成功！");
                    } else {
                        addMsg("发送指令失败！");
                    }
                } else {
                    Toast.makeText(this, "串口未连接！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_close:
                if (isSpOpen) {
                    if (serialPortManager.sendBytes(SEND_CLOSE)) {
                        addMsg("发送指令成功！");
                    } else {
                        addMsg("发送指令失败！");
                    }
                } else {
                    Toast.makeText(this, "串口未连接！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_refresh_sp_list:
                showDevices();
                break;
            case R.id.btn_alarm_state:
                if (isSpOpen) {
                    if (serialPortManager.sendBytes(SEND_ALARM_STATE)) {
                        addMsg("发送指令成功！");
                    } else {
                        addMsg("发送指令失败！");
                    }
                } else {
                    Toast.makeText(this, "串口未连接！", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 打印16进制的byte数组
     *
     * @param bytes
     * @return
     */
    public String bytes2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String tmp = null;
        for (byte b : bytes) {
            // 将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制
            tmp = Integer.toHexString(0xFF & b);
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            sb.append(tmp);
        }
        return sb.toString();
    }

    /**
     * @param hexString
     * @return 将十六进制转换为二进制字节数组   16-2
     */
    private String hexStr = "0123456789ABCDEF";

    public byte[] hexStr2BinArr(String hexString) {
        //hexString的长度对2取整，作为bytes的长度
        int len = hexString.length() / 2;
        byte[] bytes = new byte[len];
        byte high = 0;//字节高四位
        byte low = 0;//字节低四位
        for (int i = 0; i < len; i++) {
            //右移四位得到高位
            high = (byte) ((hexStr.indexOf(hexString.charAt(2 * i))) << 4);
            low = (byte) hexStr.indexOf(hexString.charAt(2 * i + 1));
            bytes[i] = (byte) (high | low);//高地位做或运算
        }
        return bytes;
    }

}
