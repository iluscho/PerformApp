package com.example.performapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkerActivity extends AppCompatActivity implements TaskAdapter.TaskItemListener {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private static final String TAG = "WorkerActivity";

    // Ссылка на узел "tasks" в Realtime Database
    private DatabaseReference tasksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker);

        recyclerView = findViewById(R.id.recyclerViewWorkerTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, this);
        recyclerView.setAdapter(taskAdapter);

        // Инициализация ссылки на базу данных
        tasksRef = FirebaseDatabase.getInstance().getReference("tasks");

        // Слушаем изменения в базе данных и загружаем задачи с нужным статусом
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Task task = ds.getValue(Task.class);
                    // Загружаем только задачи, которые находятся в ожидании (PENDING)
                    if (task != null && task.getStatus() == TaskStatus.PENDING || task != null && task.getStatus() == TaskStatus.BOOKED) {
                        taskList.add(task);
                    }
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Ошибка при загрузке задач", error.toException());
                Toast.makeText(WorkerActivity.this, "Ошибка загрузки задач", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAcceptClicked(Task task) {
        // Обновляем статус задачи и дату принятия
        task.setStatus(TaskStatus.ACCEPTED);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        task.setAcceptanceDate(currentDate);

        // Обновляем задачу в базе данных
        tasksRef.child(task.getId()).setValue(task)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(WorkerActivity.this, "Задача принята", Toast.LENGTH_SHORT).show();
                    // Удаляем задачу из локального списка
                    taskList.remove(task);
                    taskAdapter.notifyDataSetChanged();
                    // Переход на экран деталей задачи
                    Intent intent = new Intent(WorkerActivity.this, TaskDetailActivity.class);
                    intent.putExtra("task", task);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(WorkerActivity.this, "Ошибка обновления задачи", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Ошибка обновления задачи", e);
                });
    }

    @Override
    public void onBookClicked(Task task) {
        // Обновляем статус задачи на "BOOKED"
        task.setStatus(TaskStatus.BOOKED);
        tasksRef.child(task.getId()).setValue(task)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(WorkerActivity.this, "Задача забронирована", Toast.LENGTH_SHORT).show();
                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(WorkerActivity.this, "Ошибка бронирования задачи", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Ошибка бронирования задачи", e);
                });
    }
}
