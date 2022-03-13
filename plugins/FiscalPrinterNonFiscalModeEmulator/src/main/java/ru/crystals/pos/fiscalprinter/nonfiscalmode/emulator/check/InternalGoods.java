package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.check;

import java.util.ArrayList;
import java.util.List;

public class InternalGoods {
	public long posNumber;
	public String goodsName;
	public long quantity;
	public long price;
	public long sum;
	public long departNumber;

	List<InternalDisc> discs = new ArrayList<>();
	List<InternalMargin> margins = new ArrayList<>();
}
