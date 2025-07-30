#include "Uart.hpp"
#include "common.hpp"
#include <iostream>
#include <utility>

int main(void) {
    printf("SimUart start\n");
    printf("This is a simulation for UART communication\n");
    printf("This simulation uses a simulated UART transmitter and receiver\n");
    printf("In this simulation, SimRx.rxd <-> SimTx.txd\n");
    static UartTxSim tx_device(125000000, 115200);
    uint8_t (*get_rxd_from_txd)(void) = []() -> uint8_t {
        return tx_device.get_txd();
    };
    static UartRxSim rx_device(125000000, 115200, get_rxd_from_txd);
    printf("tx_device max cycle is %lu\n", tx_device.get_max_cycle());
    uint8_t pdata =0;
    uint64_t ptime = 0;
    uint64_t cnt =0;
    int bit_cnt =0;
    SignalLane lane;
    tx_device.reset();
    for(uint8_t i =0xf0; i < 0xf3; i++) {
        tx_device.post_byte(i);
        for(int j =0; j < 10*tx_device.get_max_cycle()+1;j++ ){
            tx_device.run();
            rx_device.run();
            if(tx_device.get_txd() != pdata || tx_device.current_time() - ptime >= tx_device.get_max_cycle()) {
                cnt = tx_device.current_time() - ptime;
                bit_cnt = (int)loosely_div(cnt, tx_device.get_max_cycle());
                //printf("At cycle %lu, txd switch to %d since last time point %lu( %d bit )( as %lu cycles ) \n",tx_device.current_time(), tx_device.get_txd(),ptime,bit_cnt,cnt);
                if(bit_cnt != 0){
                    lane.push_back(std::make_pair(pdata ? '1' : '0', bit_cnt));
                }
                pdata = tx_device.get_txd();
                ptime = tx_device.current_time();
            }
        }
        if(rx_device.isDataValid()){
            printf("at cycle %lu,rx valid\n", rx_device.sim_time);
            assert(rx_device.getData() == i);
        }
    }
    WaveDrom wave_watcher;
    std::string wave_contents;
    wave_contents = wave_watcher.signallane2wave(lane);
    wave_watcher.singlewave2json("txd", wave_contents);
    wave_watcher.dump2console();
    return 0;
}