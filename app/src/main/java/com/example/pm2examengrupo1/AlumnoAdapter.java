package com.example.pm2examengrupo1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pm2examengrupo1.RestApiMethods.Alumno;

import java.util.List;

public class AlumnoAdapter extends ArrayAdapter<Alumno> {
    public AlumnoAdapter(Context context, List<Alumno> alumnos) {
        super(context, 0, alumnos);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflar vista personalizada para el elemento de la lista
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_alumno, parent, false);
        }

        // Obtener el objeto Alumno correspondiente a la posición actual
        Alumno alumno = getItem(position);

        // Asignar los valores a las vistas de la vista personalizada
        TextView nombreTextView = convertView.findViewById(R.id.nombre_text_view);
        TextView telefonoTextView = convertView.findViewById(R.id.telefono_text_view);
        ImageView fotoImageView = convertView.findViewById(R.id.foto_image_view);

        nombreTextView.setText(alumno.getNombre());
        telefonoTextView.setText(alumno.getTelefono());

        // Decodificar la cadena Base64 de la foto y establecerla en el ImageView
        byte[] fotoBytes = Base64.decode(alumno.getFoto(), Base64.DEFAULT);
        Bitmap fotoBitmap = BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
        fotoImageView.setImageBitmap(fotoBitmap);

        return convertView;
    }
}