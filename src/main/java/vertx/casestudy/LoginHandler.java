package vertx.casestudy;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;

public class LoginHandler implements Handler<RoutingContext> {

    private static final String SELECT_USER_QUERY = "SELECT id FROM \"user\" WHERE email = $1 AND password = $2";

    private final PgPool pgPool;

    private final Responder responder;

    private final JWTAuth jwtAuth;

    private final JsonObject jwtConfig;



    @Inject
    public LoginHandler(PgPool pgPool, Responder responder, JWTAuth jwtAuth, JsonObject config) {
        this.pgPool = pgPool;
        this.responder = responder;
        this.jwtAuth = jwtAuth;
        this.jwtConfig = config.getJsonObject("jwt");
    }



    @Override
    public void handle(RoutingContext ctx) {
        final var body = ctx.getBodyAsJson();
        final var email = body.getString("email");

        this.pgPool
            .preparedQuery(SELECT_USER_QUERY)
            .execute(
                Tuple.of(email, body.getString("password")),
                ar -> {
                    try {
                        if (ar.succeeded()) {
                            final var rowIterator = ar.result().iterator();
                            final var loginSuccessful = rowIterator.hasNext();

                            if (loginSuccessful) {
                                final var userId = rowIterator.next().getInteger("id");
                                final var token = this.jwtAuth.generateToken(
                                    new JsonObject(),
                                    new JWTOptions()
                                        .setExpiresInMinutes(this.jwtConfig.getInteger("expiresInMinutes"))
                                        .setSubject(String.valueOf(userId))
                                        .setAlgorithm(this.jwtConfig.getString("algorithm"))
                                );

                                this.responder
                                    .respond(
                                        ctx,
                                        HttpResponseStatus.OK,
                                        "application/jwt",
                                        token
                                    );

                            } else {
                                this.responder
                                    .respondError(
                                        ctx,
                                        HttpResponseStatus.UNAUTHORIZED,
                                        "Invalid email or password"
                                    );
                            }
                        } else {
                            ctx.fail(ar.cause());
                        }
                    } catch (Throwable t) {
                        ctx.fail(t);
                    }
                }
            );
    }
}
