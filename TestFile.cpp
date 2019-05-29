#include "TestFile.h"

/**
 * Rectangle Docs
 */
// == 28
// < 8
// > 9
Rectangle::Rectangle (int a, int b) {
  width = a;
  height = b;
}
int Rectangle::setWidth(int a, int b)
{
 if (a < b)
 {
    width = a;
    height = b;
 }
 else
 {
    height = a;
    width = b;
 }
 return 1;
}

int Rectangle::getWidth() { return width; }
