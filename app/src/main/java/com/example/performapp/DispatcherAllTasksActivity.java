package com.example.performapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
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

public class DispatcherAllTasksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DispatcherTaskAdapter taskAdapter;
    private final List<Task> taskList = new ArrayList<>();
    private String filterStartDate = null;
    private String filterEndDate = null;
    private String filterWorker = null;
    private List<String> allWorkers = new ArrayList<>();
    private TextInputEditText dateRangeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatcher_all_tasks);

        findViewById(R.id.btnClearFilters).setOnClickListener(v -> {
            filterStartDate = null;
            filterEndDate = null;
            filterWorker = null;
            loadAllTasks();
        });

        recyclerView = findViewById(R.id.recyclerViewAllTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new DispatcherTaskAdapter(taskList, task -> {
            Toast.makeText(this, "Работник снят", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnFilter).setOnClickListener(v -> showFilterDialog());

        loadWorkers();
        recyclerView.setAdapter(taskAdapter);
        loadAllTasks();
    }

    private void loadWorkers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.orderByChild("role").equalTo("worker").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allWorkers.clear();
                allWorkers.add("Все");
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

        dateRangeEditText = dialogView.findViewById(R.id.dateEditText);
        Spinner workerSpinner = dialogView.findViewById(R.id.workerSpinner);
        Button btnApply = dialogView.findViewById(R.id.btnApplyFilters);

        // Настройка Material Date Picker для выбора диапазона
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> datePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Выберите период")
                        .build();

        dateRangeEditText.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "DATE_RANGE_PICKER"));

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = sdf.format(new Date(selection.first));
            String endDate = sdf.format(new Date(selection.second));
            dateRangeEditText.setText(String.format("%s - %s", startDate, endDate));
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allWorkers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workerSpinner.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnApply.setOnClickListener(v -> {
            String dateRange = dateRangeEditText.getText().toString().trim();
            if (!dateRange.isEmpty()) {
                String[] dates = dateRange.split(" - ");
                filterStartDate = dates[0];
                filterEndDate = dates.length > 1 ? dates[1] : dates[0];
            } else {
                filterStartDate = null;
                filterEndDate = null;
            }

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

                    // Фильтрация по дате (если задан диапазон)
                    if (filterStartDate != null && taskDate != null) {
                        if (taskDate.compareTo(filterStartDate) < 0 ||
                                (filterEndDate != null && taskDate.compareTo(filterEndDate) > 0)) {
                            continue;
                        }
                    }

                    // Фильтрация по работнику
                    if (filterWorker != null && (workerName == null || !workerName.equals(filterWorker))) {
                        continue;
                    }

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
                    String id = ds.getKey();
                    String address = ds.child("address").getValue(String.class);
                    String organization = ds.child("organization").getValue(String.class);
                    String workerName = ds.child("workerName").getValue(String.class);
                    String taskDate = ds.child("taskDate").getValue(String.class);
                    String acceptanceDate = ds.child("acceptanceDate").getValue(String.class);
                    String completionDate = ds.child("completionDate").getValue(String.class);
                    String comment = ds.child("comment").getValue(String.class);
                    String statusStr = ds.child("status").getValue(String.class);

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