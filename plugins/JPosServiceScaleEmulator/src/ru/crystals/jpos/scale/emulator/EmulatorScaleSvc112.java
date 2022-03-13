package ru.crystals.jpos.scale.emulator;

import jpos.JposConst;
import jpos.JposException;
import jpos.ScaleConst;
import jpos.config.JposEntry;
import jpos.loader.JposServiceLoader;
import jpos.services.EventCallbacks;
import jpos.services.ScaleService112;

public class EmulatorScaleSvc112 implements ScaleService112, JposConst {

	protected static final int _iServiceVer = 1012000;
	protected boolean _fAutoDisable;
	protected boolean _fCapCompareFWVer;
	protected boolean _fCapStatsReporting;
	protected boolean _fCapUpdateFW;
	protected boolean _fCapUpdateStats;

	protected boolean _fdeviceEnabled;
	protected boolean _fClaimed;
	protected boolean _fOpened;
	protected String _strHealthText;
	protected String _strPhysicalDesc;
	protected String _strPhysicalName;
	protected String _strServiceDesc;

	protected int _iState;
	protected int _iCapPowerReporting;
	protected int _iPowerNotify;

	protected boolean _fasyncMode;

	protected JposEntry _jpe = null;
	protected ScaleConfig _jscaleConfig = new ScaleConfig();
	private Timer timer = new Timer();
	private Integer weight = null;

	public EmulatorScaleSvc112() {
		_fAutoDisable = false;
		_fCapCompareFWVer = false;
		_fCapStatsReporting = false;
		_fCapUpdateFW = false;
		_fCapUpdateStats = false;

		_fdeviceEnabled = false;
		_fClaimed = false;
		_fOpened = false;
		_strHealthText = "";

		_strPhysicalDesc = null;
		_strPhysicalName = null;
		_strServiceDesc = "UnifiedPOS Compatible Serial Scale Service Driver, (C) 2009 Crystal Service, Inc.";

		_iState = 1;
		_iCapPowerReporting = 0;
		_iPowerNotify = 0;

		_fasyncMode = false;

		_jpe = null;
	}

	@Override
	public boolean getAutoDisable() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _fAutoDisable;
	}

	@Override
	public void setAutoDisable(boolean flag) throws JposException {
		if (!_fOpened) {
			throw new JposException(101, "Service is not open");
		} else {
			_fAutoDisable = flag;
			return;
		}
	}

	@Override
	public boolean getCapCompareFirmwareVersion() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _fCapCompareFWVer;
	}

	@Override
	public int getCapPowerReporting() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _iCapPowerReporting;
	}

	@Override
	public boolean getCapStatisticsReporting() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _fCapStatsReporting;
	}

	@Override
	public boolean getCapUpdateFirmware() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _fCapUpdateFW;
	}

	@Override
	public boolean getCapUpdateStatistics() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _fCapUpdateStats;
	}

	@Override
	public String getCheckHealthText() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _strHealthText;
	}

	@Override
	public boolean getClaimed() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _fClaimed;
	}

	public int getDataCount() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public boolean getDataEventEnabled() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void setDataEventEnabled(boolean arg0) throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public boolean getDeviceEnabled() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _fdeviceEnabled;
	}

	@Override
	public void setDeviceEnabled(boolean flag) throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		if (!_fClaimed)
			throw new JposException(103, "Device is not claimed");

		_fdeviceEnabled = flag;
	}

	@Override
	public String getDeviceServiceDescription() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _strServiceDesc;
	}

	@Override
	public int getDeviceServiceVersion() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _iServiceVer;
	}

	@Override
	public boolean getFreezeEvents() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void setFreezeEvents(boolean arg0) throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public String getPhysicalDeviceDescription() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _strPhysicalDesc;
	}

	@Override
	public String getPhysicalDeviceName() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _strPhysicalName;
	}

	@Override
	public int getPowerNotify() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return _iPowerNotify;
	}

	@Override
	public void setPowerNotify(int i) throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		if (_fdeviceEnabled)
			throw new JposException(106, "Device is already enabled");
		if (_iCapPowerReporting == 0 && i != 0) {
			throw new JposException(106,
					"Service does not support power notifications");
		} else {
			_iPowerNotify = i;
			return;
		}
	}

	@Override
	public int getPowerState() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public int getState() throws JposException {
		return _iState;
	}

	@Override
	public void checkHealth(int i) throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		if (!_fClaimed)
			throw new JposException(103, "Device is not claimed");

		if (!_fdeviceEnabled)
			throw new JposException(105, "Device is not enabled");
		if (i != 1) {
			throw new JposException(106,
					"Device only supports internal health checks");
		} else {
			_strHealthText = "Internal HCheck: Successful";
			return;
		}
	}

	@Override
	public void clearInput() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void compareFirmwareVersion(String s, int ai[]) throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void directIO(int i, int ai[], Object obj) throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void resetStatistics(String s) throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void retrieveStatistics(String as[]) throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void updateFirmware(String s) throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void updateStatistics(String s) throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void deleteInstance() throws JposException {
	}

	@Override
	public boolean getCapStatusUpdate() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public int getScaleLiveWeight() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public int getStatusNotify() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void setStatusNotify(int arg0) throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void displayText(String arg0) throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	// ******************************

	@Override
	public boolean getAsyncMode() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return false;
	}

	@Override
	public boolean getCapDisplayText() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return false;
	}

	@Override
	public boolean getCapPriceCalculating() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return false;
	}

	@Override
	public boolean getCapTareWeight() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return false;
	}

	@Override
	public boolean getCapZeroScale() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return false;
	}

	@Override
	public int getMaxDisplayTextChars() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public long getSalesPrice() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public int getTareWeight() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public long getUnitPrice() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void setAsyncMode(boolean arg0) throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void setTareWeight(int arg0) throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void setUnitPrice(long arg0) throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public void zeroScale() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public boolean getCapDisplay() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		else
			return false;
	}

	@Override
	public int getMaximumWeight() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}

	@Override
	public int getWeightUnit() throws JposException {
		return ScaleConst.SCAL_WU_GRAM;
	}

	@Override
	public void readWeight(int[] result, int msTimeOut) throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		if (!_fClaimed)
			throw new JposException(103, "Device is not claimed");
		if (!_fdeviceEnabled)
			throw new JposException(105, "Device is not enabled");

		try {
			if((weight == null) || timer.isOverflow()) {
				weight = (int)(_jscaleConfig.getMinWeight() + (int)(Math.random()*_jscaleConfig.getMaxWeight()));
				timer.reset();
			}
			result[0] = weight;
		} catch (Exception e) {
			e.printStackTrace();
			throw new JposException(JPOS_E_FAILURE,
					"Unhandled exception from Device Service", e);
		}
	}

	@Override
	public void open(String logicalName, EventCallbacks arg1)
			throws JposException {
		if (_fOpened)
			throw new JposException(106, "Service is already open");
		_jpe = JposServiceLoader.getManager().getEntryRegistry().getJposEntry(
				logicalName);
		if (_jpe == null)
			throw new JposException(109, "Logical device could not be found");

		_fAutoDisable = false;
		_fCapCompareFWVer = false;
		_fCapStatsReporting = false;
		_fCapUpdateFW = false;
		_fCapUpdateStats = false;

		_fdeviceEnabled = false;
		_strHealthText = "";
		_iPowerNotify = 0;

		_strPhysicalDesc = "";

		jpos.config.JposEntry.Prop prop = _jpe.getProp("productDescription");
		if (prop != null)
			_strPhysicalDesc = prop.getValueAsString();
		if (_strPhysicalDesc == "")
			_strPhysicalDesc = "Digi Serial Scale";
		_strPhysicalName = "";

		prop = _jpe.getProp("productName");
		if (prop != null)
			_strPhysicalName = prop.getValueAsString();
		if (_strPhysicalName == "")
			_strPhysicalName = "Emulator Scale";

		prop = _jpe.getProp("delayWeightChanges");
		if (prop != null) {
			Long value = Long.parseLong(prop.getValueAsString());
			if (value != null) {
				_jscaleConfig.setDelayWeightChanges(value);
			}
		}
		
		prop = _jpe.getProp("maxWeight");
		if (prop != null) {
			Long value = Long.parseLong(prop.getValueAsString());
			if (value != null) {
				_jscaleConfig.setMaxWeight(value);
			}
		}
		
		prop = _jpe.getProp("minWeight");
		if (prop != null) {
			Long value = Long.parseLong(prop.getValueAsString());
			if (value != null) {
				_jscaleConfig.setMinWeight(value);
			}
		}
		
		_fOpened = true;
		_iState = 2;
	}

	@Override
	public void claim(int timeOut) throws JposException {
		if (timeOut < -1)
			throw new JposException(106, "Invalid timeout value");

		if (!_fOpened)
			throw new JposException(101, "Service is not open");
		
		if (_fClaimed)
			throw new JposException(101, "Service is already claimed");

		_fClaimed = true;
	}

	@Override
	public void close() throws JposException {
		if (!_fOpened)
			throw new JposException(101, "Service is not open");

		_fOpened = false;
		_iState = 1;
	}

	@Override
	public void release() throws JposException {
		_fClaimed = false;
	}

	public boolean getCapServiceAllowManagement() throws JposException {
		throw new JposException(106, "Method is not supported by this service");
	}
}
