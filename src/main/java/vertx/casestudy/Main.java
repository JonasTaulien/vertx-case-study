package vertx.casestudy;

import com.google.inject.Guice;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);



    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx(
            new VertxOptions()
                .setEventLoopPoolSize(4)
                .setWorkerPoolSize(50)
                .setMaxEventLoopExecuteTime(500)
                .setMaxEventLoopExecuteTimeUnit(TimeUnit.MILLISECONDS)
        );

        final var injector = Guice.createInjector(new Module(vertx));

        vertx
            .rxDeployVerticle(
                () -> injector.getInstance(HttpServerVerticle.class),
                new DeploymentOptions().setInstances(4)
            )
            .subscribe(
                deploymentId -> log.info("A Successfully started my first verticle: {}", deploymentId),
                error -> log.error("A Failed to start my first verticle", error)
            );
    }
}
