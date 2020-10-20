package vertx.casestudy.auth;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;

public class LoginHandler implements Handler<RoutingContext> {

    private final PgPool pgPool;

    private final JWTAuth jwtAuth;

    private final ConfigRetriever configRetriever;



    @Inject
    public LoginHandler(PgPool pgPool, JWTAuth jwtAuth, ConfigRetriever configRetriever) {
        this.pgPool = pgPool;
        this.jwtAuth = jwtAuth;
        this.configRetriever = configRetriever;
    }



    @Override
    public void handle(RoutingContext ctx) {
        final var body = ctx.getBodyAsJson();

        this.pgPool
            .preparedQuery("SELECT id FROM \"user\" WHERE email = $1 AND password = $2")
            .rxExecute(Tuple.of(body.getString("email"), body.getString("password")))
            .subscribe(
                rowSet -> {
                    try {
                        final var rowIterator = rowSet.iterator();
                        final var loginSuccessful = rowIterator.hasNext();

                        if (loginSuccessful) {
                            final var userId = rowIterator.next().getInteger("id");

                            createToken(userId)
                                .subscribe(
                                    jwt -> ctx.response()
                                              .putHeader(HttpHeaderNames.CONTENT_TYPE, "application/jwt")
                                              .setStatusCode(200)
                                              .end(jwt),
                                    ctx::fail
                                );
                        } else {
                            ctx.response()
                               .setStatusCode(401)
                               .end();
                        }
                    } catch (Throwable t) {
                        ctx.fail(t);
                    }
                },
                ctx::fail
            );
    }



    private Single<String> createToken(int userId) {
        return this.configRetriever
            .rxGetConfig()
            .map(config -> {
                final var jwtConfig = config.getJsonObject("jwt");
                return this.jwtAuth.generateToken(
                    new JsonObject(),
                    new JWTOptions()
                        .setExpiresInMinutes(jwtConfig.getInteger("expiresInMinutes"))
                        .setSubject(String.valueOf(userId))
                        .setAlgorithm(jwtConfig.getString("algorithm"))
                );
            });
    }
}
