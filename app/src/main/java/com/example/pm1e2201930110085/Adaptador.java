package com.example.PM2E2201930110085;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class Adaptador extends ArrayAdapter<OBJ_foto> {
    ArrayList<OBJ_foto> listado = new ArrayList<>();
    ArrayList<OBJ_foto> copyContactos = new ArrayList<>();

    public Adaptador(Context context, int textViewResourceId, ArrayList<OBJ_foto> objects) {
        super(context, textViewResourceId, objects);
        listado = objects;
        copyContactos.addAll(listado);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vis = convertView;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vis = inflater.inflate(R.layout.items, null);

        ImageView imageView = (ImageView)vis.findViewById(R.id.img);//imagen
        imageView.setImageBitmap(listado.get(position).getImagen());

        TextView textView = (TextView)vis.findViewById(R.id.txt);//texto del lado
        textView.setText(listado.get(position).getNombre());
        return vis;
    }
    public void buscarcontact(String texto) {
        listado.clear();//borra el listado
        if (texto.length() == 0) {
            listado.addAll(copyContactos);      //add todos los registro si esta vacio el texto
        } else {
            for (OBJ_foto contacto : copyContactos) {
                if (contacto.getNombre().toLowerCase().contains(texto.toLowerCase())) {//si contacto en nombre contiene lo mismo que el texto
                    listado.add(contacto);                                              //devolver el contacto con ese nombre
                }
            }
        }
        notifyDataSetChanged();//notificar actualizacion
    }

    public ArrayList<Contacto> Buscar_result_lista(String texto) {
        ArrayList<Contacto> listadoContactos = new ArrayList<>();
        listadoContactos.clear();

        if (texto.length() == 0) {
            for (OBJ_foto contacto : copyContactos) {
                if (contacto.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                    Contacto contactoLista = new Contacto();
                    contactoLista.setId(String.valueOf(contacto.getId()));
                    contactoLista.setNombre(contacto.getNombre());
                    contactoLista.setFoto(contacto.getImagen().toString());
                    contactoLista.setLatitud(contacto.getLatitud());
                    contactoLista.setLongitud(contacto.getLongitud());
                    listadoContactos.add(contactoLista);
                }
            }
        } else {
            for (OBJ_foto contacto : copyContactos) {
                if (contacto.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                    Contacto contactoLista = new Contacto();
                    contactoLista.setId(String.valueOf(contacto.getId()));
                    contactoLista.setNombre(contacto.getNombre());
                    contactoLista.setFoto(contacto.getImagen().toString());
                    contactoLista.setLatitud(contacto.getLatitud());
                    contactoLista.setLongitud(contacto.getLongitud());
                    listadoContactos.add(contactoLista);
                }
            }
        }
        notifyDataSetChanged();
        return listadoContactos;
    }
}
