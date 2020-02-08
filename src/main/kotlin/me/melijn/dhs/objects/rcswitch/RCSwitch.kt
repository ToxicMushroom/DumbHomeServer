package me.melijn.dhs.objects.rcswitch

import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.Pin
import com.pi4j.wiringpi.Gpio
import java.util.*

/**
 * Transmittes signals to 433 MHz electrical switching units. Based on the Arduino library
 * but enhanced to fit a more object oriented approach in Java.
 *
 *
 * This library is designed to be used with a RF transmitter like this
 * one: http://www.watterott.com/de/RF-Link-Sender-434MHz
 *
 *
 *
 * Just connect the DATA IN Pin with the pin provided in the constructor. The
 * VCC with +5V (PIN2) and GND with Ground (PIN6).
 *
 *
 *
 * Usage example:
 *
 * <pre>
 * //our switching group address is 01011 (marked with 1 to 5 on the DIP switch
 * //on the switching unit itself)
 * BitSet address = RCSwitch.getSwitchGroupAddress("01011");
 *
 * RCSwitch transmitter = new RCSwitch(RaspiPin.GPIO_00);
 * transmitter.switchOn(address, 1); //switches the switch unit A (A = 1, B = 2, ...) on
 * Thread.sleep(5000); //wait 5 sec.
 * transmitter.switchOff(address, 1); //switches the switch unit A off
</pre> *
 *
 * @author Suat Özgür
 * @author Florian Frankenberger
 * @author Christoph Stiefel
 */
class RCSwitch @JvmOverloads constructor(transmitterPin: Pin?, protocol: Protocol = Protocol.PROTOCOL_01) {

    private val transmitterPin: GpioPinDigitalOutput?
    private var protocol: Protocol
    private val repeatTransmit = 10
    /**
     * Switch a remote switch on (Type A with 10 pole DIP switches)
     *
     * @param switchGroupAddress Code of the switch group (refers to DIP
     * switches 1..5 where "1" = on and "0" = off, if all DIP switches are on
     * it's "11111")
     * @param switchCode         Number of the switch itself (1..4)
     */
    fun switchOn(switchGroupAddress: BitSet, switchCode: Int) {
        require(switchGroupAddress.length() <= 5) { "switch group address has more than 5 bits!" }
        sendTriState(getCodeWordA(switchGroupAddress, switchCode, true))
    }

    /**
     * Switch a remote switch off
     *
     * @param switchGroupAddress Code of the switch group (refers to DIP
     * switches 1..5 where "1" = on and "0" = off, if all DIP switches are on
     * it's "11111")
     * @param switchCode         Number of the switch itself (1..4 for A..D)
     */
    fun switchOff(switchGroupAddress: BitSet, switchCode: Int) {
        require(switchGroupAddress.length() <= 5) { "switch group address has more than 5 bits!" }
        sendTriState(getCodeWordA(switchGroupAddress, switchCode, false))
    }

    /**
     * Switch a remote switch on (Type B with two rotary/sliding switches)
     *
     * @param nAddressCode Number of the switch group (1..4)
     * @param nChannelCode Number of the switch itself (1..4)
     */
    fun switchOn(nAddressCode: Int, nChannelCode: Int) {
        sendTriState(getCodeWordB(nAddressCode, nChannelCode, true))
    }

    /**
     * Switch a remote switch off (Type B with two rotary/sliding switches)
     *
     * @param nAddressCode Number of the switch group (1..4)
     * @param nChannelCode Number of the switch itself (1..4)
     */
    fun switchOff(nAddressCode: Int, nChannelCode: Int) {
        sendTriState(getCodeWordB(nAddressCode, nChannelCode, false))
    }

    /**
     * Send a string of bits
     *
     * @param bitString Bits (e.g. 000000000001010100010001)
     */
    fun send(bitString: String) {
        val bitSet = BitSet(bitString.length)
        for (i in 0 until bitString.length) {
            if (bitString[i] == '1') {
                bitSet.set(i)
            }
        }
        send(bitSet, bitString.length)
    }

    /**
     * Send a set of bits
     *
     * @param bitSet Bits (000000000001010100010001)
     * @param length Length of the bit string (24)
     */
    fun send(bitSet: BitSet, length: Int) {
        if (transmitterPin != null) {
            for (nRepeat in 0 until repeatTransmit) {
                for (i in 0 until length) {
                    if (bitSet[i]) {
                        transmit(protocol.oneBit)
                    } else {
                        transmit(protocol.zeroBit)
                    }
                }
                sendSync()
            }
            transmitterPin.low()
        }
    }

    /**
     * Like getCodeWord (Type A)
     */
    private fun getCodeWordA(switchGroupAddress: BitSet, switchCode: Int, status: Boolean): String {
        var nReturnPos = 0
        val sReturn = CharArray(12)
        val code = arrayOf("FFFFF", "0FFFF", "F0FFF", "FF0FF", "FFF0F", "FFFF0")
        require(!(switchCode < 1 || switchCode > 5)) {
            ("switch code has to be between "
                + "1 (outlet A) and 5 (outlet E)")
        }
        for (i in 0..4) {
            if (!switchGroupAddress[i]) {
                sReturn[nReturnPos++] = 'F'
            } else {
                sReturn[nReturnPos++] = '0'
            }
        }
        for (i in 0..4) {
            sReturn[nReturnPos++] = code[switchCode][i]
        }
        if (status) {
            sReturn[nReturnPos++] = '0'
            sReturn[nReturnPos] = 'F'
        } else {
            sReturn[nReturnPos++] = 'F'
            sReturn[nReturnPos] = '0'
        }
        return String(sReturn)
    }

    /**
     * Returns a char[13], representing the Code Word to be send. A Code Word
     * consists of 9 address bits, 3 data bits and one sync bit but in our case
     * only the first 8 address bits and the last 2 data bits were used. A Code
     * Bit can have 4 different states: "F" (floating), "0" (low), "1" (high),
     * "S" (synchronous bit)
     *
     *
     * +-------------------------------+--------------------------------+-----------------------------------------+-----------------------------------------+----------------------+------------+
     * | 4 bits address (switch group) | 4 bits address (switch number) | 1 bit address (not used, so never mind) | 1 bit address (not used, so never mind) | 2 data bits (on|off) | 1 sync bit |
     * | 1=0FFF 2=F0FF 3=FF0F 4=FFF0   | 1=0FFF 2=F0FF 3=FF0F 4=FFF0    | F                                       | F                                       | on=FF off=F0         | S          |
     * +-------------------------------+--------------------------------+-----------------------------------------+-----------------------------------------+----------------------+------------+
     *
     * @param nAddressCode Number of the switch group (1..4)
     * @param nChannelCode Number of the switch itself (1..4)
     * @param bStatus      Wether to switch on (true) or off (false)
     * @return char[13]
     */
    private fun getCodeWordB(nAddressCode: Int, nChannelCode: Int,
                             bStatus: Boolean): String {
        var nReturnPos = 0
        val sReturn = CharArray(13)
        val code = arrayOf("FFFF", "0FFF", "F0FF", "FF0F", "FFF0")
        if (nAddressCode < 1 || nAddressCode > 4 || nChannelCode < 1 || nChannelCode > 4) {
            return ""
        }
        for (i in 0..3) {
            sReturn[nReturnPos++] = code[nAddressCode][i]
        }
        for (i in 0..3) {
            sReturn[nReturnPos++] = code[nChannelCode][i]
        }
        sReturn[nReturnPos++] = 'F'
        sReturn[nReturnPos++] = 'F'
        sReturn[nReturnPos++] = 'F'
        if (bStatus) {
            sReturn[nReturnPos] = 'F'
        } else {
            sReturn[nReturnPos] = '0'
        }
        return String(sReturn)
    }

    /**
     * Sends a Code Word
     *
     * @param codeWord /^[10FS]*$/ -> see getCodeWord
     */
    fun sendTriState(codeWord: String) {
        if (transmitterPin == null) return
        for (nRepeat in 0 until repeatTransmit) {
            for (i in 0 until codeWord.length) {
                when (codeWord[i]) {
                    '0' -> sendT0()
                    'F' -> sendTF()
                    '1' -> sendT1()
                }
            }
            sendSync()
        }
        transmitterPin.low()
    }

    /**
     * Sends a "Sync" Bit _ Waveform Protocol 1: |
     * |_______________________________ _ Waveform Protocol 2: | |__________
     */
    private fun sendSync() {
        this.transmit(protocol.syncBit)
    }

    /**
     * Sends a Tri-State "0" Bit _ _ Waveform: | |___| |___
     */
    private fun sendT0() {
        transmit(protocol.zeroBit)
        transmit(protocol.zeroBit)
    }

    /**
     * Sends a Tri-State "1" Bit ___ ___ Waveform: | |_| |_
     */
    private fun sendT1() {
        transmit(protocol.oneBit)
        transmit(protocol.oneBit)
    }

    /**
     * Sends a Tri-State "F" Bit _ ___ Waveform: | |___| |_
     */
    private fun sendTF() {
        transmit(protocol.zeroBit)
        transmit(protocol.oneBit)
    }

    private fun transmit(waveform: Waveform) {
        transmit(waveform.high, waveform.low)
    }

    private fun transmit(nHighPulses: Int, nLowPulses: Int) {
        adjustPinValue(true)
        Gpio.delayMicroseconds(protocol.pulseLength * nHighPulses.toLong())
        adjustPinValue(false)
        Gpio.delayMicroseconds(protocol.pulseLength * nLowPulses.toLong())
    }

    private fun adjustPinValue(value: Boolean) {
        if (protocol.isInvertedSignal xor value) {
            transmitterPin!!.high()
        } else {
            transmitterPin!!.low()
        }
    }

    fun setProtocol(protocol: Protocol) {
        this.protocol = protocol
    }

    companion object {
        /**
         * convenient method to convert a string like "11011" to a BitSet.
         *
         * @param address the string representation of the rc address
         * @return a bitset containing the address that can be used for
         * switchOn()/switchOff()
         */
        fun getSwitchGroupAddress(address: String): BitSet {
            require(address.length == 5) { "the switchGroupAddress must consist of exactly 5 bits!" }
            val bitSet = BitSet(5)
            for (i in 0..4) {
                bitSet[i] = address[i] == '1'
            }
            return bitSet
        }
    }

    init {
        val gpio = GpioFactory.getInstance()
        this.transmitterPin = gpio.provisionDigitalOutputPin(transmitterPin)
        this.protocol = protocol
    }
}
