package vertx.casestudy;

import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {
        final var vertx = Vertx.vertx();

        vertx.deployVerticle(
            new HttpServerVerticle(vertx),
            future -> {
                if (future.succeeded()) {
                    System.out.println("Successfully started server");
                } else {
                    System.out.println("Failed to start server");
                }
            }
        );
    }
}
