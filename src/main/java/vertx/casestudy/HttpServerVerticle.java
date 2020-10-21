package vertx.casestudy;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {

    private final AsyncLogger asyncLogger;



    @Inject
    public HttpServerVerticle(AsyncLogger asyncLogger) {
        this.asyncLogger = asyncLogger;
    }



    @Override
    public Completable rxStart() {
        this.asyncLogger.info("Starting http server");

        final var router = Router.router(vertx);

        router.route()
              .handler(ctx -> {
                  this.asyncLogger.info(
                      "New Request {} {}",
                      ctx.request().method(),
                      ctx.request().path()
                  );

                  ctx.next();
              })
              .failureHandler(
                  ctx -> {
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
              );

        router.post()
              .handler(BodyHandler.create());

        router.get("/api/v1/headlines")
              .handler(ctx -> {
                  ctx.response()
                     .setStatusCode(200)
                     .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                     .end("[]");
              });

        router.post("/api/v1/headlines")
              .handler(ctx -> {
                  final var body = ctx.getBodyAsJson();
                  ctx.response()
                     .setStatusCode(201)
                     .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                     .end(body.put("id", 1).encode());
              });

        return this.vertx
            .createHttpServer()
            .requestHandler(router)
            .rxListen(8080)
            .ignoreElement();
    }
}
