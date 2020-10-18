package vertx.casestudy;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Responder {

    private final Logger log = LoggerFactory.getLogger(this.getClass());



    public void respondError(RoutingContext ctx, HttpResponseStatus statusCode, Throwable t) {
        log.error("An error occurred:", t);

        this.respond(
            ctx,
            statusCode,
            new JsonObject().put("error", (t != null) ? t.getMessage() : "Unknown error")
        );
    }



    public void respondError(RoutingContext ctx, HttpResponseStatus statusCode, String errorMessage) {
        log.error("An error occurred: {}", errorMessage);

        this.respond(
            ctx,
            statusCode,
            new JsonObject().put("error", errorMessage)
        );
    }



    public void respond(RoutingContext ctx, HttpResponseStatus statusCode, JsonObject body) {
        this.respond(ctx, statusCode, HttpHeaderValues.APPLICATION_JSON, body.encode());
    }



    public void respond(RoutingContext ctx, HttpResponseStatus statusCode, JsonArray body) {
        this.respond(ctx, statusCode, HttpHeaderValues.APPLICATION_JSON, body.encode());
    }



    public void respond(RoutingContext ctx, HttpResponseStatus statusCode, CharSequence contentType, String body) {
        ctx.response()
           .setStatusCode(statusCode.code())
           .putHeader(HttpHeaderNames.CONTENT_TYPE, contentType)
           .end(body);
    }
}
