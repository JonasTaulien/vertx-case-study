package vertx.casestudy;

import io.netty.handler.codec.http.HttpResponseStatus;
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
              + "VALUES ($1, $2, $3, $4, $5::timestamptz) "
              + "RETURNING id";

    private final PgPool pgPool;

    private final Responder responder;



    public HeadlineCreateHandler(PgPool pgPool, Responder responder) {
        this.pgPool = pgPool;
        this.responder = responder;
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
                ar -> respond(ctx, body, ar)
            );
    }



    private void respond(
        RoutingContext ctx,
        JsonObject body,
        AsyncResult<RowSet<Row>> ar
    ) {
        try {
            if (ar.succeeded()) {
                final var idOfCreatedHeadline = ar.result().iterator().next().getInteger("id");

                this.responder
                    .respond(ctx, HttpResponseStatus.CREATED, body.put("id", idOfCreatedHeadline));

            } else {
                this.responder
                    .respond(
                        ctx,
                        HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        new JsonObject().put("error", ar.cause().toString())
                    );
            }
        } catch (Throwable t) {
            ctx.fail(t);
        }
    }
}
