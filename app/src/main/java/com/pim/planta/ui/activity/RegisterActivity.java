package com.pim.planta.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.*;
import com.pim.planta.R;
import com.pim.planta.base.NotificationActivity;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;
import com.pim.planta.repository.FirestoreRepository;

import java.util.List;

public class RegisterActivity extends NotificationActivity {

    private EditText emailEditText, passwordEditText, userEditText;
    private Button registerButton;
    private TextView logInText;
    private CheckBox customCheckbox;

    private FirebaseAuth mAuth;
    private FirestoreRepository firestoreRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firestoreRepo = FirestoreRepository.getInstance();

        userEditText = findViewById(R.id.editTextUser);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        registerButton = findViewById(R.id.buttonRegister);
        logInText = findViewById(R.id.textViewToLogIn);
        customCheckbox = findViewById(R.id.customCheckBox);
        TextView termsConditions = findViewById(R.id.terms_conditions);

        logInText.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        termsConditions.setOnClickListener(v -> showTermsConditionsPopup());
        registerButton.setOnClickListener(v -> attemptRegistration());
    }

    private void attemptRegistration() {
        String username = userEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showToast("Por favor, completa todos los campos");
            return;
        }

        if (!isValidEmail(email)) {
            showToast("Formato de correo electrónico incorrecto");
            return;
        }

        if (password.length() < 6) {
            showToast("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!customCheckbox.isChecked()) {
            showToast("Debes aceptar los términos y condiciones");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null && firebaseUser.getUid() != null && !firebaseUser.getUid().trim().isEmpty()) {
                            User newUser = new User(username, email, firebaseUser.getUid());
                            saveUserToFirestore(newUser);
                        } else {
                            showToast("Error: UID de usuario no válido");
                            mAuth.signOut();
                        }
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            checkSignInMethods(email);
                        } else {
                            showToast("Error al crear cuenta: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void checkSignInMethods(String email) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(result -> {
                    List<String> methods = result.getSignInMethods();
                    if (methods != null && methods.contains("google.com")) {
                        showToast("Este correo ya está registrado con Google. Inicia sesión con Google.");
                    } else if (methods != null && methods.contains("password")) {
                        showToast("Este correo ya está registrado. Inicia sesión normalmente.");
                    } else {
                        showToast("Este correo ya está registrado con otro método.");
                    }
                })
                .addOnFailureListener(e -> showToast("Error al comprobar métodos de inicio: " + e.getMessage()));
    }

    private void saveUserToFirestore(User newUser) {
        if (newUser.getUid() == null || newUser.getUid().trim().isEmpty()) {
            showToast("Error: UID no válido, no se puede guardar el usuario");
            mAuth.signOut();
            return;
        }

        firestoreRepo.insertUser(newUser)
                .thenRun(() -> {
                    UserLogged.getInstance().setCurrentUser(newUser);
                    runOnUiThread(() -> {
                        showToast("Registro exitoso");
                        startActivity(new Intent(this, PlantListActivity.class));
                        finish();
                    });
                })
                .exceptionally(e -> {
                    Log.e("Register", "Error al insertar usuario en Firestore", e);
                    runOnUiThread(() -> showToast("Error al guardar el usuario en Firestore"));
                    return null;
                });
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void showTermsConditionsPopup() {
        String message = "Plantoo - Términos y Condiciones\n\n[...]";
        new AlertDialog.Builder(this)
                .setTitle("Términos y Condiciones")
                .setMessage(message)
                .setPositiveButton("Aceptar", null)
                .show();
    }
}
