#include "Uart.hpp"

int main() {
    UartRxSim uart(125000000, 115200);
    uart.run();
    return 0;
}