package vertx.casestudy.data.headline;

import com.google.inject.Inject;
import io.reactivex.Observable;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;

public class HeadlineGetAllConsumer implements Handler<Message<JsonObject>> {

    private final PgPool client;



    @Inject
    public HeadlineGetAllConsumer(PgPool client) {
        this.client = client;
    }



    @Override
    public void handle(Message<JsonObject> msg) {
        this.client
            .query("SELECT * FROM headline")
            .rxExecute()
            .flatMapObservable(Observable::fromIterable)
            .map(
                row -> new JsonObject()
                    .put("id", row.getInteger("id"))
                    .put("source", row.getString("source"))
                    .put("author", row.getString("author"))
                    .put("title", row.getString("title"))
                    .put("description", row.getString("description"))
                    .put(
                        "publishedAt",
                        row.getOffsetDateTime("published_at").toString()
                    )
            )
            .toList()
            .map(JsonArray::new)
            .subscribe(
                msg::reply,
                error -> msg.fail(500, error.getMessage())
            );
    }
}
