#include "Uart.hpp"
#include "common.hpp"
#include <iostream>

int main(void) {
    printf("SimUart start\n");
    printf("This circuit just make Tx.txd <-> Rx.rxd\n");
    printf("I will make Tx.data from 0 to 0xff\n");
    UartTxSim txsim(125000000, 115200);
    UartRxSim rxsim(125000000, 115200,&(txsim.txd));
    uint64_t max_cycle = (uint64_t)125000000 / 115200;
    uint8_t pdata=1;
    uint64_t ptime = 0;
    printf("losely_div %d , %d = %d ( really %d)\n", 1152*3-2 , 1152, loosely_div(1252*3-2, 1152), (1152*3-2) / 1152);
    printf("max_cycle = %lu\n", max_cycle);
    WaveDrom wave_watcher;
    wave_watcher.singlewave2json("Tx","0101010");
    wave_watcher.dump2console();
    return 0;
}