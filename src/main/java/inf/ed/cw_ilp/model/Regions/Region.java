package inf.ed.cw_ilp.model.Regions;
import java.util.List;

public record Region(
        String name,
        Position[] vertices
) {
}
