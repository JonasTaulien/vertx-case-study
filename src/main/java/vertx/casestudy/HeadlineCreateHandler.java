package vertx.casestudy;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

public class HeadlineCreateHandler implements Handler<RoutingContext> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String INSERT_HEADLINE_QUERY
        = "INSERT INTO headline (source, author, title, description, published_at) "
              + "VALUES ($1, $2, $3, $4, $5::timestamptz) "
              + "RETURNING id";

    private final PgPool pgPool;

    private final Responder responder;



    @Inject
    public HeadlineCreateHandler(PgPool pgPool, Responder responder) {
        this.pgPool = pgPool;
        this.responder = responder;
    }



    @Override
    public void handle(RoutingContext ctx) {
        final var body = ctx.getBodyAsJson();
        final var user = ctx.user().principal();

        log.info("User with id {} will create headline", user.getString("sub"));

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
                idOfCreatedHeadline -> this.responder
                                           .respond(
                                               ctx,
                                               HttpResponseStatus.CREATED,
                                               body.put("id", idOfCreatedHeadline)
                                           ),
                ctx::fail
            );
    }
}
