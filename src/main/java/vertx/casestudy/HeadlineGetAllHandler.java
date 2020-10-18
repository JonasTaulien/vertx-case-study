package vertx.casestudy;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import vertx.casestudy.util.StreamHelper;

import java.util.List;
import java.util.stream.Collectors;

public class HeadlineGetAllHandler implements Handler<RoutingContext> {

    private static final String SELECT_ALL_HEADLINES_QUERY
        = "SELECT id, source, author, title, description, published_at FROM headline";

    private final PgPool pgPool;

    private final Responder responder;



    public HeadlineGetAllHandler(PgPool pgPool, Responder responder) {
        this.pgPool = pgPool;
        this.responder = responder;
    }



    @Override
    public void handle(RoutingContext ctx) {
        this.pgPool
            .preparedQuery(SELECT_ALL_HEADLINES_QUERY)
            .execute(ar -> respond(ctx, ar));
    }



    private void respond(RoutingContext ctx, AsyncResult<RowSet<Row>> ar) {
        try {
            if (ar.succeeded()) {
                final var headlines = HeadlineGetAllHandler.convertRowsIntoJsonObjects(ar);

                this.responder
                    .respond(ctx, HttpResponseStatus.OK, new JsonArray(headlines));

            } else {
                this.responder
                    .respondError(
                        ctx,
                        HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        ar.cause()
                    );
            }
        } catch (Throwable t) {
            ctx.fail(t);
        }
    }



    private static List<JsonObject> convertRowsIntoJsonObjects(AsyncResult<RowSet<Row>> ar) {
        return StreamHelper.streamFromIterator(ar.result().iterator())
                           .map(HeadlineGetAllHandler::rowToJson)
                           .collect(Collectors.toList());
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
