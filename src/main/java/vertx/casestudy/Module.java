package vertx.casestudy;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

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
    PgPool providePgPool(Vertx vertx, JsonObject config) {
        final var dbConfig = config.getJsonObject("db");

        return PgPool.pool(
            vertx,
            new PgConnectOptions()
                .setHost(dbConfig.getString("host"))
                .setPort(dbConfig.getInteger("port"))
                .setUser(dbConfig.getString("user"))
                .setPassword(dbConfig.getString("password"))
                .setDatabase(dbConfig.getString("database")),
            new PoolOptions()
        );
    }



    @Provides
    @Singleton
    JsonObject provideConfig(ConfigRetriever configRetriever) {
        return configRetriever.rxGetConfig()
                              .blockingGet();
    }



    @Provides
    @Singleton
    JWTAuth provideJwtAuth(Vertx vertx, JsonObject config) {
        final var jwtConfig = config.getJsonObject("jwt");

        return JWTAuth.create(
            vertx,
            new JWTAuthOptions().addPubSecKey(
                new PubSecKeyOptions()
                    .setAlgorithm(jwtConfig.getString("algorithm"))
                    .setSecretKey(jwtConfig.getString("privateKey"))
                    .setPublicKey(jwtConfig.getString("publicKey"))
            )
        );
    }



    @Provides
    @Singleton
    JWTAuthHandler provideJwtAuthHandler(JWTAuth jwtAuth) {
        return JWTAuthHandler.create(jwtAuth);
    }



    @Provides
    @Singleton
    ConfigRetriever provideConfigRetriever(Vertx vertx) {
        return ConfigRetriever.create(
            vertx,
            new ConfigRetrieverOptions()
                .addStore(
                    new ConfigStoreOptions()
                        .setType("file")
                        .setConfig(new JsonObject().put("path", "config.json"))
                )
                .setScanPeriod(5000)
        );
    }
}
