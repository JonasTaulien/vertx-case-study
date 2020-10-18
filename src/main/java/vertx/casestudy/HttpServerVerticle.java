package vertx.casestudy;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
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

    private final LoginHandler loginHandler;

    private final JWTAuth jwtAuth;



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

        this.jwtAuth = JWTAuth.create(
            vertx,
            new JWTAuthOptions().addPubSecKey(
                new PubSecKeyOptions()
                    .setAlgorithm("RS256")
                    .setSecretKey("MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDeDxXIJe6HYXQq5q/dXIMz4IJCjsem4RO73MJlxlSNDRM6oAbOe6u6GN84+/LAAENriYQyspGa6Hk1SfzUTI7FkmY/yyApY8YpEUAGsM/7UoZglUcisx8MAsoc6WEm9TAXFmXEuYzVSR8PoSUsZXELJngaeKmqw4YG4HqFF0U9duXK/en7iMe6qyV1uUXuZDLIQFg+2VNtbBzgvOZ+CEpg0YIYrtYuD4wPpR6rLIVKiePwdqMhE72v6VQ96xBBWXHDy0XDk3xWEutGDS0cXxpXVhOMNMq8/5X9IdsFSRkYnoL/yN66WPVPc6Yg1gi9dn3r4beohm0dvH3SfJk/8qu5AgMBAAECggEBAL4IYNahB4lZ8AVK2BPzOODrb+4vrFJ26eFqYf6hBPiUyPDxxAFuup6O7HEixDJlx0y/KaANSygrbJcbE0BHqCMST2iml4Z0OUAp4AYZyFNG43LCLVdXvOkVdgvZ+WdSiu4nH76EIC7i3a8Nc/eTdHNbX4sf2esmWouGDbPaH0oh3rOccTb22imAiH2JCPU5LjSWe3LZRwlEtyG0LqUJCAx0f4w9bW9XPM7IFnHllOiFrZyhw/IMdvHrJI0QV2qzSBC0ofqwEEag59x2ahGreopT35XGdihQj2qzWJAT8L057SGk0NOxIyuWQpBYh+mn4Owfoe10KIopypLT+i6n5AUCgYEA+WwM6RAEuRTSUNcZxzVp9gMa24UrRwV1t8ohdI9Iq+SPTBmAQ5iE89Vb2Fw1ZOG4J6qFGtAFOijvg3sxUTro84aGePme1yNqIRdPIffWnGgFo15jlkl7hQgAo0e2umpX+2fPCxLxRedAFMPWLIzKfk+fL5uNED/fOrkWQJ0n3+8CgYEA4+pLgmGMiG1V1qs0eAskdiygBWO2nKtRP9qQeGweWa/gRmXzpqRBF4EOAhU7wTdg5VRY6nDHMYiLrlo9djd0VBA7cbUXU5hcMfpG1/9ByETCgDq3XZU9XQTpCo35tNbqLwH+6ynRSRhTRawGMvOIBeQhRjZBSR6BV+kXVtkqBtcCgYEAsyXIoB3BRq6N4UKeYVccJHdWcxXTX8+sHbYxEWdY6x3fTumCFwsVc41ryptwPmVhwBvaPsfq+TstzWJOqemGKGQXcgzY6e2l7N6xF9TLcMvGQPEq1mcxMPvueVipBnwbdZe6Pln8shjHDiMbUiRNzSujSq6PQ3yc+bC3KvG2EkECgYEAqofH78olaH7GV1TKcXeE1JcXNHEjVZ4psOlMZ44eN2UNHh9yayFDQt7hKFXS3AMfkPfHf8LaiAlu2gnTKvzbIXrzv2SMC5RYh1yIXV00TzCDh4ZnZLIhs9PUunz4dYLIvltz7WjgmxyEUgEgskbdY+sJqos11gvpI2nYeLcMZwkCgYEA3EDFWCPV1oz5cZAze7eE8Qc8Z4tFIhaoKKmCI2AbRqXrQPFwPHt7CUP8o8/stYKYSuHe0bPGUnFgA0sxLxZXHOpHhEDAEGYrXXFVmuX9c8Dyx7o/0O39b95CgKYUkTAIbDXEZISw+FV1zc2U1guCNi39N4u45rFgnUWoDKucH9Y=")
                    .setPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3g8VyCXuh2F0Kuav3VyDM+CCQo7HpuETu9zCZcZUjQ0TOqAGznuruhjfOPvywABDa4mEMrKRmuh5NUn81EyOxZJmP8sgKWPGKRFABrDP+1KGYJVHIrMfDALKHOlhJvUwFxZlxLmM1UkfD6ElLGVxCyZ4GnipqsOGBuB6hRdFPXblyv3p+4jHuqsldblF7mQyyEBYPtlTbWwc4LzmfghKYNGCGK7WLg+MD6UeqyyFSonj8HajIRO9r+lUPesQQVlxw8tFw5N8VhLrRg0tHF8aV1YTjDTKvP+V/SHbBUkZGJ6C/8jeulj1T3OmINYIvXZ96+G3qIZtHbx90nyZP/KruQIDAQAB")
            )
        );

        this.responder = new Responder();

        this.headlineCreateHandler = new HeadlineCreateHandler(this.pgPool, responder);
        this.headlineGetAllHandler = new HeadlineGetAllHandler(this.pgPool, responder);
        this.headlineGetOneHandler = new HeadlineGetOneHandler(this.pgPool, responder);
        this.loginHandler = new LoginHandler(this.pgPool, responder, this.jwtAuth);
    }



    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        final var router = Router.router(this.vertx);

        final var authHandler = JWTAuthHandler.create(jwtAuth);
        router.route()
              .handler(BodyHandler.create())
              .handler(ctx -> {
                  log.info("New Request {}", ctx.request().path());
                  ctx.next();
              });

        router.post("/headline")
              .handler(authHandler)
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
