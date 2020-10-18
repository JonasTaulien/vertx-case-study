package vertx.casestudy;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;

public class HeadlineGetAllHandler implements Handler<RoutingContext> {

    private static final String SELECT_ALL_HEADLINES_QUERY
        = "SELECT id, source, author, title, description, published_at FROM headline";

    private final PgPool pgPool;

    private final Responder responder;



    @Inject
    public HeadlineGetAllHandler(PgPool pgPool, Responder responder) {
        this.pgPool = pgPool;
        this.responder = responder;
    }



    @Override
    public void handle(RoutingContext ctx) {
        this.pgPool
            .preparedQuery(SELECT_ALL_HEADLINES_QUERY)
            .rxExecute()
            .flatMapObservable(Observable::fromIterable)
            .map(HeadlineGetAllHandler::rowToJson)
            .toList()
            .subscribe(
                headlines -> this.responder.respond(ctx, HttpResponseStatus.OK, new JsonArray(headlines)),
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
