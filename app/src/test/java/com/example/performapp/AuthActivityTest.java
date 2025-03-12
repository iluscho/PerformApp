package com.example.performapp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.widget.EditText;
import android.widget.Toast;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthActivityTest {

    @Mock
    private FirebaseDatabase mockDatabase;

    @Mock
    private DatabaseReference mockUsersRef;

    @Mock
    private EditText mockLoginField, mockPasswordField;

    @InjectMocks
    private AuthActivity authActivity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockDatabase.getReference("users")).thenReturn(mockUsersRef);
        authActivity = new AuthActivity();
        authActivity.etLogin = mockLoginField;
        authActivity.etPassword = mockPasswordField;
        authActivity.usersRef = mockUsersRef;
    }

    @Test
    public void testRegisterUser_EmptyFields_ShouldShowToast() {
        when(mockLoginField.getText().toString().trim()).thenReturn("");
        when(mockPasswordField.getText().toString().trim()).thenReturn("");

        authActivity.registerUser();

        verify(mockUsersRef, never()).push();
    }

    @Test
    public void testRegisterUser_ValidInput_ShouldPushToDatabase() {
        when(mockLoginField.getText().toString().trim()).thenReturn("testuser");
        when(mockPasswordField.getText().toString().trim()).thenReturn("password");

        DatabaseReference mockNewUserRef = mock(DatabaseReference.class);
        when(mockUsersRef.push()).thenReturn(mockNewUserRef);
        when(mockNewUserRef.getKey()).thenReturn("12345");

        authActivity.registerUser();

        verify(mockNewUserRef).setValue(any(User.class), any());
    }
}
