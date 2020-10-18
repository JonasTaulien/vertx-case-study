package vertx.casestudy;

import com.google.inject.Inject;
import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;

public class AuthVerticle extends AbstractVerticle {

    private final PgPool pgPool;

    private final MessageResponder responder;

    private final JWTAuth jwtAuth;

    private final JsonObject jwtConfig;



    @Inject
    public AuthVerticle(
        PgPool pgPool,
        MessageResponder responder,
        JWTAuth jwtAuth,
        JsonObject config
    ) {
        this.pgPool = pgPool;
        this.responder = responder;
        this.jwtAuth = jwtAuth;
        this.jwtConfig = config.getJsonObject("jwt");
    }



    @Override
    public Completable rxStart() {
        return vertx.eventBus()
                    .consumer("user.login", this::login)
                    .rxCompletionHandler();
    }



    private static final String SELECT_USER_QUERY = "SELECT id FROM \"user\" WHERE email = $1 AND password = $2";



    private void login(Message<JsonObject> message) {
        final var body = message.body();
        final var email = body.getString("email");

        this.pgPool
            .preparedQuery(SELECT_USER_QUERY)
            .rxExecute(Tuple.of(email, body.getString("password")))
            .subscribe(
                rowSet -> {
                    try {
                        final var rowIterator = rowSet.iterator();
                        final var loginSuccessful = rowIterator.hasNext();

                        if (loginSuccessful) {
                            final var userId = rowIterator.next().getInteger("id");
                            final var token = createJwtToken(userId);

                            message.reply(new JsonObject().put("result", token));

                        } else {
                            this.responder.replyWithError(
                                message,
                                "INVALID_EMAIL_OR_PASSWORD",
                                new Exception("Invalid email or password")
                            );
                        }
                    } catch (Throwable t) {
                        this.responder.replyWithError(message, t);
                    }
                },
                this.responder.replyWithError(message)
            );
    }



    private String createJwtToken(Integer userId) {
        return this.jwtAuth.generateToken(
            new JsonObject(),
            new JWTOptions()
                .setExpiresInMinutes(this.jwtConfig.getInteger("expiresInMinutes"))
                .setSubject(String.valueOf(userId))
                .setAlgorithm(this.jwtConfig.getString("algorithm"))
        );
    }
}
