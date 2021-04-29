import org.redisson.api.annotation.REntity;
import org.redisson.api.annotation.RId;
import org.redisson.liveobject.resolver.UUIDGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@REntity
public class TestClassLive implements Serializable{
    @RId(generator = UUIDGenerator.class)
    private String Id;
    private String name;
    private int value;
    private TestClassLive2 nestedClass;

    public TestClassLive() {
    }
    public TestClassLive(String Id) {
        super();
        this.Id = Id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getId() {
        return Id;
    }

    public TestClassLive2 getNestedClass() {
        return nestedClass;
    }

    public void setNestedClass(TestClassLive2 nestedClass) {
        this.nestedClass = nestedClass;
    }

    @Override
    public String toString() {
        return "TestClassLive{" +
                "Id='" + Id + '\'' +
                ", name='" + name + '\'' +
                ", value=" + value +
                ", nestedClass=" + nestedClass +
                '}';
    }
}
