package com.example.pm2examengrupo1.RestApiMethods;

public class Alumno {
    public int id;
    public String nombre;
    public String telefono;
    public String foto;
    public double  latitud;
    public double  longitud;

    public Alumno(int id, String nombre, String telefono, String foto, double  latitud, double  longitud) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.foto = foto;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public double  getLatitud() {
        return latitud;
    }

    public void setLatitud(double  latitud) {
        this.latitud = latitud;
    }

    public double  getLongitud() {
        return longitud;
    }

    public void setLongitud(double  longitud) {
        this.longitud = longitud;
    }
}
