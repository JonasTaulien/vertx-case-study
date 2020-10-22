package vertx.casestudy;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import jdk.jfr.Name;

public class Module extends AbstractModule {

    private final Vertx vertx;



    public Module(Vertx vertx) {
        this.vertx = vertx;
    }



    @Provides
    @Singleton
    Vertx provideVertx() {
        return this.vertx;
    }



    @Provides
    @Singleton
    @Named("config")
    JsonObject provideConfig(ConfigRetriever configRetriever) {
        return configRetriever.rxGetConfig().blockingGet();
    }



    @Provides
    @Singleton
    @Named("dbConfig")
    JsonObject provideDbConfig(@Named("config") JsonObject config) {
        return config.getJsonObject("db");
    }



    @Provides
    @Singleton
    PgPool providePgClient(@Named("dbConfig") JsonObject dbConfig) {
        return PgPool.pool(
            this.vertx,
            new PgConnectOptions()
                .setHost(dbConfig.getString("host"))
                .setPort(dbConfig.getInteger("port"))
                .setDatabase(dbConfig.getString("database"))
                .setPassword(dbConfig.getString("password"))
                .setUser(dbConfig.getString("user")),
            new PoolOptions().setMaxSize(dbConfig.getInteger("poolSize"))
        );
    }



    @Provides
    @Singleton
    JWTAuth provideJWTAuth(@Named("config") JsonObject config) {
        final var jwtConfig = config.getJsonObject("jwt");

        return JWTAuth.create(
            this.vertx,
            new JWTAuthOptions()
                .addPubSecKey(
                    new PubSecKeyOptions()
                        .setAlgorithm(jwtConfig.getString("algorithm"))
                        .setSecretKey(jwtConfig.getString("privateKey"))
                        .setPublicKey(jwtConfig.getString("publicKey"))
                )
        );
    }



    @Provides
    @Singleton
    JWTAuthHandler provideJWTAuthHandler(JWTAuth jwtAuth) {
        return JWTAuthHandler.create(jwtAuth);
    }



    @Provides
    @Singleton
    ConfigRetriever provideConfigRetriever() {
        return ConfigRetriever.create(
            this.vertx,
            new ConfigRetrieverOptions()
                .addStore(
                    new ConfigStoreOptions()
                        .setType("file")
                        .setConfig(new JsonObject().put("path", "config.json"))
                )
        );
    }
}
