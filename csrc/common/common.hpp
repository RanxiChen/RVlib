#include <math.h>
#include "json.hpp"
using json = nlohmann::json;
#include <iostream>
#include <fstream>
#include <utility>
template<typename T>
T loosely_div(T dividend, T divisor) {
    T lquotient = dividend / divisor;
    T lreminder = dividend % divisor;
    T left =  0;
    if(lreminder > divisor) {
        left = lreminder - divisor;
    }else {
        left = divisor - lreminder;
    }
    if( static_cast<double> (left) < divisor*0.001 ){
        return lquotient +1;
    }else {
        return lquotient;
    }
}

typedef std::vector<std::pair<char,int>> SignalLane;
class WaveDrom {
    public:
        std::string signallane2wave(SignalLane & lane) {
            std::string wave;
            for(auto it:lane) {
                wave.append(it.second, it.first);
            }
            return wave;
        }
        void singlewave2json( std::string signal_name, std::string wave_contents) {
            json item;
            item["name"] = signal_name;
            item["wave"] = wave_contents;
            json j;
            j["signal"] = json::array();
            j["signal"].push_back(item);
            this -> j = j;
        }
        void dump2console() {
            std::cout << std::setw(4) << j << std::endl;
        }
    private:
        json j;
};