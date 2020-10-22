package vertx.casestudy.headline;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;

public class HeadlineCreateHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        final var body = ctx.getBodyAsJson();
        ctx.response()
           .setStatusCode(201)
           .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
           .end(body.put("id", 1).encode());
    }
}
