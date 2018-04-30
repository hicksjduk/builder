package uk.org.thehickses.builder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.InOrder;

public class ObjectBuilderTest
{
    private static interface TestObj
    {
        void setName(String name);

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
                .set(name, TestObj::setName)
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
        InOrder inOrder = inOrder(actual1, actual2);
        Stream.of(actual1, actual2).forEach(o -> {
            assertThat(o).isNotSameAs(initObj);
            assertThat(o).isNotSameAs(snapshot);
            inOrder.verify(o).setName(name);
            inOrder.verify(o).setNumber(number);
            inOrder.verify(o).setFlag(flag);
        });
        verify(copier).apply(initObj);
        verify(copier, times(2)).apply(snapshot);
        verifyNoMoreInteractions(initObj, actual1, actual2);
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
}
