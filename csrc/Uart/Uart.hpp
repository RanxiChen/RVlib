#include <stdio.h>
#include<stdint.h>
class UartRxSim {
public:
    UartRxSim() = delete;
    UartRxSim(const UartRxSim&) = delete;
    UartRxSim& operator=(const UartRxSim&) = delete;
    uint64_t sim_time =0;
    uint8_t * rxd_ptr = NULL ;
    UartRxSim(int freq = 125000000, int baud = 115200, uint8_t * rxd = NULL) {
        this ->freq = freq;
        this ->baud = baud;
        this ->max_cycle = (uint64_t)freq / baud;
        this ->rxd_ptr = rxd;
    }
    uint8_t getData() {
        return data;
    }
    uint8_t isDataValid() {
        return data_valid;
    }
    void notify() {
        // This function can be used to notify the system that new data is available
        //printf("Sim Uart get data:%d\n",data);
        ;
    }
    void run () {
        sim_time++;
        uint8_t rxd = 0;
        rxd = *rxd_ptr;
        if(busy == 0) {
            if(rxd == 0){
                busy = 1;
                statecnt = 0;
                stored_time = sim_time;
                data = 0;
                data_valid = 0;
                return;
            }else {
                busy = 0;
                data_valid = 0;
                data = 0;
                return;
            }
        }else {
            //read bit by bit
            if(sim_time - stored_time == max_cycle/4) {
                vote0 = *rxd_ptr;
            }else if(sim_time - stored_time == max_cycle/2) {
                vote1 = *rxd_ptr;
            }else if(sim_time - stored_time == max_cycle*3/4) {
                vote2 = *rxd_ptr;
            }else if(sim_time - stored_time == max_cycle) {
                stored_time = sim_time;
                if(vote0 + vote1 + vote2 >= 2) {
                    bit_after_vote = 1;
                }else {
                    bit_after_vote = 0;
                }
                if(statecnt == 0) {
                    // Start bit
                    if(bit_after_vote == 0) {
                        statecnt++;
                    }else {
                        busy =0; // Invalid start bit, reset state
                        statecnt = 0;
                    }
                }else if(statecnt < 9) {
                    // Data bits
                    data = bit_after_vote << (statecnt-1) | data;
                    statecnt++;
                }else if(statecnt == 9) {
                    // Stop bit
                    if(bit_after_vote == 1) {
                        data_valid = 1; // Data is valid
                        notify();
                    }else {
                        data_valid = 0; // Invalid stop bit, reset state
                    }
                    busy = 0; // Transmission complete
                    statecnt = 0; // Reset state counter
                }
            }
        }
    }
private:
    int freq; // Frequency of the UART clock
    int baud; // Baud rate for UART communication
    uint8_t data =0;
    // 0 for un avaliable, 1 for available
    uint8_t data_valid = 0; 
    uint64_t max_cycle = 0;
    int statecnt = 0; // State counter for bit position
    uint8_t busy = 0; // 0 for idle, 1 for busy
    uint64_t stored_time = 0;
    uint8_t vote0 = 0;
    uint8_t vote1 = 0;
    uint8_t vote2 = 0;
    uint8_t bit_after_vote = 0;
};

class UartTxSim {
public:
    uint64_t current_time() {
        return sim_time;
    }
    UartTxSim() = delete;
    UartTxSim(const UartTxSim&) = delete;
    UartTxSim& operator=(const UartTxSim&) = delete;
    uint64_t sim_time =0;
    uint64_t stored_time =0;
    uint8_t txd =1;    
    UartTxSim(int freq = 125000000, int baud = 115200) {
        this ->freq = freq;
        this ->baud = baud;
        this ->max_cycle = (uint64_t)freq / baud;
    }

    void setData(uint8_t data) {
        this ->post_data = data;
        this ->busy =1;
        this -> tx_data = (post_data & 0xff) << 1 | 0x0 | (0x1 << 9); // Start bit + data bits + stop bit
    }

    void run() {
        sim_time++;
        if(busy ==1){
            this ->txd = (tx_data >> statecnt) & 0x1; // Get the current bit to transmit
            if(sim_time - stored_time == max_cycle){
                stored_time = sim_time;
                if(statecnt == 9){
                    busy =0; // Transmission complete
                    statecnt = 0;
                }else{
                    statecnt++;
                }
            }
            
        }else {
            this ->txd = 1;
        }
    }

private:
    int freq; // Frequency of the UART clock
    int baud; // Baud rate for UART communication
    uint64_t max_cycle = 0;
    uint8_t post_data =0;
    uint16_t tx_data =0;
    uint8_t busy = 0; // 0 for idle, 1 for busy
    int statecnt=0;
};