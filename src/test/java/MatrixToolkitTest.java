import junit.framework.TestCase;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;

public class MatrixToolkitTest extends TestCase {

  @SuppressWarnings("boxing")
  public void test1() {
    Matrix m = new DenseMatrix(2, 2);
    m.set(0, 0, -1);
    m.set(0, 1, 0);
    m.set(1, 0, 1);
    m.set(1, 1, 2);
    double n = -1;
    for (MatrixEntry cell : m.transpose()) {
      System.out.println(cell.row() + "," + cell.column() + "=" + cell.get());
      assertEquals(n++, cell.get());
    }
  }

}
