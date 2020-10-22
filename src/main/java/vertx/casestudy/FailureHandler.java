package vertx.casestudy;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import vertx.casestudy.log.AsyncLogger;

public class FailureHandler implements Handler<RoutingContext> {

    private final AsyncLogger asyncLogger;



    @Inject
    public FailureHandler(AsyncLogger asyncLogger) {
        this.asyncLogger = asyncLogger;
    }



    @Override
    public void handle(RoutingContext ctx) {
        this.asyncLogger.error("Error", ctx.failure());

        final var message = (ctx.failure() != null)
            ? ctx.failure().getMessage()
            : "Something went wrong";

        ctx.response()
           .setStatusCode(ctx.statusCode())
           .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
           .end(
               new JsonObject()
                   .put("error", message)
                   .encode()
           );
    }
}
