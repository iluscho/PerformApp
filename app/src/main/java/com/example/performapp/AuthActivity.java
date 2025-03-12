package com.example.performapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AuthActivity extends AppCompatActivity {

    EditText etLogin;
    EditText etPassword;
    private Button btnLogin, btnRegister;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Получаем ссылку на узел "users" в Realtime Database
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
    }

    // Функция регистрации нового пользователя по логину
    void registerUser() {
        final String login = etLogin.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password)) {
            Toast.makeText(AuthActivity.this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверяем, существует ли пользователь с таким логином
        Query query = usersRef.orderByChild("login").equalTo(login);
        query.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(AuthActivity.this, "Пользователь с таким логином уже зарегистрирован", Toast.LENGTH_SHORT).show();
                } else {
                    // Создаем нового пользователя с dispatcher по умолчанию false
                    String userId = usersRef.push().getKey();
                    User newUser = new User(userId, login, password, false);
                    usersRef.child(userId).setValue(newUser, new DatabaseReference.CompletionListener(){
                        @Override
                        public void onComplete(DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {
                                Toast.makeText(AuthActivity.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AuthActivity.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                            }
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

    // Функция логина
    private void loginUser() {
        final String login = etLogin.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password)) {
            Toast.makeText(AuthActivity.this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ищем пользователя по логину
        Query query = usersRef.orderByChild("login").equalTo(login);
        query.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Предполагаем, что логин уникален – берем первого найденного пользователя
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        if (user != null) {
                            if (user.getPassword().equals(password)) {
                                // Авторизация успешна, проверяем значение поля dispatcher
                                if (user.isDispatcher()) {
                                    // Если dispatcher == true, открываем DispatcherActivity
                                    startActivity(new Intent(AuthActivity.this, DispatcherActivity.class));
                                } else {
                                    // Если dispatcher == false, открываем WorkerActivity
                                    startActivity(new Intent(AuthActivity.this, WorkerActivity.class));
                                }
                                finish();
                            } else {
                                Toast.makeText(AuthActivity.this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(AuthActivity.this, "Пользователь не найден. Зарегистрируйтесь.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AuthActivity.this, "Ошибка: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
