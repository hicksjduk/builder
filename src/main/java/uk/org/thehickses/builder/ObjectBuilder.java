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
     * Gets an initializer which is based on a copy of the specified object. Enables the initialization of a builder
     * from a particular object (see {@link ObjectBuilder#ObjectBuilder(Object, Function)}.
     * 
     * @param object
     *            the object.
     * @param copier
     *            a function which creates a copy of a specified object.
     * @return a supplier which creates a copy of a copy of the input object.
     */
    private static <T> Supplier<T> copyInitializer(T object, Function<T, T> copier)
    {
        T snapshot = copier.apply(object);
        return () -> copier.apply(snapshot);
    }

    /**
     * The initializer which is called by the builder to obtain the object to build.
     */
    private final Supplier<T> initializer;

    /**
     * The modifier, or chain of modifiers, which is called by the builder to modify the object to build.
     */
    private Consumer<T> modifier = null;

    /**
     * Initializes the builder with the specified initializer.
     * 
     * @param initializer
     *            a supplier which returns an instance of the type constructed by the builder.
     */
    public ObjectBuilder(Supplier<T> initializer)
    {
        this.initializer = initializer;
    }

    /**
     * Initializes the builder with a copy of the specified object, and an initializer which creates a copy of that copy
     * on each invocation.
     * 
     * @param object
     *            the object.
     * @param copier
     *            a function which creates a copy of a specified object.
     */
    public ObjectBuilder(T object, Function<T, T> copier)
    {
        this(copyInitializer(object, copier));
    }

    /**
     * Builds an instance of the type constructed by the builder, by getting the object from the initializer and then
     * modifying it with the modifier(s).
     * 
     * @return the instance.
     */
    public T build()
    {
        T answer = initializer.get();
        if (modifier != null)
        {
            modifier.accept(answer);
        }
        return answer;
    }

    /**
     * Adds a modification that is to be applied when building an object.
     * 
     * @param modifier
     *            a consumer which accepts the built object and may perform any processing on it.
     * @return the builder, to enable chaining of calls.
     */
    public ObjectBuilder<T> modify(Consumer<T> modifier)
    {
        this.modifier = this.modifier == null ? modifier : this.modifier.andThen(modifier);
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
