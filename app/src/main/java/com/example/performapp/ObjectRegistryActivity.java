package com.example.performapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ObjectRegistryActivity extends AppCompatActivity {
    private static final String TAG = "ObjectRegistryActivity";
    private EditText etObjectName, etObjectAddress, etObjectDescription;
    private Button btnAddObject;
    DatabaseReference objectsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Инициализация Firebase (если ещё не инициализирован)
            FirebaseApp.initializeApp(this);
            setContentView(R.layout.activity_object_registry);
            Log.d(TAG, "Разметка activity_object_registry загружена");

            // Инициализация элементов разметки
            etObjectName = findViewById(R.id.etObjectName);
            etObjectAddress = findViewById(R.id.etObjectAddress);
            etObjectDescription = findViewById(R.id.etObjectDescription);
            btnAddObject = findViewById(R.id.btnAddObject);

            if (etObjectName == null) Log.e(TAG, "etObjectName не найден");
            if (etObjectAddress == null) Log.e(TAG, "etObjectAddress не найден");
            if (etObjectDescription == null) Log.e(TAG, "etObjectDescription не найден");
            if (btnAddObject == null) Log.e(TAG, "btnAddObject не найден");

            // Получаем ссылку на узел "objects" в Realtime Database
            objectsRef = FirebaseDatabase.getInstance().getReference("objects");
            Log.d(TAG, "Ссылка на Firebase Realtime Database получена");

            btnAddObject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = etObjectName.getText().toString().trim();
                    String address = etObjectAddress.getText().toString().trim();
                    String description = etObjectDescription.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address)) {
                        Toast.makeText(ObjectRegistryActivity.this, "Введите название и адрес объекта", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Создаем уникальный идентификатор для объекта
                    String id = String.valueOf(System.currentTimeMillis());
                    RegistryObject registryObject = new RegistryObject(id, name, address, description);

                    // Сохраняем объект в Realtime Database
                    objectsRef.child(id).setValue(registryObject, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Toast.makeText(ObjectRegistryActivity.this, "Ошибка добавления объекта", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Ошибка при добавлении объекта", error.toException());
                            } else {
                                Toast.makeText(ObjectRegistryActivity.this, "Объект успешно добавлен", Toast.LENGTH_SHORT).show();
                                clearFields();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Исключение в onCreate", e);
        }
    }

    private void clearFields() {
        etObjectName.setText("");
        etObjectAddress.setText("");
        etObjectDescription.setText("");
    }
}
