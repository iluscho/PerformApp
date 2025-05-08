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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ObjectRegistryActivity extends AppCompatActivity {
    private static final String TAG = "ObjectRegistryActivity";

    private EditText etObjectName, etObjectAddress, etObjectDescription;
    private Button btnAddObject, btnBack;
    private RecyclerView recyclerViewObjects;

    private DatabaseReference objectsRef;
    private ObjectAdapter objectAdapter;
    private ArrayList<RegistryObject> objectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_object_registry);

        initViews();
        initFirebase();
        setupRecyclerView();
        loadObjectsFromFirebase();

        btnAddObject.setOnClickListener(v -> addObject());
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        etObjectName = findViewById(R.id.etObjectName);
        etObjectAddress = findViewById(R.id.etObjectAddress);
        etObjectDescription = findViewById(R.id.etObjectDescription);
        btnAddObject = findViewById(R.id.btnAddObject);
        btnBack = findViewById(R.id.btnBack);
        recyclerViewObjects = findViewById(R.id.recyclerViewObjects);
    }

    private void initFirebase() {
        objectsRef = FirebaseDatabase.getInstance().getReference("objects");
    }

    private void setupRecyclerView() {
        objectList = new ArrayList<>();
        objectAdapter = new ObjectAdapter(objectList);
        recyclerViewObjects.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewObjects.setAdapter(objectAdapter);
    }

    private void loadObjectsFromFirebase() {
        objectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                objectList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RegistryObject obj = ds.getValue(RegistryObject.class);
                    if (obj != null) {
                        objectList.add(obj);
                    }
                }
                objectAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ObjectRegistryActivity.this, "Ошибка загрузки объектов", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Ошибка чтения из базы данных", error.toException());
            }
        });
    }

    private void addObject() {
        String name = etObjectName.getText().toString().trim();
        String address = etObjectAddress.getText().toString().trim();
        String description = etObjectDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Введите название и адрес объекта", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = String.valueOf(System.currentTimeMillis());
        RegistryObject registryObject = new RegistryObject(id, name, address, description);

        objectsRef.child(id).setValue(registryObject, (error, ref) -> {
            if (error != null) {
                Toast.makeText(this, "Ошибка добавления объекта", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Ошибка при добавлении объекта", error.toException());
            } else {
                Toast.makeText(this, "Объект успешно добавлен", Toast.LENGTH_SHORT).show();
                clearFields();
            }
        });
    }

    private void clearFields() {
        etObjectName.setText("");
        etObjectAddress.setText("");
        etObjectDescription.setText("");
    }
}
