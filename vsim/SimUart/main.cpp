#include "Uart.hpp"
#include "common.hpp"
#include <iostream>
#include <utility>

int main(void) {
    printf("SimUart start\n");
    printf("This circuit just make Tx.txd <-> Rx.rxd\n");
    printf("I will make Tx.data from 0 to 0xff\n");
    UartTxSim tx_device(125000000, 115200);
    printf("tx_device max cycle is %lu\n", tx_device.get_max_cycle());
    uint8_t pdata =0;
    uint64_t ptime = 0;
    uint64_t cnt =0;
    int bit_cnt =0;
    SignalLane lane;
    tx_device.reset();
    //for(int i =0xf0; i < 0xf3; i++) {
        tx_device.post_byte(0xf3);
        for(int j =0; j < 12*tx_device.get_max_cycle();j++ ){
            tx_device.run();
            if(tx_device.get_txd() != pdata) {
                cnt = tx_device.current_time() - ptime;
                bit_cnt = (int)loosely_div(cnt, tx_device.get_max_cycle());
                printf("At cycle %lu, txd switch to %d since last time point %lu( %d bit )( as %lu cycles ) \n",tx_device.current_time(), tx_device.get_txd(),ptime,bit_cnt,cnt);
                if(bit_cnt != 0){
                    lane.push_back(std::make_pair(pdata ? '1' : '0', bit_cnt));
                }
                pdata = tx_device.get_txd();
                ptime = tx_device.current_time();
            }
        }
    //}
    WaveDrom wave_watcher;
    std::string wave_contents;
    wave_contents = wave_watcher.signallane2wave(lane);
    wave_watcher.singlewave2json("txd", wave_contents);
    wave_watcher.dump2console();
    return 0;
}