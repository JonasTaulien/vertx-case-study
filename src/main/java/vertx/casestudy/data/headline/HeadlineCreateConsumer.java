package vertx.casestudy.data.headline;

import com.google.inject.Inject;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

public class HeadlineCreateConsumer implements Handler<Message<JsonObject>> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final PgPool pgPool;



    @Inject
    public HeadlineCreateConsumer(PgPool pgPool) {
        this.pgPool = pgPool;
    }



    @Override
    public void handle(Message<JsonObject> msg) {
        final var body = msg.body();

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
            .map(rowSet -> rowSet.iterator().next().getInteger("id"))
            .subscribe(
                id -> msg.reply(body.put("id", id)),
                error -> msg.fail(500, "Failed to create headline: " + error.getMessage())
            );
    }
}
