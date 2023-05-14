package com.example.mp08_firebase;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class DetailedImageFragment extends Fragment {

    private NavController navController;
    private String imageUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_detailed_image, container, false);
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        PhotoView detailedImageView = view.findViewById(R.id.detailedImageView);
        imageUrl = getArguments().getString("imageUrl");
        Glide.with(this).load(imageUrl).into(detailedImageView);

        ImageButton downloadImageButton = view.findViewById(R.id.downloadImageButton);
        downloadImageButton.setOnClickListener(v -> downloadImage());

        // ? Flecha Back
        view.findViewById(R.id.crossImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });

    }

    private void downloadImage() {
        // Primero, comprueba si tienes permiso para escribir en el almacenamiento del dispositivo
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Si no tienes permiso, solicítalo
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            // Si tienes permiso, descarga y guarda la imagen
            Toast.makeText(getContext(), "Descargando imagen...", Toast.LENGTH_SHORT).show();
            Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            saveImage(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }
    }

    private void saveImage(Bitmap bitmapImage) {
        OutputStream fos;
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            // En Android 10 (API nivel 29) y versiones posteriores, usa MediaStore para guardar la imagen
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + "StarGO");
            values.put(MediaStore.Images.Media.IS_PENDING, true);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "image_stargo_" + System.currentTimeMillis() + ".jpg");

            Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            try {
                fos = getContext().getContentResolver().openOutputStream(uri);
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Objects.requireNonNull(fos);

                values.put(MediaStore.Images.Media.IS_PENDING, false);
                getContext().getContentResolver().update(uri, values, null, null);

                Toast.makeText(getContext(), "Imagen descargada", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            // En Android 9 (API nivel 28) y versiones anteriores, usa el método más antiguo
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File image = new File(imagesDir, "image_stargo_" + System.currentTimeMillis() + ".jpg");

            try {
                fos = new FileOutputStream(image);
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Objects.requireNonNull(fos);

                Toast.makeText(getContext(), "Imagen descargada", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}