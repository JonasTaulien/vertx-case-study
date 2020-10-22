package vertx.casestudy.http.headline;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.web.RoutingContext;

public class HeadlineCreateHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        final var body = ctx.getBodyAsJson();

        ctx.vertx()
           .eventBus()
            .<JsonObject>rxRequest("headline.create", body)
            .map(Message::body)
            .subscribe(
                createdHeadline -> ctx.response()
                                      .setStatusCode(201)
                                      .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                                      .end(createdHeadline.encode()),
                ctx::fail
            );
    }
}
