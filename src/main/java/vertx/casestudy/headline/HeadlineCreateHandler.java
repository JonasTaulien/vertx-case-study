package vertx.casestudy.headline;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;

import java.time.OffsetDateTime;

public class HeadlineCreateHandler implements Handler<RoutingContext> {

    private final PgPool client;



    @Inject
    public HeadlineCreateHandler(PgPool client) {
        this.client = client;
    }



    @Override
    public void handle(RoutingContext ctx) {
        final var body = ctx.getBodyAsJson();

        this.createHeadline(ctx.vertx(), body)
            .subscribe(
                createdHeadline -> ctx.response()
                                      .setStatusCode(201)
                                      .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                                      .end(createdHeadline.encode()),
                ctx::fail
            );

    }



    private Single<JsonObject> createHeadline(Vertx vertx, JsonObject body) {
        return
            this.client
                .preparedQuery(
                    "INSERT INTO headline (source, author, title, description, published_at) "
                        + "VALUES ($1, $2, $3, $4, $5::timestamptz) "
                        + "RETURNING id"
                )
                .rxExecute(
                    Tuple.of(
                        body.getString("source"),
                        body.getString("author"),
                        body.getString("title"),
                        body.getString("description"),
                        OffsetDateTime.parse(body.getString("publishedAt"))
                    )
                )
                .map(rowSet -> rowSet.iterator().next().getInteger("id"))
                .map(idOfCreatedHeadline -> body.put("id", idOfCreatedHeadline));
    }
}
