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
    uint64_t max_time = 1000000;
    uint64_t sim_time = 0;
    uint8_t clock_data_sim =0;
    uint8_t clock_data_rx =0;
    int dumpable=1;
    sim_rx.setSilent(0);
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
            //assert(sim_rx.getData() == clock_data);
            if(sim_rx.getData() != clock_data_sim) {
                printf("[Error] Sim rx get %x, however clock data is %x at cycles %lu \n", sim_rx.getData(), clock_data_sim,sim_rx.sim_time);
            }
            clock_data_sim += 1;
        }
        if(top ->io_rx_valid){
            //printf("Rx get data:%x at %lu cycles\n",top ->io_rx_data,sim_rx.sim_time);
            //assert(top ->io_rx_data == clock_data);
            if(top ->io_rx_data != clock_data_rx) {
                printf("[Error] rx device get %x, however clock data for rx  is %x at cycles %lu \n",top ->io_rx_data,clock_data_rx,sim_rx.sim_time);
            }
            clock_data_rx += 1;
        }
    }
    tfp->close();
    delete top;
    delete tfp;
    printf("Simulation finished.\n");
    return 0;
}
