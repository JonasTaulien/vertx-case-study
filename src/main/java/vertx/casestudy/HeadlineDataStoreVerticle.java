package vertx.casestudy;

import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Observable;
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

    private final MessageResponder responder;



    @Inject
    public HeadlineDataStoreVerticle(PgPool pgPool, MessageResponder responder) {
        this.pgPool = pgPool;
        this.responder = responder;
    }



    @Override
    public Completable rxStart() {
        final var headlineCreate = vertx.eventBus()
                                        .consumer("headline.create", this::createHeadline)
                                        .rxCompletionHandler();

        final var headlineGetAll = vertx.eventBus()
                                        .consumer("headline.getAll", this::getAllHeadlines)
                                        .rxCompletionHandler();

        final var headlineGetOne = vertx.eventBus()
                                        .consumer("headline.getOne", this::getOneHeadline)
                                        .rxCompletionHandler();

        return Completable.concatArray(headlineCreate, headlineGetAll, headlineGetOne);
    }



    private static final String INSERT_HEADLINE_QUERY
        = "INSERT INTO headline (source, author, title, description, published_at) "
              + "VALUES ($1, $2, $3, $4, $5::timestamptz) "
              + "RETURNING id";



    private void createHeadline(Message<JsonObject> message) {
        final var body = message.body();

        this.log.info("Creating headline with title: {}", body.getString("title"));

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
                this.responder.replyWithError(message)
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
                this.responder.replyWithError(message)
            );
    }



    private static final String SELECT_ONE_HEADLINE_QUERY
        = "SELECT id, source, author, title, description, published_at FROM headline WHERE headline.id = $1";



    private void getOneHeadline(Message<JsonObject> message) {
        final var body = message.body();

        this.pgPool
            .preparedQuery(SELECT_ONE_HEADLINE_QUERY)
            .rxExecute(Tuple.of(body.getInteger("id")))
            .map(resultSet -> resultSet.iterator().next())
            .map(HeadlineDataStoreVerticle::rowToJson)
            .subscribe(
                message::reply,
                this.responder.replyWithError(message)
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
