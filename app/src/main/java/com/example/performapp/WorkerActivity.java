package com.example.performapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WorkerActivity extends AppCompatActivity implements TaskAdapter.TaskItemListener {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker);

        recyclerView = findViewById(R.id.recyclerViewWorkerTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, this);
        recyclerView.setAdapter(taskAdapter);

        // TODO: Загрузка задач с сервера или получение через уведомления
        // Добавляем тестовую задачу
        Task dummyTask = new Task(
                "1",
                "2025-02-21",
                "",
                "ул. Пушкина, 456",
                "Задача назначена",
                "Организация Б",
                TaskStatus.PENDING
        );
        taskList.add(dummyTask);
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAcceptClicked(Task task) {
        // Обновляем статус задачи и дату принятия
        task.setStatus(TaskStatus.ACCEPTED);
        task.setAcceptanceDate("2025-02-21"); // Здесь можно использовать текущую дату

        // Удаляем задачу из списка доступных
        taskList.remove(task);
        taskAdapter.notifyDataSetChanged();

        // Переход на экран деталей задачи
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra("task", task);
        startActivity(intent);
        // TODO: Сообщить серверу, что задача принята
    }

    @Override
    public void onBookClicked(Task task) {
        task.setStatus(TaskStatus.BOOKED);
        taskAdapter.notifyDataSetChanged();
        // TODO: Реализовать логику бронирования задачи
    }
}
