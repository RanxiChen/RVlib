#include "Uart.hpp"

int main(void) {
    printf("SimUart start\n");
    printf("This circuit just make Tx.txd <-> Rx.rxd\n");
    printf("I will make Tx.data from 0 to 0xff\n");
    UartTxSim txsim(125000000, 115200);
    UartRxSim rxsim(125000000, 115200,&(txsim.txd));
    uint64_t max_cycle = (uint64_t)125000000 / 115200;
    for(int i = 0; i < 256; i++) {
        txsim.setData(i);
        for(int j = 0; j < 2*max_cycle; j++) {
            txsim.run();
            rxsim.run();
        }
    }
}