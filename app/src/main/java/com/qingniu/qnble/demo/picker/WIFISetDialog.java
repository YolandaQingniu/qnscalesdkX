package com.qingniu.qnble.demo.picker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.qingniu.qnble.demo.R;


/**
 * Created by ch on 2019/4/26.
 */

public class WIFISetDialog extends Dialog {

    private EditText ssidEditText;
    private EditText pwdEditText;
    private Button confirmBtn;
    private Button cancleBtn;

    public void setDialogClickListener(DialogClickListener dialogClickListener) {
        this.dialogClickListener = dialogClickListener;
    }

    private DialogClickListener dialogClickListener;





    public WIFISetDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_wifi_set);
        initView();
        initEvent();
    }


    private void initEvent() {
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null!=dialogClickListener){
                    dialogClickListener.confirmClick(ssidEditText.getText().toString(),pwdEditText.getText().toString());
                }
            }
        });
        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null!=dialogClickListener){
                    dialogClickListener.cancelClick();
                    dismiss();
                }
            }
        });
    }

    private void initView() {
        ssidEditText = findViewById(R.id.ssid);
        pwdEditText = findViewById(R.id.pwd);
        cancleBtn =findViewById(R.id.no);
        confirmBtn =findViewById(R.id.yes);
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    public interface DialogClickListener {
        void  confirmClick(String ssid, String pwd);
        void cancelClick();
    }

}
