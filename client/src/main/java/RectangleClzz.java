import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.annotation.*;

@Properties(
	value=@Platform(include={"TestFile.h"},
				linkpath = {"/home/shyabith/Documents/workspace/testGenerator"},
				link="Test"),
	target="RectangleClzz"
)
public class RectangleClzz {
	public static class Rectangle extends Pointer {
		static {
			Loader.load();
		}
		public Rectangle(int x, int y){
			allocate(x, y);
		}
		private native void allocate(int x, int y);
		public native @ByVal int getWidth();
		public native @ByVal int setWidth(int a, int b);
	}

	public static void main(String[] args) {}
}