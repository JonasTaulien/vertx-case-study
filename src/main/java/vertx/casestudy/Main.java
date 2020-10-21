package vertx.casestudy;

import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);



    public static void main(String[] args) {
        final var vertx = Vertx.vertx();

        vertx.deployVerticle(
            new MyFirstVerticle(),
            ar -> {
                if (ar.succeeded()) {
                    log.info("Successfully started my first verticle!");

                } else {
                    log.error("Failed to start my first verticle", ar.cause());
                }
            }
        );
    }
}
