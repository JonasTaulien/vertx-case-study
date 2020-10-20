package vertx.casestudy.data.headline;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeadlineCreateConsumer implements Handler<Message<JsonObject>> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void handle(Message<JsonObject> msg) {
        log.info("received message {}", msg.body().encode());
        msg.reply(new JsonObject().put("reply", "123"));
    }
}
