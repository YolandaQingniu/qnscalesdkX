package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.adapter.StorageDataAdapter;
import com.qingniu.qnble.demo.util.QNDataUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author: yxb
 * @description:
 * @date: 2026/1/19
 */
public class StorageDataActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    public static Intent getCallIntent(Context context) {
        Intent intent = new Intent(context, StorageDataActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_data);

        ButterKnife.bind(this);

        StorageDataAdapter storageDataAdapter = new StorageDataAdapter(this, QNDataUtils.qnScaleStoreDataList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(storageDataAdapter);
    }
}
