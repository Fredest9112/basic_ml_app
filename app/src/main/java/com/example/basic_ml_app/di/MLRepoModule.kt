package com.example.basic_ml_app.di

import android.content.Context
import com.example.basic_ml_app.data.repoimpl.MLRepo
import com.example.basic_ml_app.domain.repo.IMLRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MLRepoModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideIMLRepo(context: Context): IMLRepo {
        return MLRepo(context = context)
    }
}