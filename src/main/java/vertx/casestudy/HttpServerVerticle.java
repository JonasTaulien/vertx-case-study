package vertx.casestudy;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Responder responder;

    private final HeadlineCreateHandler headlineCreateHandler;

    private final HeadlineGetAllHandler headlineGetAllHandler;

    private final HeadlineGetOneHandler headlineGetOneHandler;

    private final LoginHandler loginHandler;

    private final JWTAuthHandler jwtAuthHandler;

    private final JsonObject config;



    @Inject
    public HttpServerVerticle(
        Responder responder,
        HeadlineCreateHandler headlineCreateHandler,
        HeadlineGetAllHandler headlineGetAllHandler,
        HeadlineGetOneHandler headlineGetOneHandler,
        LoginHandler loginHandler,
        JWTAuthHandler jwtAuthHandler,
        ConfigRetriever configRetriever,
        JsonObject config
    ) {
        this.responder = responder;
        this.headlineCreateHandler = headlineCreateHandler;
        this.headlineGetAllHandler = headlineGetAllHandler;
        this.headlineGetOneHandler = headlineGetOneHandler;
        this.loginHandler = loginHandler;
        this.jwtAuthHandler = jwtAuthHandler;
        this.config = config;
    }



    @Override
    public Completable rxStart() {
        final var router = Router.router(this.vertx);

        router.route()
              .handler(BodyHandler.create())
              .handler(ctx -> {
                  log.info("New Request {}", ctx.request().path());
                  ctx.next();
              });

        router.post("/headline")
              .handler(this.jwtAuthHandler)
              .handler(this.headlineCreateHandler);

        router.get("/headlines")
              .handler(this.headlineGetAllHandler);

        router.get("/headline/:id")
              .handler(this.headlineGetOneHandler);

        router.post("/login")
              .handler(this.loginHandler);

        router.route()
              .failureHandler(
                  ctx -> this.responder
                             .respondError(
                                 ctx,
                                 HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                 ctx.failure()
                             )
              );

        return this.vertx
                   .createHttpServer(new HttpServerOptions())
                   .requestHandler(router)
                   .rxListen(config.getInteger("port"))
                   .ignoreElement();
    }
}
