package vertx.casestudy;

import com.google.inject.Guice;
import io.vertx.core.DeploymentOptions;
import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);



    public static void main(String[] args) {
        final var vertx = Vertx.vertx();

        final var injector = Guice.createInjector(new GuiceModule(vertx));

        vertx
            .rxDeployVerticle(
                () -> injector.getInstance(HttpServerVerticle.class),
                new DeploymentOptions().setInstances(3)
            )
            .subscribe(
                id -> log.info("Successfully started server"),
                err -> log.info("Failed to start server")
            );

        vertx
            .rxDeployVerticle(
                () -> injector.getInstance(HeadlineDataStoreVerticle.class),
                new DeploymentOptions().setInstances(2)
            )
            .subscribe(
                id -> log.info("Successfully started data stores"),
                err -> log.info("Failed to start data stores")
            );

        vertx
            .rxDeployVerticle(
                () -> injector.getInstance(AuthVerticle.class),
                new DeploymentOptions().setInstances(1)
            )
            .subscribe(
                id -> log.info("Successfully started auth"),
                err -> log.info("Failed to start auth")
            );

        vertx
            .rxDeployVerticle(
                () -> injector.getInstance(HeadlineFetcherVerticle.class),
                new DeploymentOptions().setInstances(1)
            )
            .subscribe(
                id -> log.info("Successfully started fetcher"),
                err -> log.info("Failed to start fetcher")
            );
    }
}
