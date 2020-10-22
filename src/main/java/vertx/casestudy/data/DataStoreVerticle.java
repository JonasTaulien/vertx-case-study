package vertx.casestudy.data;

import com.google.inject.Inject;
import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import vertx.casestudy.data.headline.HeadlineCreateConsumer;

public class DataStoreVerticle extends AbstractVerticle {

    private final HeadlineCreateConsumer headlineCreateConsumer;



    @Inject
    public DataStoreVerticle(HeadlineCreateConsumer headlineCreateConsumer) {
        this.headlineCreateConsumer = headlineCreateConsumer;
    }



    @Override
    public Completable rxStart() {
        final var headlineCreate = vertx.eventBus()
                                        .consumer("headline.create", this.headlineCreateConsumer)
                                        .rxCompletionHandler();

        return Completable.concatArray(headlineCreate);
    }
}
