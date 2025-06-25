package com.pim.planta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.pim.planta.db.DAO;
import com.pim.planta.db.DatabaseExecutor;
import com.pim.planta.db.PlantRepository;
import com.pim.planta.models.Plant;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;
import com.pim.planta.models.UserPlantRelation;

public class RegisterActivity extends NotificationActivity {

    private EditText emailEditText, passwordEditText, userEditText;
    private Button registerButton;
    private User newUser;
    private TextView logInText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userEditText = findViewById(R.id.editTextUser);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        registerButton = findViewById(R.id.buttonRegister);
        logInText = findViewById(R.id.textViewToLogIn);
        CheckBox customCheckbox = findViewById(R.id.customCheckBox);

        logInText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        TextView termsConditions = findViewById(R.id.terms_conditions);
        termsConditions.setOnClickListener(v -> showTermsConditionsPopup());

        registerButton.setOnClickListener(v -> {
            String user = userEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();


            if (email.isEmpty() || password.isEmpty() || user.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return; // Salir del método si hay campos vacíos
            }

            if (!customCheckbox.isChecked()) {
                Toast.makeText(RegisterActivity.this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail(email)) {
                Toast.makeText(RegisterActivity.this, "Formato de correo electrónico incorrecto", Toast.LENGTH_SHORT).show();
                return; // Salir del método si el email es inválido
            }

            newUser = new User(user, email, password);
            this.registerUser(newUser);
            PlantRepository plantRepo = PlantRepository.getInstance(this);
            DAO dao = plantRepo.getPlantaDAO();
            DatabaseExecutor.executeAndWait(() -> {
                newUser = dao.getUserByEmail(email);
                for (Plant plant : dao.getAllPlantas()) {
                    UserPlantRelation relation = new UserPlantRelation(newUser.getId(), plant.getId());
                    Log.d("RegisterActivity", "Plant ID: " + plant.getId());
                    Log.d("RegisterActivity", "User ID: " + newUser.getId());
                    Log.d("RegisterActivity", "Relation: " + relation.getPlantId() + " " + relation.getUserId() + " " + relation.growCount);
                    dao.insert(relation);
                }
            });
            Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
            UserLogged.getInstance().setCurrentUser(newUser);
            Intent intent = new Intent(RegisterActivity.this, PlantListActivity.class);
            startActivity(intent);
        });
    }

    private void showTermsConditionsPopup() {
        String message = "Plantoo - Términos y Condiciones\n\n" +
                "Última actualización: 29/11/2024\n\n" +
                "1. Introducción\n" +
                "Bienvenido a Plantoo, una aplicación diseñada para promover el bienestar mental a través de un jardín virtual interactivo. Al usar Plantoo, aceptas cumplir estos Términos y Condiciones. Si no estás de acuerdo con alguna parte de estos términos, no debes usar la aplicación.\n\n" +
                "2. Uso de la Aplicación\n" +
                "- Plantoo está diseñada para uso personal y no comercial.\n" +
                "- Debes ser mayor de 13 años para usar esta app. Los menores deben contar con la supervisión de un tutor legal.\n" +
                "- Queda prohibido usar la app de manera que viole leyes locales, nacionales o internacionales.\n\n" +
                "3. Cuentas de Usuario\n" +
                "- Para usar ciertas funciones, puede que necesites crear una cuenta. Es tu responsabilidad mantener la confidencialidad de tus datos de acceso.\n" +
                "- Plantoo no se hace responsable de accesos no autorizados a tu cuenta por negligencia en la protección de tus credenciales.\n\n" +
                "4. Privacidad y Datos Personales\n" +
                "- Plantoo recopila datos necesarios para su funcionamiento, como emociones registradas, estadísticas de uso y datos de interacción.\n" +
                "- La información que proporcionas se usará únicamente para mejorar la experiencia de la app y no se compartirá con terceros sin tu consentimiento, salvo que sea requerido por ley.\n" +
                "- Para más detalles, consulta nuestra Política de Privacidad.\n\n" +
                "5. Contenido del Usuario\n" +
                "- Puedes registrar emociones y notas dentro de la app. Al hacerlo, declaras que el contenido es tuyo y no infringe los derechos de terceros.\n" +
                "- Plantoo no se hace responsable del contenido que guardes en la app.\n\n" +
                "6. Propiedad Intelectual\n" +
                "- Todos los elementos de la app, incluidos el diseño, logo, texto, gráficos y funcionalidades, son propiedad de Plantoo y están protegidos por derechos de autor.\n" +
                "- No está permitido copiar, distribuir, modificar o utilizar cualquier contenido de la app sin permiso explícito.\n\n" +
                "7. Limitaciones de Responsabilidad\n" +
                "- Plantoo se ofrece \"tal cual\" sin garantías explícitas o implícitas sobre su funcionalidad, precisión o disponibilidad.\n" +
                "- No nos hacemos responsables de daños directos o indirectos derivados del uso o incapacidad de uso de la app.\n\n" +
                "8. Cambios en los Términos\n" +
                "- Nos reservamos el derecho de modificar estos Términos y Condiciones en cualquier momento. Se notificará a los usuarios sobre cualquier cambio significativo a través de la app o por correo electrónico.\n" +
                "- El uso continuado de Plantoo después de la actualización implica tu aceptación de los nuevos términos.\n\n" +
                "9. Contacto\n" +
                "Si tienes preguntas o inquietudes sobre estos Términos y Condiciones, puedes contactarnos en:\n" +
                "- Correo: support@plantoo.com\n" +
                "- Dirección: Universidad Politécnica de Valencia";

        // Crear el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Términos y Condiciones")
                .setMessage(message)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    private void registerUser(User user){
        PlantRepository plantRepo = PlantRepository.getInstance(this);
        DatabaseExecutor.execute(() -> {
            plantRepo.getPlantaDAO().insert(user);
        });
    }
}