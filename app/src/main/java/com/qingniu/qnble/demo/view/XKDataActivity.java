package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qingniu.qnble.demo.R;
import com.qingniu.scale.utils.ConvertUtils;
import com.qn.device.out.QNBleApi;
import com.qn.device.out.QNXKData;

/**
 * @author: yxb
 * @description:
 * @date: 2026/5/19
 */
public class XKDataActivity extends AppCompatActivity {

    public static Intent getCallIntent(Context context) {
        return new Intent(context, XKDataActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xk_data);

        EditText weightEt = findViewById(R.id.weightEt);
        EditText ageEt = findViewById(R.id.ageEt);
        EditText heightEt = findViewById(R.id.heightEt);
        EditText genderEt = findViewById(R.id.genderEt);
        EditText res50Et = findViewById(R.id.res50Et);
        EditText res500Et = findViewById(R.id.res500Et);

        Button calBtn = findViewById(R.id.calBtn);
        TextView dataTv = findViewById(R.id.dataTv);

        calBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes = generateBleData(weightEt.getText().toString(), heightEt.getText().toString(), ageEt.getText().toString(),
                        Integer.parseInt(genderEt.getText().toString()), res50Et.getText().toString(), res500Et.getText().toString());

                QNXKData qnxkData = QNBleApi.getInstance(XKDataActivity.this).calcXKIndicators(
                        bytes,
                        Integer.parseInt(ageEt.getText().toString()),
                        Integer.parseInt(genderEt.getText().toString()),
                        Double.parseDouble(heightEt.getText().toString()),
                        true
                );

                String result = qnxkData == null ? "result null" : qnxkData.toString();

                dataTv.setText(result);
            }
        });
    }

    private byte[] generateBleData(String weightStr, String heightStr, String ageStr, int gender, String res50Str, String res500Str) {

        int weight = (int) (Double.parseDouble(weightStr) * 100);
        int height = (int) (Double.parseDouble(heightStr) * 10);
        int age = Integer.parseInt(ageStr);
        int res50 = Integer.parseInt(res50Str);
        int res500 =  Integer.parseInt(res500Str);

        byte[] weightBytes = ConvertUtils.int2Bytes(weight, 2);
        byte[] heightBytes = ConvertUtils.int2Bytes(height, 2);
        byte[] k50Bytes = ConvertUtils.int2Bytes(res50, 2);
        byte[] k500Bytes = ConvertUtils.int2Bytes(res500, 2);


        byte[] byteArray = new byte[16];

        byteArray[0] = 0x25;
        byteArray[1] = (byte) 0xF0;
        byteArray[2] = (byte) 0x03;
        byteArray[3] = (byte) 0x00;

        //体重
        byteArray[4] = weightBytes[0];
        byteArray[5] = weightBytes[1];
        //身高
        byteArray[6] = heightBytes[0];
        byteArray[7] = heightBytes[1];
        //50k
        byteArray[8] = k50Bytes[0];
        byteArray[9] = k50Bytes[1];
        //500k
        byteArray[10] = k500Bytes[0];
        byteArray[11] = k500Bytes[1];

        byteArray[12] = 0x01;
        byteArray[13] = 0x01;

        //requireID
        byteArray[14] = 0x06;

        //checksum
        byteArray[15] = 0x00;

        return byteArray;

    }

}
