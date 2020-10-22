package vertx.casestudy.headline;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;

public class HeadlineGetAllHandler implements Handler<RoutingContext> {

    private final PgPool client;



    @Inject
    public HeadlineGetAllHandler(PgPool client) {
        this.client = client;
    }



    @Override
    public void handle(RoutingContext ctx) {
        this.client
            .query("SELECT * FROM headline")
            .rxExecute()
            .map(rowSet -> {
                final var array = new JsonArray();
                for (Row row : rowSet) {
                    array.add(
                        new JsonObject()
                            .put("id", row.getInteger("id"))
                            .put("source", row.getString("source"))
                            .put("author", row.getString("author"))
                            .put("title", row.getString("title"))
                            .put("description", row.getString("description"))
                            .put(
                                "publishedAt",
                                row.getOffsetDateTime("published_at").toString()
                            )
                    );
                }
                return array;
            })
            .subscribe(
                headlines -> ctx.response()
                                .setStatusCode(200)
                                .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                                .end(headlines.encode()),
                ctx::fail
            );
    }
}
