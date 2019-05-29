CC	= g++
BASEDIR	= .
VERSION	= ` date "+%Y.%m%d%" `

clean:
	rm *.o *.so *.gch demo
 
main:
	echo "Creating shared lib"
	mkdir -p $(BASEDIR)/libccp
	$(CC) -Wall -fPIC -o $(BASEDIR)/libccp/Test.o -c TestFile.cpp 
	$(CC) -Wall -shared -o $(BASEDIR)/libccp/libTest.so $(BASEDIR)/libccp/Test.o
