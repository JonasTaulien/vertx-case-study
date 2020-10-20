package vertx.casestudy;

import com.google.inject.Guice;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);



    public static void main(String[] args) {
        final var vertx = Vertx.vertx(
            new VertxOptions().setEventLoopPoolSize(4)
                              .setWorkerPoolSize(10)
        );

        final var injector = Guice.createInjector(new Module(vertx));

        vertx
            .rxDeployVerticle(
                () -> injector.getInstance(HttpServerVerticle.class),
                new DeploymentOptions().setInstances(3)
            )
            .subscribe(
                deyploymentId -> log.info("Successfully deployed server(s) {}", deyploymentId),
                error -> log.error("Failed to start server ", error)
            );
    }
}
