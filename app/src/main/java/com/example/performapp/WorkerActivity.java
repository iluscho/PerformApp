package com.example.performapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
    private DatabaseReference usersRef;
    private String currentWorkerId;
    private static final String PREF_NAME = "MyAppPrefs";

    private TextView tvUserInfo;
    private TextView tvNoTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker);

        tvUserInfo = findViewById(R.id.tvUserInfo);
        tvNoTasks = findViewById(R.id.tvNoTasks);

        Button btnActiveTask = findViewById(R.id.btnCompleteTask);
        btnActiveTask.setVisibility(View.GONE);

        currentWorkerId = getIntent().getStringExtra("userId");

        usersRef = FirebaseDatabase.getInstance().getReference("users");
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

        loadWorkerLogin(currentWorkerId);
        loadTasks();
        checkForActiveTask(btnActiveTask);
    }

    private void loadWorkerLogin(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.getLogin() != null) {
                    tvUserInfo.setText("Вы вошли как " + user.getLogin());
                } else {
                    tvUserInfo.setText("Вы вошли как [неизвестный пользователь]");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WorkerActivity.this, "Ошибка загрузки данных пользователя", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTasks() {
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Task task = ds.getValue(Task.class);
                    if (task != null &&
                            (task.getStatus() == TaskStatus.PENDING || task.getStatus() == TaskStatus.BOOKED)) {
                        taskList.add(task);
                    }
                }

                if (taskList.isEmpty()) {
                    tvNoTasks.setVisibility(View.VISIBLE);
                } else {
                    tvNoTasks.setVisibility(View.GONE);
                }

                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WorkerActivity.this, "Ошибка загрузки задач", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkForActiveTask(Button activeTaskButton) {
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Task activeTask = null;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Task task = ds.getValue(Task.class);
                    if (task != null &&
                            task.getStatus() == TaskStatus.ACCEPTED &&
                            currentWorkerId.equals(task.getWorkerName()) &&
                            (task.getCompletionDate() == null || task.getCompletionDate().isEmpty())) {
                        activeTask = task;
                        break;
                    }
                }

                if (activeTask != null) {
                    activeTaskButton.setVisibility(View.VISIBLE);
                    Task finalActiveTask = activeTask;
                    activeTaskButton.setOnClickListener(v -> {
                        Intent intent = new Intent(WorkerActivity.this, TaskDetailActivity.class);
                        intent.putExtra("task", finalActiveTask);
                        startActivity(intent);
                    });
                } else {
                    activeTaskButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WorkerActivity.this, "Ошибка проверки активной задачи", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAcceptClicked(Task task) {
        task.setStatus(TaskStatus.ACCEPTED);
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        task.setAcceptanceDate(dateTime);
        task.setWorkerName(currentWorkerId);

        tasksRef.child(task.getId()).setValue(task)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Задача принята", Toast.LENGTH_SHORT).show();
                    taskList.remove(task);
                    taskAdapter.notifyDataSetChanged();
                    checkForActiveTask(findViewById(R.id.btnCompleteTask));
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
