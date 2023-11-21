package com.example.PM2E2201930110085;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;

public class EditarActivity extends AppCompatActivity {

    private String id_contact;
    EditText nombre, telefono;
    TextView latitud, longitud;
    ImageView imgFoto2;
    Contacto contacto;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PETICION_ACCESO_PERMISOS = 100;
    private static final int SELECT_PICTURE = 0;
    private static final int REQUEST_CAMERA = 1;
    Uri outputFileUri;
    String currentfotopath;
    byte[] byteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);
        nombre = (EditText) findViewById(R.id.txt_nombre1);
        telefono = (EditText) findViewById(R.id.txt_telefono1);
        imgFoto2 = (ImageView) findViewById(R.id.foto1);
        longitud = (TextView) findViewById(R.id.txt_longitud1);
        latitud = (TextView) findViewById(R.id.txt_latitud1);

        //Area de Botones
        Button btnVolver2 = (Button) findViewById(R.id.btnVolver1);
        ImageButton btnActualizarFoto = (ImageButton) findViewById(R.id.btnFoto2);
        Button btnActualizarContacto = (Button) findViewById(R.id.btn_guardar1);


        Intent intent = getIntent();
        id_contact = intent.getStringExtra("idCont");
        buscar(id_contact);

        btnVolver2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent lista = new Intent(getApplicationContext(), ListActivity.class);
                startActivity(lista);
                finish();
            }
        });

        btnActualizarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accesos();
            }
        });

        btnActualizarContacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actualizarContacto();
            }
        });
    }
    private void buscar(String id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = RestApiMethods.ApiGetID + id;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONArray obj_array = obj.getJSONArray("Contactos");

                    for (int i = 0; i < obj_array.length(); i++) {

                        JSONObject contactoObject = obj_array.getJSONObject(i);

                        contacto = new Contacto(contactoObject.getString("ID"),
                                contactoObject.getString("NOMBRE"),
                                contactoObject.getString("TELEFONO"),
                                contactoObject.getString("LATITUD"),
                                contactoObject.getString("LONGITUD"),
                                contactoObject.getString("IMAGEN"),
                                contactoObject.getString("ARCHIVO"));
                    }
                    nombre.setText(contacto.getNombre());
                    telefono.setText(contacto.getTelefono());
                    longitud.setText(contacto.getLongitud());
                    latitud.setText(contacto.getLatitud());

                    byte[] fotografia = Base64.decode(contacto.getFoto().getBytes(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(fotografia, 0, fotografia.length);
                    imgFoto2.setImageBitmap(bitmap);

                } catch (JSONException ex) {
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Error en Response", "onResponse: " +  error.getMessage().toString() );
            }
        });

        queue.add(stringRequest);
    }
    private void accesos() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditarActivity.this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PETICION_ACCESO_PERMISOS);
        } else {
            selectImage();
        }
    }
    ///////////////////////////////
    /////METODOS DE LA IMAGEN
    /////////////////////////////
    //dialogo de tomar foto o galeria
    private void selectImage() {
        final CharSequence[] items = {"Tomar Foto", "Selecciona de Galeria",
                "Cancelar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(EditarActivity.this);
        builder.setTitle("Añadir Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Tomar Foto")) {
                    File photoFile = null;
                    Intent tomarfoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Uri photoURI = FileProvider.getUriForFile(EditarActivity.this,"com.example.PM2E2201930110085.provider",photoFile);
                    if(tomarfoto.resolveActivity(getPackageManager())!=null){
                        tomarfoto.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(tomarfoto,REQUEST_CAMERA);
                    }
                } else if (items[item].equals("Selecciona de Galeria")) {
                    if (Build.VERSION.SDK_INT <= 19) {
                        Intent i = new Intent();
                        i.setType("image/*");
                        i.setAction(Intent.ACTION_GET_CONTENT);
                        i.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(i, SELECT_PICTURE);
                    } else if (Build.VERSION.SDK_INT > 19) {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, SELECT_PICTURE);
                    }
                } else if (items[item].equals("Cancelar")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    //crear el archivo de la foto
    private File createImageFile() throws IOException {
        // Crear el archivo
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentfotopath = image.getAbsolutePath();
        return image;
    }
    public String getPath(Uri uri, Activity activity) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = activity
                .managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                File imgFile = new  File(currentfotopath);
                Uri selectedImageUri;
                try {
                    selectedImageUri = outputFileUri;
                    Bitmap bm;
                    BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                    btmapOptions.inSampleSize = 2;
                    bm = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),btmapOptions);
                    // bm = Bitmap.createScaledBitmap(bm, 70, 70, true);

                    imgFoto2.setImageBitmap(bm);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();

                    String encode = Base64.encodeToString(byteArray, Base64.DEFAULT);//luego a base64
                    Calendar calendar = Calendar.getInstance();
                    DateFormat actual_date = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);//pasarle la fecha que se tomo
                    SimpleTimeZone yx = new SimpleTimeZone(0,"CST");
                    calendar.setTimeZone(yx);
                    contacto.setFoto(encode);
                    contacto.setArchivo(actual_date.format(calendar.getTime()));
                    Localizacion();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
//                String selectedImagePath = getRealPathFromURI(selectedImageUri);
                String tempPath = getPath(selectedImageUri, this);

//                BitmapFactory.Options btmapOptions;
                Bitmap bm = BitmapFactory.decodeFile(tempPath);
                try {
                    bm = BitmapFactory.decodeStream(EditarActivity.this.getContentResolver().openInputStream(selectedImageUri));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                imgFoto2.setImageBitmap(bm);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                byteArray = stream.toByteArray();

                String encode = Base64.encodeToString(byteArray, Base64.DEFAULT);//luego a base64
                Calendar calendar = Calendar.getInstance();
                DateFormat actual_date = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);//pasarle la fecha que se tomo
                SimpleTimeZone yx = new SimpleTimeZone(0,"CST");
                calendar.setTimeZone(yx);
                contacto.setFoto(encode);
                contacto.setArchivo(actual_date.format(calendar.getTime()));
                Localizacion();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PETICION_ACCESO_PERMISOS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFoto();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Se necesitan permisos de acceso", Toast.LENGTH_LONG).show();
        }
    }
    private void tomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }
    public void Ubicacion(Context contexto, LocationManager locationManager) {
        String location_context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) contexto.getSystemService(location_context);
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(provider, 1000, 0,
                    new LocationListener() {

                        public void onLocationChanged(Location location) {
                            String txtlongitud = String.valueOf(location.getLongitude());
                            String txtlatitud = String.valueOf(location.getLatitude());
                            contacto.setLongitud(txtlongitud);
                            contacto.setLatitud(txtlatitud);
                            longitud.setText(txtlongitud);
                            latitud.setText(txtlatitud);
                        }

                        public void onProviderDisabled(String provider) {
                        }

                        public void onProviderEnabled(String provider) {
                        }

                        public void onStatusChanged(String provider, int status,
                                                    Bundle extras) {
                        }
                    });
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                String txtlongitud = String.valueOf(location.getLongitude());
                String txtlatitud = String.valueOf(location.getLatitude());
                contacto.setLongitud(txtlongitud);
                contacto.setLatitud(txtlatitud);
                longitud.setText(txtlongitud);
                latitud.setText(txtlatitud);
            }
        }
    }
    private void Localizacion() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Ubicacion(getApplicationContext(), lm);
    }
    private void actualizarContacto() {
        int comprobaciones = 0;
        int numeros = 0;
        if(nombre.getText().toString().isEmpty() || telefono.getText().toString().isEmpty()) {
            mostrarDialogoVacios();
            comprobaciones = 1;
        }

        if(contacto.getFoto() == "" && comprobaciones == 0) {
            mostrarDialogoImagenNoTomada();
            comprobaciones = 1;
        }

        if((contacto.getLatitud() == "" || contacto.getLongitud() == "") && comprobaciones == 0) {
            mostrarDialogoLocalizacionNoEncontrada();
            comprobaciones = 1;
        }

        if(comprobaciones == 0) {
            contacto.setNombre(nombre.getText().toString());
            contacto.setTelefono(telefono.getText().toString());
            JSONObject object = new JSONObject();
            String url = RestApiMethods.ApiUpdateUrl;
            try
            {
                object.put("id",contacto.getId());
                object.put("nombre",contacto.getNombre());
                object.putOpt("telefono",contacto.getTelefono());
                object.putOpt("latitud",contacto.getLatitud());
                object.putOpt("longitud",contacto.getLongitud());
                object.putOpt("imagen",contacto.getFoto());
                object.putOpt("archivo",contacto.getArchivo());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String Error = response.getString("httpStatus");
                                if (Error.equals("")||Error.equals(null)){
                                }else if(Error.equals("OK")){

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(getApplicationContext(),"Contacto Actualizado",Toast.LENGTH_LONG).show();
                            Intent pantallaRegresoList = new Intent(getApplicationContext(), ListActivity.class);
                            startActivity(pantallaRegresoList);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("Error", "Error: " + error.getMessage());
                    Toast.makeText(EditarActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(jsonObjectRequest);
        }
    }
    private void mostrarDialogoVacios() {
        new AlertDialog.Builder(this)
                .setTitle("Alerta de Vacíos")
                .setMessage("No puede dejar ningún campo vacío")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }
    private void mostrarDialogoImagenNoTomada() {
        new AlertDialog.Builder(this)
                .setTitle("Alerta de Fotografía")
                .setMessage("No se ha capturo ninguna fotografía")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }
    private void mostrarDialogoLocalizacionNoEncontrada() {
        new AlertDialog.Builder(this)
                .setTitle("Alerta de Localización")
                .setMessage("No se ha encontrado su localización")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }


}