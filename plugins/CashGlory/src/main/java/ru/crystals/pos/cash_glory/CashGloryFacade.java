package ru.crystals.pos.cash_glory;

import jp.co.glory.bruebox.BrueBoxPortType;
import jp.co.glory.bruebox.BrueBoxService;
import jp.co.glory.bruebox.CashType;
import jp.co.glory.bruebox.CashinCancelRequestType;
import jp.co.glory.bruebox.CashoutRequestType;
import jp.co.glory.bruebox.ChangeCancelRequestType;
import jp.co.glory.bruebox.ChangeRequestType;
import jp.co.glory.bruebox.CloseRequestType;
import jp.co.glory.bruebox.CollectOptionType;
import jp.co.glory.bruebox.CollectRequestType;
import jp.co.glory.bruebox.DenominationType;
import jp.co.glory.bruebox.DepositCurrencyType;
import jp.co.glory.bruebox.EndCashinRequestType;
import jp.co.glory.bruebox.InventoryOptionType;
import jp.co.glory.bruebox.InventoryRequestType;
import jp.co.glory.bruebox.InventoryResponseType;
import jp.co.glory.bruebox.LockUnitOptionType;
import jp.co.glory.bruebox.LockUnitRequestType;
import jp.co.glory.bruebox.OpenRequestType;
import jp.co.glory.bruebox.OpenResponseType;
import jp.co.glory.bruebox.PowerControlOptionType;
import jp.co.glory.bruebox.PowerControlRequestType;
import jp.co.glory.bruebox.RegisterEventRequestType;
import jp.co.glory.bruebox.ResetRequestType;
import jp.co.glory.bruebox.ReturnCashOptionType;
import jp.co.glory.bruebox.ReturnCashRequestType;
import jp.co.glory.bruebox.StartCashinRequestType;
import jp.co.glory.bruebox.StatusOptionType;
import jp.co.glory.bruebox.StatusRequestType;
import jp.co.glory.bruebox.StatusResponseType;
import jp.co.glory.bruebox.UnLockUnitOptionType;
import jp.co.glory.bruebox.UnLockUnitRequestType;
import jp.co.glory.bruebox.UnRegisterEventRequestType;
import jp.co.glory.bruebox.UpdateManualDepositTotalRequestType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;
import ru.crystals.pos.cash_glory.constants.DeviceType;
import ru.crystals.pos.cash_machine.Constants;
import ru.crystals.pos.cash_machine.Response;
import ru.crystals.pos.cash_machine.entities.interfaces.CashInterface;
import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;
import ru.crystals.pos.cash_machine.entities.interfaces.InventoryResponseInterface;
import ru.crystals.pos.cash_machine.exceptions.CashMachineException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CashGloryFacade implements CashGloryFacadeInterface {

    private static final BigInteger COLLECT_WITHOUT_MIXER = BigInteger.valueOf(0);
    private static final BigInteger COLLECT_WITH_MIXER = BigInteger.valueOf(1);
    private static final BigInteger COLLECT_TO_EXIT = BigInteger.valueOf(1);
    private static final BigInteger COLLECT_TO_CASSETE = BigInteger.valueOf(0);
    private static final BigInteger MIXED_STACKER = BigInteger.valueOf(9);
    private static final BigInteger DENOMINATION_CONTROL = BigInteger.valueOf(5);
    private static final String ID = "1";
    private static final String SEQ_NO = "1234";
    private static final String TAG = "[CashGloryFacade(low level)]";
    private GloryConverter converter = new GloryConverter();

    private BrueBoxPortType fcc = null;

    public CashGloryFacade(final String ipAddr, final int eventPort) {
        this(ipAddr, eventPort, true);
    }

    public CashGloryFacade(final String ipAddr, final int eventPort, boolean detectSelfIp) {
        this.gloryIp = ipAddr;
        this.eventPort = eventPort;
        makeFcc(detectSelfIp);
    }

    private int eventPort;
    private String gloryIp;
    private InetAddress address;
    private boolean alive;
	private String sessionId = null;
    private long sessionBirthTime;
    private GloryConverter gloryConverter = new GloryConverter();

    /*************СЛУЖЕБНЫЕ МЕТОДЫ**************/
    private void makeFcc(boolean detectSelfIp) {
        try {
            address = detectSelfIp ? getAddress() : null;
            if (address != null || !detectSelfIp) {
                BrueBoxService bbs = null;
                File file = new File("modules/cashMachine/BrueBoxService.wsdl");
                String path = file.getAbsolutePath();
                Constants.LOG.info("{} Open wsdl file {}", TAG, path);
                if (!file.exists()) {
                    String url = "http://" + gloryIp + "/axis2/services/BrueBoxService?wsdl";
                    Constants.LOG.info("{} File not exists. Downloading from url: {}", TAG, url);
                    FileUtils.copyURLToFile(new URL(url), file);
                    Constants.LOG.info("{} Downloading successful");
                }
                changeUrlIntoFile(file, gloryIp);
                path = "file:" + (path.charAt(0) == '/' ? "" : "/") + path;
                Constants.LOG.info("{} File path converted to {}", TAG, path);
                Constants.LOG.info("{} Create BrueBoxService object", TAG);
                bbs = new BrueBoxService(path);
                fcc = bbs.getBrueBoxPort();
                Constants.LOG.info("{} BrueBoxService created successful. Connected to {}. Ready to work", TAG, gloryIp);
                init(detectSelfIp);
                alive = true;
            } else {
                alive = false;
            }
        } catch (SAXParseException e){
        	  Constants.LOG.error("{} ERROR: {}", TAG, ExceptionUtils.getMessage(e));
        	  File file = new File("modules/cashMachine/BrueBoxService.wsdl");
        	  file.delete();
            makeFcc(detectSelfIp);
        } catch (Exception e) {
            alive = false;
            Constants.LOG.error("{} ERROR: {}", TAG, ExceptionUtils.getFullStackTrace(e));
        }

    }

    private void changeUrlIntoFile(File file, String ip) throws Exception {
        Constants.LOG.info("{} Change soap address location to {}", TAG, gloryIp);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        NodeList elements = document.getElementsByTagName("soap:address");

        for (int i = 0; i < elements.getLength(); i++) {
            Element e = (Element) elements.item(i);
            if (e.hasAttribute("location")) {
                e.setAttribute("location", "http://" + ip + "/axis2/services/BrueBoxService");
            }
        }

        document.getDocumentElement().normalize();

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
        Constants.LOG.info("{} Change successful", TAG);
    }

    private void init(boolean detectSelfIp) {
        open();
        StatusResponseType status = getStatus();
        if (!status.getStatus().getCode().equals(BigInteger.ONE)) {
            this.reset();
        }
        if (detectSelfIp) {
            initSubscription();
        }
        close();
    }

    private void initSubscription() {

        try {
            if (address == null) {
                address = getAddress();
            }
            if (address != null) {
                String ip = address.getHostAddress();
                unregisterEvent(ip, BigInteger.valueOf(eventPort));
                registerEvent(ip, BigInteger.valueOf(eventPort));

            } else {
                Constants.LOG.error("{} Detecting ip address failure", TAG);
            }
        } catch (Exception e) {
            Constants.LOG.error("{} Detecting ip address failure", TAG);
            return;
        }
    }

    private InetAddress getAddress() throws IOException {
        Constants.LOG.debug("{} Try detecting ip address", TAG);
        List<NetworkInterface> netInts = Collections.list(NetworkInterface.getNetworkInterfaces());
        if (netInts.size() == 1) {
            return InetAddress.getLocalHost();
        }

        for (NetworkInterface net : netInts) {
            if (!net.isLoopback() && !net.isVirtual() && net.isUp()) {
                Constants.LOG.debug("{} Found interface: \"{}\"", TAG, net.getDisplayName());
                if ("eth10".equals(net.getDisplayName())) {
                    Constants.LOG.debug("{} Skipped RNDIS interface", TAG);
                    continue;
                }
                Constants.LOG.debug("{} Try to ping glory CI from this interface", TAG);
                InetAddress glory = InetAddress.getByName(gloryIp);
                if (glory.isReachable(net, 0, 5000)) {
                    Constants.LOG.debug("{} Ping successfull", TAG);
                    Enumeration<InetAddress> addrEnum = net.getInetAddresses();
                    while (addrEnum.hasMoreElements()) {
                        InetAddress addr = addrEnum.nextElement();
                        if (!addr.isLoopbackAddress() && !addr.isAnyLocalAddress() && !addr.isLinkLocalAddress() && !addr.isMulticastAddress()) {
                            Constants.LOG.info("{} Detecting ip address success: {}", TAG, addr.getHostAddress());
                            return addr;
                        }
                    }
                } else {
                    Constants.LOG.debug("{} Glory ip is NOT reachable from this interface", TAG);
                }
            }
        }
        return null;
    }

	private String getSessionId() {
		if (sessionId == null) {
			open();
//			occupy();
        } else {
            long sessionLiveTime = (System.currentTimeMillis() - sessionBirthTime) / 1000 / 60;
            if (sessionLiveTime > 700) {
                Constants.LOG.debug("{} session: {}; expired. Reopen session", TAG, sessionId);
                open();
//                occupy();
            }
		}
		return sessionId;
	}

    @Override
	public boolean isAlive() {
        return alive;
    }

    /*************ВЗАИМОДЕЙСТВИЕ С GLORY**************/
    /**
     * Подписаться на события
     */
    private Response registerEvent(final String ip, BigInteger port) {
        Constants.LOG.debug("{} session: {}; command: RegisterEventRequest. Subscribe destination: {}:{}", TAG, sessionId, ip, port);
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                RegisterEventRequestType param = fillParam(RegisterEventRequestType.class);//new RegisterEventRequestType();
                param.setUrl(ip);
                param.setPort(BigInteger.valueOf(eventPort));
                return fcc.registerEventOperation(param);
            }

        });
        Constants.LOG.debug("{} session: {}; command: RegisterEventRequest. Request result: {}", TAG, sessionId, result);
        return result;
    }

    /**
     * Отписаться от получения событий
     */
    private Response unregisterEvent(final String ip, BigInteger port) {
        Constants.LOG.debug("{} session: {}; command: UnRegisterEventRequest. Unsubscribe {}:{}", TAG, sessionId, ip, port);
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                UnRegisterEventRequestType param = fillParam(UnRegisterEventRequestType.class);
                param.setUrl(ip);
                param.setPort(BigInteger.valueOf(eventPort));
                return fcc.unRegisterEventOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; command: UnRegisterEventRequest. Request result: {}", TAG, sessionId, result);
        return result;
    }

    /**
     * Отменить запрос конкретной суммы
     *
     */
    @Override
	public Response cancelCashRequest() {
        Constants.LOG.debug("{} session: {}; command: CANCEL CASH REQUEST, Currentthread.name() = {}", TAG, sessionId, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ChangeCancelRequestType param = fillParam(ChangeCancelRequestType.class);
                return fcc.changeCancelOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; command: CANCEL CASH REQUEST. Request result: {}, Currentthread.name() = {}", TAG, sessionId, result,
            Thread.currentThread().getName());
        close();
		return result;
    }

    /**
     * Выдать деньги (номиналы и количество передать через cashForOut)
     *
     */
    @Override
    public Response cashOut(final CashType cashForOut) {
        Constants.LOG.debug("{} session: {}; command: CASH OUT(CashType), Currentthread.name() = {}", TAG, sessionId, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                CashoutRequestType param = fillParam(CashoutRequestType.class);
                param.setCash(cashForOut);
                return fcc.cashoutOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; command: CASH OUT. Request result: {}, Currentthread.name() = {}", TAG, sessionId, result, Thread.currentThread().getName());
		return result;
    }

    @Override
    public Response cashOut(long amount) {
        Constants.LOG.debug("{} session: {}; command: CASH OUT({}), Currentthread.name() = {}", TAG, sessionId, amount, Thread.currentThread().getName());
        Response result = null;
        try {
            result = cashOut(getDenominationsByAmount(amount));
        } catch (Exception e) {
            Constants.LOG.error("{} session: {}; command: CASH OUT({}). Currentthread.name() = {}: {}", TAG, sessionId, amount, Thread.currentThread().getName(),
                ExceptionUtils.getFullStackTrace(e));
        }
        Constants.LOG.error("{} session: {}; command: CASH OUT({}). Request result: {}, Currentthread.name() = {}", TAG, sessionId, amount, String.valueOf(result),
            Thread.currentThread().getName());
        return result;
    }



    /**
     * Получить статус оборудования
     *
     */
    @Override
	public StatusResponseType getStatus() {
        Constants.LOG.debug("{} session: {}; command: GET STATUS(type = {}), Currentthread.name() = {}", TAG, sessionId, 0, Thread.currentThread().getName());
        StatusRequestType param = fillParam(StatusRequestType.class);
        StatusOptionType opt = new StatusOptionType();
        opt.setType(BigInteger.ZERO);
        param.setOption(opt);
        StatusResponseType status = fcc.getStatus(param);
        Constants.LOG.debug("{} session: {}; Result command GET STATUS: {}, Currentthread.name() = {}", TAG, sessionId,
            status != null && status.getStatus() != null ? status.getStatus().getCode() : "NA", Thread.currentThread().getName());
        return status;
    }

    /**
     * Requests “CI-10(ISP-K05)” to start change transaction.<br>
     * Respond cash in information and cash out information after the normal finish of change<br>
     * transaction.<br>
     * When change transaction was cancelled, cash in information and amount of the refund<br>
     * will be responded.<br>
     *
     * @param amount
     */
    @Override
    public Response cashRequest(final long amount) {
        open();
//        occupy();
        Constants.LOG.debug("{} session: {}; command: CASH REQUEST(amount = {}), Currentthread.name() = {}", TAG, sessionId, amount, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ChangeRequestType param = fillParam(ChangeRequestType.class);
                param.setAmount(String.valueOf(amount));
                CashType cash = new CashType();
                param.setCash(cash);
                return fcc.changeOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; command: CASH REQUEST(amount = {}). Request result: {}, Currentthread.name() = {}", TAG, sessionId, amount, result,
            Thread.currentThread().getName());
//        release();
        close();
		return result;
    }

    /**
     * To control “CI-10(ISP-K05)” power status（Shutdown/ Reboot）
     *
     * @param type
     *            0 - выключить 1 - перезагрузить
     */
    @Override
    public Response powerControl(final int type) {
        Constants.LOG.debug("{} session: {}; command: POWER CONTROL(type = {}), Currentthread.name() = {}", TAG, sessionId, type, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                PowerControlRequestType param = fillParam(PowerControlRequestType.class);
                PowerControlOptionType opt = new PowerControlOptionType();
                opt.setType(BigInteger.valueOf(type));
                param.setOption(opt);
                return fcc.powerControlOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; command: POWER CONTROL. Request result: {}, Currentthread.name() = {}", TAG, sessionId, result,
            Thread.currentThread().getName());
		return result;
    }

    /**
     * Requests canceling cash-in transaction to the device.<br>
     * Except “CI-10(ISP-K05)” is in the middle of cash-in transaction, returns result=11<br>
     * exclusive error.<br>
     *
     */
    @Override
	public Response cancelCashIn() {
        Constants.LOG.debug("{} session: {}; command: CANCEL CASH IN, Currentthread.name() = {}", TAG, sessionId, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                CashinCancelRequestType param = fillParam(CashinCancelRequestType.class);
                return fcc.cashinCancelOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; command: CANCEL CASH IN. Request result: {}, Currentthread.name() = {}", TAG, sessionId, result,
            Thread.currentThread().getName());
		return result;
    }

    /**
     * Requests start transaction of cash in to the device.<br>
     * The device enters cash in status by “StartCashinRequest”.<br>
     * Cash in status device ends this status by endCashIn command.<br>
     *
     */
    @Override
	public Response cashIn() {
    	close();
        Constants.LOG.debug("{} session: {}; command: CASH IN, Currentthread.name() = {}", TAG, sessionId, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                StartCashinRequestType param = fillParam(StartCashinRequestType.class);
                return fcc.startCashinOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; command: CASH IN. Request result: {}, Currentthread.name() = {}", TAG, sessionId, result, Thread.currentThread().getName());
		return result;
    }

    /**
     * Requests ending cash-in transaction to the device.<br>
     * The device enters the status of end of cash in transaction by “EndCashinRequest”.<br>
     * Except the device is in the middle of the cash in transaction,<br>
     * returns exclusive error (result=11).<br>
     *
     */
    @Override
	public Response cashEnd() {
        Constants.LOG.debug("{} session: {}; command: CASH END, Currentthread.name() = {}", TAG, sessionId, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                EndCashinRequestType param = fillParam(EndCashinRequestType.class);
                return fcc.endCashinOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; command: CASH END. Request result: {}, Currentthread.name() = {}", TAG, sessionId, result, Thread.currentThread().getName());
		return result;
    }

    /**
     * To get the device inventory.<br>
     * To get the information of device inventory and payable number of the moneys.<br>
     *
     */
    @Override
    public InventoryResponseType inventory() {
        Constants.LOG.debug("{} session: {}; command: INVENTORY", TAG, sessionId, Thread.currentThread().getName());

        InventoryRequestType param = fillParam(InventoryRequestType.class);
        InventoryOptionType opt = new InventoryOptionType();
        opt.setType(BigInteger.ZERO);
        param.setOption(opt);
        InventoryResponseType inventory = fcc.inventoryOperation(param);
        Response result = converter.convResponse(inventory.getResult().intValue());
        if (result == Response.INVALID_SESSION) {
            Constants.LOG.warn("{}{}, Currentthread.name() = {}", TAG, " INVALID_SESSION. Try to reopen session and repeat.", Thread.currentThread().getName());
            open();
            return inventory();
        }
        inventory.setResponse(result);
        Constants.LOG.debug("{} session: {}; command: INVENTORY. Request result: {}, Currentthread.name() = {}", TAG, sessionId, result,
            Thread.currentThread().getName());
        return inventory;
    }

    /**
     * Вернуть монеты и купюры, положенные в приемник, пока оборудование было неактивно
     *
     */
    @Override
	public Response returnCash() {
        Constants.LOG.debug("{} session: {}; command: RETURN CASH, Currentthread.name() = {}", TAG, sessionId, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ReturnCashRequestType param = fillParam(ReturnCashRequestType.class);
                ReturnCashOptionType opt = new ReturnCashOptionType();
                opt.setType(BigInteger.ZERO);
                param.setOption(opt);
                return fcc.returnCashOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; command: RETURN CASH. Request result: {}, Currentthread.name() = {}", TAG, sessionId, result,
            Thread.currentThread().getName());
		return result;
    }

//    public OccupyResponseType occupy() {
    //        Constants.LOG.debug("{} session: {}; command: OCCUPY", sessionId);
//        OccupyRequestType param = fillParam(OccupyRequestType.class);
//        OccupyResponseType result = fcc.occupyOperation(param);
    //        Constants.LOG.debug("{} session: {}; command: OCCUPY. Request result: {}", TAG, sessionId, result.getResult());
//		return result;
//    }
//
//    public ReleaseResponseType release() {
    //        Constants.LOG.debug("{} session: {}; command: RELEASE", sessionId);
//        ReleaseRequestType param = fillParam(ReleaseRequestType.class);
//        ReleaseResponseType result = fcc.releaseOperation(param);
    //        Constants.LOG.debug("{} session: {}; command: RELEASE. Request result: {}", TAG, sessionId, result.getResult());
//		return result;
//    }

    @Override
	public Response open() {
        Constants.LOG.debug("{} session: {}; command: OPEN, Currentthread.name() = {}", TAG, sessionId, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                OpenRequestType param = new OpenRequestType();
                param.setId("1");
                param.setSeqNo("1234");
                param.setUser("sco");
                param.setUserPwd("sco");
                param.setDeviceName("");
                OpenResponseType openReponse = fcc.openOperation(param);
                sessionId = openReponse.getSessionID();
                sessionBirthTime = System.currentTimeMillis();
                return openReponse;
            }
        });
        Constants.LOG.debug("{} command: OPEN. Request result: {}. SessionId: {}, Currentthread.name() = {}", TAG, result, sessionId, Thread.currentThread().getName());
        return result;
    }

    @Override
	public Response close() {
        Constants.LOG.debug("{} session: {}; command: CLOSE, Currentthread.name() = {}", TAG, sessionId, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                CloseRequestType param = fillParam(CloseRequestType.class);
                return fcc.closeOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; command: CLOSE. Request result: {}, Currentthread.name() = {}", TAG, sessionId, result, Thread.currentThread().getName());
        sessionId = null;
		return result;
    }

    @Override
    public Response collect(final CashType cash, final boolean collectMix, final boolean toExit) {
        Constants.LOG.debug("{} session: {}; command: COLLECT, Currentthread.name() = {}", TAG, sessionId, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                CollectRequestType param = fillParam(CollectRequestType.class);
                { // Option - куда изымаем
                    CollectOptionType opt = new CollectOptionType();
                    opt.setType(toExit ? COLLECT_TO_EXIT : COLLECT_TO_CASSETE);
                    param.setOption(opt);
                }
                { // Конфигурация миксера
                    CollectOptionType option = new CollectOptionType();
                    option.setType(collectMix ? COLLECT_WITH_MIXER : COLLECT_WITHOUT_MIXER);
                    param.setMix(option);
                }
                { // Дополнительная информация
                    CashType internalCash = cash;
                    if (internalCash == null) {
                        internalCash = new CashType();
                    }
                    internalCash.setType(collectMix ? MIXED_STACKER : DENOMINATION_CONTROL);
                    param.setCash(internalCash);
                }
                return fcc.collectOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; command: COLLECT. Request result: {}, Currentthread.name() = {}", TAG, sessionId, result, Thread.currentThread().getName());
        return result;
    }

    @Override
	public Response reset() {
    	close();
        Constants.LOG.debug("{} session: {}; command: RESET MACHINES, Currentthread.name() = {}", TAG, sessionId, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ResetRequestType param = fillParam(ResetRequestType.class);
                return fcc.resetOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; result: RESET MACHINES {}, Currentthread.name() = {}", TAG, sessionId, result, Thread.currentThread().getName());
        return result;
    }

    @Override
    public Response lock(final DeviceType type) {
        Constants.LOG.debug("{} session: {}; command: LOCK (type = {}), Currentthread.name() = {}", TAG, sessionId, type, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                LockUnitRequestType param = fillParam(LockUnitRequestType.class);
                LockUnitOptionType opt = new LockUnitOptionType();
                opt.setType(type.getType());
                param.setOption(opt);
                return fcc.lockUnitOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; result: LOCK {}, Currentthread.name() = {}", TAG, sessionId, result, Thread.currentThread().getName());
		return result;
    }

    @Override
    public Response unlock(final DeviceType type) {
        Constants.LOG.debug("{} session: {}; command: UNLOCK (type = {}), Currentthread.name() = {}", TAG, sessionId, type, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                UnLockUnitRequestType param = fillParam(UnLockUnitRequestType.class);
                UnLockUnitOptionType opt = new UnLockUnitOptionType();
                opt.setType(type.getType());
                param.setOption(opt);
                return fcc.unLockUnitOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; result: UNLOCK {}, Currentthread.name() = {}", TAG, sessionId, result, Thread.currentThread().getName());
        return result;
    }

    @Override
    public Response cashManualDeposit(final long manualAmount) {
        Constants.LOG.debug("{} session: {}; command: MANUAL DEPOSIT, Currentthread.name() = {}", TAG, sessionId, Thread.currentThread().getName());
        Response result = execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                UpdateManualDepositTotalRequestType param = fillParam(UpdateManualDepositTotalRequestType.class);
                param.setAmount(String.valueOf(manualAmount));
                DepositCurrencyType value = new DepositCurrencyType();
                param.setDepositCurrency(value);
                return fcc.updateManualDepositTotalOperation(param);
            }
        });
        Constants.LOG.debug("{} session: {}; result: UNLOCK {}, Currentthread.name() = {}", TAG, sessionId, result, Thread.currentThread().getName());
		return result;
    }

    private <T> T fillParam(Class<T> c) {
        T object = null;
        try {
            object = c.newInstance();
            Class< ? extends Object> clazz = object.getClass();
            Method setId = clazz.getMethod("setId", String.class);
            Method setSeqNo = clazz.getMethod("setSeqNo", String.class);
            Method setSessionID = clazz.getMethod("setSessionID", String.class);
            setId.invoke(object, ID);
            setSeqNo.invoke(object, SEQ_NO);
            setSessionID.invoke(object, getSessionId());
        } catch (Exception e) {
            Constants.LOG.error("{}{}", TAG, ExceptionUtils.getFullStackTrace(e));
        }
        return object;
    }

    ExecutorService ex = Executors.newCachedThreadPool();

    private Response execute(Callable<Object> task) {
        Object response;
        try {
            response = ex.submit(task).get();
            Class<? extends Object> clazz = response.getClass();
            Method getResult;
            getResult = clazz.getMethod("getResult");
            int code = ((BigInteger) getResult.invoke(response)).intValue();
            Response result = gloryConverter.convResponse(code);
            result.setCode(code);
            if (result == Response.INVALID_SESSION) {
                Constants.LOG.warn("{} method execute(task): {}, Currentthread.name() = {}", TAG, " INVALID_SESSION. Try to reopen session and repeat.",
                    Thread.currentThread().getName());
                open();
                return execute(task);
            }
            return result;

        } catch (Exception e) {
            Constants.LOG.error("{} method execute(task): {}, Currentthread.name() = {}", TAG, ExceptionUtils.getFullStackTrace(e), Thread.currentThread().getName());
        }
        return Response.NA;
	}

    @Override
    public CashType getDenominationsByAmount(long amount) throws CashMachineException {
        InventoryResponseInterface inventory = inventory();
        CashType resultCash = new CashType();
        CashInterface cash = inventory.getDispensableCash();
        List<? extends DenominationInterface> denominations = cash.getDenomintaions();
        Collections.sort(denominations, new Comparator<DenominationInterface>() {
            @Override
            public int compare(DenominationInterface o1, DenominationInterface o2) {
                return Long.compare(o2.getValueInf(), o1.getValueInf());
            }

        });
        for (DenominationInterface denomination : denominations) {
            if (denomination.getPieceInf() == 0 || denomination.getValueInf() == 0 || !denomination.getCurrencyInf().equals("RUB")) {
                continue;
            }
            long piece = amount / denomination.getValueInf();
            if (piece > denomination.getPieceInf()) {
                piece = denomination.getPieceInf();
            }
            denomination.setPieceInf(piece);
            resultCash.addDenomination(converter.convToDenomintionType(denomination));
            amount -= denomination.getAmountInf();
            if (amount == 0) {
                break;
            }
        }
        if (amount > 0) {
            throw new CashMachineException("Не достаточно номиналов");
        }
        return resultCash;
    }

    @Override
    public DenominationType convToDenomintionType(DenominationInterface denomination) {
        return converter.convToDenomintionType(denomination);
    }

    @Override
    public boolean isAccessibleByIP(int timeout){
        if (StringUtils.isBlank(gloryIp)) {
            return false;
        }
        try {
            List<NetworkInterface> netInts = Collections.list(NetworkInterface.getNetworkInterfaces());
            if (netInts.size() == 1) {
                return false;
            }
            for (NetworkInterface net : netInts) {
                if ("eth10".equals(net.getDisplayName())) {
                    Constants.LOG.debug("{} Skipped RNDIS interface", TAG);
                    continue;
                }
                if (!net.isLoopback() && !net.isVirtual() && net.isUp()) {
                    InetAddress glory = InetAddress.getByName(gloryIp);
                    if (glory.isReachable(net, 0, timeout == 0 ? 100 : Math.abs(timeout))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Constants.LOG.error("{} ERROR: {}", TAG, ExceptionUtils.getFullStackTrace(e));
            return false;
        }
        return false;
    }
}
