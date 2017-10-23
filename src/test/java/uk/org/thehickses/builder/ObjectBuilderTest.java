package uk.org.thehickses.builder;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class ObjectBuilderTest
{
    private static class TestObj implements Cloneable
    {
        private String name;
        private int number;
        private boolean flag;

        public TestObj()
        {
        }

        public TestObj(String name, int number, boolean flag)
        {
            this.name = name;
            this.number = number;
            this.flag = flag;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public void setNumber(int number)
        {
            this.number = number;
        }

        public void setFlag(boolean flag)
        {
            this.flag = flag;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (flag ? 1231 : 1237);
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + number;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestObj other = (TestObj) obj;
            if (flag != other.flag)
                return false;
            if (name == null)
            {
                if (other.name != null)
                    return false;
            }
            else if (!name.equals(other.name))
                return false;
            if (number != other.number)
                return false;
            return true;
        }

        @Override
        public TestObj clone()
        {
            try
            {
                return (TestObj) super.clone();
            }
            catch (CloneNotSupportedException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString()
        {
            return "TestObj [name=" + name + ", number=" + number + ", flag=" + flag + "]";
        }
    }

    @Test
    public void testFromNew()
    {
        String name = "halghasgshg";
        int number = 12141142;
        boolean flag = true;
        TestObj expected = new TestObj(name, number, flag);
        TestObj actual = new ObjectBuilder<>(TestObj::new)
                .set(name, TestObj::setName)
                .set(number, TestObj::setNumber)
                .set(flag, TestObj::setFlag)
                .build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testFromCopy()
    {
        String name = "halghasgshg";
        int number = 12141142;
        boolean flag = true;
        TestObj expected = new TestObj(name, number, flag);
        TestObj initObj = new TestObj("a", 1, false);
        ObjectBuilder<TestObj> builder = new ObjectBuilder<>(initObj, o -> o.clone());
        initObj.setName("aaa");
        builder.set(name, TestObj::setName).set(number, TestObj::setNumber).set(flag,
                TestObj::setFlag);
        TestObj actual1 = builder.build();
        assertThat(actual1).isNotSameAs(initObj);
        assertThat(actual1).isEqualTo(expected);
        TestObj actual2 = builder.build();
        assertThat(actual2).isNotSameAs(initObj);
        assertThat(actual2).isNotSameAs(actual1);
        assertThat(actual2).isEqualTo(expected);
    }
    
    @Test
    public void testWithArbitraryModifiers()
    {
        String name = "halghasgshg";
        int number = 12141142;
        boolean flag = true;
        TestObj expected = new TestObj("Team " + name, number - 4, flag);
        TestObj actual = new ObjectBuilder<>(TestObj::new)
                .set(name, TestObj::setName)
                .modify(obj -> obj.setFlag(number % 2 == 0))
                .modify(obj -> obj.setNumber(number - 4))
                .modify(obj -> obj.setName("Team " + name))
                .build();
        assertThat(actual).isEqualTo(expected);
    }
}
