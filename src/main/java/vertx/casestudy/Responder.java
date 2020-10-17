package vertx.casestudy;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class Responder {

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
