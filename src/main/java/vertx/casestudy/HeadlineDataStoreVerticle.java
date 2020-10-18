package vertx.casestudy;

import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

public class HeadlineDataStoreVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final PgPool pgPool;



    @Inject
    public HeadlineDataStoreVerticle(PgPool pgPool) {
        this.pgPool = pgPool;
    }



    @Override
    public Completable rxStart() {
        vertx.eventBus()
             .consumer("headline.create", this::createHeadline);

        vertx.eventBus()
             .consumer("headline.getAll", this::getAllHeadlines);

        return Completable.complete();
    }



    private static final String INSERT_HEADLINE_QUERY
        = "INSERT INTO headline (source, author, title, description, published_at) "
              + "VALUES ($1, $2, $3, $4, $5::timestamptz) "
              + "RETURNING id";



    private void createHeadline(Message<JsonObject> message) {
        final var body = message.body();

        this.pgPool
            .preparedQuery(INSERT_HEADLINE_QUERY)
            .rxExecute(Tuple.of(
                body.getString("source"),
                body.getString("author"),
                body.getString("title"),
                body.getString("description"),
                OffsetDateTime.parse(body.getString("publishedAt"))
            ))
            .map(rowSet -> rowSet.iterator().next().getInteger("id"))
            .subscribe(
                idOfCreatedHeadline -> message.reply(body.put("id", idOfCreatedHeadline)),
                this.replyWithError(message)
            );
    }



    private static final String SELECT_ALL_HEADLINES_QUERY
        = "SELECT id, source, author, title, description, published_at FROM headline";



    private void getAllHeadlines(Message<JsonObject> message) {
        this.pgPool
            .preparedQuery(SELECT_ALL_HEADLINES_QUERY)
            .rxExecute()
            .flatMapObservable(Observable::fromIterable)
            .map(HeadlineDataStoreVerticle::rowToJson)
            .toList()
            .subscribe(
                headlines -> message.reply(new JsonObject().put("result", new JsonArray(headlines))),
                this.replyWithError(message)
            );
    }



    private Consumer<Throwable> replyWithError(Message<JsonObject> message) {
        return error -> {
            log.error("Error while creating headline", error);

            message.reply(
                new JsonObject().put("error", error.getMessage()),
                new DeliveryOptions().addHeader("FAILED", "FAILED")
            );
        };
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
