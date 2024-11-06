package com.example.carhive.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Módulo de Dagger Hilt para la inyección de dependencias de la aplicación.
 *
 * Proporciona instancias singleton de las clases necesarias para el funcionamiento
 * de la aplicación, incluyendo Firebase, repositorios y casos de uso.
 */
@Module
@InstallIn(SingletonComponent::class)
object PresentationModule {


}
