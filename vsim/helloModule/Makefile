BaseDir ?= ../..

verilog:
	@echo "Generating Verilog to $(realpath $(BaseDir)/build/helloModule)"
	@cd $(BaseDir) && sbt 'runMain cache.HelloModule' && echo "Done"
clean:
	rm -rf $(BaseDir)/build/helloModule
	rm -rf obj_dir

sim:verilog
	verilator --cc -f  $(BaseDir)/build/helloModule/filelist.f \
	-I$(BaseDir)/build/helloModule  --exe helloModule.cpp
	make -C obj_dir -f VHelloModule.mk VHelloModule

run:sim
	./obj_dir/VHelloModule