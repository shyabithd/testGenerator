import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.annotation.*;

@Properties(
	value=@Platform(include={"TestFile.h"},
				linkpath = {"/home/shyabith/Documents/testGenerator/libccp/"},
				link="Test"),
	target="MathUtilsClzz"
)
public class MathUtilsClzz {
	public static class MathUtils extends Pointer {
		static {
			Loader.load();
		}
		public MathUtils(int x, int y){
			allocate(x, y);
		}
		private native void allocate(int x, int y);
		public native @ByVal int sum(int x, int y);
		public native @ByVal int substraction();
		public native @ByVal int factorial(int x);
		public native @ByVal int multiplication();
		public native @ByVal int area();
		public native @ByVal int getWidth();
		public native @ByVal int setWidth(int a, int b);
	}

	public static void main(String[] args) {
		int int0 = (-2714);
		int int1 = (-406);
		MathUtils class0 = new MathUtils(int0, int1);
		int int2 = (-1508);
		int int3 = 446;
		int setWidth__Val__ = class0.setWidth(int2, int3);
		System.out.println("####setWidth__Val__"+ setWidth__Val__);
		
		}
}