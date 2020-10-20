package vertx.casestudy.data;

import com.google.inject.Inject;
import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import vertx.casestudy.EventBusAddress;
import vertx.casestudy.data.headline.HeadlineCreateConsumer;

public class DataVerticle extends AbstractVerticle {

    private final HeadlineCreateConsumer headlineCreateConsumer;



    @Inject
    public DataVerticle(HeadlineCreateConsumer headlineCreateConsumer) {
        this.headlineCreateConsumer = headlineCreateConsumer;
    }



    @Override
    public Completable rxStart() {
        final var headlineCreateCom =  vertx.eventBus()
            .<JsonObject>consumer(EventBusAddress.HEADLINE_CREATE)
            .handler(this.headlineCreateConsumer)
            .rxCompletionHandler();

        return Completable.concatArray(headlineCreateCom);
    }
}
