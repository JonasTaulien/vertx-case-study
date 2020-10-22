package vertx.casestudy.auth;

import com.google.inject.Inject;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;

public class LoginHandler implements Handler<RoutingContext> {

    private final PgPool client;

    private final JWTAuth jwtAuth;



    @Inject
    public LoginHandler(PgPool client, JWTAuth jwtAuth) {
        this.client = client;
        this.jwtAuth = jwtAuth;
    }



    @Override
    public void handle(RoutingContext ctx) {
        final var body = ctx.getBodyAsJson();

        final var email = body.getString("email");
        final var password = body.getString("password");

        this.client
            .preparedQuery("SELECT id FROM \"user\" WHERE email = $1 AND password = $2")
            .rxExecute(Tuple.of(email, password))
            .subscribe(
                rowSet -> {
                    final var loginSuccessful = rowSet.iterator().hasNext();
                    if (loginSuccessful) {
                        final var userId = rowSet.iterator().next().getInteger("id");

                        final var token = this.jwtAuth.generateToken(
                            new JsonObject(),
                            new JWTOptions()
                                .setExpiresInMinutes(60)
                                .setSubject(String.valueOf(userId))
                                .setAlgorithm("RS256")
                        );

                        ctx.response()
                           .setStatusCode(200)
                           .putHeader("Content-Type", "application/jwt")
                           .end(token);

                    } else {
                        ctx.fail(401, new Exception("Invalid email or password"));
                    }
                },
                ctx::fail
            );
    }
}
