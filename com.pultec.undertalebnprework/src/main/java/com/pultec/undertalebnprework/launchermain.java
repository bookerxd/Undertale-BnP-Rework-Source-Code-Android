package com.pultec.undertalebnprework;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

public class launchermain extends AppCompatActivity {

    private int architecture;
    public static launchermain LAUNCHER;
    public static String Path;
    public static int hecho;
    private static final String CHECKBOX1_STATE_KEY = "checkbox1_state";
    private static final String CHECKBOX2_STATE_KEY = "checkbox2_state";
    private static final String CHECKBOX3_STATE_KEY = "checkbox3_state";
    private SharedPreferences sharedPreferences;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_MANAGE_EXTERNAL_STORAGE = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.md_theme_dark_background));
        setContentView(R.layout.launchermain); // Inflar el diseño XML

        LAUNCHER = this;
        hecho = 0;
        Path = "";
        architecture = checkArchitecture();
        // Check for permissions
        if (!checkPermission()) {
            requestPermissions();
        } else {
            // No se hace nada aquí si los permisos ya están concedidos
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); // Inicializar sharedPreferences aquí
        // Declaración de los nuevos CheckBox
        CheckBox checkBox1 = findViewById(R.id.checkBox);
        CheckBox checkBox2 = findViewById(R.id.checkbox_disable_shaders);
        CheckBox checkBox3 = findViewById(R.id.checkbox_disable_check);


        // Restaurar el estado de los nuevos CheckBox desde SharedPreferences
        checkBox1.setChecked(sharedPreferences.getBoolean(CHECKBOX1_STATE_KEY, false));
        checkBox2.setChecked(sharedPreferences.getBoolean(CHECKBOX2_STATE_KEY, false));
        checkBox3.setChecked(sharedPreferences.getBoolean(CHECKBOX3_STATE_KEY, false));

        // Agregar OnCheckedChangeListener para guardar el estado de los nuevos CheckBox en SharedPreferences
        checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(CHECKBOX1_STATE_KEY, isChecked);
                editor.apply();
            }
        });

        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Guardar el estado del checkBox2 en SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(CHECKBOX2_STATE_KEY, isChecked);
                editor.apply();

                // Lógica para crear el archivo config.ini
                String filePath = "/data/user/0/com.pultec.undertalebnprework/files/config.ini";
                String iniContent = "[SHADERS]\n";
                iniContent += "ANDROID-CRASH=" + (isChecked ? "1" : "0") + "\n"; // Si está seleccionado, escribe 1; de lo contrario, escribe 0

                try {
                    FileWriter writer = new FileWriter(filePath);
                    writer.write(iniContent);
                    writer.close();
                    System.out.println("Archivo INI creado con éxito en: " + filePath);
                    // Mostrar un mensaje Toast para indicar éxito
                    Toast.makeText(getApplicationContext(), getString(R.string.ini_file_created_successfully), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    System.out.println("Error al crear el archivo INI: " + e.getMessage());
                    // Mostrar un mensaje Toast para indicar error
                    Toast.makeText(getApplicationContext(), getString(R.string.error_creating_ini_file), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        });

        checkBox3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(CHECKBOX3_STATE_KEY, isChecked);
                editor.apply();
            }
        });
        // Resto de tu código...

        Button launchButton = findViewById(R.id.button_launch);
        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRunnerActivity();
            }
        });

        Button aboutButton = findViewById(R.id.button_about);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String credits = getString(R.string.credits);  // Obtiene los créditos del archivo strings.xml
                showAlertDialog(credits, "OK", null); // Muestra los créditos en el AlertDialog
            }
        });

        Button button = findViewById(R.id.button_send_error);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear el AlertDialog con un EditText
                AlertDialog.Builder builder = new AlertDialog.Builder(launchermain.this, R.style.Theme_Material3AlertDialog);
                builder.setTitle(R.string.descripcion);

                // Crear un EditText para que el usuario ingrese el texto
                final EditText editText = new EditText(launchermain.this);
                builder.setView(editText);

                // Agregar los botones "OK" y "Cancelar" al AlertDialog
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Obtener el texto ingresado por el usuario
                        String texto = editText.getText().toString();
                        // Enviar el texto por correo electrónico
                        enviarCorreo(texto);
                    }
                });
                builder.setNegativeButton("Cancel", null);

                // Mostrar el AlertDialog
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        });

        if (architecture == 32) {
            // Obtener el texto de strings.xml
            String errorMessage = getString(R.string.message_error);
            // Mostrar el diálogo con el texto obtenido
            showAlertDialognew("Error", errorMessage, "OK", null);
        }
        Button TutorialButton = findViewById(R.id.button_tutorial);
        TutorialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // URL del enlace de Discord
                String discordLink = "https://discord.gg/4ZnbSn7evf";

                // Crea un Intent para abrir el enlace en el navegador web
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(discordLink));

                // Inicia el Intent sin verificar si hay un navegador web disponible
                startActivity(intent);
            }
        });
    }
    private void enviarCorreo(String texto) {
        // Dirección de correo electrónico a la que deseas enviar el mensaje
        String correoDestino = "pultecsoftware@gmail.com";

        // Generar un número aleatorio para agregar al asunto del correo
        Random random = new Random();
        int numeroAleatorio = random.nextInt(10000); // Genera un número aleatorio entre 0 y 9999

        // Crear el asunto del correo con el texto "Bug Report" y el número aleatorio
        String asunto = "Bug Report #" + numeroAleatorio;

        // Crear un intent para enviar correo electrónico
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain"); // Establecer el tipo de contenido del intent

        // Establecer el destinatario del correo electrónico
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{correoDestino});

        // Establecer el asunto y el texto del correo electrónico
        intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        intent.putExtra(Intent.EXTRA_TEXT, texto);

        // Verificar si hay una aplicación de correo electrónico disponible para manejar el intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
        } else {
            // Obtener el texto de strings.xml
            String noEmailAppMessage = getString(R.string.no_email_app_message);
            // Mostrar el diálogo con el texto obtenido
            showAlertDialog(noEmailAppMessage, "OK", null);
        }

    }


    public int checkArchitecture() {
        String abi = System.getProperty("os.arch");
        if (abi.contains("64")) {
            return 64; // 64 bits
        } else if (abi.contains("32")) {
            return 32; // 32 bits
        } else {
            return -1; // Arquitectura desconocida
        }
    }


    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Para Android 11 o posteriores, verifica el permiso MANAGE_EXTERNAL_STORAGE
            return Environment.isExternalStorageManager();
        } else {
            // Para versiones anteriores a Android 11, verifica los permisos WRITE_EXTERNAL_STORAGE y READ_EXTERNAL_STORAGE
            int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            return writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Para Android 11 o posteriores, solicita el permiso MANAGE_EXTERNAL_STORAGE
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Para Android 10 (Q) y posteriores, solicita el permiso READ_EXTERNAL_STORAGE
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // Para versiones anteriores a Android 10 (Q), solicita permisos WRITE_EXTERNAL_STORAGE y READ_EXTERNAL_STORAGE
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE || requestCode == REQUEST_WRITE_EXTERNAL_STORAGE || requestCode == REQUEST_MANAGE_EXTERNAL_STORAGE) {
            // Verifica si se concedieron los permisos READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE o MANAGE_EXTERNAL_STORAGE
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, puedes realizar las operaciones necesarias que requieren acceso al almacenamiento externo
            } else {
                // Permiso denegado, maneja el caso en que el usuario rechazó el permiso
                // Puedes mostrar un mensaje o realizar cualquier otra acción aquí
            }
        }
    }


    //No Funciona Correctamente
     /* private int checkGameState() {
        File gameFile = new File(getExternalFilesDir(null), "game files/game.droid");
        if (gameFile.exists()) {
            Log.d("TAG", "Se encontró el Archivo");
            return 1;
        } else {
            Log.d("TAG", "No se encontró el Archivo");
            return 0;
        }
    }*/

    private void startRunnerActivity() {
        if (architecture == 64) {
        CheckBox checkBox = findViewById(R.id.checkBox);
        if (checkBox.isChecked()) {
            // Si el checkBox está marcado, inicia RunnerActivity y finaliza esta actividad
            Intent intent = new Intent(launchermain.this, RunnerActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Si el checkBox no está marcado, muestra ProgressDialog y copia los archivos
            String cache = getString(R.string.cache);
            ProgressDialog progressDialog = new ProgressDialog(this, R.style.Theme_Material3AlertDialog);
            progressDialog.setMessage(cache);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false); // Evita que se pueda cancelar el diálogo
            progressDialog.show();

            // Crear un nuevo hilo para realizar la copia de los archivos filtrados de assets
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Lista los nombres de los archivos en la carpeta assets
                        String[] assetFiles = getAssets().list("");

                        // Directorio de destino para copiar los archivos
                        File destinationDir = new File(getExternalFilesDir(null), "game files/");
                        if (!destinationDir.exists()) {
                            destinationDir.mkdirs(); // Crea el directorio si no existe
                        }

                        int totalFiles = 0;
                        // Calcular la cantidad total de archivos que coinciden con las extensiones
                        for (String assetFile : assetFiles) {
                            if (assetFile.endsWith(".ogg") || assetFile.endsWith(".droid") || assetFile.endsWith(".png") || assetFile.endsWith(".ini")) {
                                totalFiles++;
                            }
                        }

                        int filesCopied = 0;
                        // Copiar cada archivo de la carpeta assets al directorio de destino
                        for (String assetFile : assetFiles) {
                            if (assetFile.endsWith(".ogg") || assetFile.endsWith(".droid") || assetFile.endsWith(".png") || assetFile.endsWith(".ini")) {
                                InputStream inputStream = getAssets().open(assetFile);
                                OutputStream outputStream = new FileOutputStream(new File(destinationDir, assetFile));

                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = inputStream.read(buffer)) > 0) {
                                    outputStream.write(buffer, 0, length);
                                }

                                // Cerrar los flujos de entrada y salida
                                outputStream.flush();
                                outputStream.close();
                                inputStream.close();

                                // Actualizar el progreso utilizando un Handler para comunicarse con el hilo principal
                                filesCopied++;
                                int progress = (int) ((filesCopied / (float) totalFiles) * 100);
                                final int finalProgress = progress;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.setProgress(finalProgress);
                                        progressDialog.setMessage("loading cache files " + assetFile);
                                    }
                                });
                            }
                        }

                        // Cerrar el diálogo de progreso después de que se hayan copiado todos los archivos
                        progressDialog.dismiss();

                        // Continuar con la actividad RunnerActivity
                        Intent intent = new Intent(launchermain.this, RunnerActivity.class);
                        startActivity(intent);
                        finish(); // Finaliza la actividad actual
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
        CheckBox checkBox3 = findViewById(R.id.checkbox_disable_check);
        if (checkBox3.isChecked()) {
            Intent intent = new Intent(launchermain.this, RunnerActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void showAlertDialognew(String title, String message, String positiveButtonLabel, DialogInterface.OnClickListener positiveButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Material3AlertDialog);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonLabel, positiveButtonClickListener)
                .setCancelable(false); // Evitar que se pueda cancelar pulsando fuera del diálogo o con el botón de retroceso

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false); // Establecer la propiedad en el AlertDialog, no en un objeto dialog inexistente
        alertDialog.setCanceledOnTouchOutside(false); // Establecer la propiedad en el AlertDialog, no en un objeto dialog inexistente
        alertDialog.show();
    }

    private void showAlertDialog(String message, String buttonText, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Material3AlertDialog);
        builder.setMessage(message)
                .setPositiveButton(buttonText, listener)
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
