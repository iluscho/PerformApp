package com.example.performapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.*;

public class DispatcherActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private static final String PREF_NAME = "MyAppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatcher);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, null);
        recyclerView.setAdapter(taskAdapter);

        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference("tasks");

        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Task task = ds.getValue(Task.class);
                    if (task != null) {
                        taskList.add(task);
                    }
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("DispatcherActivity", "Ошибка загрузки задач", error.toException());
            }
        });

        findViewById(R.id.btnObjectRegistry).setOnClickListener(v ->
                startActivity(new Intent(this, ObjectRegistryActivity.class)));

        findViewById(R.id.btnAddTask).setOnClickListener(v ->
                startActivity(new Intent(this, AddRequestActivity.class)));

        Button buttonExit = findViewById(R.id.buttondispexit);
        buttonExit.setOnClickListener(v -> {
            getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
