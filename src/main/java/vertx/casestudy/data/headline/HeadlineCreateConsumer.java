package vertx.casestudy.data.headline;

import com.google.inject.Inject;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;

import java.time.OffsetDateTime;

public class HeadlineCreateConsumer implements Handler<Message<JsonObject>> {

    private final PgPool client;



    @Inject
    public HeadlineCreateConsumer(PgPool client) {
        this.client = client;
    }



    @Override
    public void handle(Message<JsonObject> msg) {
        final var body = msg.body();

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
            .map(idOfCreatedHeadline -> body.put("id", idOfCreatedHeadline))
            .subscribe(
                msg::reply,
                error -> msg.fail(500, error.getMessage())
            );
    }
}
