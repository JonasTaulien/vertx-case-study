package vertx.casestudy.headline;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.Tuple;
import io.vertx.sqlclient.PoolOptions;

import java.time.OffsetDateTime;
import java.util.List;

public class HeadlineCreateHandler implements Handler<RoutingContext> {

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
        final PgPool client = PgPool.pool(
            vertx,
            new PgConnectOptions()
                .setHost("localhost")
                .setPort(5432)
                .setDatabase("case-study")
                .setPassword("example")
                .setUser("example"),
            new PoolOptions().setMaxSize(10)
        );

        return
            client
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
