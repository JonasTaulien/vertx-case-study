package vertx.casestudy;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final PgPool pgPool;

    private final Responder responder;

    private final HeadlineCreateHandler headlineCreateHandler;

    private final HeadlineGetAllHandler headlineGetAllHandler;

    private final HeadlineGetOneHandler headlineGetOneHandler;



    public HttpServerVerticle(Vertx vertx) {
        this.pgPool = PgPool.pool(
            vertx,
            new PgConnectOptions()
                .setPort(5432)
                .setHost("localhost")
                .setDatabase("case-study")
                .setUser("example")
                .setPassword("example"),
            new PoolOptions()
        );

        this.responder = new Responder();

        this.headlineCreateHandler = new HeadlineCreateHandler(this.pgPool, responder);
        this.headlineGetAllHandler = new HeadlineGetAllHandler(this.pgPool, responder);
        this.headlineGetOneHandler = new HeadlineGetOneHandler(this.pgPool, responder);
    }



    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        final var router = Router.router(this.vertx);
        router.route()
              .handler(BodyHandler.create())
              .handler(ctx -> {
                  log.info("New Request {}", ctx.request().path());
                  ctx.next();
              });

        router.post("/headline")
              .handler(this.headlineCreateHandler);

        router.get("/headlines")
              .handler(this.headlineGetAllHandler);

        router.get("/headline/:id")
              .handler(this.headlineGetOneHandler);

        router.route()
              .failureHandler(
                  ctx -> this.responder
                             .respondError(
                                 ctx,
                                 HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                 ctx.failure()
                             )
              );

        this.vertx.createHttpServer(new HttpServerOptions())
                  .requestHandler(router)
                  .listen(
                      8080,
                      ar -> {
                          try {
                              if (ar.succeeded()) {
                                  startPromise.complete();
                              } else {
                                  startPromise.fail(ar.cause());
                              }
                          } catch (Throwable t) {
                              startPromise.fail(t);
                          }
                      }
                  );
    }



    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        this.pgPool.close();
        stopPromise.complete();
    }
}
