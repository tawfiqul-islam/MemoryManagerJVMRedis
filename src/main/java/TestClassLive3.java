import org.redisson.api.annotation.REntity;
import org.redisson.api.annotation.RId;
import org.redisson.liveobject.resolver.LongGenerator;

@REntity
public class TestClassLive3 {
    @RId
    private long id;
    private int value3;
    private String data;
    protected TestClassLive3() {
    }

    public TestClassLive3(long id, int value) {
        super();
        this.id=id;
        this.value3 = value;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getValue3() {
        return value3;
    }

    public void setValue3(int value3) {
        this.value3 = value3;
    }
}
