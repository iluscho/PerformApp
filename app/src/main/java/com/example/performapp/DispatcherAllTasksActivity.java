package com.example.performapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.Collections;
import java.util.Comparator;
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
    private TextView textViewNoTasks;
    private boolean sortDescending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatcher_all_tasks);

        textViewNoTasks = findViewById(R.id.textViewNoTasks);

        recyclerView = findViewById(R.id.recyclerViewAllTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new DispatcherTaskAdapter(taskList, task -> {
            Toast.makeText(this, "Работник снят", Toast.LENGTH_SHORT).show();
        });

        recyclerView.setAdapter(taskAdapter);

        findViewById(R.id.btnFilter).setOnClickListener(v -> {
            loadWorkers(this::showFilterDialog);
        });

        loadAllTasks();
    }

    private void loadWorkers(Runnable onWorkersLoaded) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.orderByChild("dispatcher").equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allWorkers.clear();
                allWorkers.add("Все");
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String login = ds.child("login").getValue(String.class);
                    if (login != null && !login.isEmpty()) {
                        allWorkers.add(login);
                    }
                }
                if (onWorkersLoaded != null) {
                    onWorkersLoaded.run();
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
        Button btnClear = dialogView.findViewById(R.id.btnClearFilters);
        Button btnSort = dialogView.findViewById(R.id.btnSort);

        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> datePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Выберите период")
                        .build();

        dateRangeEditText.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "DATE_RANGE_PICKER"));

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = sdf.format(new Date(selection.first));
            String endDate = sdf.format(new Date(selection.second));
            dateRangeEditText.setText(startDate + " - " + endDate);
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, allWorkers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workerSpinner.setAdapter(adapter);

        if (filterStartDate != null && filterEndDate != null) {
            dateRangeEditText.setText(filterStartDate + " - " + filterEndDate);
        }
        if (filterWorker != null) {
            int pos = allWorkers.indexOf(filterWorker);
            if (pos >= 0) workerSpinner.setSelection(pos);
        }

        btnSort.setText(sortDescending ? "Сортировать: новые сверху" : "Сортировать: старые сверху");

        AlertDialog dialog = builder.create();

        btnClear.setOnClickListener(v -> {
            filterStartDate = null;
            filterEndDate = null;
            filterWorker = null;
            dateRangeEditText.setText("");
            workerSpinner.setSelection(0);
            dialog.dismiss();
            applyFilters();
        });

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

        btnSort.setOnClickListener(v -> {
            sortDescending = !sortDescending;
            btnSort.setText(sortDescending ? "Сортировать: новые сверху" : "Сортировать: старые сверху");
            applyFilters();
        });

        dialog.show();
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

                    if (filterStartDate != null && taskDate != null) {
                        if (taskDate.compareTo(filterStartDate) < 0 ||
                                (filterEndDate != null && taskDate.compareTo(filterEndDate) > 0)) {
                            continue;
                        }
                    }

                    if (filterWorker != null && (workerName == null || !workerName.equals(filterWorker))) {
                        continue;
                    }

                    TaskStatus status = TaskStatus.PENDING;
                    if (statusStr != null) {
                        try {
                            status = TaskStatus.valueOf(statusStr);
                        } catch (IllegalArgumentException ignored) {}
                    }

                    Task t = new Task(id,
                            taskDate != null ? taskDate : "",
                            acceptanceDate != null ? acceptanceDate : "",
                            address != null ? address : "",
                            comment != null ? comment : "",
                            organization != null ? organization : "",
                            status);
                    t.setWorkerName(workerName != null ? workerName : "");
                    t.setCompletionDate(completionDate != null ? completionDate : "");
                    taskList.add(t);
                }

                Collections.sort(taskList, (t1, t2) -> sortDescending ?
                        t2.getTaskDate().compareTo(t1.getTaskDate()) :
                        t1.getTaskDate().compareTo(t2.getTaskDate()));

                taskAdapter.notifyDataSetChanged();
                updateNoTasksMessage();
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
                    if (statusStr != null) {
                        try {
                            status = TaskStatus.valueOf(statusStr);
                        } catch (IllegalArgumentException ignored) {}
                    }

                    Task t = new Task(id,
                            taskDate != null ? taskDate : "",
                            acceptanceDate != null ? acceptanceDate : "",
                            address != null ? address : "",
                            comment != null ? comment : "",
                            organization != null ? organization : "",
                            status);
                    t.setWorkerName(workerName != null ? workerName : "");
                    t.setCompletionDate(completionDate != null ? completionDate : "");
                    taskList.add(t);
                }

                Collections.sort(taskList, (t1, t2) -> sortDescending ?
                        t2.getTaskDate().compareTo(t1.getTaskDate()) :
                        t1.getTaskDate().compareTo(t2.getTaskDate()));

                taskAdapter.notifyDataSetChanged();
                updateNoTasksMessage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DispatcherAllTasksActivity.this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNoTasksMessage() {
        if (taskList.isEmpty()) {
            StringBuilder filtersInfo = new StringBuilder("Фильтры: ");
            boolean hasFilters = false;

            if (filterStartDate != null && filterEndDate != null) {
                filtersInfo.append("Дата: ").append(filterStartDate).append(" - ").append(filterEndDate);
                hasFilters = true;
            }
            if (filterWorker != null) {
                if (hasFilters) filtersInfo.append("; ");
                filtersInfo.append("Работник: ").append(filterWorker);
                hasFilters = true;
            }

            if (!hasFilters) {
                filtersInfo.append("Нет");
            }

            textViewNoTasks.setText("Задачи не найдены.\n" + filtersInfo);
            textViewNoTasks.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textViewNoTasks.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}