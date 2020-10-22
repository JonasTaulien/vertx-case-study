package vertx.casestudy;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;
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
    PgPool providePgClient() {
        return PgPool.pool(
            vertx,
            new PgConnectOptions()
                .setHost("localhost")
                .setPort(5432)
                .setDatabase("case-study")
                .setPassword("example")
                .setUser("example"),
            new PoolOptions().setMaxSize(10)
        );
    }
}
