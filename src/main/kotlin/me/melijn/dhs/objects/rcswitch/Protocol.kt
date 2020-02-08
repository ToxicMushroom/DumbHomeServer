package me.melijn.dhs.objects.rcswitch

class Protocol(val pulseLength: Int, val syncBit: Waveform, val zeroBit: Waveform, val oneBit: Waveform, val isInvertedSignal: Boolean) {

    companion object {
        val PROTOCOL_01 = Protocol(350, Waveform(1, 31), Waveform(1, 3), Waveform(3, 1), false) // protocol 1
        val PROTOCOL_433 = Protocol(430, Waveform(1, 31), Waveform(1, 3), Waveform(3, 1), false) // protocol 433
        val PROTOCOL_02 = Protocol(650, Waveform(1, 10), Waveform(1, 2), Waveform(2, 1), false) // protocol 2
        val PROTOCOL_03 = Protocol(100, Waveform(30, 71), Waveform(4, 11), Waveform(9, 6), false) // protocol 3
        val PROTOCOL_04 = Protocol(380, Waveform(1, 6), Waveform(1, 3), Waveform(3, 1), false) // protocol 4
        val PROTOCOL_05 = Protocol(500, Waveform(6, 14), Waveform(1, 2), Waveform(2, 1), false) // protocol 5
        val PROTOCOL_06 = Protocol(450, Waveform(23, 1), Waveform(1, 2), Waveform(2, 1), true) // protocol 6 (HT6P20B)
        val PROTOCOL_07 = Protocol(150, Waveform(2, 62), Waveform(1, 6), Waveform(6, 1), false) // protocol 7 (HS2303-PT, i. e. used in AUKEY Remote)
    }

}
