#include <iostream>

class Rectangle {
private:
    int width;
    int height;
  public:
    Rectangle (int x,int y);

    /** area function Docs */
    int area () {return (width*height);}

    int getWidth();
    int setWidth(int a, int b);
};

namespace CommandType {
	enum CommandType {
		ON_OFF = 1,
		ANALOG = 2

	};
}

struct Command {
	uint8_t type; // values of CommandType
	uint8_t id;
	uint8_t deviceID;
	uint16_t value;
	char *data; // raw data received (if not default command's)
	size_t length;
};