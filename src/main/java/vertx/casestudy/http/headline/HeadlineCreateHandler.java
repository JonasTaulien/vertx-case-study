package vertx.casestudy.http.headline;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vertx.casestudy.EventBusAddress;

import java.time.OffsetDateTime;

public class HeadlineCreateHandler implements Handler<RoutingContext> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());



    @Override
    public void handle(RoutingContext ctx) {
        final var body = ctx.getBodyAsJson();

        final var userId = ctx.user().principal().getString("sub");

        log.info("User with id {} creates new headline", userId);

        ctx.vertx()
           .eventBus()
            .<JsonObject>rxRequest(EventBusAddress.HEADLINE_CREATE, body)
            .subscribe(
                message -> ctx.response()
                              .setStatusCode(201)
                              .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                              .end(message.body().encode()),
                ctx::fail
            );
    }
}
