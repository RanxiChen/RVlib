#include<verilated.h>
#include "VHelloModule.h"
#include<stdint.h>

int main(void){
    VHelloModule* dut = new VHelloModule;
    //initial value
    dut -> reset = 0;
    for(int i =0;i< 100;i++){
        dut -> clock = 1;
        dut -> eval();
        if(i == 0){
            dut -> reset = 1;
        }else {
            dut -> reset = 0;
        }
        dut -> io_in = i;
        printf("current input :%ld, output:%ld\n",dut -> io_in,dut -> io_out);
        dut -> clock =0;
        dut -> eval();
    }
    return 0;
}