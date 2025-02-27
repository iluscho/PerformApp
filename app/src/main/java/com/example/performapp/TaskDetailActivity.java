package com.example.performapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TaskDetailActivity extends AppCompatActivity {
    private TextView tvAddress, tvComment, tvTaskDate, tvAcceptanceDate, tvOrganization;
    private Button btnOpenMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        tvAddress = findViewById(R.id.tvAddress);
        tvComment = findViewById(R.id.tvComment);
        tvTaskDate = findViewById(R.id.tvTaskDate);
        tvAcceptanceDate = findViewById(R.id.tvAcceptanceDate);
        tvOrganization = findViewById(R.id.tvOrganization);
        btnOpenMap = findViewById(R.id.btnOpenMap);

        Task task = (Task) getIntent().getSerializableExtra("task");
        if (task != null) {
            tvAddress.setText(task.getAddress());
            tvComment.setText(task.getComment());
            tvTaskDate.setText("Дата задачи: " + task.getTaskDate());
            tvAcceptanceDate.setText("Дата принятия: " + task.getAcceptanceDate());
            tvOrganization.setText("Организация: " + task.getOrganization());

            btnOpenMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Получаем адрес из TextView как строку
                    String address = tvAddress.getText().toString();
                    // Формируем URI с запросом адреса
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
                    // Создаем Intent для просмотра карты
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    // Создаем chooser, чтобы пользователь мог выбрать приложение
                    Intent chooser = Intent.createChooser(mapIntent, "Выберите приложение для карт");
                    if (chooser.resolveActivity(getPackageManager()) != null) {
                        startActivity(chooser);
                    }
                }
            });

        }
    }
}
