package com.xplorenow.di;
import com.xplorenow.data.api.XploreNowApi;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;

@Module
@InstallIn(SingletonComponent.class)
public class ApiModule {

    @Provides
    @Singleton
    public XploreNowApi provideXploreNowApi(Retrofit retrofit) {
        return retrofit.create(XploreNowApi.class);
    }
}