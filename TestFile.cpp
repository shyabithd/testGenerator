#include "TestFile.h"

ClassB::ClassB() : x(0), y(0) {}

int ClassB::set(int x1, int y1) {

    if (x1 < y1) {
        x = x1;
        y = y1;
    } else {
        x = y1;
        y = x1;
    }
}