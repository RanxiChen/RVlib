BASE_DIR := $(shell dirname $(shell dirname $(CURDIR)))
all: clean  verilog exe
exe:
	verilator --cc -f ../../build/uart_clock_generator/filelist.f \
		-I../../build/uart_clock_generator \
		--top-module Uart_Clock_Generator \
		--CFLAGS "-I${BASE_DIR}/csrc/common -I${BASE_DIR}/csrc/Uart" \
		--exe main.cpp 
	make -C obj_dir -f VUart_Clock_Generator.mk
	./obj_dir/VUart_Clock_Generator
verilog:
	cd ${BASE_DIR} && sbt "runMain uart.Uart_Clock_Generator"
clean:
	find . -type f -executable -exec rm {} \;
	rm -rf obj_dir
	rm -rf ${BASE_DIR}/build/uart_clock_generator/
