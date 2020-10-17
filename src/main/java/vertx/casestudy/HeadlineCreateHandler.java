package vertx.casestudy;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.time.OffsetDateTime;

public class HeadlineCreateHandler implements Handler<RoutingContext> {

    private static final String INSERT_HEADLINE_QUERY
        = "INSERT INTO headline (source, author, title, description, published_at) "
              + "VALUES ($1, $2, $3, $4, $5::timestamptz)";

    private final PgPool pgPool;



    public HeadlineCreateHandler(PgPool pgPool) {
        this.pgPool = pgPool;
    }



    @Override
    public void handle(RoutingContext ctx) {
        final var body = ctx.getBodyAsJson();

        this.pgPool
            .preparedQuery(INSERT_HEADLINE_QUERY)
            .execute(
                Tuple.of(
                    body.getString("source"),
                    body.getString("author"),
                    body.getString("title"),
                    body.getString("description"),
                    OffsetDateTime.parse(body.getString("publishedAt"))
                ),
                ar -> HeadlineCreateHandler.respond(ctx, body, ar)
            );
    }



    private static void respond(
        RoutingContext ctx,
        JsonObject body,
        AsyncResult<RowSet<Row>> ar
    ) {
        try {
            if (ar.succeeded()) {
                ctx.response()
                   .end(body.encodePrettily());

            } else {
                ctx.response()
                   .setStatusCode(500)
                   .end(new JsonObject().put("error", ar.cause().toString()).encodePrettily());
            }
        } catch (Throwable t) {
            ctx.fail(t);
        }
    }
}
