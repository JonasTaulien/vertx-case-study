package vertx.casestudy.headline;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.sqlclient.PoolOptions;

public class HeadlineGetAllHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        final var client = PgPool.pool(
            ctx.vertx(),
            new PgConnectOptions()
                .setHost("localhost")
                .setPort(5432)
                .setUser("example")
                .setPassword("example")
                .setDatabase("case-study"),
            new PoolOptions()
        );

        client.query("SELECT * FROM headline")
              .rxExecute()
              .subscribe(
                  rowSet -> {
                      try {
                          final var headlines = new JsonArray();
                          for (Row row : rowSet) {
                              headlines.add(
                                  new JsonObject()
                                      .put("id", row.getInteger("id"))
                                      .put("author", row.getString("author"))
                                      .put("source", row.getString("source"))
                                      .put("title", row.getString("title"))
                                      .put("description", row.getString("description"))
                                      .put("publishedAt", row.getOffsetDateTime("publishedAt").toString())
                              );
                          }
                          ctx.response()
                             .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                             .end(headlines.encode());
                      }catch (Throwable t){
                          ctx.fail(t);
                      }
                  },
                  ctx::fail
              );
    }
}
