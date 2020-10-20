package vertx.casestudy.headline;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;

import java.time.OffsetDateTime;

public class HeadlineCreateHandler implements Handler<RoutingContext> {

    private final PgPool pgPool;



    @Inject
    public HeadlineCreateHandler(PgPool pgPool) {
        this.pgPool = pgPool;
    }



    @Override
    public void handle(RoutingContext ctx) {
        final var body = ctx.getBodyAsJson();

        final var source = body.getString("source");
        final var author = body.getString("author");
        final var title = body.getString("title");
        final var description = body.getString("description");
        final var publishedAt = OffsetDateTime.parse(body.getString("publishedAt"));

        this.pgPool
            .preparedQuery(
                "INSERT INTO headline (source, author, title, description, published_at) "
                    + "VALUES ($1, $2, $3, $4, $5::timestamptz) "
                    + "RETURNING id"
            )
            .rxExecute(Tuple.of(source, author, title, description, publishedAt))
            .subscribe(
                rowSet -> {
                    try {
                        final var id = rowSet.iterator().next().getInteger("id");

                        ctx.response()
                           .setStatusCode(201)
                           .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                           .end(body.put("id", id).encode());
                    } catch (Throwable t) {
                        ctx.fail(t);
                    }
                },
                ctx::fail
            );
    }
}
