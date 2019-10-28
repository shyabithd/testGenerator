#include <iostream>
#include <vector>
#include <map>
#include "Util.h"

using namespace std;

class ClassB {

    private:
        int x;
        int y;

    public:
        ClassB();
        int set(int x1, int y1);
        int getX() { return x; }
        int getY() { return y; }
};