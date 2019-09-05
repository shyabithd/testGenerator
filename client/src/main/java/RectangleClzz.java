import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.annotation.*;

@Properties(
	value=@Platform(include={"TestFile.h"},
				linkpath = {"/home/shyabith/Documents/workspace/testGenerator/libccp/"},
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
		public native @ByVal int area();
		public native @ByVal int getWidth();
		public native @ByVal int setWidth(int a, int b);
	}

	public static void main(String[] args) {
		int int0 = 0;
		int int1 = 1326;
		Rectangle class0 = new Rectangle(int0, int1);
		int int2 = 0;
		int int3 = 3242;
		int setWidth__Val__ = class0.setWidth(int2, int3);
		System.out.println("####setWidth__Val__"+ setWidth__Val__);
		
		}
}