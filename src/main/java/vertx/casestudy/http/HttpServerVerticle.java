package vertx.casestudy.http;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.reactivex.Completable;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import vertx.casestudy.http.auth.LoginHandler;
import vertx.casestudy.http.headline.HeadlineCreateHandler;
import vertx.casestudy.http.headline.HeadlineGetAllHandler;
import vertx.casestudy.AsyncLogger;

public class HttpServerVerticle extends AbstractVerticle {

    private final AsyncLogger asyncLogger;

    private final HeadlineGetAllHandler headlineGetAllHandler;

    private final HeadlineCreateHandler headlineCreateHandler;

    private final RequestLoggingHandler requestLoggingHandler;

    private final FailureHandler failureHandler;

    private final LoginHandler loginHandler;

    private final JWTAuthHandler jwtAuthHandler;

    private final JsonObject config;


    @Inject
    public HttpServerVerticle(
        AsyncLogger asyncLogger,
        HeadlineGetAllHandler headlineGetAllHandler,
        HeadlineCreateHandler headlineCreateHandler,
        RequestLoggingHandler requestLoggingHandler,
        FailureHandler failureHandler,
        LoginHandler loginHandler,
        JWTAuthHandler jwtAuthHandler,
        @Named("config") JsonObject config
    ) {
        this.asyncLogger = asyncLogger;
        this.headlineGetAllHandler = headlineGetAllHandler;
        this.headlineCreateHandler = headlineCreateHandler;
        this.requestLoggingHandler = requestLoggingHandler;
        this.failureHandler = failureHandler;
        this.loginHandler = loginHandler;
        this.jwtAuthHandler = jwtAuthHandler;
        this.config = config;
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
        v1.post("/headlines")
          .handler(this.jwtAuthHandler)
          .handler(this.headlineCreateHandler);

        router.mountSubRouter("/api/v1", v1);

        return this.vertx
            .createHttpServer()
            .requestHandler(router)
            .rxListen(this.config.getInteger("port"))
            .ignoreElement();
    }
}
