package com.example.performapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
        Button btnCompleteTask = findViewById(R.id.btnCompleteTask);
        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference("tasks");

        Task task = (Task) getIntent().getSerializableExtra("task");
        if (task != null) {
            tvAddress.setText(tvAddress.getText() + task.getAddress());
            tvComment.setText(tvComment.getText() + task.getComment());
            tvTaskDate.setText(tvTaskDate.getText() + task.getTaskDate());
            tvAcceptanceDate.setText(tvAcceptanceDate.getText() + task.getAcceptanceDate());
            tvOrganization.setText(tvOrganization.getText() + task.getOrganization());

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

            btnCompleteTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (task != null) {
                        // Получаем текущий часовой пояс устройства
                        TimeZone timeZone = TimeZone.getDefault();

                        // Создаем объект SimpleDateFormat, который будет учитывать часовой пояс
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        sdf.setTimeZone(timeZone);  // Устанавливаем часовой пояс

                        // Формируем дату и время завершения с учетом часового пояса
                        String completionDate = sdf.format(new Date());

                        // Устанавливаем дату завершения и статус
                        task.setCompletionDate(completionDate);
                        task.setStatus(TaskStatus.COMPLETED); // Если у тебя есть статус COMPLETED

                        // Обновление задачи в Firebase
                        tasksRef.child(task.getId()).setValue(task)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(TaskDetailActivity.this, "Задача завершена", Toast.LENGTH_SHORT).show();
                                    finish(); // Закрыть экран или перейти на другой
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(TaskDetailActivity.this, "Ошибка завершения", Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            });
        }
    }
}
