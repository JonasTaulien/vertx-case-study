package vertx.casestudy;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Responder {

    private final Logger log = LoggerFactory.getLogger(Responder.class);

    public void respondError(RoutingContext ctx, HttpResponseStatus statusCode, Throwable t){
        log.error("An error occurred:", t);

        this.respond(
            ctx,
            statusCode,
            new JsonObject().put("error", (t != null) ? t.toString(): "Unknown error")
        );
    }

    public void respond(RoutingContext ctx, HttpResponseStatus statusCode, JsonObject body) {
        ctx.response()
           .setStatusCode(statusCode.code())
           .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
           .end(body.encode());
    }



    public void respond(RoutingContext ctx, HttpResponseStatus statusCode, JsonArray body) {
        ctx.response()
           .setStatusCode(statusCode.code())
           .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
           .end(body.encode());
    }
}
