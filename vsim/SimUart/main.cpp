#include "Uart.hpp"
#include "common.hpp"
#include <iostream>
#include <utility>

int main(void) {
    printf("SimUart start\n");
    printf("This circuit just make Tx.txd <-> Rx.rxd\n");
    printf("I will make Tx.data from 0 to 0xff\n");
    UartTxSim txsim(125000000, 115200);
    UartRxSim rxsim(125000000, 115200,&(txsim.txd));
    SignalLane lane;
    lane.push_back({'1',2});
    lane.push_back({'0',2});
    lane.push_back({'1',2});
    WaveDrom wave;
    std::string wave_contents = wave.signallane2wave(lane);
    std::cout << "Wave contents: " << wave_contents << std::endl;
    wave.singlewave2json("test", wave_contents);
    wave.dump2console();
    return 0;
}