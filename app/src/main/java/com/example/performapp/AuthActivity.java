package com.example.performapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class AuthActivity extends AppCompatActivity {

    EditText etLogin, etPassword;
    Button btnLogin, btnRegister;
    DatabaseReference usersRef;

    SharedPreferences preferences;
    private static final String PREF_NAME = "MyAppPrefs";
    private static final String KEY_USER_ID = "userId";
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Инициализация Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference("users");

        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String savedUserId = preferences.getString(KEY_USER_ID, null);

        if (savedUserId != null) {
            autoLogin(savedUserId);
            return;
        }

        setContentView(R.layout.activity_auth);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void autoLogin(String userId) {
        if (usersRef == null) {
            Toast.makeText(this, "Ошибка подключения к Firebase", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    launchNextActivity(user);
                } else {
                    preferences.edit().remove(KEY_USER_ID).apply();
                    setContentView(R.layout.activity_auth);
                    Toast.makeText(AuthActivity.this, "Пользователь не найден, повторите вход", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AuthActivity.this, "Ошибка: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser() {
        final String login = etLogin.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.orderByChild("login").equalTo(login)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(AuthActivity.this, "Пользователь уже существует", Toast.LENGTH_SHORT).show();
                        } else {
                            String userId = usersRef.push().getKey();
                            User newUser = new User(userId, login, password, false);
                            usersRef.child(userId).setValue(newUser, (error, ref) -> {
                                if (error == null) {
                                    Toast.makeText(AuthActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AuthActivity.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AuthActivity.this, "Ошибка: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginUser() {
        final String login = etLogin.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.orderByChild("login").equalTo(login)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            User user = ds.getValue(User.class);
                            if (user != null && user.getPassword().equals(password)) {
                                preferences.edit().putString(KEY_USER_ID, user.getId()).apply();
                                launchNextActivity(user);
                                return;
                            }
                        }
                        Toast.makeText(AuthActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AuthActivity.this, "Ошибка: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void launchNextActivity(User user) {
        Intent intent = new Intent(this, user.isDispatcher() ? DispatcherActivity.class : WorkerActivity.class);
        intent.putExtra("userId", user.getId());
        startActivity(intent);
        finish();
    }
}
