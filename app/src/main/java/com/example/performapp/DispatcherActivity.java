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

import java.util.ArrayList;
import java.util.List;

public class DispatcherActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DispatcherTaskAdapter taskAdapter;
    private final List<Task> taskList = new ArrayList<>();
    private static final String PREF_NAME = "MyAppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatcher);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // При снятии работника просто показываем тост
        taskAdapter = new DispatcherTaskAdapter(taskList, task -> {
            Toast.makeText(
                    DispatcherActivity.this,
                    "Работник снят, задача переведена в PENDING",
                    Toast.LENGTH_SHORT
            ).show();
        });
        recyclerView.setAdapter(taskAdapter);

        loadTasks();

        findViewById(R.id.btnObjectRegistry).setOnClickListener(v ->
                startActivity(new Intent(this, ObjectRegistryActivity.class))
        );
        findViewById(R.id.btnAddTask).setOnClickListener(v ->
                startActivity(new Intent(this, AddRequestActivity.class))
        );
        findViewById(R.id.buttondispexit).setOnClickListener(v -> {
            getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadTasks() {
        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference("tasks");
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id             = ds.getKey();
                    String address        = ds.child("address").getValue(String.class);
                    String organization   = ds.child("organization").getValue(String.class);
                    String workerName     = ds.child("workerName").getValue(String.class);
                    String taskDate       = ds.child("taskDate").getValue(String.class);
                    String acceptanceDate = ds.child("acceptanceDate").getValue(String.class);
                    String completionDate = ds.child("completionDate").getValue(String.class);
                    String comment        = ds.child("comment").getValue(String.class);
                    String statusStr      = ds.child("status").getValue(String.class);

                    // Подстановка PENDING, если статус пустой или некорректный
                    TaskStatus status = TaskStatus.PENDING;
                    if (statusStr != null && !statusStr.isEmpty()) {
                        try {
                            status = TaskStatus.valueOf(statusStr);
                        } catch (IllegalArgumentException ignored) { }
                    }

                    // ❗ Фильтрация по статусу
                    if (status == TaskStatus.PENDING || status == TaskStatus.ACCEPTED || status == TaskStatus.BOOKED) {
                        Task t = new Task(
                                id,
                                taskDate       != null ? taskDate       : "",
                                acceptanceDate != null ? acceptanceDate : "",
                                address        != null ? address        : "",
                                comment        != null ? comment        : "",
                                organization   != null ? organization   : "",
                                status
                        );
                        t.setWorkerName(workerName   != null ? workerName   : "");
                        t.setCompletionDate(completionDate != null ? completionDate : "");
                        taskList.add(t);
                    }
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("DispatcherActivity", "Ошибка загрузки задач", error.toException());
                Toast.makeText(
                        DispatcherActivity.this,
                        "Не удалось загрузить задачи: " + error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

}
