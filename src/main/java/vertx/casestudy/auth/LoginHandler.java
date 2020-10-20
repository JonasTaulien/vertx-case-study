package vertx.casestudy.auth;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;

public class LoginHandler implements Handler<RoutingContext> {

    private final PgPool pgPool;

    private final JWTAuth jwtAuth;



    @Inject
    public LoginHandler(PgPool pgPool, JWTAuth jwtAuth) {
        this.pgPool = pgPool;
        this.jwtAuth = jwtAuth;
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
                            final var jwt = createToken(userId);

                            ctx.response()
                               .putHeader(HttpHeaderNames.CONTENT_TYPE, "application/jwt")
                               .setStatusCode(200)
                               .end(jwt);
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



    private String createToken(int userId) {
        return this.jwtAuth.generateToken(
            new JsonObject(),
            new JWTOptions()
                .setExpiresInMinutes(60)
                .setSubject(String.valueOf(userId))
                .setAlgorithm("RS256")
        );
    }
}
