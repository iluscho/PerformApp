package com.example.performapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class ObjectRegistryActivity extends AppCompatActivity {
    private static final String TAG = "ObjectRegistryActivity";

    private EditText etSearch;
    private Button btnAddObject;
    private RecyclerView recyclerViewObjects;

    private DatabaseReference objectsRef;
    private ObjectAdapter objectAdapter;
    private ArrayList<RegistryObject> objectList = new ArrayList<>();
    private ArrayList<RegistryObject> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_object_registry);

        etSearch = findViewById(R.id.etSearch);
        btnAddObject = findViewById(R.id.btnAddObject);
        recyclerViewObjects = findViewById(R.id.recyclerViewObjects);

        objectsRef = FirebaseDatabase.getInstance().getReference("objects");

        objectAdapter = new ObjectAdapter(filteredList);
        recyclerViewObjects.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewObjects.setAdapter(objectAdapter);

        loadObjectsFromFirebase();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterObjects(s.toString());
            }
        });

        btnAddObject.setOnClickListener(v -> showAddObjectDialog());
    }

    private void loadObjectsFromFirebase() {
        objectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                objectList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RegistryObject obj = ds.getValue(RegistryObject.class);
                    if (obj != null) objectList.add(obj);
                }
                filterObjects(etSearch.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ObjectRegistryActivity.this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Ошибка базы", error.toException());
            }
        });
    }

    private void filterObjects(String query) {
        filteredList.clear();
        for (RegistryObject obj : objectList) {
            if (obj.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(obj);
            }
        }
        objectAdapter.notifyDataSetChanged();
    }

    private void showAddObjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_object, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etDialogObjectName);
        TextInputEditText etAddress = dialogView.findViewById(R.id.etDialogObjectAddress);
        TextInputEditText etDesc = dialogView.findViewById(R.id.etDialogObjectDescription);
        Button saveButton = dialogView.findViewById(R.id.btnSave);
        Button cancelButton = dialogView.findViewById(R.id.btnCancel);

        builder.setView(dialogView);
        builder.setTitle("Добавить объект");

        // создаём диалог
        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String description = etDesc.getText().toString().trim();

            boolean valid = true;

            if (TextUtils.isEmpty(name)) {
                etName.setError("Введите название");
                valid = false;
            } else {
                etName.setError(null);
            }

            if (TextUtils.isEmpty(address)) {
                etAddress.setError("Введите адрес");
                valid = false;
            } else {
                etAddress.setError(null);
            }

            if (valid) {
                String id = String.valueOf(System.currentTimeMillis());
                RegistryObject obj = new RegistryObject(id, name, address, description);

                objectsRef.child(id).setValue(obj)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Объект добавлен", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            hideKeyboard(etName);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
