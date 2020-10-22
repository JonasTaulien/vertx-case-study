package vertx.casestudy.data;

import com.google.inject.Inject;
import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import vertx.casestudy.data.headline.HeadlineCreateConsumer;
import vertx.casestudy.data.headline.HeadlineGetAllConsumer;

public class DataStoreVerticle extends AbstractVerticle {

    private final HeadlineCreateConsumer headlineCreateConsumer;

    private final HeadlineGetAllConsumer headlineGetAllConsumer;



    @Inject
    public DataStoreVerticle(
        HeadlineCreateConsumer headlineCreateConsumer,
        HeadlineGetAllConsumer headlineGetAllConsumer
    ) {
        this.headlineCreateConsumer = headlineCreateConsumer;
        this.headlineGetAllConsumer = headlineGetAllConsumer;
    }



    @Override
    public Completable rxStart() {
        final var headlineCreate = vertx.eventBus()
                                        .consumer("headline.create", this.headlineCreateConsumer)
                                        .rxCompletionHandler();

        final var headlineGetAll = vertx.eventBus()
                                        .consumer("headline.getAll", this.headlineGetAllConsumer)
                                        .rxCompletionHandler();

        return Completable.concatArray(headlineCreate, headlineGetAll);
    }
}
