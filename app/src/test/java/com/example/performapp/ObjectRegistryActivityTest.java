package com.example.performapp;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class ObjectRegistryActivityTest {

    @Rule
    public ActivityScenarioRule<ObjectRegistryActivity> activityRule =
            new ActivityScenarioRule<>(ObjectRegistryActivity.class);

    @Mock
    private Toast toastMock;

    private EditText etObjectName, etObjectAddress, etObjectDescription;
    private Button btnAddObject;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // Мы можем использовать ActivityScenario для взаимодействия с активностью
        activityRule.getScenario().onActivity(activity -> {
            this.etObjectName = activity.findViewById(R.id.etObjectName);
            this.etObjectAddress = activity.findViewById(R.id.etObjectAddress);
            this.etObjectDescription = activity.findViewById(R.id.etObjectDescription);
            this.btnAddObject = activity.findViewById(R.id.btnAddObject);
        });
    }

    @Test
    public void testAddObjectSuccess() {
        // Подготовка тестовых данных
        etObjectName.setText("Test Object");
        etObjectAddress.setText("123 Test St");
        etObjectDescription.setText("A description");

        // Вызов метода нажатия кнопки
        btnAddObject.performClick();

        // Проверка, что Toast отображается (с использованием моков)
        verify(toastMock).show();
    }

    @Test
    public void testAddObjectEmptyFields() {
        // Подготовка тестовых данных
        etObjectName.setText("");
        etObjectAddress.setText("");
        etObjectDescription.setText("Description");

        // Вызов метода нажатия кнопки
        btnAddObject.performClick();

        // Проверка, что Toast с ошибкой отображается
        verify(toastMock).show();
    }
}
