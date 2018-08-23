package uk.org.thehickses.builder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A generic implementation of the Builder pattern which can construct an instance of any type. It works best with types
 * whose properties are not final, but can be modified using setter and other modifier methods.
 * 
 * @author Jeremy Hicks
 *
 * @param <T>
 *            the type of the objects constructed by the builder.
 */
public class ObjectBuilder<T>
{
    /**
     * The builder function which creates and initialises the built object.
     */
    private Supplier<T> builder;

    /**
     * Initializes the builder with the specified creator.
     * 
     * @param creator
     *            a supplier which returns a newly-created instance of the type constructed by the builder.
     */
    public ObjectBuilder(Supplier<T> creator)
    {
        builder = creator;
    }

    /**
     * Initializes the builder with a copy of the specified object, and a creator which creates a copy of that copy on
     * each invocation.
     * 
     * @param object
     *            the object.
     * @param copier
     *            a function which creates a copy of a specified object.
     */
    public ObjectBuilder(T object, Function<T, T> copier)
    {
        T snapshot = copier.apply(object);
        builder = () -> copier.apply(snapshot);
    }

    /**
     * Builds an instance of the type constructed by the builder, by getting the object from the creator and then
     * modifying it with the modifier(s).
     * 
     * @return the instance.
     */
    public synchronized T build()
    {
        return builder.get();
    }

    /**
     * Adds a modification that is to be applied when building an object.
     * 
     * @param modifier
     *            a consumer which accepts the built object and may perform any processing on it.
     * @return the builder, to enable chaining of calls.
     */
    public synchronized ObjectBuilder<T> modify(Consumer<T> modifier)
    {
        Supplier<T> b = builder;
        builder = ()  -> {
            T obj = b.get();
            modifier.accept(obj);
            return obj;
        };
        return this;
    }

    /**
     * Adds a modification that consists of calling a setter method with the specified value.
     * 
     * @param value
     *            the value that is to be passed to the setter.
     * @param setter
     *            a consumer which invokes the setter method on the built object, passing the specified value.
     * @return the builder, to enable chaining of calls.
     */
    public <V> ObjectBuilder<T> set(V value, BiConsumer<T, V> setter)
    {
        return modify(obj -> setter.accept(obj, value));
    }
}
