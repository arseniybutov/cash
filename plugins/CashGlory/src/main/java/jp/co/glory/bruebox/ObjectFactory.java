
package jp.co.glory.bruebox;

import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the jp.co.glory.bruebox package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ChangeResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ChangeResponse");
    private final static QName _CST1_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CST1");
    private final static QName _CST3_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CST3");
    private final static QName _MAINAP_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "MAIN_AP");
    private final static QName _CST2_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CST2");
    private final static QName _UnLockUnitRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UnLockUnitRequest");
    private final static QName _OccupyRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "OccupyRequest");
    private final static QName _CVCOUNTRY_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CV_COUNTRY");
    private final static QName _PLD1_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "PLD1");
    private final static QName _BVPLD2_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BVPLD2");
    private final static QName _PLD2_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "PLD2");
    private final static QName _CounterClearResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CounterClearResponse");
    private final static QName _CashinCancelResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CashinCancelResponse");
    private final static QName _StatusResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "StatusResponse");
    private final static QName _LogoutUserResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "LogoutUserResponse");
    private final static QName _CST4_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CST4");
    private final static QName _CST5_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CST5");
    private final static QName _CST6_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CST6");
    private final static QName _DisableDenomResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "DisableDenomResponse");
    private final static QName _CST7_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CST7");
    private final static QName _Port_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "Port");
    private final static QName _OpenRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "OpenRequest");
    private final static QName _CST8_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CST8");
    private final static QName _ReturnCashRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ReturnCashRequest");
    private final static QName _RomVersionRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "RomVersionRequest");
    private final static QName _UNGENERAL_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UN_GENERAL");
    private final static QName _ReturnCashResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ReturnCashResponse");
    private final static QName _DisableDenomRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "DisableDenomRequest");
    private final static QName _UnRegisterEventRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UnRegisterEventRequest");
    private final static QName _LoginUserResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "LoginUserResponse");
    private final static QName _BVControl_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BVControl");
    private final static QName _EnableDenomRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "EnableDenomRequest");
    private final static QName _BVSetting_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BVSetting");
    private final static QName _EventOfflineRecoveryRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "EventOfflineRecoveryRequest");
    private final static QName _RomVersionResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "RomVersionResponse");
    private final static QName _User_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "User");
    private final static QName _CUTE_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CUTE");
    private final static QName _BVDownload_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BVDownload");
    private final static QName _StartCashinRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "StartCashinRequest");
    private final static QName _BVDL_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BV_DL");
    private final static QName _ChangeCancelResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ChangeCancelResponse");
    private final static QName _SetExchangeRateRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "SetExchangeRateRequest");
    private final static QName _LockUnitResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "LockUnitResponse");
    private final static QName _PowerControlRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "PowerControlRequest");
    private final static QName _EndReplenishmentFromEntranceResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "EndReplenishmentFromEntranceResponse");
    private final static QName _Url_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "Url");
    private final static QName _StartDownloadResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "StartDownloadResponse");
    private final static QName _CashinCancelRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CashinCancelRequest");
    private final static QName _FPGA_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "FPGA");
    private final static QName _ReleaseRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ReleaseRequest");
    private final static QName _SessionID_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "SessionID");
    private final static QName _ESCROW_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ESCROW");
    private final static QName _StatusRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "StatusRequest");
    private final static QName _OccupyResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "OccupyResponse");
    private final static QName _ReleaseResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ReleaseResponse");
    private final static QName _StartDownload_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "StartDownload");
    private final static QName _COLLECT_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "COLLECT");
    private final static QName _RefreshSalesTotalResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "RefreshSalesTotalResponse");
    private final static QName _EventOfflineRecoveryResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "EventOfflineRecoveryResponse");
    private final static QName _CloseExitCoverResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CloseExitCoverResponse");
    private final static QName _UNCC_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UN_CC");
    private final static QName _Code_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "Code");
    private final static QName _InventoryRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "InventoryRequest");
    private final static QName _ReplenishmentFromEntranceCancelResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ReplenishmentFromEntranceCancelResponse");
    private final static QName _BVGENERAL2_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BV_GENERAL2");
    private final static QName _BVGENERAL1_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BV_GENERAL1");
    private final static QName _OpenExitCoverRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "OpenExitCoverRequest");
    private final static QName _LogoutUserRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "LogoutUserRequest");
    private final static QName _CollectRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CollectRequest");
    private final static QName _ReplenishmentFromEntranceCancelRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ReplenishmentFromEntranceCancelRequest");
    private final static QName _LOWAPL_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "LOW_APL");
    private final static QName _Boot_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "Boot");
    private final static QName _UpdateSettingFileRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UpdateSettingFileRequest");
    private final static QName _BVFormat_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BVFormat");
    private final static QName _EventNotificationStatusResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "EventNotificationStatusResponse");
    private final static QName _UpdateManualDepositTotalResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UpdateManualDepositTotalResponse");
    private final static QName _FPGA1_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "FPGA1");
    private final static QName _FPGA2_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "FPGA2");
    private final static QName _SPECINFO_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "SPEC_INFO");
    private final static QName _AdjustTimeRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "AdjustTimeRequest");
    private final static QName _BVPARAM2_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BV_PARAM2");
    private final static QName _DeviceName_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "DeviceName");
    private final static QName _Amount_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "Amount");
    private final static QName _StartLogread_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "StartLogread");
    private final static QName _AP_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "AP");
    private final static QName _Id_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "Id");
    private final static QName _StartReplenishmentFromEntranceRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "StartReplenishmentFromEntranceRequest");
    private final static QName _BVPARAM1_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BV_PARAM1");
    private final static QName _LockUnitRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "LockUnitRequest");
    private final static QName _BVPLD_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BV_PLD");
    private final static QName _SeqNo_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "SeqNo");
    private final static QName _OpenExitCoverResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "OpenExitCoverResponse");
    private final static QName _BVCONTROL_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BV_CONTROL");
    private final static QName _RegisterEventRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "RegisterEventRequest");
    private final static QName _CVAP_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CV_AP");
    private final static QName _OpenResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "OpenResponse");
    private final static QName _CashoutRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CashoutRequest");
    private final static QName _PowerControlResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "PowerControlResponse");
    private final static QName _UnRegisterEventResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UnRegisterEventResponse");
    private final static QName _ManualDeposit_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ManualDeposit");
    private final static QName _EndCashinRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "EndCashinRequest");
    private final static QName _UPAPL_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UP_APL");
    private final static QName _RegisterEventResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "RegisterEventResponse");
    private final static QName _CloseRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CloseRequest");
    private final static QName _Piece_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "Piece");
    private final static QName _UNCMB_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UN_CMB");
    private final static QName _CashoutResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CashoutResponse");
    private final static QName _CounterClearRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CounterClearRequest");
    private final static QName _StartReplenishmentFromCassetteRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "StartReplenishmentFromCassetteRequest");
    private final static QName _UpdateManualDepositTotalRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UpdateManualDepositTotalRequest");
    private final static QName _StartReplenishmentFromEntranceResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "StartReplenishmentFromEntranceResponse");
    private final static QName _IPL_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "IPL");
    private final static QName _CloseResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CloseResponse");
    private final static QName _ChangeCancelRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ChangeCancelRequest");
    private final static QName _UnLockUnitResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UnLockUnitResponse");
    private final static QName _ChangeRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ChangeRequest");
    private final static QName _CollectResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CollectResponse");
    private final static QName _EndCashinResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "EndCashinResponse");
    private final static QName _EventNotificationStatusRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "EventNotificationStatusRequest");
    private final static QName _EnableDenomResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "EnableDenomResponse");
    private final static QName _EndReplenishmentFromCassetteResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "EndReplenishmentFromCassetteResponse");
    private final static QName _Status_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "Status");
    private final static QName _EndReplenishmentFromEntranceRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "EndReplenishmentFromEntranceRequest");
    private final static QName _EndReplenishmentFromCassetteRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "EndReplenishmentFromCassetteRequest");
    private final static QName _SetExchangeRateResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "SetExchangeRateResponse");
    private final static QName _CloseExitCoverRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "CloseExitCoverRequest");
    private final static QName _BVSET_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BV_SET");
    private final static QName _UserPwd_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UserPwd");
    private final static QName _LoginUserRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "LoginUserRequest");
    private final static QName _StartCashinResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "StartCashinResponse");
    private final static QName _BVAP_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BV_AP");
    private final static QName _UNSERIAL_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UN_SERIAL");
    private final static QName _ComStatus_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ComStatus");
    private final static QName _UpdateSettingFileResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UpdateSettingFileResponse");
    private final static QName _BV_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BV");
    private final static QName _ResetRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ResetRequest");
    private final static QName _InventoryResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "InventoryResponse");
    private final static QName _StartLogreadResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "StartLogreadResponse");
    private final static QName _AdjustTimeResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "AdjustTimeResponse");
    private final static QName _RefreshSalesTotalRequest_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "RefreshSalesTotalRequest");
    private final static QName _SerialNo_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "SerialNo");
    private final static QName _ResetResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "ResetResponse");
    private final static QName _UNFUNC_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "UN_FUNC");
    private final static QName _StartReplenishmentFromCassetteResponse_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "StartReplenishmentFromCassetteResponse");
    private final static QName _BVFORMAT_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "BV_FORMAT");
    private final static QName _COLLECTSERIAL_QNAME = new QName("http://www.glory.co.jp/bruebox.xsd", "COLLECT_SERIAL");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: jp.co.glory.bruebox
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RequireEventListType }
     * 
     */
    public RequireEventListType createRequireEventListType() {
        return new RequireEventListType();
    }

    /**
     * Create an instance of {@link EventNotificationStatusRequestType }
     * 
     */
    public EventNotificationStatusRequestType createEventNotificationStatusRequestType() {
        return new EventNotificationStatusRequestType();
    }

    /**
     * Create an instance of {@link DelayOptionType }
     * 
     */
    public DelayOptionType createDelayOptionType() {
        return new DelayOptionType();
    }

    /**
     * Create an instance of {@link RequireVerifyMixStackerInfosType }
     * 
     */
    public RequireVerifyMixStackerInfosType createRequireVerifyMixStackerInfosType() {
        return new RequireVerifyMixStackerInfosType();
    }

    /**
     * Create an instance of {@link StartDownloadResponseType }
     * 
     */
    public StartDownloadResponseType createStartDownloadResponseType() {
        return new StartDownloadResponseType();
    }

    /**
     * Create an instance of {@link StatusOptionType }
     * 
     */
    public StatusOptionType createStatusOptionType() {
        return new StatusOptionType();
    }

    /**
     * Create an instance of {@link RequireVerifyInfosType }
     * 
     */
    public RequireVerifyInfosType createRequireVerifyInfosType() {
        return new RequireVerifyInfosType();
    }

    /**
     * Create an instance of {@link RBW10RomVerType }
     * 
     */
    public RBW10RomVerType createRBW10RomVerType() {
        return new RBW10RomVerType();
    }

    /**
     * Create an instance of {@link RZ50RomVerType }
     * 
     */
    public RZ50RomVerType createRZ50RomVerType() {
        return new RZ50RomVerType();
    }

    /**
     * Create an instance of {@link CounterClearResponseType }
     * 
     */
    public CounterClearResponseType createCounterClearResponseType() {
        return new CounterClearResponseType();
    }

    /**
     * Create an instance of {@link EventNotificationStatusResponseType }
     * 
     */
    public EventNotificationStatusResponseType createEventNotificationStatusResponseType() {
        return new EventNotificationStatusResponseType();
    }

    /**
     * Create an instance of {@link RequireVerifyCollectionContainerType }
     * 
     */
    public RequireVerifyCollectionContainerType createRequireVerifyCollectionContainerType() {
        return new RequireVerifyCollectionContainerType();
    }

    /**
     * Create an instance of {@link RequireVerifyCollectionContainerInfosType }
     * 
     */
    public RequireVerifyCollectionContainerInfosType createRequireVerifyCollectionContainerInfosType() {
        return new RequireVerifyCollectionContainerInfosType();
    }

    /**
     * Create an instance of {@link OpenExitCoverRequestType }
     * 
     */
    public OpenExitCoverRequestType createOpenExitCoverRequestType() {
        return new OpenExitCoverRequestType();
    }

    /**
     * Create an instance of {@link RequireEventType }
     * 
     */
    public RequireEventType createRequireEventType() {
        return new RequireEventType();
    }

    /**
     * Create an instance of {@link RegisterEventResponseType }
     * 
     */
    public RegisterEventResponseType createRegisterEventResponseType() {
        return new RegisterEventResponseType();
    }

    /**
     * Create an instance of {@link EndCashinRequestType }
     * 
     */
    public EndCashinRequestType createEndCashinRequestType() {
        return new EndCashinRequestType();
    }

    /**
     * Create an instance of {@link LogoutUserResponseType }
     * 
     */
    public LogoutUserResponseType createLogoutUserResponseType() {
        return new LogoutUserResponseType();
    }

    /**
     * Create an instance of {@link StartReplenishmentFromEntranceRequestType }
     * 
     */
    public StartReplenishmentFromEntranceRequestType createStartReplenishmentFromEntranceRequestType() {
        return new StartReplenishmentFromEntranceRequestType();
    }

    /**
     * Create an instance of {@link RomVersionRequestType }
     * 
     */
    public RomVersionRequestType createRomVersionRequestType() {
        return new RomVersionRequestType();
    }

    /**
     * Create an instance of {@link StartReplenishmentFromEntranceResponseType }
     * 
     */
    public StartReplenishmentFromEntranceResponseType createStartReplenishmentFromEntranceResponseType() {
        return new StartReplenishmentFromEntranceResponseType();
    }

    /**
     * Create an instance of {@link CashUnitType }
     * 
     */
    public CashUnitType createCashUnitType() {
        return new CashUnitType();
    }

    /**
     * Create an instance of {@link CloseExitCoverRequestType }
     * 
     */
    public CloseExitCoverRequestType createCloseExitCoverRequestType() {
        return new CloseExitCoverRequestType();
    }

    /**
     * Create an instance of {@link LockUnitOptionType }
     * 
     */
    public LockUnitOptionType createLockUnitOptionType() {
        return new LockUnitOptionType();
    }

    /**
     * Create an instance of {@link EventNotificationStatusType }
     * 
     */
    public EventNotificationStatusType createEventNotificationStatusType() {
        return new EventNotificationStatusType();
    }

    /**
     * Create an instance of {@link StartCashinResponseType }
     * 
     */
    public StartCashinResponseType createStartCashinResponseType() {
        return new StartCashinResponseType();
    }

    /**
     * Create an instance of {@link DepositCurrencyType }
     * 
     */
    public DepositCurrencyType createDepositCurrencyType() {
        return new DepositCurrencyType();
    }

    /**
     * Create an instance of {@link RBW100RomVerType }
     * 
     */
    public RBW100RomVerType createRBW100RomVerType() {
        return new RBW100RomVerType();
    }

    /**
     * Create an instance of {@link SetExchangeRateRequestType }
     * 
     */
    public SetExchangeRateRequestType createSetExchangeRateRequestType() {
        return new SetExchangeRateRequestType();
    }

    /**
     * Create an instance of {@link EndReplenishmentFromCassetteResponseType }
     * 
     */
    public EndReplenishmentFromCassetteResponseType createEndReplenishmentFromCassetteResponseType() {
        return new EndReplenishmentFromCassetteResponseType();
    }

    /**
     * Create an instance of {@link ResetResponseType }
     * 
     */
    public ResetResponseType createResetResponseType() {
        return new ResetResponseType();
    }

    /**
     * Create an instance of {@link CashoutResponseType }
     * 
     */
    public CashoutResponseType createCashoutResponseType() {
        return new CashoutResponseType();
    }

    /**
     * Create an instance of {@link StatusType }
     * 
     */
    public StatusType createStatusType() {
        return new StatusType();
    }

    /**
     * Create an instance of {@link EndCashinResponseType }
     * 
     */
    public EndCashinResponseType createEndCashinResponseType() {
        return new EndCashinResponseType();
    }

    /**
     * Create an instance of {@link ResetRequestType }
     * 
     */
    public ResetRequestType createResetRequestType() {
        return new ResetRequestType();
    }

    /**
     * Create an instance of {@link RZ100RomVerType }
     * 
     */
    public RZ100RomVerType createRZ100RomVerType() {
        return new RZ100RomVerType();
    }

    /**
     * Create an instance of {@link RequireVerifyMixStackerType }
     * 
     */
    public RequireVerifyMixStackerType createRequireVerifyMixStackerType() {
        return new RequireVerifyMixStackerType();
    }

    /**
     * Create an instance of {@link ReturnCashRequestType }
     * 
     */
    public ReturnCashRequestType createReturnCashRequestType() {
        return new ReturnCashRequestType();
    }

    /**
     * Create an instance of {@link CounterClearOptionType }
     * 
     */
    public CounterClearOptionType createCounterClearOptionType() {
        return new CounterClearOptionType();
    }

    /**
     * Create an instance of {@link RomVersionResponseType }
     * 
     */
    public RomVersionResponseType createRomVersionResponseType() {
        return new RomVersionResponseType();
    }

    /**
     * Create an instance of {@link ReturnCashResponseType }
     * 
     */
    public ReturnCashResponseType createReturnCashResponseType() {
        return new ReturnCashResponseType();
    }

    /**
     * Create an instance of {@link OpenRequestType }
     * 
     */
    public OpenRequestType createOpenRequestType() {
        return new OpenRequestType();
    }

    /**
     * Create an instance of {@link StartDownloadRequestType }
     * 
     */
    public StartDownloadRequestType createStartDownloadRequestType() {
        return new StartDownloadRequestType();
    }

    /**
     * Create an instance of {@link AdjustTimeDateType }
     * 
     */
    public AdjustTimeDateType createAdjustTimeDateType() {
        return new AdjustTimeDateType();
    }

    /**
     * Create an instance of {@link EnableDenomRequestType }
     * 
     */
    public EnableDenomRequestType createEnableDenomRequestType() {
        return new EnableDenomRequestType();
    }

    /**
     * Create an instance of {@link EventOfflineRecoveryResponseType }
     * 
     */
    public EventOfflineRecoveryResponseType createEventOfflineRecoveryResponseType() {
        return new EventOfflineRecoveryResponseType();
    }

    /**
     * Create an instance of {@link EndReplenishmentFromCassetteRequestType }
     * 
     */
    public EndReplenishmentFromCassetteRequestType createEndReplenishmentFromCassetteRequestType() {
        return new EndReplenishmentFromCassetteRequestType();
    }

    /**
     * Create an instance of {@link UnLockUnitResponseType }
     * 
     */
    public UnLockUnitResponseType createUnLockUnitResponseType() {
        return new UnLockUnitResponseType();
    }

    /**
     * Create an instance of {@link EventOfflineRecoveryRequestType }
     * 
     */
    public EventOfflineRecoveryRequestType createEventOfflineRecoveryRequestType() {
        return new EventOfflineRecoveryRequestType();
    }

    /**
     * Create an instance of {@link UpdateType }
     * 
     */
    public UpdateType createUpdateType() {
        return new UpdateType();
    }

    /**
     * Create an instance of {@link RefreshSalesTotalResponseType }
     * 
     */
    public RefreshSalesTotalResponseType createRefreshSalesTotalResponseType() {
        return new RefreshSalesTotalResponseType();
    }

    /**
     * Create an instance of {@link DevStatusType }
     * 
     */
    public DevStatusType createDevStatusType() {
        return new DevStatusType();
    }

    /**
     * Create an instance of {@link EndReplenishmentFromEntranceRequestType }
     * 
     */
    public EndReplenishmentFromEntranceRequestType createEndReplenishmentFromEntranceRequestType() {
        return new EndReplenishmentFromEntranceRequestType();
    }

    /**
     * Create an instance of {@link CollectRequestType }
     * 
     */
    public CollectRequestType createCollectRequestType() {
        return new CollectRequestType();
    }

    /**
     * Create an instance of {@link RequireVerifyDenominationType }
     * 
     */
    public RequireVerifyDenominationType createRequireVerifyDenominationType() {
        return new RequireVerifyDenominationType();
    }

    /**
     * Create an instance of {@link LoginUserResponseType }
     * 
     */
    public LoginUserResponseType createLoginUserResponseType() {
        return new LoginUserResponseType();
    }

    /**
     * Create an instance of {@link LogoutUserRequestType }
     * 
     */
    public LogoutUserRequestType createLogoutUserRequestType() {
        return new LogoutUserRequestType();
    }

    /**
     * Create an instance of {@link ReplenishmentFromEntranceCancelRequestType }
     * 
     */
    public ReplenishmentFromEntranceCancelRequestType createReplenishmentFromEntranceCancelRequestType() {
        return new ReplenishmentFromEntranceCancelRequestType();
    }

    /**
     * Create an instance of {@link CloseRequestType }
     * 
     */
    public CloseRequestType createCloseRequestType() {
        return new CloseRequestType();
    }

    /**
     * Create an instance of {@link StatusRequestType }
     * 
     */
    public StatusRequestType createStatusRequestType() {
        return new StatusRequestType();
    }

    /**
     * Create an instance of {@link EndReplenishmentFromEntranceResponseType }
     * 
     */
    public EndReplenishmentFromEntranceResponseType createEndReplenishmentFromEntranceResponseType() {
        return new EndReplenishmentFromEntranceResponseType();
    }

    /**
     * Create an instance of {@link StartReplenishmentFromCassetteResponseType }
     * 
     */
    public StartReplenishmentFromCassetteResponseType createStartReplenishmentFromCassetteResponseType() {
        return new StartReplenishmentFromCassetteResponseType();
    }

    /**
     * Create an instance of {@link AdjustTimeRequestType }
     * 
     */
    public AdjustTimeRequestType createAdjustTimeRequestType() {
        return new AdjustTimeRequestType();
    }

    /**
     * Create an instance of {@link UpdateSettingFileRequestType }
     * 
     */
    public UpdateSettingFileRequestType createUpdateSettingFileRequestType() {
        return new UpdateSettingFileRequestType();
    }

    /**
     * Create an instance of {@link CollectResponseType }
     * 
     */
    public CollectResponseType createCollectResponseType() {
        return new CollectResponseType();
    }

    /**
     * Create an instance of {@link RegisterEventRequestType }
     * 
     */
    public RegisterEventRequestType createRegisterEventRequestType() {
        return new RegisterEventRequestType();
    }

    /**
     * Create an instance of {@link UpdateListType }
     * 
     */
    public UpdateListType createUpdateListType() {
        return new UpdateListType();
    }

    /**
     * Create an instance of {@link ChangeResponseType }
     * 
     */
    public ChangeResponseType createChangeResponseType() {
        return new ChangeResponseType();
    }

    /**
     * Create an instance of {@link SetExchangeRateResponseType }
     * 
     */
    public SetExchangeRateResponseType createSetExchangeRateResponseType() {
        return new SetExchangeRateResponseType();
    }

    /**
     * Create an instance of {@link CashUnitsType }
     * 
     */
    public CashUnitsType createCashUnitsType() {
        return new CashUnitsType();
    }

    /**
     * Create an instance of {@link InventoryRequestType }
     * 
     */
    public InventoryRequestType createInventoryRequestType() {
        return new InventoryRequestType();
    }

    /**
     * Create an instance of {@link ChangeRequestType }
     * 
     */
    public ChangeRequestType createChangeRequestType() {
        return new ChangeRequestType();
    }

    /**
     * Create an instance of {@link UnLockUnitRequestType }
     * 
     */
    public UnLockUnitRequestType createUnLockUnitRequestType() {
        return new UnLockUnitRequestType();
    }

    /**
     * Create an instance of {@link DisableDenomRequestType }
     * 
     */
    public DisableDenomRequestType createDisableDenomRequestType() {
        return new DisableDenomRequestType();
    }

    /**
     * Create an instance of {@link StartCashinRequestType }
     * 
     */
    public StartCashinRequestType createStartCashinRequestType() {
        return new StartCashinRequestType();
    }

    /**
     * Create an instance of {@link StatusResponseType }
     * 
     */
    public StatusResponseType createStatusResponseType() {
        return new StatusResponseType();
    }

    /**
     * Create an instance of {@link OccupyRequestType }
     * 
     */
    public OccupyRequestType createOccupyRequestType() {
        return new OccupyRequestType();
    }

    /**
     * Create an instance of {@link UnLockUnitOptionType }
     * 
     */
    public UnLockUnitOptionType createUnLockUnitOptionType() {
        return new UnLockUnitOptionType();
    }

    /**
     * Create an instance of {@link DenominationType }
     * 
     */
    public DenominationType createDenominationType() {
        return new DenominationType();
    }

    /**
     * Create an instance of {@link CloseExitCoverResponseType }
     * 
     */
    public CloseExitCoverResponseType createCloseExitCoverResponseType() {
        return new CloseExitCoverResponseType();
    }

    /**
     * Create an instance of {@link ReleaseResponseType }
     * 
     */
    public ReleaseResponseType createReleaseResponseType() {
        return new ReleaseResponseType();
    }

    /**
     * Create an instance of {@link ExchangeRateSettingType }
     * 
     */
    public ExchangeRateSettingType createExchangeRateSettingType() {
        return new ExchangeRateSettingType();
    }

    /**
     * Create an instance of {@link CurrencyType }
     * 
     */
    public CurrencyType createCurrencyType() {
        return new CurrencyType();
    }

    /**
     * Create an instance of {@link AdjustTimeResponseType }
     * 
     */
    public AdjustTimeResponseType createAdjustTimeResponseType() {
        return new AdjustTimeResponseType();
    }

    /**
     * Create an instance of {@link ChangeCancelResponseType }
     * 
     */
    public ChangeCancelResponseType createChangeCancelResponseType() {
        return new ChangeCancelResponseType();
    }

    /**
     * Create an instance of {@link CounterClearRequestType }
     * 
     */
    public CounterClearRequestType createCounterClearRequestType() {
        return new CounterClearRequestType();
    }

    /**
     * Create an instance of {@link DisableDenomResponseType }
     * 
     */
    public DisableDenomResponseType createDisableDenomResponseType() {
        return new DisableDenomResponseType();
    }

    /**
     * Create an instance of {@link StartLogreadRequestType }
     * 
     */
    public StartLogreadRequestType createStartLogreadRequestType() {
        return new StartLogreadRequestType();
    }

    /**
     * Create an instance of {@link LockUnitRequestType }
     * 
     */
    public LockUnitRequestType createLockUnitRequestType() {
        return new LockUnitRequestType();
    }

    /**
     * Create an instance of {@link UpdateManualDepositTotalResponseType }
     * 
     */
    public UpdateManualDepositTotalResponseType createUpdateManualDepositTotalResponseType() {
        return new UpdateManualDepositTotalResponseType();
    }

    /**
     * Create an instance of {@link RefreshSalesTotalRequestType }
     * 
     */
    public RefreshSalesTotalRequestType createRefreshSalesTotalRequestType() {
        return new RefreshSalesTotalRequestType();
    }

    /**
     * Create an instance of {@link UnRegisterEventResponseType }
     * 
     */
    public UnRegisterEventResponseType createUnRegisterEventResponseType() {
        return new UnRegisterEventResponseType();
    }

    /**
     * Create an instance of {@link OpenExitCoverResponseType }
     * 
     */
    public OpenExitCoverResponseType createOpenExitCoverResponseType() {
        return new OpenExitCoverResponseType();
    }

    /**
     * Create an instance of {@link PowerControlOptionType }
     * 
     */
    public PowerControlOptionType createPowerControlOptionType() {
        return new PowerControlOptionType();
    }

    /**
     * Create an instance of {@link OpenResponseType }
     * 
     */
    public OpenResponseType createOpenResponseType() {
        return new OpenResponseType();
    }

    /**
     * Create an instance of {@link StartReplenishmentFromCassetteRequestType }
     * 
     */
    public StartReplenishmentFromCassetteRequestType createStartReplenishmentFromCassetteRequestType() {
        return new StartReplenishmentFromCassetteRequestType();
    }

    /**
     * Create an instance of {@link PowerControlResponseType }
     * 
     */
    public PowerControlResponseType createPowerControlResponseType() {
        return new PowerControlResponseType();
    }

    /**
     * Create an instance of {@link AdjustTimeTimeType }
     * 
     */
    public AdjustTimeTimeType createAdjustTimeTimeType() {
        return new AdjustTimeTimeType();
    }

    /**
     * Create an instance of {@link StartLogreadResponseType }
     * 
     */
    public StartLogreadResponseType createStartLogreadResponseType() {
        return new StartLogreadResponseType();
    }

    /**
     * Create an instance of {@link CollectOptionType }
     * 
     */
    public CollectOptionType createCollectOptionType() {
        return new CollectOptionType();
    }

    /**
     * Create an instance of {@link UpdateManualDepositTotalRequestType }
     * 
     */
    public UpdateManualDepositTotalRequestType createUpdateManualDepositTotalRequestType() {
        return new UpdateManualDepositTotalRequestType();
    }

    /**
     * Create an instance of {@link PowerControlRequestType }
     * 
     */
    public PowerControlRequestType createPowerControlRequestType() {
        return new PowerControlRequestType();
    }

    /**
     * Create an instance of {@link ReleaseRequestType }
     * 
     */
    public ReleaseRequestType createReleaseRequestType() {
        return new ReleaseRequestType();
    }

    /**
     * Create an instance of {@link InventoryResponseType }
     * 
     */
    public InventoryResponseType createInventoryResponseType() {
        return new InventoryResponseType();
    }

    /**
     * Create an instance of {@link RequireVerifyDenominationInfosType }
     * 
     */
    public RequireVerifyDenominationInfosType createRequireVerifyDenominationInfosType() {
        return new RequireVerifyDenominationInfosType();
    }

    /**
     * Create an instance of {@link ReplenishmentFromEntranceCancelResponseType }
     * 
     */
    public ReplenishmentFromEntranceCancelResponseType createReplenishmentFromEntranceCancelResponseType() {
        return new ReplenishmentFromEntranceCancelResponseType();
    }

    /**
     * Create an instance of {@link InventoryOptionType }
     * 
     */
    public InventoryOptionType createInventoryOptionType() {
        return new InventoryOptionType();
    }

    /**
     * Create an instance of {@link LoginUserRequestType }
     * 
     */
    public LoginUserRequestType createLoginUserRequestType() {
        return new LoginUserRequestType();
    }

    /**
     * Create an instance of {@link CashinCancelResponseType }
     * 
     */
    public CashinCancelResponseType createCashinCancelResponseType() {
        return new CashinCancelResponseType();
    }

    /**
     * Create an instance of {@link RCW100RomVerType }
     * 
     */
    public RCW100RomVerType createRCW100RomVerType() {
        return new RCW100RomVerType();
    }

    /**
     * Create an instance of {@link CashoutRequestType }
     * 
     */
    public CashoutRequestType createCashoutRequestType() {
        return new CashoutRequestType();
    }

    /**
     * Create an instance of {@link ReturnCashOptionType }
     * 
     */
    public ReturnCashOptionType createReturnCashOptionType() {
        return new ReturnCashOptionType();
    }

    /**
     * Create an instance of {@link RequireVerificationType }
     * 
     */
    public RequireVerificationType createRequireVerificationType() {
        return new RequireVerificationType();
    }

    /**
     * Create an instance of {@link CollectPartialType }
     * 
     */
    public CollectPartialType createCollectPartialType() {
        return new CollectPartialType();
    }

    /**
     * Create an instance of {@link ExchangeRateType }
     * 
     */
    public ExchangeRateType createExchangeRateType() {
        return new ExchangeRateType();
    }

    /**
     * Create an instance of {@link RCW8XRomVerType }
     * 
     */
    public RCW8XRomVerType createRCW8XRomVerType() {
        return new RCW8XRomVerType();
    }

    /**
     * Create an instance of {@link UnRegisterEventRequestType }
     * 
     */
    public UnRegisterEventRequestType createUnRegisterEventRequestType() {
        return new UnRegisterEventRequestType();
    }

    /**
     * Create an instance of {@link UpdateSettingFileResponseType }
     * 
     */
    public UpdateSettingFileResponseType createUpdateSettingFileResponseType() {
        return new UpdateSettingFileResponseType();
    }

    /**
     * Create an instance of {@link EnableDenomResponseType }
     * 
     */
    public EnableDenomResponseType createEnableDenomResponseType() {
        return new EnableDenomResponseType();
    }

    /**
     * Create an instance of {@link LockUnitResponseType }
     * 
     */
    public LockUnitResponseType createLockUnitResponseType() {
        return new LockUnitResponseType();
    }

    /**
     * Create an instance of {@link CashinCancelRequestType }
     * 
     */
    public CashinCancelRequestType createCashinCancelRequestType() {
        return new CashinCancelRequestType();
    }

    /**
     * Create an instance of {@link CloseResponseType }
     * 
     */
    public CloseResponseType createCloseResponseType() {
        return new CloseResponseType();
    }

    /**
     * Create an instance of {@link ChangeCancelRequestType }
     * 
     */
    public ChangeCancelRequestType createChangeCancelRequestType() {
        return new ChangeCancelRequestType();
    }

    /**
     * Create an instance of {@link CashType }
     * 
     */
    public CashType createCashType() {
        return new CashType();
    }

    /**
     * Create an instance of {@link OccupyResponseType }
     * 
     */
    public OccupyResponseType createOccupyResponseType() {
        return new OccupyResponseType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangeResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ChangeResponse")
    public JAXBElement<ChangeResponseType> createChangeResponse(ChangeResponseType value) {
        return new JAXBElement<ChangeResponseType>(_ChangeResponse_QNAME, ChangeResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CST1")
    public JAXBElement<String> createCST1(String value) {
        return new JAXBElement<String>(_CST1_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CST3")
    public JAXBElement<String> createCST3(String value) {
        return new JAXBElement<String>(_CST3_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "MAIN_AP")
    public JAXBElement<String> createMAINAP(String value) {
        return new JAXBElement<String>(_MAINAP_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CST2")
    public JAXBElement<String> createCST2(String value) {
        return new JAXBElement<String>(_CST2_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnLockUnitRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UnLockUnitRequest")
    public JAXBElement<UnLockUnitRequestType> createUnLockUnitRequest(UnLockUnitRequestType value) {
        return new JAXBElement<UnLockUnitRequestType>(_UnLockUnitRequest_QNAME, UnLockUnitRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OccupyRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "OccupyRequest")
    public JAXBElement<OccupyRequestType> createOccupyRequest(OccupyRequestType value) {
        return new JAXBElement<OccupyRequestType>(_OccupyRequest_QNAME, OccupyRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CV_COUNTRY")
    public JAXBElement<String> createCVCOUNTRY(String value) {
        return new JAXBElement<String>(_CVCOUNTRY_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "PLD1")
    public JAXBElement<String> createPLD1(String value) {
        return new JAXBElement<String>(_PLD1_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BVPLD2")
    public JAXBElement<String> createBVPLD2(String value) {
        return new JAXBElement<String>(_BVPLD2_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "PLD2")
    public JAXBElement<String> createPLD2(String value) {
        return new JAXBElement<String>(_PLD2_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CounterClearResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CounterClearResponse")
    public JAXBElement<CounterClearResponseType> createCounterClearResponse(CounterClearResponseType value) {
        return new JAXBElement<CounterClearResponseType>(_CounterClearResponse_QNAME, CounterClearResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CashinCancelResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CashinCancelResponse")
    public JAXBElement<CashinCancelResponseType> createCashinCancelResponse(CashinCancelResponseType value) {
        return new JAXBElement<CashinCancelResponseType>(_CashinCancelResponse_QNAME, CashinCancelResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StatusResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "StatusResponse")
    public JAXBElement<StatusResponseType> createStatusResponse(StatusResponseType value) {
        return new JAXBElement<StatusResponseType>(_StatusResponse_QNAME, StatusResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LogoutUserResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "LogoutUserResponse")
    public JAXBElement<LogoutUserResponseType> createLogoutUserResponse(LogoutUserResponseType value) {
        return new JAXBElement<LogoutUserResponseType>(_LogoutUserResponse_QNAME, LogoutUserResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CST4")
    public JAXBElement<String> createCST4(String value) {
        return new JAXBElement<String>(_CST4_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CST5")
    public JAXBElement<String> createCST5(String value) {
        return new JAXBElement<String>(_CST5_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CST6")
    public JAXBElement<String> createCST6(String value) {
        return new JAXBElement<String>(_CST6_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DisableDenomResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "DisableDenomResponse")
    public JAXBElement<DisableDenomResponseType> createDisableDenomResponse(DisableDenomResponseType value) {
        return new JAXBElement<DisableDenomResponseType>(_DisableDenomResponse_QNAME, DisableDenomResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CST7")
    public JAXBElement<String> createCST7(String value) {
        return new JAXBElement<String>(_CST7_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "Port")
    public JAXBElement<BigInteger> createPort(BigInteger value) {
        return new JAXBElement<BigInteger>(_Port_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OpenRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "OpenRequest")
    public JAXBElement<OpenRequestType> createOpenRequest(OpenRequestType value) {
        return new JAXBElement<OpenRequestType>(_OpenRequest_QNAME, OpenRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CST8")
    public JAXBElement<String> createCST8(String value) {
        return new JAXBElement<String>(_CST8_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReturnCashRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ReturnCashRequest")
    public JAXBElement<ReturnCashRequestType> createReturnCashRequest(ReturnCashRequestType value) {
        return new JAXBElement<ReturnCashRequestType>(_ReturnCashRequest_QNAME, ReturnCashRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RomVersionRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "RomVersionRequest")
    public JAXBElement<RomVersionRequestType> createRomVersionRequest(RomVersionRequestType value) {
        return new JAXBElement<RomVersionRequestType>(_RomVersionRequest_QNAME, RomVersionRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UN_GENERAL")
    public JAXBElement<String> createUNGENERAL(String value) {
        return new JAXBElement<String>(_UNGENERAL_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReturnCashResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ReturnCashResponse")
    public JAXBElement<ReturnCashResponseType> createReturnCashResponse(ReturnCashResponseType value) {
        return new JAXBElement<ReturnCashResponseType>(_ReturnCashResponse_QNAME, ReturnCashResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DisableDenomRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "DisableDenomRequest")
    public JAXBElement<DisableDenomRequestType> createDisableDenomRequest(DisableDenomRequestType value) {
        return new JAXBElement<DisableDenomRequestType>(_DisableDenomRequest_QNAME, DisableDenomRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnRegisterEventRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UnRegisterEventRequest")
    public JAXBElement<UnRegisterEventRequestType> createUnRegisterEventRequest(UnRegisterEventRequestType value) {
        return new JAXBElement<UnRegisterEventRequestType>(_UnRegisterEventRequest_QNAME, UnRegisterEventRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoginUserResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "LoginUserResponse")
    public JAXBElement<LoginUserResponseType> createLoginUserResponse(LoginUserResponseType value) {
        return new JAXBElement<LoginUserResponseType>(_LoginUserResponse_QNAME, LoginUserResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BVControl")
    public JAXBElement<String> createBVControl(String value) {
        return new JAXBElement<String>(_BVControl_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnableDenomRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "EnableDenomRequest")
    public JAXBElement<EnableDenomRequestType> createEnableDenomRequest(EnableDenomRequestType value) {
        return new JAXBElement<EnableDenomRequestType>(_EnableDenomRequest_QNAME, EnableDenomRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BVSetting")
    public JAXBElement<String> createBVSetting(String value) {
        return new JAXBElement<String>(_BVSetting_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EventOfflineRecoveryRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "EventOfflineRecoveryRequest")
    public JAXBElement<EventOfflineRecoveryRequestType> createEventOfflineRecoveryRequest(EventOfflineRecoveryRequestType value) {
        return new JAXBElement<EventOfflineRecoveryRequestType>(_EventOfflineRecoveryRequest_QNAME, EventOfflineRecoveryRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RomVersionResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "RomVersionResponse")
    public JAXBElement<RomVersionResponseType> createRomVersionResponse(RomVersionResponseType value) {
        return new JAXBElement<RomVersionResponseType>(_RomVersionResponse_QNAME, RomVersionResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "User")
    public JAXBElement<String> createUser(String value) {
        return new JAXBElement<String>(_User_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CUTE")
    public JAXBElement<String> createCUTE(String value) {
        return new JAXBElement<String>(_CUTE_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BVDownload")
    public JAXBElement<String> createBVDownload(String value) {
        return new JAXBElement<String>(_BVDownload_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartCashinRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "StartCashinRequest")
    public JAXBElement<StartCashinRequestType> createStartCashinRequest(StartCashinRequestType value) {
        return new JAXBElement<StartCashinRequestType>(_StartCashinRequest_QNAME, StartCashinRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BV_DL")
    public JAXBElement<String> createBVDL(String value) {
        return new JAXBElement<String>(_BVDL_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangeCancelResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ChangeCancelResponse")
    public JAXBElement<ChangeCancelResponseType> createChangeCancelResponse(ChangeCancelResponseType value) {
        return new JAXBElement<ChangeCancelResponseType>(_ChangeCancelResponse_QNAME, ChangeCancelResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SetExchangeRateRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "SetExchangeRateRequest")
    public JAXBElement<SetExchangeRateRequestType> createSetExchangeRateRequest(SetExchangeRateRequestType value) {
        return new JAXBElement<SetExchangeRateRequestType>(_SetExchangeRateRequest_QNAME, SetExchangeRateRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LockUnitResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "LockUnitResponse")
    public JAXBElement<LockUnitResponseType> createLockUnitResponse(LockUnitResponseType value) {
        return new JAXBElement<LockUnitResponseType>(_LockUnitResponse_QNAME, LockUnitResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PowerControlRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "PowerControlRequest")
    public JAXBElement<PowerControlRequestType> createPowerControlRequest(PowerControlRequestType value) {
        return new JAXBElement<PowerControlRequestType>(_PowerControlRequest_QNAME, PowerControlRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EndReplenishmentFromEntranceResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "EndReplenishmentFromEntranceResponse")
    public JAXBElement<EndReplenishmentFromEntranceResponseType> createEndReplenishmentFromEntranceResponse(EndReplenishmentFromEntranceResponseType value) {
        return new JAXBElement<EndReplenishmentFromEntranceResponseType>(_EndReplenishmentFromEntranceResponse_QNAME, EndReplenishmentFromEntranceResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "Url")
    public JAXBElement<String> createUrl(String value) {
        return new JAXBElement<String>(_Url_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartDownloadResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "StartDownloadResponse")
    public JAXBElement<StartDownloadResponseType> createStartDownloadResponse(StartDownloadResponseType value) {
        return new JAXBElement<StartDownloadResponseType>(_StartDownloadResponse_QNAME, StartDownloadResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CashinCancelRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CashinCancelRequest")
    public JAXBElement<CashinCancelRequestType> createCashinCancelRequest(CashinCancelRequestType value) {
        return new JAXBElement<CashinCancelRequestType>(_CashinCancelRequest_QNAME, CashinCancelRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "FPGA")
    public JAXBElement<String> createFPGA(String value) {
        return new JAXBElement<String>(_FPGA_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReleaseRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ReleaseRequest")
    public JAXBElement<ReleaseRequestType> createReleaseRequest(ReleaseRequestType value) {
        return new JAXBElement<ReleaseRequestType>(_ReleaseRequest_QNAME, ReleaseRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "SessionID")
    public JAXBElement<String> createSessionID(String value) {
        return new JAXBElement<String>(_SessionID_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ESCROW")
    public JAXBElement<String> createESCROW(String value) {
        return new JAXBElement<String>(_ESCROW_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StatusRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "StatusRequest")
    public JAXBElement<StatusRequestType> createStatusRequest(StatusRequestType value) {
        return new JAXBElement<StatusRequestType>(_StatusRequest_QNAME, StatusRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OccupyResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "OccupyResponse")
    public JAXBElement<OccupyResponseType> createOccupyResponse(OccupyResponseType value) {
        return new JAXBElement<OccupyResponseType>(_OccupyResponse_QNAME, OccupyResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReleaseResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ReleaseResponse")
    public JAXBElement<ReleaseResponseType> createReleaseResponse(ReleaseResponseType value) {
        return new JAXBElement<ReleaseResponseType>(_ReleaseResponse_QNAME, ReleaseResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartDownloadRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "StartDownload")
    public JAXBElement<StartDownloadRequestType> createStartDownload(StartDownloadRequestType value) {
        return new JAXBElement<StartDownloadRequestType>(_StartDownload_QNAME, StartDownloadRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "COLLECT")
    public JAXBElement<String> createCOLLECT(String value) {
        return new JAXBElement<String>(_COLLECT_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RefreshSalesTotalResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "RefreshSalesTotalResponse")
    public JAXBElement<RefreshSalesTotalResponseType> createRefreshSalesTotalResponse(RefreshSalesTotalResponseType value) {
        return new JAXBElement<RefreshSalesTotalResponseType>(_RefreshSalesTotalResponse_QNAME, RefreshSalesTotalResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EventOfflineRecoveryResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "EventOfflineRecoveryResponse")
    public JAXBElement<EventOfflineRecoveryResponseType> createEventOfflineRecoveryResponse(EventOfflineRecoveryResponseType value) {
        return new JAXBElement<EventOfflineRecoveryResponseType>(_EventOfflineRecoveryResponse_QNAME, EventOfflineRecoveryResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CloseExitCoverResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CloseExitCoverResponse")
    public JAXBElement<CloseExitCoverResponseType> createCloseExitCoverResponse(CloseExitCoverResponseType value) {
        return new JAXBElement<CloseExitCoverResponseType>(_CloseExitCoverResponse_QNAME, CloseExitCoverResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UN_CC")
    public JAXBElement<String> createUNCC(String value) {
        return new JAXBElement<String>(_UNCC_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "Code")
    public JAXBElement<BigInteger> createCode(BigInteger value) {
        return new JAXBElement<BigInteger>(_Code_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InventoryRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "InventoryRequest")
    public JAXBElement<InventoryRequestType> createInventoryRequest(InventoryRequestType value) {
        return new JAXBElement<InventoryRequestType>(_InventoryRequest_QNAME, InventoryRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReplenishmentFromEntranceCancelResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ReplenishmentFromEntranceCancelResponse")
    public JAXBElement<ReplenishmentFromEntranceCancelResponseType> createReplenishmentFromEntranceCancelResponse(ReplenishmentFromEntranceCancelResponseType value) {
        return new JAXBElement<ReplenishmentFromEntranceCancelResponseType>(_ReplenishmentFromEntranceCancelResponse_QNAME, ReplenishmentFromEntranceCancelResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BV_GENERAL2")
    public JAXBElement<String> createBVGENERAL2(String value) {
        return new JAXBElement<String>(_BVGENERAL2_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BV_GENERAL1")
    public JAXBElement<String> createBVGENERAL1(String value) {
        return new JAXBElement<String>(_BVGENERAL1_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OpenExitCoverRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "OpenExitCoverRequest")
    public JAXBElement<OpenExitCoverRequestType> createOpenExitCoverRequest(OpenExitCoverRequestType value) {
        return new JAXBElement<OpenExitCoverRequestType>(_OpenExitCoverRequest_QNAME, OpenExitCoverRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LogoutUserRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "LogoutUserRequest")
    public JAXBElement<LogoutUserRequestType> createLogoutUserRequest(LogoutUserRequestType value) {
        return new JAXBElement<LogoutUserRequestType>(_LogoutUserRequest_QNAME, LogoutUserRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CollectRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CollectRequest")
    public JAXBElement<CollectRequestType> createCollectRequest(CollectRequestType value) {
        return new JAXBElement<CollectRequestType>(_CollectRequest_QNAME, CollectRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReplenishmentFromEntranceCancelRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ReplenishmentFromEntranceCancelRequest")
    public JAXBElement<ReplenishmentFromEntranceCancelRequestType> createReplenishmentFromEntranceCancelRequest(ReplenishmentFromEntranceCancelRequestType value) {
        return new JAXBElement<ReplenishmentFromEntranceCancelRequestType>(_ReplenishmentFromEntranceCancelRequest_QNAME, ReplenishmentFromEntranceCancelRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "LOW_APL")
    public JAXBElement<String> createLOWAPL(String value) {
        return new JAXBElement<String>(_LOWAPL_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "Boot")
    public JAXBElement<String> createBoot(String value) {
        return new JAXBElement<String>(_Boot_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateSettingFileRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UpdateSettingFileRequest")
    public JAXBElement<UpdateSettingFileRequestType> createUpdateSettingFileRequest(UpdateSettingFileRequestType value) {
        return new JAXBElement<UpdateSettingFileRequestType>(_UpdateSettingFileRequest_QNAME, UpdateSettingFileRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BVFormat")
    public JAXBElement<String> createBVFormat(String value) {
        return new JAXBElement<String>(_BVFormat_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EventNotificationStatusResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "EventNotificationStatusResponse")
    public JAXBElement<EventNotificationStatusResponseType> createEventNotificationStatusResponse(EventNotificationStatusResponseType value) {
        return new JAXBElement<EventNotificationStatusResponseType>(_EventNotificationStatusResponse_QNAME, EventNotificationStatusResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateManualDepositTotalResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UpdateManualDepositTotalResponse")
    public JAXBElement<UpdateManualDepositTotalResponseType> createUpdateManualDepositTotalResponse(UpdateManualDepositTotalResponseType value) {
        return new JAXBElement<UpdateManualDepositTotalResponseType>(_UpdateManualDepositTotalResponse_QNAME, UpdateManualDepositTotalResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "FPGA1")
    public JAXBElement<String> createFPGA1(String value) {
        return new JAXBElement<String>(_FPGA1_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "FPGA2")
    public JAXBElement<String> createFPGA2(String value) {
        return new JAXBElement<String>(_FPGA2_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "SPEC_INFO")
    public JAXBElement<String> createSPECINFO(String value) {
        return new JAXBElement<String>(_SPECINFO_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AdjustTimeRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "AdjustTimeRequest")
    public JAXBElement<AdjustTimeRequestType> createAdjustTimeRequest(AdjustTimeRequestType value) {
        return new JAXBElement<AdjustTimeRequestType>(_AdjustTimeRequest_QNAME, AdjustTimeRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BV_PARAM2")
    public JAXBElement<String> createBVPARAM2(String value) {
        return new JAXBElement<String>(_BVPARAM2_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "DeviceName")
    public JAXBElement<String> createDeviceName(String value) {
        return new JAXBElement<String>(_DeviceName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "Amount")
    public JAXBElement<String> createAmount(String value) {
        return new JAXBElement<String>(_Amount_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartLogreadRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "StartLogread")
    public JAXBElement<StartLogreadRequestType> createStartLogread(StartLogreadRequestType value) {
        return new JAXBElement<StartLogreadRequestType>(_StartLogread_QNAME, StartLogreadRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "AP")
    public JAXBElement<String> createAP(String value) {
        return new JAXBElement<String>(_AP_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "Id")
    public JAXBElement<String> createId(String value) {
        return new JAXBElement<String>(_Id_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartReplenishmentFromEntranceRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "StartReplenishmentFromEntranceRequest")
    public JAXBElement<StartReplenishmentFromEntranceRequestType> createStartReplenishmentFromEntranceRequest(StartReplenishmentFromEntranceRequestType value) {
        return new JAXBElement<StartReplenishmentFromEntranceRequestType>(_StartReplenishmentFromEntranceRequest_QNAME, StartReplenishmentFromEntranceRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BV_PARAM1")
    public JAXBElement<String> createBVPARAM1(String value) {
        return new JAXBElement<String>(_BVPARAM1_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LockUnitRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "LockUnitRequest")
    public JAXBElement<LockUnitRequestType> createLockUnitRequest(LockUnitRequestType value) {
        return new JAXBElement<LockUnitRequestType>(_LockUnitRequest_QNAME, LockUnitRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BV_PLD")
    public JAXBElement<String> createBVPLD(String value) {
        return new JAXBElement<String>(_BVPLD_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "SeqNo")
    public JAXBElement<String> createSeqNo(String value) {
        return new JAXBElement<String>(_SeqNo_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OpenExitCoverResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "OpenExitCoverResponse")
    public JAXBElement<OpenExitCoverResponseType> createOpenExitCoverResponse(OpenExitCoverResponseType value) {
        return new JAXBElement<OpenExitCoverResponseType>(_OpenExitCoverResponse_QNAME, OpenExitCoverResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BV_CONTROL")
    public JAXBElement<String> createBVCONTROL(String value) {
        return new JAXBElement<String>(_BVCONTROL_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegisterEventRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "RegisterEventRequest")
    public JAXBElement<RegisterEventRequestType> createRegisterEventRequest(RegisterEventRequestType value) {
        return new JAXBElement<RegisterEventRequestType>(_RegisterEventRequest_QNAME, RegisterEventRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CV_AP")
    public JAXBElement<String> createCVAP(String value) {
        return new JAXBElement<String>(_CVAP_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OpenResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "OpenResponse")
    public JAXBElement<OpenResponseType> createOpenResponse(OpenResponseType value) {
        return new JAXBElement<OpenResponseType>(_OpenResponse_QNAME, OpenResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CashoutRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CashoutRequest")
    public JAXBElement<CashoutRequestType> createCashoutRequest(CashoutRequestType value) {
        return new JAXBElement<CashoutRequestType>(_CashoutRequest_QNAME, CashoutRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PowerControlResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "PowerControlResponse")
    public JAXBElement<PowerControlResponseType> createPowerControlResponse(PowerControlResponseType value) {
        return new JAXBElement<PowerControlResponseType>(_PowerControlResponse_QNAME, PowerControlResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnRegisterEventResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UnRegisterEventResponse")
    public JAXBElement<UnRegisterEventResponseType> createUnRegisterEventResponse(UnRegisterEventResponseType value) {
        return new JAXBElement<UnRegisterEventResponseType>(_UnRegisterEventResponse_QNAME, UnRegisterEventResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ManualDeposit")
    public JAXBElement<String> createManualDeposit(String value) {
        return new JAXBElement<String>(_ManualDeposit_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EndCashinRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "EndCashinRequest")
    public JAXBElement<EndCashinRequestType> createEndCashinRequest(EndCashinRequestType value) {
        return new JAXBElement<EndCashinRequestType>(_EndCashinRequest_QNAME, EndCashinRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UP_APL")
    public JAXBElement<String> createUPAPL(String value) {
        return new JAXBElement<String>(_UPAPL_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegisterEventResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "RegisterEventResponse")
    public JAXBElement<RegisterEventResponseType> createRegisterEventResponse(RegisterEventResponseType value) {
        return new JAXBElement<RegisterEventResponseType>(_RegisterEventResponse_QNAME, RegisterEventResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CloseRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CloseRequest")
    public JAXBElement<CloseRequestType> createCloseRequest(CloseRequestType value) {
        return new JAXBElement<CloseRequestType>(_CloseRequest_QNAME, CloseRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "Piece")
    public JAXBElement<BigInteger> createPiece(BigInteger value) {
        return new JAXBElement<BigInteger>(_Piece_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UN_CMB")
    public JAXBElement<String> createUNCMB(String value) {
        return new JAXBElement<String>(_UNCMB_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CashoutResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CashoutResponse")
    public JAXBElement<CashoutResponseType> createCashoutResponse(CashoutResponseType value) {
        return new JAXBElement<CashoutResponseType>(_CashoutResponse_QNAME, CashoutResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CounterClearRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CounterClearRequest")
    public JAXBElement<CounterClearRequestType> createCounterClearRequest(CounterClearRequestType value) {
        return new JAXBElement<CounterClearRequestType>(_CounterClearRequest_QNAME, CounterClearRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartReplenishmentFromCassetteRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "StartReplenishmentFromCassetteRequest")
    public JAXBElement<StartReplenishmentFromCassetteRequestType> createStartReplenishmentFromCassetteRequest(StartReplenishmentFromCassetteRequestType value) {
        return new JAXBElement<StartReplenishmentFromCassetteRequestType>(_StartReplenishmentFromCassetteRequest_QNAME, StartReplenishmentFromCassetteRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateManualDepositTotalRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UpdateManualDepositTotalRequest")
    public JAXBElement<UpdateManualDepositTotalRequestType> createUpdateManualDepositTotalRequest(UpdateManualDepositTotalRequestType value) {
        return new JAXBElement<UpdateManualDepositTotalRequestType>(_UpdateManualDepositTotalRequest_QNAME, UpdateManualDepositTotalRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartReplenishmentFromEntranceResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "StartReplenishmentFromEntranceResponse")
    public JAXBElement<StartReplenishmentFromEntranceResponseType> createStartReplenishmentFromEntranceResponse(StartReplenishmentFromEntranceResponseType value) {
        return new JAXBElement<StartReplenishmentFromEntranceResponseType>(_StartReplenishmentFromEntranceResponse_QNAME, StartReplenishmentFromEntranceResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "IPL")
    public JAXBElement<String> createIPL(String value) {
        return new JAXBElement<String>(_IPL_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CloseResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CloseResponse")
    public JAXBElement<CloseResponseType> createCloseResponse(CloseResponseType value) {
        return new JAXBElement<CloseResponseType>(_CloseResponse_QNAME, CloseResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangeCancelRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ChangeCancelRequest")
    public JAXBElement<ChangeCancelRequestType> createChangeCancelRequest(ChangeCancelRequestType value) {
        return new JAXBElement<ChangeCancelRequestType>(_ChangeCancelRequest_QNAME, ChangeCancelRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnLockUnitResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UnLockUnitResponse")
    public JAXBElement<UnLockUnitResponseType> createUnLockUnitResponse(UnLockUnitResponseType value) {
        return new JAXBElement<UnLockUnitResponseType>(_UnLockUnitResponse_QNAME, UnLockUnitResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangeRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ChangeRequest")
    public JAXBElement<ChangeRequestType> createChangeRequest(ChangeRequestType value) {
        return new JAXBElement<ChangeRequestType>(_ChangeRequest_QNAME, ChangeRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CollectResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CollectResponse")
    public JAXBElement<CollectResponseType> createCollectResponse(CollectResponseType value) {
        return new JAXBElement<CollectResponseType>(_CollectResponse_QNAME, CollectResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EndCashinResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "EndCashinResponse")
    public JAXBElement<EndCashinResponseType> createEndCashinResponse(EndCashinResponseType value) {
        return new JAXBElement<EndCashinResponseType>(_EndCashinResponse_QNAME, EndCashinResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EventNotificationStatusRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "EventNotificationStatusRequest")
    public JAXBElement<EventNotificationStatusRequestType> createEventNotificationStatusRequest(EventNotificationStatusRequestType value) {
        return new JAXBElement<EventNotificationStatusRequestType>(_EventNotificationStatusRequest_QNAME, EventNotificationStatusRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnableDenomResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "EnableDenomResponse")
    public JAXBElement<EnableDenomResponseType> createEnableDenomResponse(EnableDenomResponseType value) {
        return new JAXBElement<EnableDenomResponseType>(_EnableDenomResponse_QNAME, EnableDenomResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EndReplenishmentFromCassetteResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "EndReplenishmentFromCassetteResponse")
    public JAXBElement<EndReplenishmentFromCassetteResponseType> createEndReplenishmentFromCassetteResponse(EndReplenishmentFromCassetteResponseType value) {
        return new JAXBElement<EndReplenishmentFromCassetteResponseType>(_EndReplenishmentFromCassetteResponse_QNAME, EndReplenishmentFromCassetteResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "Status")
    public JAXBElement<BigInteger> createStatus(BigInteger value) {
        return new JAXBElement<BigInteger>(_Status_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EndReplenishmentFromEntranceRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "EndReplenishmentFromEntranceRequest")
    public JAXBElement<EndReplenishmentFromEntranceRequestType> createEndReplenishmentFromEntranceRequest(EndReplenishmentFromEntranceRequestType value) {
        return new JAXBElement<EndReplenishmentFromEntranceRequestType>(_EndReplenishmentFromEntranceRequest_QNAME, EndReplenishmentFromEntranceRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EndReplenishmentFromCassetteRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "EndReplenishmentFromCassetteRequest")
    public JAXBElement<EndReplenishmentFromCassetteRequestType> createEndReplenishmentFromCassetteRequest(EndReplenishmentFromCassetteRequestType value) {
        return new JAXBElement<EndReplenishmentFromCassetteRequestType>(_EndReplenishmentFromCassetteRequest_QNAME, EndReplenishmentFromCassetteRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SetExchangeRateResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "SetExchangeRateResponse")
    public JAXBElement<SetExchangeRateResponseType> createSetExchangeRateResponse(SetExchangeRateResponseType value) {
        return new JAXBElement<SetExchangeRateResponseType>(_SetExchangeRateResponse_QNAME, SetExchangeRateResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CloseExitCoverRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "CloseExitCoverRequest")
    public JAXBElement<CloseExitCoverRequestType> createCloseExitCoverRequest(CloseExitCoverRequestType value) {
        return new JAXBElement<CloseExitCoverRequestType>(_CloseExitCoverRequest_QNAME, CloseExitCoverRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BV_SET")
    public JAXBElement<String> createBVSET(String value) {
        return new JAXBElement<String>(_BVSET_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UserPwd")
    public JAXBElement<String> createUserPwd(String value) {
        return new JAXBElement<String>(_UserPwd_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoginUserRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "LoginUserRequest")
    public JAXBElement<LoginUserRequestType> createLoginUserRequest(LoginUserRequestType value) {
        return new JAXBElement<LoginUserRequestType>(_LoginUserRequest_QNAME, LoginUserRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartCashinResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "StartCashinResponse")
    public JAXBElement<StartCashinResponseType> createStartCashinResponse(StartCashinResponseType value) {
        return new JAXBElement<StartCashinResponseType>(_StartCashinResponse_QNAME, StartCashinResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BV_AP")
    public JAXBElement<String> createBVAP(String value) {
        return new JAXBElement<String>(_BVAP_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UN_SERIAL")
    public JAXBElement<String> createUNSERIAL(String value) {
        return new JAXBElement<String>(_UNSERIAL_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ComStatus")
    public JAXBElement<BigInteger> createComStatus(BigInteger value) {
        return new JAXBElement<BigInteger>(_ComStatus_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateSettingFileResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UpdateSettingFileResponse")
    public JAXBElement<UpdateSettingFileResponseType> createUpdateSettingFileResponse(UpdateSettingFileResponseType value) {
        return new JAXBElement<UpdateSettingFileResponseType>(_UpdateSettingFileResponse_QNAME, UpdateSettingFileResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BV")
    public JAXBElement<String> createBV(String value) {
        return new JAXBElement<String>(_BV_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResetRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ResetRequest")
    public JAXBElement<ResetRequestType> createResetRequest(ResetRequestType value) {
        return new JAXBElement<ResetRequestType>(_ResetRequest_QNAME, ResetRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InventoryResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "InventoryResponse")
    public JAXBElement<InventoryResponseType> createInventoryResponse(InventoryResponseType value) {
        return new JAXBElement<InventoryResponseType>(_InventoryResponse_QNAME, InventoryResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartLogreadResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "StartLogreadResponse")
    public JAXBElement<StartLogreadResponseType> createStartLogreadResponse(StartLogreadResponseType value) {
        return new JAXBElement<StartLogreadResponseType>(_StartLogreadResponse_QNAME, StartLogreadResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AdjustTimeResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "AdjustTimeResponse")
    public JAXBElement<AdjustTimeResponseType> createAdjustTimeResponse(AdjustTimeResponseType value) {
        return new JAXBElement<AdjustTimeResponseType>(_AdjustTimeResponse_QNAME, AdjustTimeResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RefreshSalesTotalRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "RefreshSalesTotalRequest")
    public JAXBElement<RefreshSalesTotalRequestType> createRefreshSalesTotalRequest(RefreshSalesTotalRequestType value) {
        return new JAXBElement<RefreshSalesTotalRequestType>(_RefreshSalesTotalRequest_QNAME, RefreshSalesTotalRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "SerialNo")
    public JAXBElement<String> createSerialNo(String value) {
        return new JAXBElement<String>(_SerialNo_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResetResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "ResetResponse")
    public JAXBElement<ResetResponseType> createResetResponse(ResetResponseType value) {
        return new JAXBElement<ResetResponseType>(_ResetResponse_QNAME, ResetResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "UN_FUNC")
    public JAXBElement<String> createUNFUNC(String value) {
        return new JAXBElement<String>(_UNFUNC_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartReplenishmentFromCassetteResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "StartReplenishmentFromCassetteResponse")
    public JAXBElement<StartReplenishmentFromCassetteResponseType> createStartReplenishmentFromCassetteResponse(StartReplenishmentFromCassetteResponseType value) {
        return new JAXBElement<StartReplenishmentFromCassetteResponseType>(_StartReplenishmentFromCassetteResponse_QNAME, StartReplenishmentFromCassetteResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "BV_FORMAT")
    public JAXBElement<String> createBVFORMAT(String value) {
        return new JAXBElement<String>(_BVFORMAT_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.glory.co.jp/bruebox.xsd", name = "COLLECT_SERIAL")
    public JAXBElement<String> createCOLLECTSERIAL(String value) {
        return new JAXBElement<String>(_COLLECTSERIAL_QNAME, String.class, null, value);
    }

}
