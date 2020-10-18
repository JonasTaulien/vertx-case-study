package vertx.casestudy.util;

import java.util.Iterator;
import java.util.stream.Stream;

public class StreamHelper {

    public static <T> Stream<T> streamFromIterator(Iterator<T> it) {
        return Stream.generate(() -> null)
                     .takeWhile(x -> it.hasNext())
                     .map(n -> it.next());
    }
}
