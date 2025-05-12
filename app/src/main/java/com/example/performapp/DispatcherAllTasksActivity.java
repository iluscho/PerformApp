package com.example.performapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

public class DispatcherAllTasksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DispatcherTaskAdapter taskAdapter;
    private final List<Task> taskList = new ArrayList<>();
    private String filterDate = null;
    private String filterWorker = null;
    private List<String> allWorkers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatcher_all_tasks);
        findViewById(R.id.btnClearFilters).setOnClickListener(v -> {
            filterDate = null;
            filterWorker = null;
            loadAllTasks(); // Перезагрузка всех задач без фильтрации
        });


        recyclerView = findViewById(R.id.recyclerViewAllTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new DispatcherTaskAdapter(taskList, task -> {
            Toast.makeText(this, "Работник снят", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnFilter).setOnClickListener(v -> showFilterDialog());

        loadWorkers(); // Загружаем список работников заранее

        recyclerView.setAdapter(taskAdapter);
        loadAllTasks();
    }
    private void loadWorkers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.orderByChild("role").equalTo("worker").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allWorkers.clear();
                allWorkers.add("Все"); // Пункт "Все"
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = ds.child("name").getValue(String.class);
                    if (name != null) allWorkers.add(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DispatcherAllTasksActivity.this, "Ошибка загрузки работников", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filters, null);
        builder.setView(dialogView);

        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        Spinner workerSpinner = dialogView.findViewById(R.id.workerSpinner);
        Button btnApply = dialogView.findViewById(R.id.btnApplyFilters);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allWorkers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workerSpinner.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnApply.setOnClickListener(v -> {
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1;
            int year = datePicker.getYear();

            filterDate = String.format("%04d-%02d-%02d", year, month, day);
            filterWorker = workerSpinner.getSelectedItem().toString();
            if ("Все".equals(filterWorker)) filterWorker = null;

            dialog.dismiss();
            applyFilters();
        });
    }
    private void applyFilters() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("tasks");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = ds.getKey();
                    String taskDate = ds.child("taskDate").getValue(String.class);
                    String workerName = ds.child("workerName").getValue(String.class);
                    String organization = ds.child("organization").getValue(String.class);
                    String address = ds.child("address").getValue(String.class);
                    String comment = ds.child("comment").getValue(String.class);
                    String acceptanceDate = ds.child("acceptanceDate").getValue(String.class);
                    String completionDate = ds.child("completionDate").getValue(String.class);
                    String statusStr = ds.child("status").getValue(String.class);

                    TaskStatus status = TaskStatus.PENDING;
                    if (statusStr != null && !statusStr.isEmpty()) {
                        try {
                            status = TaskStatus.valueOf(statusStr);
                        } catch (IllegalArgumentException ignored) { }
                    }

                    if (filterDate != null && (taskDate == null || !taskDate.equals(filterDate))) continue;
                    if (filterWorker != null && (workerName == null || !workerName.equals(filterWorker))) continue;

                    Task t = new Task(id, taskDate, acceptanceDate, address, comment, organization, status);
                    t.setWorkerName(workerName != null ? workerName : "");
                    t.setCompletionDate(completionDate != null ? completionDate : "");
                    taskList.add(t);
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DispatcherAllTasksActivity.this, "Ошибка при фильтрации", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadAllTasks() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("tasks");
        ref.addValueEventListener(new ValueEventListener() {
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

                    TaskStatus status = TaskStatus.PENDING;
                    if (statusStr != null && !statusStr.isEmpty()) {
                        try {
                            status = TaskStatus.valueOf(statusStr);
                        } catch (IllegalArgumentException ignored) { }
                    }

                    Task t = new Task(
                            id,
                            taskDate != null ? taskDate : "",
                            acceptanceDate != null ? acceptanceDate : "",
                            address != null ? address : "",
                            comment != null ? comment : "",
                            organization != null ? organization : "",
                            status
                    );
                    t.setWorkerName(workerName != null ? workerName : "");
                    t.setCompletionDate(completionDate != null ? completionDate : "");
                    taskList.add(t);
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DispatcherAllTasks", "Ошибка чтения данных", error.toException());
                Toast.makeText(DispatcherAllTasksActivity.this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
