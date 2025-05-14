package com.example.performapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WorkerListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WorkerAdapter adapter;
    private List<User> workerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_list);

        recyclerView = findViewById(R.id.recyclerViewWorkers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Получаем список работников из Firebase
        DatabaseReference workersRef = FirebaseDatabase.getInstance().getReference("users");
        workersRef.orderByChild("dispatcher").equalTo(false) // фильтруем только работников
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        workerList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null) {
                                workerList.add(user);
                            }
                        }
                        adapter = new WorkerAdapter(workerList);
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(WorkerListActivity.this, "Ошибка при загрузке данных", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
