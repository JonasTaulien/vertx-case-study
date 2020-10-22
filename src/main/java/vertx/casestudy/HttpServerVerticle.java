package vertx.casestudy;

import com.google.inject.Inject;
import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import vertx.casestudy.auth.LoginHandler;
import vertx.casestudy.headline.HeadlineCreateHandler;
import vertx.casestudy.headline.HeadlineGetAllHandler;
import vertx.casestudy.headline.HeadlineGetOneHandler;
import vertx.casestudy.log.AsyncLogger;
import vertx.casestudy.log.RequestLoggingHandler;

public class HttpServerVerticle extends AbstractVerticle {

    private final AsyncLogger asyncLogger;

    private final HeadlineGetAllHandler headlineGetAllHandler;

    private final HeadlineGetOneHandler headlineGetOneHandler;

    private final HeadlineCreateHandler headlineCreateHandler;

    private final RequestLoggingHandler requestLoggingHandler;

    private final FailureHandler failureHandler;

    private final LoginHandler loginHandler;

    private final JWTAuthHandler jwtAuthHandler;



    @Inject
    public HttpServerVerticle(
        AsyncLogger asyncLogger,
        HeadlineGetAllHandler headlineGetAllHandler,
        HeadlineGetOneHandler headlineGetOneHandler,
        HeadlineCreateHandler headlineCreateHandler,
        RequestLoggingHandler requestLoggingHandler,
        FailureHandler failureHandler,
        LoginHandler loginHandler,
        JWTAuthHandler jwtAuthHandler
    ) {
        this.asyncLogger = asyncLogger;
        this.headlineGetAllHandler = headlineGetAllHandler;
        this.headlineGetOneHandler = headlineGetOneHandler;
        this.headlineCreateHandler = headlineCreateHandler;
        this.requestLoggingHandler = requestLoggingHandler;
        this.failureHandler = failureHandler;
        this.loginHandler = loginHandler;
        this.jwtAuthHandler = jwtAuthHandler;
    }



    @Override
    public Completable rxStart() {
        this.asyncLogger.info("Starting http server");

        final var router = Router.router(vertx);
        router.route()
              .handler(this.requestLoggingHandler)
              .failureHandler(this.failureHandler);

        router.post()
              .handler(BodyHandler.create());

        final var v1 = Router.router(vertx);
        v1.post("/login").handler(this.loginHandler);
        v1.get("/headlines").handler(this.headlineGetAllHandler);
        v1.get("/headlines/:id").handler(this.headlineGetOneHandler);
        v1.post("/headlines")
          .handler(this.jwtAuthHandler)
          .handler(this.headlineCreateHandler);

        router.mountSubRouter("/api/v1", v1);

        return this.vertx
            .createHttpServer()
            .requestHandler(router)
            .rxListen(8080)
            .ignoreElement();
    }
}
