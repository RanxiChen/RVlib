#include "Uart.hpp"
#include "common.hpp"
#include <stdint.h>
#include<verilated.h>
#include<verilated_vcd_c.h>
#include "VUart_Clock_Generator.h"

int main() {
    static VUart_Clock_Generator* top = new VUart_Clock_Generator;
    Verilated::traceEverOn(true);
    VerilatedVcdC* tfp = new VerilatedVcdC;
    top->trace(tfp, 99);
    tfp->open("Uart_Clock_Generator.vcd");
    uint8_t (*get_txd)(void) = []() -> uint8_t {
        return top -> io_out;
    };
    UartRxSim sim_rx(125000000,115200,get_txd);
    printf("Starting simulation...\n");
    uint64_t max_time = 10000000;
    uint64_t sim_time = 0;
    uint8_t clock_data =0;
    int dumpable=0;
    sim_rx.setSilent(1);
    while(sim_time < max_time) {
        if(sim_time <= 3){
            top -> reset = 1;
            sim_rx.reset();
        } else {
            top -> reset = 0;
        }
        top -> clock = 0;
        top -> eval();
        if(dumpable)tfp->dump(sim_time);
        sim_time++;
        top -> clock = 1;
        top -> eval();
        if(dumpable)tfp->dump(sim_time);
        sim_time++;
        sim_rx.run();
        if(sim_rx.isDataValid()){
            assert(sim_rx.getData() == clock_data);
            clock_data += 1;
            if(clock_data >= 0xe && clock_data <= 0x11) {
                dumpable = 1;
            }else {
                dumpable = 0;
            }
        }
        if(top ->io_rx_valid){
            printf("Rx get %x\n",top ->io_rx_data);
            printf("*******************************\n");
        }
    }
    tfp->close();
    delete top;
    delete tfp;
    printf("Simulation finished.\n");
    return 0;
}
