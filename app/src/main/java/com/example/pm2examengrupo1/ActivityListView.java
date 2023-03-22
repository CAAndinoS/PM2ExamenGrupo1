package com.example.pm2examengrupo1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.pm2examengrupo1.RestApiMethods.Alumno;
import com.example.pm2examengrupo1.RestApiMethods.Methods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityListView extends AppCompatActivity {
    private int selectedPosition = ListView.INVALID_POSITION; // Variable para mantener la selección
    ListView listView;
    List<Alumno> alumnoList;
    ArrayList<String> arrayAlumn;
    Button btnActualizar,btnEliminar;
    SearchView searchView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        ControlsSet();
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        alumnoList = new ArrayList<>();
        arrayAlumn = new ArrayList<String>();

        ConsumirApi();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position; // Guardar la selección
                listView.setItemChecked(position, true);
                Dialog();

            }
        });

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                Alumno alumno = alumnoList.get(selectedPosition);
                Intent intent = new Intent(getApplicationContext(), ActivityActualizar.class);
                intent.putExtra("id", alumno.getId());
                startActivity(intent);

       }});

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Alumno alumno = alumnoList.get(selectedPosition);
                int id = alumno.getId();
                EliminarApi(id);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String nombre) {
                // Realiza la búsqueda cuando el usuario presiona el botón de búsqueda
                GetAPIByName(nombre);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String nombre) {
                // Realiza la búsqueda mientras el usuario ingresa texto en la caja de texto
                GetAPIByName(nombre);
                return false;
            }
        });


    }
    private void ControlsSet() {
        btnActualizar = findViewById(R.id.btnActualizar);
        btnEliminar = findViewById(R.id.btnEliminar);
        searchView = findViewById(R.id.searchView);
        listView = (ListView) findViewById(R.id.listview);
    }

    private void GetAPIByName(String nombre) {

        // Crear una instancia de la cola de solicitudes de Volley
        RequestQueue queue = Volley.newRequestQueue(this);

        // Crear una solicitud GET de cadena con el nombre del método y el parámetro de nombre
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Methods.GetAlumnsByName + "?nombre=" + nombre, new Response.Listener<String>() {

            // Manejar la respuesta exitosa de la solicitud
            @Override
            public void onResponse(String response) {
                try {
                    // Crear un JSONArray a partir de la respuesta recibida
                    JSONArray jsonArray = new JSONArray(response);

                    // Crear una lista de objetos Alumno
                    List<Alumno> alumnos = new ArrayList<>();

                    // Recorrer cada elemento del JSONArray
                    for (int i = 0; i < jsonArray.length(); i++) {
                        // Obtener el objeto JSON del alumno actual
                        JSONObject rowAlumn = jsonArray.getJSONObject(i);

                        // Crear un objeto Alumno a partir de los valores en el objeto JSON
                        Alumno alumn = new Alumno(rowAlumn.getInt("id"),
                                rowAlumn.getString("nombre"),
                                rowAlumn.getString("telefono"),
                                rowAlumn.getString("foto"),
                                rowAlumn.getDouble("latitud"),
                                rowAlumn.getDouble("longitud")
                        );

                        // Agregar el objeto Alumno a la lista de alumnos
                        alumnos.add(alumn);
                    }

                    // Crear una instancia de AlumnoAdapter y pasar la lista actualizada de objetos Alumno
                    AlumnoAdapter adapter = new AlumnoAdapter(ActivityListView.this, alumnos);

                    // Establecer el adaptador en el ListView
                    listView.setAdapter(adapter);


                } catch (JSONException ex) {
                    // Manejar una excepción JSON
                    ex.toString();
                }

            }
        }, new Response.ErrorListener() {

            // Manejar la respuesta de error de la solicitud
            @Override
            public void onErrorResponse(VolleyError error) {
                // Manejar un error de solicitud Volley
            }
        });

        // Agregar la solicitud a la cola de solicitudes de Volley
        queue.add(stringRequest);
    }

    private void EliminarApi(int id) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar registro")
                .setMessage("¿Está seguro que desea eliminar este registro?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Enviar solicitud DELETE usando Volley

                        // Crear una nueva cola de solicitudes Volley
                        RequestQueue peticion = Volley.newRequestQueue(getApplicationContext());

                        // Crear una solicitud de tipo JsonObjectRequest para enviar la solicitud DELETE
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                Request.Method.DELETE, // Método de la solicitud
                                Methods.DeleteApi + "?id=" + id, // URL de la solicitud
                                null, // Cuerpo de la solicitud (no se necesita para DELETE)
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            // Eliminar el elemento seleccionado de la lista de alumnos
                                            alumnoList.remove(selectedPosition);

                                            // Notificar al adaptador del ListView que los datos han cambiado
                                            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                    }
                                });

                        // Agregar la solicitud a la cola de solicitudes Volley
                        peticion.add(jsonObjectRequest);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void ConsumirApi() {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Methods.GetApi, new Response.Listener<String>() {
            // Si la solicitud es exitosa, se llama a este método
            @Override
            public void onResponse(String response) {
                try {
                    // Se crea un JSONArray a partir de la respuesta recibida de la API
                    JSONArray EmpleArray = new JSONArray(response);

                    // Se recorre el JSONArray para crear objetos Alumno a partir de cada objeto JSON en el array
                    for(int i=0; i < EmpleArray.length(); i++) {
                        JSONObject RowAlumn = EmpleArray.getJSONObject(i);
                        Alumno alumn = new Alumno(RowAlumn.getInt("id"),
                                RowAlumn.getString("nombre"),
                                RowAlumn.getString("telefono"),
                                RowAlumn.getString("foto"),
                                RowAlumn.getDouble("latitud"),
                                RowAlumn.getDouble("longitud")
                        );

                        // Se agrega cada objeto Alumno a una lista y a un array de strings
                        alumnoList.add(alumn);
                        arrayAlumn.add(alumn.getNombre() + " " + alumn.getTelefono());
                    }

                    // Se crea un adaptador personalizado para mostrar la lista de alumnos en una ListView
                    AlumnoAdapter adp = new AlumnoAdapter(getApplicationContext(), alumnoList);
                    listView.setAdapter(adp);

                } catch (JSONException ex) {
                    ex.toString();
                }

            }
        }, new Response.ErrorListener() {
            // Si la solicitud falla, se llama a este método
            @Override
            public void onErrorResponse(VolleyError error) {
                // Manejar el error
            }
        });

        // Se agrega la solicitud a la cola de solicitudes Volley
        queue.add(stringRequest);
    }

    private void Dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Desea abrir la ubicación en el mapa?");

        // Configuración del botón "Sí" que se muestra en el diálogo de alerta
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Obtiene el objeto Alumno seleccionado y lo utiliza para abrir la ubicación en el mapa
                Alumno alumno = alumnoList.get(selectedPosition);
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("latitud", alumno.getLatitud());
                intent.putExtra("longitud", alumno.getLongitud());
                startActivity(intent);
            }
        });

        // Configuración del botón "No" que se muestra en el diálogo de alerta
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // No hace nada cuando el usuario hace clic en "No"
            }
        });

        // Crea y muestra el diálogo de alerta
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedPosition != ListView.INVALID_POSITION) {
            listView.setItemChecked(selectedPosition, true); // Establecer la selección del elemento de la lista
            // que fue seleccionado antes de abrir la actividad de mapa
        }
    }


}