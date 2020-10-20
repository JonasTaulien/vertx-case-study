package vertx.casestudy.headline;

import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;

public class HeadlineCreateHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        final var body = ctx.getBodyAsJson();

        ctx.response().end();
    }
}
