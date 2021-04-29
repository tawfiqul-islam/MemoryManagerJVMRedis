import java.io.Serializable;

public class Circle implements Serializable{

    private String Id;
    private int radius;
    private Point centre;

    public Circle() {
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Point getCentre() {
        return centre;
    }

    public void setCentre(Point centre) {
        this.centre = centre;
    }

    @Override
    public String toString() {
        return "Circle{" +
                "Id='" + Id + '\'' +
                ", radius=" + radius +
                ", centre=" + centre +
                '}';
    }
}
