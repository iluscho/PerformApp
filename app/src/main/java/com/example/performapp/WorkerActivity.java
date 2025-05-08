package com.example.performapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class WorkerActivity extends AppCompatActivity implements TaskAdapter.TaskItemListener {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private DatabaseReference tasksRef;
    private String currentWorkerId;
    private static final String PREF_NAME = "MyAppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker);

        currentWorkerId = getIntent().getStringExtra("userId");
        tasksRef = FirebaseDatabase.getInstance().getReference("tasks");

        recyclerView = findViewById(R.id.recyclerViewWorkerTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, this);
        recyclerView.setAdapter(taskAdapter);

        Button buttonExit = findViewById(R.id.buttonexit);
        buttonExit.setOnClickListener(v -> {
            getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        loadTasks();
    }

    private void loadTasks() {
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Task task = ds.getValue(Task.class);
                    if (task != null && (task.getStatus() == TaskStatus.PENDING || task.getStatus() == TaskStatus.BOOKED)) {
                        taskList.add(task);
                    }
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WorkerActivity.this, "Ошибка загрузки задач", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAcceptClicked(Task task) {
        task.setStatus(TaskStatus.ACCEPTED);
        task.setAcceptanceDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        task.setWorkerName(currentWorkerId);

        tasksRef.child(task.getId()).setValue(task)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Задача принята", Toast.LENGTH_SHORT).show();
                    taskList.remove(task);
                    taskAdapter.notifyDataSetChanged();
                    Intent intent = new Intent(this, TaskDetailActivity.class);
                    intent.putExtra("task", task);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBookClicked(Task task) {
        task.setStatus(TaskStatus.BOOKED);
        task.setWorkerName(currentWorkerId);

        tasksRef.child(task.getId()).setValue(task)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Задача забронирована", Toast.LENGTH_SHORT).show();
                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка бронирования", Toast.LENGTH_SHORT).show());
    }
}
