package com.example.PM2E2201930110085;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    private String idCont, nombre, telefono, longitud, latitud;
    private ProgressBar progressBar;
    ListView lstContactos;
    ArrayList<String> arrayListContactos;
    ArrayList<OBJ_foto> listadoContactos;
    ArrayList<Contacto> lista;
    EditText Buscar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        lstContactos = findViewById(R.id.lstContactos);
        arrayListContactos = new ArrayList<String>();
        listadoContactos = new ArrayList<>();;
        Buscar = (EditText) findViewById(R.id.txtBuscar);
        Button btnVolver = (Button) findViewById(R.id.btnVolver2);
        Button btnActualizar = (Button) findViewById(R.id.btnEditar);
        Button btnUbicacion = (Button) findViewById(R.id.btnUbicacion);
        Button btnRuta = (Button) findViewById(R.id.btnRuta);
        Button btnEliminar = (Button) findViewById(R.id.btnEliminar);
        //LISTA DE CONTACTOS
        consultar_lista();
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                eliminarContacto();


            }
        });

        btnUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               ubicacion();
            }
        });

        btnRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idCont != null){
                    //Inicio de la navegación paso a paso
                    ubicacionContacto();
                }else{
                    dialog_seleccione();
                }
            }
        });

        lstContactos.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                idCont = lista.get(position).getId();
                nombre = lista.get(position).getNombre();
                telefono = lista.get(position).getTelefono();
                longitud = lista.get(position).getLongitud();
                latitud = lista.get(position).getLatitud();
                Toast.makeText(getApplicationContext(), "seleccionaste a "+nombre, Toast.LENGTH_SHORT).show();
            }
        });

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(idCont != null){
                    Intent intent = new Intent(getApplicationContext(), EditarActivity.class);
                    intent.putExtra("idCont", String.valueOf(idCont));
                    startActivity(intent);
                }else{
                    dialog_seleccione();
                }
            }
        });
    }
    //dialogo de seleccionar contacto
    private void dialog_seleccione() {
        new AlertDialog.Builder(this)
                .setTitle("Aviso")
                .setMessage("Seleccione un contacto de la lista")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

    //consulta a la API
    private void consultar_lista() {
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = RestApiMethods.ApiGetUrl;
        lista = new ArrayList<Contacto>();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONArray ContactoArray = obj.getJSONArray("Contactos");//buscar el array llamado Contactos
                    if (ContactoArray.length() > 0) {

                        for (int i = 0; i < ContactoArray.length(); i++) {//recorrer todos los elementos o jsonobject
                            JSONObject contactoObject = ContactoArray.getJSONObject(i);

                            Contacto contact = new Contacto(
                                    contactoObject.getString("ID"),
                                    contactoObject.getString("NOMBRE"),
                                    contactoObject.getString("TELEFONO"),
                                    contactoObject.getString("LATITUD"),
                                    contactoObject.getString("LONGITUD"),
                                    contactoObject.getString("IMAGEN"),
                                    contactoObject.getString("ARCHIVO"));
                            lista.add(contact);
                            arrayListContactos.add(contact.getNombre());

                            byte[] foto = Base64.decode(contact.getFoto().getBytes(), Base64.DEFAULT);
                            OBJ_foto fotografia = new OBJ_foto(BitmapFactory.decodeByteArray(foto, 0, foto.length), contact.getNombre());
                            fotografia.setId(contact.getId());              //ponerle el id al obj
                            fotografia.setLongitud(contact.getLongitud());  //ponerle el la LATITUD al obj
                            fotografia.setLatitud(contact.getLatitud());    //ponerle el la LONGITUD al obj
                            listadoContactos.add(fotografia);               //ponerla en la lista
                        }
                        Adaptador adp = new Adaptador(getApplicationContext(), R.layout.items, listadoContactos );
                        lstContactos.setAdapter(adp);

                        //buscar el contacto con buscador personalizado
                        Buscar.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                adp.buscarcontact(s.toString());   //buscar por el texto del edittext
                                lista = adp.Buscar_result_lista(s.toString());    //estar actualizando la lista
//                      adp.getFilter().filter(s);  //NO FUNCIONO
                            }
                            @Override
                            public void afterTextChanged(Editable s) {}
                        });

                    }else{
                        Toast.makeText(getApplicationContext(), "No hay contactos disponibles", Toast.LENGTH_SHORT).show();
                        //mensaje.setText("No hay Contactos");
                    }
                       } catch (JSONException ex) {
                    progressBar.setVisibility(View.GONE);
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ListActivity.this);
                    builder.setTitle("Error");
                    builder.setMessage("No se pudieron cargar los datos :(\nPor favor revisa tu conexion");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    //Toast.makeText(getApplicationContext(), "ERROR>", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ListActivity.this);
                builder.setTitle("Error");
                builder.setMessage("No se pudieron cargar los datos :(\nPor favor revisa tu conexion");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });


                builder.show();

                //Toast.makeText(getApplicationContext(), "ERROR :( Revisa tu conexion", Toast.LENGTH_LONG).show();
            }
        });

        queue.add(stringRequest);
    }

    //obtener la ruta a la ubicacion del contacto
    private void ubicacionContacto() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Desea conocer la ruta hacia la ubicación de " + nombre + "?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitud+","+longitud);
                        Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        intent.setPackage("com.google.android.apps.maps");
                        if (intent.resolveActivity(getPackageManager()) != null) {startActivity(intent);}
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "CANCELADO", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void ubicacion(){
        if(idCont != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmación")
                    .setMessage("¿Desea ver la ubicación de " + nombre + "?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Double lat = Double.valueOf(latitud);
                            Double lon = Double.valueOf(longitud);
                            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                            intent.putExtra("latitud", lat);
                            intent.putExtra("longitud", lon);
                            intent.putExtra("nombre", nombre);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getApplicationContext(), "CANCELADO", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        }
        else {
            dialog_seleccione();
        }
    }
    private void eliminarContacto() {
        if(idCont != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmación")
                    .setMessage("¿Desea eliminar el contacto de " + nombre + "?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            RequestQueue queue = Volley.newRequestQueue(ListActivity.this);
                            String url = RestApiMethods.ApiDeleteUrl + idCont;

                            StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(getApplicationContext(), "Se elimino el contacto "+nombre, Toast.LENGTH_SHORT).show();
                                    limpiarLista();
                                    consultar_lista();

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(), "Error al eliminar contacto", Toast.LENGTH_SHORT).show();
                                }
                            });
                            queue.add(stringRequest);

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getApplicationContext(), "CANCELADO", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        }
        else {
            dialog_seleccione();
        }
    }
    private void limpiarLista() {
        arrayListContactos.clear();
        listadoContactos.clear();
        lista.clear();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) lstContactos.getAdapter();
        adapter.notifyDataSetChanged();
    }



}
