package vertx.casestudy;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.LoggerFactory;

public class Main {

    public static void main(String[] args) {
        final var log = LoggerFactory.getLogger(Main.class);

        final var vertx = Vertx.vertx(new VertxOptions());

        vertx.deployVerticle(
            new HttpServerVerticle(vertx),
            new DeploymentOptions(),
            future -> {
                if (future.succeeded()) {
                    log.info("Successfully started server");
                } else {
                    log.info("Failed to start server");
                }
            }
        );
    }
}
