package co.tophe.ion.internal;

import com.koushikdutta.async.parser.AsyncParser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by Denis Babak on 12/05/15.
 */
public abstract class AsyncParserBase<T> implements AsyncParser<T> {

    public Type getType() {
        return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
