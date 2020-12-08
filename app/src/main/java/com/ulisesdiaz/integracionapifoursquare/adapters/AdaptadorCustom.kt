package com.ulisesdiaz.integracionapifoursquare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.ulisesdiaz.integracionapifoursquare.R
import com.ulisesdiaz.integracionapifoursquare.models.Venue


class AdaptadorCustom(
        items: ArrayList<Venue>,
) : RecyclerView.Adapter<AdaptadorCustom.ViewHolder>() {

    var items: ArrayList<Venue>? = null
    var itemsSeleccionados: ArrayList<Int>? = null

    var viewHolder: ViewHolder? = null
    var context: Context? = null

    init {
        this.items = items
        this.itemsSeleccionados = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdaptadorCustom.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.template_venues, parent, false)
        context = parent?.context
        viewHolder = ViewHolder(view)

        return viewHolder!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items?.get(position)
        holder.nombre?.text = item?.name
        //TODO
        if (item?.categories?.size!! > 0){
            val urlImagen = item?.categories?.get(0)!!.icon?.prefix  +  "bg_64" + item?.categories?.get(0)!!.icon?.suffix
            Picasso.get().load(urlImagen).into(holder.foto)
        }
    }

    override fun getItemCount(): Int {
        return items?.count()!!
    }


    class ViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        val view = view
        var foto: ImageView? = null
        var nombre: TextView? = null

        init {
            foto = view.findViewById(R.id.imgFoto)
            nombre = view.findViewById(R.id.txtNombre)
        }
    }

}
