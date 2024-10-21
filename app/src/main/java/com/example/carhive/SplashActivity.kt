package com.example.carhive

import android.animation.*
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash) // Tu layout splash.xml

        // Referencias a los elementos
        val carImage: ImageView = findViewById(R.id.car_image)
        val hiveImage: ImageView = findViewById(R.id.hive_image)

        // Animación de escalado para el coche
        val scaleXAnimatorCar = ObjectAnimator.ofFloat(carImage, View.SCALE_X, 0f, 1.2f, 1f)
        val scaleYAnimatorCar = ObjectAnimator.ofFloat(carImage, View.SCALE_Y, 0f, 1.2f, 1f)

        // Añadir rotación a la imagen del coche
        val rotationAnimatorCar = ObjectAnimator.ofFloat(carImage, View.ROTATION, 0f, 360f)
        rotationAnimatorCar.duration = 500

        // Animación de aparición (Fade-in) para el coche
        val fadeInAnimatorCar = ObjectAnimator.ofFloat(carImage, View.ALPHA, 0f, 1f)

        // Crear animación de conjunto para la imagen del coche
        val carAnimatorSet = AnimatorSet().apply {
            playTogether(scaleXAnimatorCar, scaleYAnimatorCar, rotationAnimatorCar, fadeInAnimatorCar)
            duration = 500
        }

        // Animación de escalado para la colmena (hive)
        val scaleXAnimatorHive = ObjectAnimator.ofFloat(hiveImage, View.SCALE_X, 0f, 1.2f, 1f)
        val scaleYAnimatorHive = ObjectAnimator.ofFloat(hiveImage, View.SCALE_Y, 0f, 1.2f, 1f)

        // Añadir rotación a la imagen de la colmena
        val rotationAnimatorHive = ObjectAnimator.ofFloat(hiveImage, View.ROTATION, 0f, 360f)
        rotationAnimatorHive.duration = 500

        // Animación de aparición (Fade-in) para la colmena
        val fadeInAnimatorHive = ObjectAnimator.ofFloat(hiveImage, View.ALPHA, 0f, 1f)

        // Crear animación de conjunto para la imagen de la colmena
        val hiveAnimatorSet = AnimatorSet().apply {
            playTogether(scaleXAnimatorHive, scaleYAnimatorHive, rotationAnimatorHive, fadeInAnimatorHive)
            duration = 500
        }

        // Mostrar las imágenes y comenzar las animaciones
        carImage.visibility = View.VISIBLE
        hiveImage.visibility = View.VISIBLE

        // Ejecutar las animaciones en secuencia
        val animatorSet = AnimatorSet().apply {
            playSequentially(carAnimatorSet, hiveAnimatorSet)
        }
        animatorSet.start()

        // Transición al MainActivity después de la animación
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000) // Duración total del splash
    }
}
