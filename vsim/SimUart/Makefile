run: sim.exe
	./sim.exe
sim.exe:main.cpp ../../csrc/Uart/Uart.hpp
	g++ main.cpp -I../../csrc/Uart -I../../csrc/common  -o sim.exe -std=c++11

clean:
	find . -type f -executable -exec rm -f {} \;