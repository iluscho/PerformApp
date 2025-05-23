package com.example.performapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AddRequestActivity extends AppCompatActivity {

    private Spinner spinnerRegistry;
    private EditText etComment;
    private Button btnAddRequest;
    private List<RegistryObject> registryObjectsList = new ArrayList<>();
    private ArrayAdapter<RegistryObject> spinnerAdapter;
    private DatabaseReference objectsRef;
    private DatabaseReference tasksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_request);

        // Инициализация UI-элементов
        spinnerRegistry = findViewById(R.id.spinnerRegistry);
        etComment = findViewById(R.id.etComment);
        btnAddRequest = findViewById(R.id.btnAddRequest);

        // Инициализация ссылок на Realtime Database
        objectsRef = FirebaseDatabase.getInstance().getReference("objects");
        tasksRef = FirebaseDatabase.getInstance().getReference("tasks");

        // Настраиваем адаптер для Spinner
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, registryObjectsList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRegistry.setAdapter(spinnerAdapter);

        // Загружаем объекты реестра из базы
        objectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                registryObjectsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RegistryObject obj = ds.getValue(RegistryObject.class);
                    if (obj != null) {
                        registryObjectsList.add(obj);
                    }
                }
                spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddRequestActivity.this, "Ошибка загрузки реестра объектов", Toast.LENGTH_SHORT).show();
            }
        });

        // Обработка нажатия на кнопку "Добавить заявку"
        btnAddRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegistryObject selectedObject = (RegistryObject) spinnerRegistry.getSelectedItem();
                if (selectedObject == null) {
                    Toast.makeText(AddRequestActivity.this, "Выберите объект из реестра", Toast.LENGTH_SHORT).show();
                    return;
                }

                String comment = etComment.getText().toString().trim();

                String taskId = String.valueOf(System.currentTimeMillis());

                // Получаем текущий часовой пояс устройства
                TimeZone timeZone = TimeZone.getDefault();

                // Создаем объект SimpleDateFormat, который будет учитывать часовой пояс
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                sdf.setTimeZone(timeZone);  // Устанавливаем часовой пояс

                // Формируем текущую дату и время с учетом часового пояса
                String taskDate = sdf.format(new Date());

                String acceptanceDate = "";
                String address = selectedObject.getAddress();
                String organization = selectedObject.getName();

                Task newTask = new Task(taskId, taskDate, acceptanceDate, address, comment, organization, TaskStatus.PENDING);

                tasksRef.child(taskId).setValue(newTask, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error != null) {
                            Toast.makeText(AddRequestActivity.this, "Ошибка добавления заявки", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddRequestActivity.this, "Заявка успешно добавлена", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        });
    }
}
