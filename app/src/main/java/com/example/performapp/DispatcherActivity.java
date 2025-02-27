package com.example.performapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private static final String TAG = "DispatcherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatcher);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, null);
        recyclerView.setAdapter(taskAdapter);

        // Получаем экземпляр Realtime Database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Ссылка на узел "tasks"
        final DatabaseReference tasksRef = database.getReference("tasks");

        // Слушатель изменений в узле "tasks"
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Преобразуем данные в объект Task
                    Task task = snapshot.getValue(Task.class);
                    if (task != null) {
                        taskList.add(task);
                    }
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Ошибка при прослушивании tasks", databaseError.toException());
            }
        });

        // Кнопка перехода в активность "Реестр объектов"
        Button btnObjectRegistry = findViewById(R.id.btnObjectRegistry);
        btnObjectRegistry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DispatcherActivity.this, ObjectRegistryActivity.class));
            }
        });

        // Кнопка открытия активности для добавления заявки
        Button btnAddTask = findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Запускаем активность для добавления новой заявки
                startActivity(new Intent(DispatcherActivity.this, AddRequestActivity.class));
            }
        });
    }
}
