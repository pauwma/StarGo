package com.example.mp08_firebase.items;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mp08_firebase.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.List;

public class CabinAdapter extends RecyclerView.Adapter<CabinAdapter.CabinViewHolder>{
    private Context context;
    private List<Cabin> cabinList;
    private Lifecycle lifecycle;
    private CabinItemClickListener clickListener;

    public CabinAdapter(Context context, List<Cabin> cabinList, Lifecycle lifecycle, CabinItemClickListener clickListener) {
        this.cabinList = cabinList;
        this.context = context;
        this.lifecycle = lifecycle;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CabinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cabin, parent, false);
        return new CabinViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CabinViewHolder holder, int position) {
        Cabin cabin = cabinList.get(position);

        // ? Carousel
        holder.sliderImages.registerLifecycle(lifecycle);
        holder.sliderImages.setShowNavigationButtons(false);
        holder.sliderImages.setAutoPlay(true);
        holder.sliderImages.setAutoPlayDelay(3500);
        List<CarouselItem> list = new ArrayList<>();
        for (String imageUrl : cabin.getImages()) {
            list.add(new CarouselItem(imageUrl));
        }
        holder.sliderImages.setData(list);

        holder.tituloTextView.setText(cabin.getName());
        holder.capacityTextView.setText(String.valueOf(cabin.getCapacity()));
        holder.descriptionTextView.setText(cabin.getDescription());

        String priceStr = String.valueOf(cabin.getPrice());
        try {
            String[] priceParts = priceStr.split("\\.");
            if (priceParts.length == 2) {
                String decimalPart = String.format("%-2s", priceParts[1]).replace(' ', '0');
                holder.priceTextView.setText(priceParts[0]); // ? Parte entera del precio
                holder.lowPriceTextView.setText("." + decimalPart + "€"); // ? Parte decimal del precio
            } else {
                // ? Manejo de casos en los que el precio no tiene una parte decimal
                holder.priceTextView.setText(priceParts[0]);
                holder.lowPriceTextView.setText(".00€");
            }
        } catch (NumberFormatException e) {
            // ? Manejo de errores en caso de que la cadena de precio no pueda ser dividida correctamente
            e.printStackTrace();
            Toast.makeText(context, "Error al mostrar el precio", Toast.LENGTH_SHORT).show();
        }

        holder.reservarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onCabinReserveClick(v, cabin);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cabinList.size();
    }


    public static class CabinViewHolder extends RecyclerView.ViewHolder {

        ImageCarousel sliderImages;
        TextView  tituloTextView, capacityTextView, descriptionTextView, lowPriceTextView, priceTextView;
        Button reservarButton;

        public CabinViewHolder(@NonNull View itemView) {
            super(itemView);
            sliderImages = itemView.findViewById(R.id.sliderImages);
            tituloTextView = itemView.findViewById(R.id.tituloTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            capacityTextView = itemView.findViewById(R.id.capacityTextView);
            reservarButton = itemView.findViewById(R.id.reservarButton);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            lowPriceTextView = itemView.findViewById(R.id.lowPriceTextView);
        }
    }

    public interface CabinItemClickListener {
        void onCabinReserveClick(View v, Cabin cabin);
    }
}