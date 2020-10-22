package vertx.casestudy.auth;

import com.google.inject.Inject;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;

public class LoginHandler implements Handler<RoutingContext> {

    private final PgPool client;

    private final JWTAuth jwtAuth;

    private final ConfigRetriever configRetriever;



    @Inject
    public LoginHandler(PgPool client, JWTAuth jwtAuth, ConfigRetriever configRetriever) {
        this.client = client;
        this.jwtAuth = jwtAuth;
        this.configRetriever = configRetriever;
    }



    @Override
    public void handle(RoutingContext ctx) {
        final var body = ctx.getBodyAsJson();

        final var email = body.getString("email");
        final var password = body.getString("password");

        this.client
            .preparedQuery("SELECT id FROM \"user\" WHERE email = $1 AND password = $2")
            .rxExecute(Tuple.of(email, password))
            .filter(rowSet -> rowSet.iterator().hasNext())
            .map(rowSet -> rowSet.iterator().next().getInteger("id"))
            .flatMap(
                _userId -> this.configRetriever
                    .rxGetConfig()
                    .map(config -> config.getJsonObject("jwt"))
                    .map(_jwtConfig -> new Object() {
                        final JsonObject jwtConfig = _jwtConfig;

                        final int userId = _userId;
                    })
                    .toMaybe()
            )
            .map(
                data -> this.jwtAuth.generateToken(
                    new JsonObject(),
                    new JWTOptions()
                        .setExpiresInMinutes(data.jwtConfig.getInteger("expiresInMinutes"))
                        .setSubject(String.valueOf(data.userId))
                        .setAlgorithm(data.jwtConfig.getString("algorithm"))
                )
            )
            .subscribe(
                token -> ctx.response()
                            .setStatusCode(200)
                            .putHeader("Content-Type", "application/jwt")
                            .end(token),
                ctx::fail,
                () -> ctx.fail(401, new Exception("Invalid email or password"))
            );
    }
}
