package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import ru.crystals.pos.CashErrorType;

public class ShtrihErrorMsg {
	
	public static CashErrorType getErrorType() {
		return CashErrorType.FISCAL_ERROR;
	}
	
	public static CashErrorType getErrorType(int errorCode) {
		switch (errorCode) {			
	        case 0x01: return CashErrorType.FATAL_ERROR;
	        case 0x02: return CashErrorType.FATAL_ERROR;
	        case 0x03: return CashErrorType.FATAL_ERROR;
	        case 0x04: return CashErrorType.FATAL_ERROR;
	        case 0x05: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x06: return CashErrorType.FATAL_ERROR;
	        case 0x07: return CashErrorType.FATAL_ERROR;
	        case 0x08: return CashErrorType.FATAL_ERROR;
	        case 0x09: return CashErrorType.FATAL_ERROR;
	        case 0x0A: return CashErrorType.FATAL_ERROR;
	        case 0x0B: return CashErrorType.FATAL_ERROR;
	        
	        case 0x11: return CashErrorType.FATAL_ERROR;
	        case 0x12: return CashErrorType.FATAL_ERROR;
	        case 0x13: return CashErrorType.FATAL_ERROR;
	        case 0x14: return CashErrorType.FATAL_ERROR;
	        case 0x15: return CashErrorType.SHIFT_OPERATION_NEED;
	        case 0x16: return CashErrorType.SHIFT_OPERATION_NEED;
	        case 0x17: return CashErrorType.FATAL_ERROR;
	        case 0x18: return CashErrorType.FATAL_ERROR;
	        case 0x19: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x1A: return CashErrorType.FATAL_ERROR;
	        case 0x1B: return CashErrorType.FATAL_ERROR;
	        case 0x1C: return CashErrorType.FATAL_ERROR;
	        case 0x1D: return CashErrorType.FATAL_ERROR;	        
	        case 0x1F: return CashErrorType.FATAL_ERROR;
	        
	        case 0x20: return CashErrorType.FATAL_ERROR;
	        case 0x21: return CashErrorType.FATAL_ERROR;
	        case 0x22: return CashErrorType.FATAL_ERROR;
	        case 0x23: return CashErrorType.FATAL_ERROR;
	        case 0x24: return CashErrorType.FATAL_ERROR;
	        case 0x25: return CashErrorType.FATAL_ERROR;
	        case 0x2F: return CashErrorType.FATAL_ERROR;
	        
	        case 0x30: return CashErrorType.FATAL_ERROR;
	        case 0x31: return CashErrorType.FATAL_ERROR;
	        case 0x32: return CashErrorType.FATAL_ERROR;
	        case 0x33: return CashErrorType.NOT_CRITICAL_ERROR;	        
	        case 0x35: return CashErrorType.FATAL_ERROR;
	        case 0x36: return CashErrorType.FATAL_ERROR;
	        case 0x37: return CashErrorType.FATAL_ERROR;
	        case 0x38: return CashErrorType.FATAL_ERROR;
	        case 0x39: return CashErrorType.FATAL_ERROR;
	        case 0x3A: return CashErrorType.FATAL_ERROR;
	        case 0x3B: return CashErrorType.FATAL_ERROR;
	        case 0x3C: return CashErrorType.FATAL_ERROR;
	        case 0x3D: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x3E: return CashErrorType.FATAL_ERROR;
	        case 0x3F: return CashErrorType.FATAL_ERROR;
	        
	        case 0x40: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x41: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x42: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x43: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x44: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x45: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x46: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x47: return CashErrorType.FATAL_ERROR;
	        case 0x48: return CashErrorType.FATAL_ERROR;
	        case 0x49: return CashErrorType.FATAL_ERROR;
	        case 0x4A: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x4B: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x4C: return CashErrorType.FATAL_ERROR;
	        case 0x4D: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x4E: return CashErrorType.SHIFT_OPERATION_NEED;
	        case 0x4F: return CashErrorType.FATAL_ERROR;
	        
	        case 0x50: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x51: return CashErrorType.FATAL_ERROR;
	        case 0x52: return CashErrorType.FATAL_ERROR;
	        case 0x53: return CashErrorType.FATAL_ERROR;
	        case 0x54: return CashErrorType.FATAL_ERROR;
	        case 0x56: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x57: return CashErrorType.FATAL_ERROR;
	        case 0x58: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x59: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x5A: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x5B: return CashErrorType.FATAL_ERROR;
	        case 0x5C: return CashErrorType.FATAL_ERROR;
	        case 0x5D: return CashErrorType.FATAL_ERROR;
	        case 0x5E: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x5F: return CashErrorType.FATAL_ERROR;
	        
	        case 0x60: return CashErrorType.FATAL_ERROR;
	        case 0x61: return CashErrorType.FATAL_ERROR;
	        case 0x62: return CashErrorType.FATAL_ERROR;
	        case 0x63: return CashErrorType.FATAL_ERROR;
	        case 0x64: return CashErrorType.FATAL_ERROR;
	        case 0x65: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x66: return CashErrorType.FATAL_ERROR;
	        case 0x67: return CashErrorType.NEED_RESTART;
	        case 0x68: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x69: return CashErrorType.FATAL_ERROR;
	        case 0x6A: return CashErrorType.FATAL_ERROR;
	        case 0x6B: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x6C: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x6D: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x6E: return CashErrorType.FATAL_ERROR;
	        case 0x6F: return CashErrorType.FATAL_ERROR;
	        
	        case 0x70: return CashErrorType.FATAL_ERROR;
	        case 0x71: return CashErrorType.FATAL_ERROR;
	        case 0x72: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x73: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x74: return CashErrorType.FATAL_ERROR;
	        case 0x75: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x76: return CashErrorType.FATAL_ERROR;
	        case 0x77: return CashErrorType.FATAL_ERROR;
	        case 0x78: return CashErrorType.FATAL_ERROR;
	        case 0x79: return CashErrorType.FATAL_ERROR;
	        case 0x7A: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x7B: return CashErrorType.FATAL_ERROR;
	        case 0x7C: return CashErrorType.FATAL_ERROR;
	        case 0x7D: return CashErrorType.FATAL_ERROR;
	        case 0x7E: return CashErrorType.FATAL_ERROR;
	        case 0x7F: return CashErrorType.FATAL_ERROR;
	        
	        case 0x80: return CashErrorType.NEED_RESTART;
	        case 0x81: return CashErrorType.NEED_RESTART;
	        case 0x82: return CashErrorType.NEED_RESTART;
	        case 0x83: return CashErrorType.NEED_RESTART;
	        case 0x84: return CashErrorType.FATAL_ERROR;
	        case 0x85: return CashErrorType.FATAL_ERROR;
	        case 0x86: return CashErrorType.FATAL_ERROR;
	        case 0x87: return CashErrorType.FATAL_ERROR;
	        case 0x88: return CashErrorType.FATAL_ERROR;
	        case 0x89: return CashErrorType.FATAL_ERROR;
	        case 0x8A: return CashErrorType.FATAL_ERROR;
	        case 0x8B: return CashErrorType.FATAL_ERROR;
	        case 0x8C: return CashErrorType.FATAL_ERROR;
	        case 0x8D: return CashErrorType.FATAL_ERROR;
	        case 0x8E: return CashErrorType.FATAL_ERROR;
	        case 0x8F: return CashErrorType.FATAL_ERROR;
	        
	        case 0x90: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0x91: return CashErrorType.FATAL_ERROR;
	        case 0x92: return CashErrorType.FATAL_ERROR;
	        case 0x93: return CashErrorType.NOT_CRITICAL_ERROR;
	        
	        case 0xA0: return CashErrorType.FATAL_ERROR;
	        case 0xA1: return CashErrorType.FATAL_ERROR;
	        case 0xA2: return CashErrorType.FATAL_ERROR;
	        case 0xA3: return CashErrorType.FATAL_ERROR;
	        case 0xA4: return CashErrorType.FATAL_ERROR;
	        case 0xA5: return CashErrorType.FATAL_ERROR;
	        case 0xA6: return CashErrorType.FATAL_ERROR;
	        case 0xA7: return CashErrorType.FATAL_ERROR;
	        case 0xA8: return CashErrorType.FATAL_ERROR;
	        case 0xA9: return CashErrorType.FATAL_ERROR;
	        case 0xAA: return CashErrorType.FATAL_ERROR;
	        
	        case 0xB0: return CashErrorType.FATAL_ERROR;
	        case 0xB1: return CashErrorType.FATAL_ERROR;
	        case 0xB2: return CashErrorType.FATAL_ERROR;
	        
	        case 0xC0: return CashErrorType.FATAL_ERROR;
	        case 0xC1: return CashErrorType.FATAL_ERROR;
	        case 0xC2: return CashErrorType.FATAL_ERROR;
	        case 0xC3: return CashErrorType.FATAL_ERROR;
	        case 0xC4: return CashErrorType.FATAL_ERROR;
	        case 0xC5: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0xC6: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0xC7: return CashErrorType.NOT_CRITICAL_ERROR;
	        case 0xC8: return CashErrorType.FATAL_ERROR;
	        
	        case 0xD1: return CashErrorType.NOT_CRITICAL_ERROR;
            
            case 0xE0: return CashErrorType.NEED_RESTART;
            case 0xE1: return CashErrorType.NOT_CRITICAL_ERROR;
            case 0xE2: return CashErrorType.FATAL_ERROR;
            case 0xE3: return CashErrorType.FATAL_ERROR;
            case 0xE4: return CashErrorType.NOT_CRITICAL_ERROR;
	        
			default: return CashErrorType.NOT_CRITICAL_ERROR;
		}
	}

}
