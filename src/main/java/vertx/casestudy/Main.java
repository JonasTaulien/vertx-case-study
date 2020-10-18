package vertx.casestudy;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(HttpServerVerticle.class);



    public static void main(String[] args) {
        final var vertx = Vertx.vertx(new VertxOptions());

        vertx.deployVerticle(
            new HttpServerVerticle(vertx),
            new DeploymentOptions(),
            future -> {
                if (future.succeeded()) {
                    Main.log.info("Successfully started server");
                } else {
                    Main.log.info("Failed to start server");
                }
            }
        );
    }
}
