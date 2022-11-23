package org.feherdave.s7hwcfg.s7.system;

public class Address {

    public enum AddressType {
        /** Used for area lengths. */
        PLAIN,
        INPUT, OUTPUT, MEMORY, TIMER, COUNTER;
        String getPrefix() {
            switch (this.ordinal()) {
                case 1:
                    return "I";

                case 2:
                    return "Q";

                case 3:
                    return "M";

                case 4:
                    return "T";

                case 5:
                    return "C";

                default:
                    return "";
            }
        }
    }

    public enum AddressDataType {
        /** Used for timers and counters. **/
        ID_NUMBER,
        BIT, BYTE, WORD, DWORD;

        String getPrefix() {
            switch(this.ordinal()) {
                case 2: return "B";
                case 3: return "W";
                case 4: return "D";
                default: return "";
            }
        }
    }

    AddressType addressType;
    AddressDataType addressDataType;
    Integer addressByte;
    Integer addressBit;

    private Address(AddressType addressType) {
        this.addressType = addressType;
    }

    /** Creates a new input instance. */
    public static Address Input() {
        return new Address(AddressType.INPUT);
    }

    /** Creates a new output instance. */
    public static Address Output() {
        return new Address(AddressType.OUTPUT);
    }

    /** Creates a new timer instance. */
    public static Address Timer() {
        return new Address(AddressType.TIMER);
    }

    /** Creates a new memory instance. */
    public static Address Memory() {
        return new Address(AddressType.MEMORY);
    }

    /** Creates a new counter instance. */
    public static Address Counter() {
        return new Address(AddressType.COUNTER);
    }

    /** Creates a new plain instance. */
    public static Address Plain() {
        return new Address(AddressType.PLAIN);
    }

    /** Let this address represent a bit address. */
    public Address x(Integer addressByte, Integer addressBit) {
        this.addressDataType = AddressDataType.BIT;
        this.addressByte = addressByte;

        return this;
    }

    /** Let this address represent a byte address. */
    public Address b(Integer addressByte) {
        this.addressDataType = AddressDataType.BYTE;
        this.addressByte = addressByte;

        return this;
    }

    /** Let this address represent a word address. */
    public Address w(Integer addressWord) {
        this.addressDataType = AddressDataType.WORD;
        this.addressByte = addressWord;

        return this;
    }

    /** Let this address represent a dword address. */
    public Address dw(Integer addressDWord) {
        this.addressDataType = AddressDataType.DWORD;
        this.addressByte = addressDWord;

        return this;
    }

    /** Let this address represent a timer/counter. */
    public Address number(Integer number) {
        this.addressDataType = AddressDataType.ID_NUMBER;
        this.addressByte = number;

        return this;
    }


    @Override
    public String toString() {
        return "Address{" +
                addressType.getPrefix() +
                addressDataType.getPrefix() +
                addressByte +
                (addressBit != null ? "." + addressBit : "") +
                '}';
    }
}
