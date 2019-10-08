package uk.org.thehickses.builder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.InOrder;

public class ObjectBuilderTest
{
    private static interface TestSuper
    {
        void setName(String name);
    }

    private static interface TestObj extends TestSuper
    {
        void setNumber(int number);

        void setFlag(boolean flag);
    }

    @Test
    public void testFromNew()
    {
        String name = "halghasgshg";
        int number = 12141142;
        boolean flag = true;
        TestObj actual = new ObjectBuilder<>(() -> mock(TestObj.class))
                .set(name, TestSuper::setName)
                .set(number, TestObj::setNumber)
                .set(flag, TestObj::setFlag)
                .build();
        InOrder inOrder = inOrder(actual);
        inOrder.verify(actual).setName(name);
        inOrder.verify(actual).setNumber(number);
        inOrder.verify(actual).setFlag(flag);
        verifyNoMoreInteractions(actual);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFromCopy()
    {
        String name = "halghasgshg";
        int number = 12141142;
        boolean flag = true;
        TestObj initObj = mock(TestObj.class);
        TestObj snapshot = mock(TestObj.class);
        Function<TestObj, TestObj> copier = mock(Function.class);
        when(copier.apply(initObj)).thenReturn(snapshot);
        when(copier.apply(snapshot)).then(iom -> mock(TestObj.class));
        ObjectBuilder<TestObj> builder = new ObjectBuilder<>(initObj, copier)
                .set(name, TestObj::setName)
                .set(number, TestObj::setNumber)
                .set(flag, TestObj::setFlag);
        TestObj actual1 = builder.build();
        TestObj actual2 = builder.build();
        assertThat(actual2).isNotSameAs(actual1);
        InOrder inOrder = inOrder(copier, actual1, actual2);
        inOrder.verify(copier).apply(initObj);
        Stream.of(actual1, actual2).forEach(o ->
            {
                assertThat(o).isNotSameAs(initObj);
                assertThat(o).isNotSameAs(snapshot);
                inOrder.verify(copier).apply(snapshot);
                inOrder.verify(o).setName(name);
                inOrder.verify(o).setNumber(number);
                inOrder.verify(o).setFlag(flag);
            });
        verifyNoMoreInteractions(initObj, snapshot, copier, actual1, actual2);
    }

    @Test
    public void testWithArbitraryModifiers()
    {
        String name = "halghasgshg";
        int number = 12141142;
        TestObj actual = new ObjectBuilder<>(() -> mock(TestObj.class))
                .set(name, TestObj::setName)
                .modify(obj -> obj.setFlag(number % 2 == 0))
                .modify(obj -> obj.setNumber(number - 4))
                .modify(obj -> obj.setName("Team " + name))
                .build();
        InOrder inOrder = inOrder(actual);
        inOrder.verify(actual).setName(name);
        inOrder.verify(actual).setFlag(true);
        inOrder.verify(actual).setNumber(number - 4);
        inOrder.verify(actual).setName("Team " + name);
        verifyNoMoreInteractions(actual);
    }

    @Test
    public void testWithNoModifiers()
    {
        TestObj actual = new ObjectBuilder<>(() -> mock(TestObj.class)).build();
        verifyNoMoreInteractions(actual);
    }

    @Test
    public void testWithConditionalModifications()
    {
        TestObj actual = new ObjectBuilder<>(() -> mock(TestObj.class))
                .modify(obj -> obj.setName("Hello"), obj -> true)
                .modify(obj -> obj.setName("Goodbye"), obj -> false)
                .set(1, TestObj::setNumber, (obj, value) -> value % 2 == 1)
                .set(2, TestObj::setNumber, (obj, value) -> value % 2 == 1)
                .build();
        InOrder inOrder = inOrder(actual);
        inOrder.verify(actual).setName("Hello");
        inOrder.verify(actual).setNumber(1);
        verifyNoMoreInteractions(actual);
    }
}
