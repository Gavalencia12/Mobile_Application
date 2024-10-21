package com.example.carhive

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        val imageView: ImageView = findViewById(R.id.gifImageView)

        // Preload del GIF para evitar retrasos en la carga
        Glide.with(this)
            .load(R.drawable.car_hive_logo) // Cambia este nombre si usas WebP
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .preload()

        // Cargar el GIF en el ImageView con optimización de Glide
        Glide.with(this)
            .asDrawable() // Usa .asGif() si es un GIF; usa .asDrawable() si es WebP animado
            .load(R.drawable.car_hive_logo) // Usa aquí el WebP animado si lo convertiste
            .apply(
                RequestOptions().format(DecodeFormat.PREFER_RGB_565) // Formato de baja memoria
            )
            .transition(withCrossFade()) // Desvanecimiento suave al cargar
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cachea para mejorar rendimiento
            .into(imageView)

        // Manejar la transición al MainActivity después de 3 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000) // 3 segundos de splash
    }
}
