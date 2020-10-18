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
                    .setSecretKey(
                        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCnorUKvcdL4jQdxF7caxWpRSC5n6mlX3TXNvs9Mcx05v4y7TaCK5MYZEW3+4doiQ62aaykhyg79lzYGJDEQFK37g9DlzlOFLLyyPFrhP8B0Qz34RR1ASsW8WvP9o9DtP/VLLHDPd7U33tTjMxIiCYWYLMkEbzajR+iMJAs18JqJyvVLnyPRDPjHgpSuk+QGKdoVWyfcEHchIDwDz046AwS2o4+tO8fGKl4LByNi6ANvESLU2HFJvULySJ9FPwxeKv/9y8t8p3XfCAg1u9qH2kuIEzEDrGGmgYxHg1p8G0eOy7HkYfaPnfl8u+1PBl7orKRb2wJWgeaKqwXdhlIJI9LAgMBAAECggEAeNDyTACE33Ly/rUlbRSccveIY1/oA3Dto6JTpkH6yhIlCI0gKObAx+aUvMnCSJvnB57Xt2hVngmr1m61/0sgksE3LQ6TVWvjoMkj/crHgwaoowoMFVu3M3zhp+taS4DqlTf2EfHIZIGAv7GNFYVSdfRRh+BkzNrC/sCjWD5UNu5DNKqF6JqpT/M/vluNUdae52LmAir5Ij4D51/gTtEIMEWgd9+6Oo2/7AfJyWs8GQmnQ4v2C1JuMvX2kajBhfgzZXCST/M9fIiHX3Sp/Nqv2FH4vDKvDlVF6KwArAlKx5f+T9XwcqqsIGL+8ey6tVX4BRtkq+h0yZmwXKTNVqG3MQKBgQDbgn8WHF6e1rkaCJ9d49tZdoc/KxpGAik5+YZZ8aUqpYO6qCx5jNiiHmiWmGWUw+CEVPqxG9gHuYptDAFspYVNOEUjsyUyAzu3QO+yljxWPPOP4u+TPGlP1p49qKfJktB243CyLKuPLV+Ywv0yfXHbKmLpNr/LOREcaM1InxlBmQKBgQDDgKRtV6BBmCOTaPyI1TLCN+38K6pZVFkFjvolA1eXEyF1MQ9UFz6k4H/mUU3A0SslWOcbTBEHTALafpTFZpOFpJn/d15dQZD3e/Fw4iYMwKKAgBYLQUBCxKeY8sjEaAPakwG02J5NitoujqwfGu85x4muKKuhTb4fJXEi2fKugwKBgGaivq9k9m0IcyYUEAgDiUVsYVKM4c0IJ6Se4k3d6d9l4pD9H0Z6L7SlyTtY6G5FpA3b+hejsD/0ZiAUs/nL7ucrP1Cw69xVnK9Ton/7NR0RhMSrVF+gqJEXBYhI+qMKjYR5VfFJZ7Ibgg5YZbEc7IaQbA0ld0obVb/V1N523YVJAoGAMssCbVId1cz0OUFGkzpo9KLZVvH1UT4j3tjVzrX+lMaoo6z6YYukf+u/7xPikp0Fi698Y9AwgGfUOfY3Ks5Kt6cbR4842hvF0TbHdN+cqSWHUsF0eLocOvyi2o8BcF3h2nDlOZoqDtVreDsjJvW1f5qC/9ebN85d/fOZYEel0IcCgYEAnlZMYP1+LVsYH4g92MUP/DG8tkQuoelDAX+VuL81ANwXr4O183aN14JOiehsTwHtQJ/gREO0z2as4zSd3kVPk+ao+B4UjB4V3Xr6nbAMhngWoo/CcsqpQSiuLdVFMG5UVJdyj2zakIzSeHup/bgwCo7l7p5ZF5zdZH5Uhn34ewY="
                    )
                    .setPublicKey(
                        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp6K1Cr3HS+I0HcRe3GsVqUUguZ+ppV901zb7PTHMdOb"
                            + "+Mu02giuTGGRFt/uHaIkOtmmspIcoO/Zc2BiQxEBSt+4PQ5c5ThSy8sjxa4T/AdEM9+EUdQErFvFrz/aPQ7T"
                            +
                            "/1Syxwz3e1N97U4zMSIgmFmCzJBG82o0fojCQLNfCaicr1S58j0Qz4x4KUrpPkBinaFVsn3BB3ISA8A89OOgMEtqOPrTvHxipeCwcjYugDbxEi1NhxSb1C8kifRT8MXir//cvLfKd13wgINbvah9pLiBMxA6xhpoGMR4NafBtHjsux5GH2j535fLvtTwZe6KykW9sCVoHmiqsF3YZSCSPSwIDAQAB"
                    )
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
