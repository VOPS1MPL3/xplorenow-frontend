package com.xplorenow.di;

import com.xplorenow.util.TokenManager;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.xplorenow.network.ApiService;


@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    // En emulador 10.0.2.2 apunta a localhost de tu PC
    // Si probás en dispositivo físico, cambiá por la IP de tu máquina
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(TokenManager tokenManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token = tokenManager.getToken();
                    if (token != null) {
                        original = original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();
                    }
                    return chain.proceed(original);
                })
                .addInterceptor(logging)
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    @Provides
    @Singleton
    public ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}