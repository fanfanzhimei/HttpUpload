package com.zhi.httpupload;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.zhi.domain.FormFile;
import com.zhi.service.SocketHttpRequester;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int UPLOAD_SUCCESS = 0x1;
    private static final int UPLOAD_FAIL = 0x2;
    private static final int NOT_EXISTS = 0x3;

    private static final String URL = "http://192.168.1.3:8080/FileUpload/UploadServlet";  // 请求的目标地址
    private static Map<String, String> params = new HashMap<String, String>(); // 文本字段信息

    private EditText mEtTitle;
    private EditText mEtLength;
    private EditText mEtAddress;
    private Button mBtnUpload;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPLOAD_SUCCESS:
                    Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                    break;
                case UPLOAD_FAIL:
                    Toast.makeText(MainActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                    break;
                case NOT_EXISTS:
                    Toast.makeText(MainActivity.this, "要上传的文件不存在", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        String path = Environment.getExternalStorageDirectory() + "/abc.jpg";
        mEtAddress.setText(path);
        mEtTitle.setText("nihao");
        mEtLength.setText("123");
        mBtnUpload.setOnClickListener(this);
    }

    private void initViews() {
        mEtTitle = (EditText) findViewById(R.id.et_title);
        mEtLength = (EditText) findViewById(R.id.et_length);
        mEtAddress = (EditText) findViewById(R.id.et_address);
        mBtnUpload = (Button) findViewById(R.id.btn_upload);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upload:
                upload();
                break;
        }
    }

    private void upload() {
        new Thread() {
            @Override
            public void run() {
                String title = mEtTitle.getText().toString();
                String length = mEtLength.getText().toString();
                String fileName = "bg_chat_mine.9.png";

                params.clear();
                params.put("title", title);
                params.put("length", length);
                try {
                    File uploadFile = new File(Environment.getExternalStorageDirectory(), fileName);
                    if(!uploadFile.exists()){
                        mHandler.sendEmptyMessage(NOT_EXISTS);
                        return;
                    }
                    /* "address" 是要上传的文件字段名称，在web端设置的是address
                    *  "image/png" 是上传的文件类型
                    * */
                    FormFile formfile = new FormFile(fileName, uploadFile, "address", "image/png");
                    boolean idSuccess = SocketHttpRequester.post(URL, params, formfile);
                    if(idSuccess){
                        mHandler.sendEmptyMessage(UPLOAD_SUCCESS);
                    } else {
                        mHandler.sendEmptyMessage(UPLOAD_FAIL);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(UPLOAD_FAIL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}