#include <stdio.h>
class UartRxSim {
public:
    UartRxSim(int freq = 125000000, int baud = 115200) {
        this ->freq = freq;
        this ->baud = baud;
    }
    void run () {
        printf("frequency: %d, baud: %d\n", freq, baud);
    }
private:
    int freq; // Frequency of the UART clock
    int baud; // Baud rate for UART communication
};