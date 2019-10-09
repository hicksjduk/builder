package uk.org.thehickses.builder;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

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
    private final AtomicReference<Supplier<T>> builder;

    /**
     * Initializes the builder with the specified creator.
     * 
     * @param creator
     *            a supplier which returns a newly-created instance of the type constructed by the builder.
     */
    public ObjectBuilder(Supplier<T> creator)
    {
        builder = new AtomicReference<>(creator);
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
    public ObjectBuilder(T object, Function<? super T, ? extends T> copier)
    {
        T snapshot = copier.apply(object);
        builder = new AtomicReference<>(() -> copier.apply(snapshot));
    }

    /**
     * Builds an instance of the type constructed by the builder, by getting the object from the creator and then
     * modifying it with the modifier(s).
     * 
     * @return the instance.
     */
    public T build()
    {
        return getBuilder().get();
    }

    private Supplier<T> getBuilder()
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
    public ObjectBuilder<T> modify(Consumer<? super T> modifier)
    {
        modify(conditionalModifier(modifier, obj -> true));
        return this;
    }

    private ObjectBuilder<T> modify(UnaryOperator<T> modifier)
    {
        builder.updateAndGet(b -> () -> modifier.apply(b.get()));
        return this;
    }

    private UnaryOperator<T> conditionalModifier(Consumer<? super T> modifier,
            Predicate<? super T> condition)
    {
        return obj -> doModification(obj, modifier, condition);
    }

    private T doModification(T object, Consumer<? super T> modifier, Predicate<? super T> condition)
    {
        if (condition.test(object))
            modifier.accept(object);
        return object;
    }

    /**
     * Adds a modification that is to be applied when building an object, provided that the specified condition is
     * satisfied.
     * 
     * @param modifier
     *            a consumer which accepts the built object and may perform any processing on it.
     * @param condition
     *            a predicate which accepts the built object and which must evaluate true (at build time) if the
     *            modification is to be performed.
     * @return the builder, to enable chaining of calls.
     */
    public ObjectBuilder<T> modify(Consumer<? super T> modifier, Predicate<? super T> condition)
    {
        return modify(conditionalModifier(modifier, condition));
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
    public <V> ObjectBuilder<T> set(V value, BiConsumer<? super T, ? super V> setter)
    {
        return modify(obj -> setter.accept(obj, value));
    }

    /**
     * Adds a modification that consists of calling a setter method with the specified value, provided that the
     * specified condition is satisfied.
     * 
     * @param value
     *            the value that is to be passed to the setter.
     * @param setter
     *            a consumer which invokes the setter method on the built object, passing the specified value.
     * @param condition
     *            a predicate which accepts the built object and the input value, and which must evaluate true (at build
     *            time) if the modification is to be performed.
     * @return the builder, to enable chaining of calls.
     */
    public <V> ObjectBuilder<T> set(V value, BiConsumer<? super T, ? super V> setter,
            BiPredicate<? super T, ? super V> condition)
    {
        return modify(obj -> setter.accept(obj, value), obj -> condition.test(obj, value));
    }
}
