package ru.crystals.pos.visualization.login.password;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.msr.MSREvent;
import ru.crystals.pos.techprocess.StateCash;
import ru.crystals.pos.techprocess.TechProcessServiceAsync;
import ru.crystals.pos.user.Right;
import ru.crystals.pos.user.UserEntity;
import ru.crystals.pos.utils.CommonLogger;
import ru.crystals.pos.visualization.CashStateChangeListener;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.eventlisteners.BarcodeEventListener;
import ru.crystals.pos.visualization.eventlisteners.EnterEventListener;
import ru.crystals.pos.visualization.eventlisteners.EscEventListener;
import ru.crystals.pos.visualization.eventlisteners.RefreshUserListEventListener;
import ru.crystals.pos.visualization.eventlisteners.ShiftMustCloseEvent;
import ru.crystals.pos.visualization.login.CommonLogin;
import ru.crystals.pos.visualization.login.LoginContainer;

import javax.swing.SwingUtilities;
import java.util.concurrent.Executor;

public class PasswordLoginContainer extends LoginContainer implements EnterEventListener, EscEventListener, CashStateChangeListener,
        RefreshUserListEventListener, BarcodeEventListener, MSREvent, ShiftMustCloseEvent {

    private static final Logger commonLogger = CommonLogger.getCommonLogger();

    private Logger logger = LoggerFactory.getLogger(PasswordLoginContainer.class);

    private PasswordLoginComponent visualPanel = null;
    private String password = null;
    private String barcodeID = null;
    private String magneticCard = null;
    private boolean isProcessing = false;
    private boolean isLogining = false;

    @Override
    public PasswordLoginComponent getVisualPanel() {
        if (visualPanel == null)
            visualPanel = new PasswordLoginComponent();
        return visualPanel;
    }

    @Override
    public void enter() {
        password = getVisualPanel().getInputPanel().getInputField().getText();
        if (password.equals("") && barcodeID.equals("") && magneticCard.equals(""))
            return;

        if (isLogining)
            return;

        isLogining = true;

        isProcessing = true;
        final UserEntity user1 = new UserEntity();

        if ((barcodeID != null) && (!barcodeID.isEmpty())) {
            password = null;
            magneticCard = null;
        }

        if ((magneticCard != null) && (!magneticCard.isEmpty())) {
            password = null;
            barcodeID = null;
        }

        user1.setPasswordUnique(password);
        user1.setBarcode(barcodeID);
        user1.setMagneticCard(magneticCard);
        try {
            logger.info("Start LOGIN");

            getVisualPanel().getInputPanel().getInputField().setEnabled(false);
            Factory.getTechProcessImpl().userLogin(user1, new TechProcessServiceAsync.UserLogin() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void userNotFound(UserEntity user) {
                    getVisualPanel().getInputPanel().getInputField().setEnabled(true);
                    ((LoginContainer) getFactory().getMainWindow().getCurrentContainer()).eventUserNotFound(user);

                    isLogining = false;
                }

                @Override
                public void userAccessDenied(UserEntity user, String reason) {
                    getVisualPanel().getInputPanel().getInputField().setEnabled(true);
                    ((LoginContainer) getFactory().getMainWindow().getCurrentContainer()).eventUserAccessDenied(user, reason);

                    isLogining = false;
                }

                @Override
                public void onSuccess(UserEntity user) {

                    getVisualPanel().hideShiftIcon();

                    if (getFactory().getMainWindow().getCurrentContainer() instanceof LoginContainer)
                        ((LoginContainer) getFactory().getMainWindow().getCurrentContainer()).eventUserAuthenticate(user);

                    commonLogger.info("getFactory().getTechProcess().isShiftOpen() = " + Factory.getTechProcessImpl().isShiftOpen());
                    commonLogger.info("getFactory().getTechProcess().checkUserRight(Right.SHOW_MAIN_MODE)) = "
                            + getFactory().checkUserRight(Right.SHOW_MAIN_MODE));

                    CommonLogin.getInstance().login(getFactory(), user);
                    getFactory().getMainWindow().getStatus().setUsername(user.getStringViewShort());

                    isLogining = false;
                    PasswordLoginContainer.this.logger.info("Finish LOGIN");
                }
            });
        } catch (Exception e) {
            PasswordLoginContainer.this.logger.error("LOGIN ERROR", e);
            ((LoginContainer) getFactory().getMainWindow().getCurrentContainer()).reset();
        }

    }

    @Override
    public void esc() {
        if (!isProcessing)
            reset();
    }

    @Override
    public void eventStateCashChange(StateCash stateCash, Integer keyPosition) {
        logger.info("Cashstate: " + stateCash);
    }

    @Override
    public void eventUserAccessDenied(UserEntity user, String reason) {
        if (reason!= null){
            getVisualPanel().setLoginFailed(true, reason);
        } else {
            getVisualPanel().setLoginFailed(true, ResBundleVisualization.getString("ACCESS_DENIED"));
        }
        password = "";
        barcodeID = "";
        magneticCard = "";
        isProcessing = false;
    }

    @Override
    public void eventUserAuthenticate(UserEntity user) {
        setUser(user);
        dispatchLoginEvent();
        isProcessing = false;
    }

    @Override
    public void eventUserNotFound(UserEntity user) {
        getVisualPanel().setLoginFailed(true, ResBundleVisualization.getString("LOGIN_FAILED"));
        password = ""; // NOSONAR
        barcodeID = "";
        magneticCard = "";
        isProcessing = false;
    }

    @Override
    public boolean isVisualPanelCreated() {
        return visualPanel != null;
    }

    @Override
    public void reset() {
        SwingUtilities.invokeLater(() -> {
            isProcessing = false;
            password = ""; // NOSONAR
            barcodeID = "";
            magneticCard = "";
            getVisualPanel().getInputPanel().getInputField().setEnabled(true);
            getVisualPanel().setLoginFailed(false, "");
            (visualPanel).updateShiftLabelText();
            (visualPanel).updateCheckPresentLabelText();
        });
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;

    }

    @Override
    public void refreshUserList() {
        logger.debug("Refresh user list not supported");
    }

    @Override
    public void barcode(String barcode) {
        if (!isProcessing) {
            this.barcodeID = barcode;
            logger.debug("Try to authenticate with barcode " + barcode);
            getVisualPanel().setLoginFailed(false, "");
            enter();
        }
    }

    @Override
    public void eventMSR(String Track1, String Track2, String Track3, String Track4) {
        if (!isProcessing) {
            this.magneticCard = (Track1 != null) ? Track1 : Track2;
            logger.debug("Try to authenticate with card " + magneticCard);
            getVisualPanel().setLoginFailed(false, "");
            enter();
        }
    }

    @Override
    public void eventShiftMustClose(int minutes) {
        getVisualPanel().showShiftIcon();
        reset();
    }
}
