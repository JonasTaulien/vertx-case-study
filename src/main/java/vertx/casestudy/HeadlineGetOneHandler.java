package vertx.casestudy;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.Tuple;

public class HeadlineGetOneHandler implements Handler<RoutingContext> {

    private static final String SELECT_ONE_HEADLINE_QUERY
        = "SELECT id, source, author, title, description, published_at FROM headline WHERE headline.id = $1";

    private final PgPool pgPool;

    private final Responder responder;



    @Inject
    public HeadlineGetOneHandler(PgPool pgPool, Responder responder) {
        this.pgPool = pgPool;
        this.responder = responder;
    }



    @Override
    public void handle(RoutingContext ctx) {
        final var headlineId = Integer.parseInt(ctx.pathParam("id"));
        this.pgPool
            .preparedQuery(SELECT_ONE_HEADLINE_QUERY)
            .rxExecute(Tuple.of(headlineId))
            .map(resultSet -> resultSet.iterator().next())
            .map(HeadlineGetOneHandler::rowToJson)
            .subscribe(
                rowAsJson -> this.responder.respond(ctx, HttpResponseStatus.OK, rowAsJson),
                ctx::fail
            );
    }



    private static JsonObject rowToJson(Row row) {
        return new JsonObject()
                   .put("id", row.getInteger("id"))
                   .put("source", row.getString("source"))
                   .put("author", row.getString("author"))
                   .put("title", row.getString("title"))
                   .put("description", row.getString("description"))
                   .put(
                       "publishedAt",
                       row.getOffsetDateTime("published_at").toString()
                   );
    }
}
