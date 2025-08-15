package com.pim.planta.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pim.planta.R;
import com.pim.planta.base.NotificationActivity;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pim.planta.repository.FirestoreRepository;

public class LoginActivity extends NotificationActivity {
    private static final String PREFS_NAME = "plant_prefs";
    private static final String SELECTED_PLANT_KEY = "selectedPlant";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            fetchAndSetCurrentUser(currentUser.getUid());
        } else {
            setContentView(R.layout.activity_login);
            initializeGoogleSignIn();
            setupListeners();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && (currentUser.getUid() == null || currentUser.getUid().trim().isEmpty())) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void initializeGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupListeners() {
        EditText elemail = findViewById(R.id.editTextEmail);
        EditText password = findViewById(R.id.editTextPassword);
        Button loginButton = findViewById(R.id.buttonLogin);
        TextView registerText = findViewById(R.id.textViewToRegister);
        com.google.android.gms.common.SignInButton googleSignInButton = findViewById(R.id.google_sign_in_button);

        registerText.setOnClickListener(v -> navigateTo(RegisterActivity.class));

        loginButton.setOnClickListener(v -> {
            String email = elemail.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (!email.isEmpty() && !pass.isEmpty()) {
                attemptLogin(email, pass);
            } else {
                showToast("Por favor complete ambos campos");
            }
        });

        googleSignInButton.setOnClickListener(v -> {
            // 🔁 Forzar selector de cuenta siempre
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        });
    }

    private void attemptLogin(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchAndSetCurrentUser(user.getUid());
                        }
                    } else {
                        showToast("Error de autenticación: " + task.getException().getMessage());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                showToast("Fallo en Google Sign-In: " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            if (uid == null || uid.trim().isEmpty()) {
                                showToast("Error: UID inválido");
                                mAuth.signOut();
                                return;
                            }

                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener(document -> {
                                        if (document.exists()) {
                                            User user = document.toObject(User.class);
                                            if (user != null && user.getUid() != null) {
                                                UserLogged.getInstance().setCurrentUser(user);
                                                navigateToAppropriateScreen();
                                            } else {
                                                showToast("Usuario inválido en Firestore");
                                                mAuth.signOut();
                                            }
                                        } else {
                                            String username = firebaseUser.getDisplayName() != null
                                                    ? firebaseUser.getDisplayName() : "Usuario";
                                            String email = firebaseUser.getEmail();

                                            User newUser = new User(username, email, uid);
                                            FirebaseFirestore.getInstance()
                                                    .collection("users")
                                                    .document(uid)
                                                    .set(newUser)
                                                    .addOnSuccessListener(unused -> {
                                                        if (newUser.getUid() == null || newUser.getUid().trim().isEmpty()) {
                                                            showToast("Error: UID no guardado correctamente");
                                                            mAuth.signOut();
                                                            return;
                                                        }
                                                        UserLogged.getInstance().setCurrentUser(newUser);
                                                        showToast("Bienvenido, " + username);
                                                        navigateToAppropriateScreen();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        showToast("Error al guardar usuario: " + e.getMessage());
                                                        mAuth.signOut();
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        showToast("Error al acceder a Firestore: " + e.getMessage());
                                        mAuth.signOut();
                                    });
                        }
                    } else {
                        showToast("Autenticación con Google fallida");
                    }
                });
    }

    private void fetchAndSetCurrentUser(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            showToast("UID no válido");
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        if (user != null && user.getUid() != null && !user.getUid().trim().isEmpty()) {
                            UserLogged.getInstance().setCurrentUser(user);
                            navigateToAppropriateScreen();
                        } else {
                            showToast("Usuario inválido en Firestore.");
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        }
                    } else {
                        showToast("Usuario no encontrado en Firestore.");
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Error al obtener usuario: " + e.getMessage());
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                });
    }

    private void navigateToAppropriateScreen() {
        User currentUser = UserLogged.getInstance().getCurrentUser();

        if (currentUser != null && currentUser.getSelectedPlant() != null && !currentUser.getSelectedPlant().trim().isEmpty()) {
            startActivity(new Intent(LoginActivity.this, JardinActivity.class));
        } else {
            startActivity(new Intent(LoginActivity.this, PlantListActivity.class));
        }
        finish();
    }


    private boolean hasUserSelectedPlant() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE).contains(SELECTED_PLANT_KEY);
    }

    private void navigateTo(Class<?> cls) {
        startActivity(new Intent(LoginActivity.this, cls));
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
