package com.example.performapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DispatcherActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatcher);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, null);
        recyclerView.setAdapter(taskAdapter);

        Button btnAddTask = findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Для примера добавляем тестовую задачу
                Task newTask = new Task(
                        String.valueOf(System.currentTimeMillis()),
                        "2025-02-21",
                        "",
                        "ул. Бердинская, 666",
                        "Новая заявка от диспетчера",
                        "Организация Оренбург",
                        TaskStatus.PENDING
                );
                taskList.add(newTask);
                taskAdapter.notifyDataSetChanged();
                // TODO: Отправить задачу на сервер и уведомить работника
            }
        });

        // TODO: Загрузка списка задач (все задачи, задачи по работникам)
    }
}
