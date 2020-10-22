package vertx.casestudy.http.headline;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;

public class HeadlineGetAllHandler implements Handler<RoutingContext> {

    private final PgPool client;



    @Inject
    public HeadlineGetAllHandler(PgPool client) {
        this.client = client;
    }



    @Override
    public void handle(RoutingContext ctx) {
        ctx.vertx()
           .eventBus()
            .<JsonArray>rxRequest("headline.getAll", new JsonObject())
            .map(Message::body)
            .subscribe(
                headlines -> ctx.response()
                                .setStatusCode(200)
                                .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                                .end(headlines.encode()),
                ctx::fail
            );
    }
}
