package vertx.casestudy.headline;

import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;

public class HeadlineGetOneHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        final var id = Integer.parseInt(ctx.pathParam("id"));

        ctx.response()
           .setStatusCode(200)
           .end("Requested headline id: " + id);
    }
}
