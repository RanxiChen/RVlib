#include "Uart.hpp"
#include "common.hpp"
#include <stdint.h>
#include<verilated.h>
#include<verilated_vcd_c.h>
#include "VUart_Clock_Generator.h"

int main() {
    static VUart_Clock_Generator dut;
    uint8_t (*get_txd)(void) = []() -> uint8_t {
        return dut.io_out;
    }; 
    UartRxSim sim_rx(125000000,115200, get_txd);
    sim_rx.setSilent(1);
    uint64_t max_time = 10000000;
    uint64_t sim_time = 0;
    for(sim_time =0; sim_time < max_time; sim_time++) {
        if(sim_time ==0){
            dut.reset = 1;
            dut.clock = 0;
            sim_rx.reset();
        }else{
            dut.reset = 0;
            dut.clock ^= 1;
        }
        dut.eval();
        if(dut.clock)sim_rx.run();
        if(sim_rx.isDataValid()){
            //assert(sim_rx.getData() == 0xf1);
        }
    }
    return 0;
}
