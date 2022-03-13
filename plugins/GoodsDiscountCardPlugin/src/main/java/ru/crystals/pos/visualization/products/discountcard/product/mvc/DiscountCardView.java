package ru.crystals.pos.visualization.products.discountcard.product.mvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.cards.internal.good.processing.FillHolderIdFormMessages;
import ru.crystals.pos.cards.exception.CardsException;
import ru.crystals.pos.catalog.ProductDiscountCardEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.CheckState;
import ru.crystals.pos.check.PositionDiscountCardEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.CommonAbstractView;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm.ExitState;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonSpinnerForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonYesNoForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.exception.NoPermissionException;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.discountcard.ResBundleGoodsDiscountCard;

import java.awt.event.KeyEvent;

/**
 * Графическая часть ("морда") для анимирования сценария "Продажа ДК (Дисконтной Карты)".
 * <p/>
 * Техпроцесс продажи ДК следующий (Flow Control):
 * <ol>
 * <li>ввести номер карты;
 * <li>[если карта персонализированная и допускается не заполнять анкету клиента при продаже] диалог на ввод анкетных данных;
 * <li>[если заполнение анкеты обязательно, либо ответили "Да" на диалог из предыдущего пункта] ввести идентификатор анкеты клиента, для которого
 * покупается эта карта;
 * <li>сразу же активировать карту по необходимости;</li>
 * <li>показать финальное окно - отображение того. что получилось после всех диалогов перед тем как добавить позицию в чек;
 * <li>применить добавленную карту в чеке - если еще не применена ни одна внутренняя карта.
 * </ol>
 *
 * @author aperevozchikov
 */
public class DiscountCardView extends CommonAbstractView<DiscountCardController> {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(DiscountCardView.class);

    /**
     * 1я форма - форма ввода номера ДК
     */
    private final DiscountCardEnterNumberForm enterNumberForm;

    /**
     * 2я форма - диалог "будете вводить анкетные данные?"
     */
    private final CommonYesNoForm fillApplicationDlg;

    /**
     * 3я форма - форма ввода ID картоносца
     */
    private final DiscountCardEnterHolderIdForm enterHolderIdForm;

    // Вспомогательные формы:

    /**
     * "Крутилка"
     */
    private final CommonSpinnerForm spinnerForm;

    /**
     * Форма быстрого удаления позиции
     */
    private CommonDeletePositionConfirmForm<ProductDiscountCardEntity, PositionDiscountCardEntity> deletePositionForm = null;

    /**
     * Форма удаления позиции через редактирование чека
     */
    private final DiscountCardEditOrDeleteForm editOrDeleteForm;


    public DiscountCardView() {
        DiscountCardView.this.setName("ru.crystals.pos.visualization.products.discountcard.product.mvc.DiscountCardView");
        this.enterNumberForm = new DiscountCardEnterNumberForm(this);
        this.fillApplicationDlg = new CommonYesNoForm(this);
        this.fillApplicationDlg.setMessageLabelText(ResBundleGoodsDiscountCard.getString("FILL_APPLICATION_QUESTION"));
        this.fillApplicationDlg.setButtonsCaptions(ResBundleVisualization.getString("BUTTON_YES"), ResBundleVisualization.getString("BUTTON_NO"));

        this.enterHolderIdForm = new DiscountCardEnterHolderIdForm(this);
        this.spinnerForm = new CommonSpinnerForm(this, "");
        this.deletePositionForm = new CommonDeletePositionConfirmForm<>(this);
        this.editOrDeleteForm = new DiscountCardEditOrDeleteForm(this);

        this.addPanel(enterNumberForm);
        this.addPanel(fillApplicationDlg);
        this.addPanel(enterHolderIdForm);
        this.addPanel(spinnerForm);
        this.addPanel(deletePositionForm);
        this.addPanel(editOrDeleteForm);
    }

    /**
     * Вернет {@code true}, если в данный момент отображается какое-либо сообщение об ошибке, без "снятия" которого нельзя продолжить работу
     * (например, перейти сразу к подытогу или добавить позицию в чек)
     *
     * @return
     */
    public boolean showsErrorMessage() {
        return currentForm == messageForm;
    }

    /**
     * Вернет {@code true}, если в данный момент отображается какое-либо сообщение (спиннер, либо сообщение об ошибке), без "снятия" которого нельзя
     * продолжить работу (например, перейти сразу к подытогу или добавить позицию в чек)
     *
     * @return
     */
    public boolean showsMessage() {
        return currentForm == spinnerForm || currentForm == messageForm;
    }

    @Override
    public void modelChanged() {
        switch (getController().getModel().getState()) {
            case ADD_CURRENT:
                // сканировали (или нажали ПОДЫТОГ) на неправильной форме
                getController().beepError("Cannot add discount card position from this form " + currentForm);
                break;
            case ADD:
                getController().getModel().setRefund(isReturnPurchase());
                modelChangedSaleScenarion(getController().getModel());
                break;
            case QUICK_EDIT: //Залипуха (при нагруженной кассе, из разных потоков иногда события на смену стейта приходят в неожиданной последовательности)
            case EDIT_OR_DELETE:
                // редактирование позиции
                setCurrentForm(editOrDeleteForm);
                break;
            case DELETE:
            case QUICK_DELETE:
                setCurrentForm(deletePositionForm, false);
                break;
            default:
                log.warn("unpredicted model state: {}", getController().getModel().getState());
                break;
        } // switch
    }


    private boolean isReturnPurchase() {
        PurchaseEntity purchaseEntity = Factory.getTechProcessImpl().getCheck();
        if (purchaseEntity != null) {
            return purchaseEntity.isReturn();
        } else {
            return Factory.getInstance().getMainWindow().getCheckContainer().getState() == CheckState.RETURN_CHECK;
        }
    }

    /**
     * Событие изменения модели в сценарии продажи (здесь: добавления позиции в чек) ДК
     *
     * @param model
     */
    private void modelChangedSaleScenarion(DiscountCardModel model) {
        log.trace("entering modelChangedSaleScenarion(DiscountCardModel)");

        DiscountCardPluginState state = getModelState(model);
        switch (state) {
            case ENTER_CARD_NUMBER:
                // начало техпроцесса
                // 1. очистим все поля ввода
                enterNumberForm.clear();
                enterHolderIdForm.clear();

                // 2. надо показать форму ввода номера карты
                enterNumberForm.changeInputFieldHeader(isReturnPurchase());
                setCurrentForm(enterNumberForm);
                break;
            case FILL_HOLDER_APPLICATION_DLG:
                // диалог "Будете вводить анкетные данные?"
                setCurrentForm(fillApplicationDlg);
                break;
            case ENTER_HOLDER_ID:
                // форма ввода ID картоносца
                // для начала поправим сообщения на форме ввода "идентификатора клиента"
                FillHolderIdFormMessages msgs = model.getHolderIdFormMessages();
                enterHolderIdForm.setInputPanelHeader(msgs.getTitle());
                enterHolderIdForm.setInputPanelWelcomeText(msgs.getWelcomeText());
                setCurrentForm(enterHolderIdForm);
                break;
            case ACTIVATE_CARD:
                if (showsErrorMessage()) {
                    getController().back();
                    break;
                }
                try {
                    getController().activateCard();
                } catch (CardsException cex) {
                    log.error("", cex);
                    showMessageForm(cex.getMessage(), ExitState.TO_LAST);
                }
                break;
            default:
                // а вот ничего. видимо это состояние применения карты в чеке - не имеет визуального представления все равно
                break;
        } // switch

        log.trace("leaving modelChangedSaleScenarion(DiscountCardModel)");
    }

    /**
     * Вернет состояние указанной модели.
     *
     * @param model
     * @return
     */
    private DiscountCardPluginState getModelState(DiscountCardModel model) {
        DiscountCardPluginState result = DiscountCardPluginState.ENTER_CARD_NUMBER;
        if (model != null && model.getStage() != null) {
            result = model.getStage();
        }
        return result;
    }

    @Override
    public boolean checkDataBeforeAdd() {
        return true;
    }

    @Override
    public long getCurrentPositionCount() {
        return BigDecimalConverter.getQuantityMultiplier();
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
            return true;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return processEnterEvent();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            // окошко с сообщением надо сначала "снять" - прежде чем начать реагировать на что-то еще
            if (currentForm == messageForm) {
                modelChanged();
                return true;
            }
            if (currentForm == spinnerForm) {
                // истерично жмут ОТМЕНА на "крутилке" - пробросим этот ESC выше - пусть там разрулят
                log.info("ESC on the spinner form detected!");
                return false;
            }

            // начали движение назад? Очистим все поля ввода:
            enterNumberForm.clear();
            enterHolderIdForm.clear();

            // тут можно по диалоговым окнам в обратном направлении пойти - вплоть до отказа добавления позиции в чек - как будто ничего и не сканировали
            return getController().back();
        }
        return false;
    }

    /**
     * Обработка события нажатия клавиши "ВВОД".
     *
     * @return {@code true}, если событие нажатия ENTER было обработано; иначе - вернет {@code false}
     */
    private boolean processEnterEvent() {
        // см. техпроцесс продажи ДК в javadoc
        if (currentForm == editOrDeleteForm) {
            // удаление позиции через редактирование чека
            getController().returnPosition(getController().getModel().getPosition());
        } else if (currentForm == enterNumberForm) {
            processCardNumberEntered(enterNumberForm.getEnteredNumber());
        } else if (currentForm == fillApplicationDlg) {
            getController().setFillApplicationInfo(fillApplicationDlg.isYes());
        } else if (currentForm == enterHolderIdForm) {
            processHolderIdEntered(enterHolderIdForm.getEnteredNumber());
        } else if (currentForm == messageForm) {
            // у нас все сообщения не критичные - покажем предыдущую форму ввода
            modelChanged();
        } else if (currentForm == deletePositionForm) {
            // на форме быстрого удаления нажали ентер - проанализируем
            if (deletePositionForm.deleteConfirmed()) {
                // 1. Удалим карту из списка позиций
                boolean checkPermission;
                try {
                    checkPermission = getController().tryRequestPermissionDeletePosition(getController().getModel().getPosition(),
                            getController().getModel().getState() == ProductContainer.ProductState.DELETE);
                } catch (NoPermissionException ex) {
                    getController().beepError(ex.getMessage());
                    return true;
                }
                getController().cashDeletePosition(getController().getModel().getPosition(), null, !checkPermission);

                // 2. и из списка примененных карт
                getController().removeCardFromAppliedOnes(getController().getModel());
            } else {
                getController().getAdapter().dispatchCloseEvent(false);
            }
        } else {
            // х.з., что за форма. событие не обработано
            log.warn("illegal form is active: {} while processing ENTER", currentForm);
            return false;
        }

        return true;
    }

    /**
     * Обработка события завершения ввода номера карты
     *
     * @param cardNumber номер карты, что был введен
     */
    private void processCardNumberEntered(String cardNumber) {
        showSpinnerForm(ResBundleGoodsDiscountCard.getString("LOOKING_FOR_APPROPRIATE_HANDLER"));
        try {
            getController().setCardNumber(cardNumber);
            // успех! номер карты валиден

            //Теперь в зависимости от обработчика изменим что-нибудь на форме/в техпроцессе/т.д.
            switch (getController().getModel().getHandler().getId()) {
                case ML:
                    //SR-1576
                    this.fillApplicationDlg.setMessageLabelText(ResBundleGoodsDiscountCard.getString("ML_FILL_APPLICATION_QUESTION"));
                    this.fillApplicationDlg.setButtonsCaptions(
                            ResBundleGoodsDiscountCard.getString("ML_FILL_APPLICATION_QUESTION_YES"),
                            ResBundleGoodsDiscountCard.getString("ML_FILL_APPLICATION_QUESTION_NO")
                    );
                    break;
                default:
                    this.fillApplicationDlg.setMessageLabelText(ResBundleGoodsDiscountCard.getString("FILL_APPLICATION_QUESTION"));
                    this.fillApplicationDlg.setButtonsCaptions(ResBundleVisualization.getString("BUTTON_YES"), ResBundleVisualization.getString("BUTTON_NO"));
            }

        } catch (CardsException e) {
            // не важно по какой причине облажались - после сообщения об ошибке следующей будет снова форма ввода номера карты
            showMessageForm(e.getMessage(), ExitState.TO_LAST);
        }
    }

    /**
     * Обработка события завершения ввода ID клиента
     *
     * @param holderId идентификатор клиента, что был введен
     */
    private void processHolderIdEntered(String holderId) {
        showSpinnerForm(ResBundleGoodsDiscountCard.getString("VALIDATING_HOLDER_ID"));
        try {
            getController().setApplicationNumber(holderId);
            // успех! идентификатор клиента валиден
        } catch (CardsException e) {
            // не важно по какой причине облажались - после сообщения об ошибке следующей будет снова форма ввода идентификатор клиента
            showMessageForm(e.getMessage(), ExitState.TO_LAST);
        }
    }

    @Override
    public boolean dispatchBarcodeScanned(String barcode) {
        if (currentForm == messageForm) {
            // "сними" окошко, дура, а потом сканируй всякую херь
            log.error("barcode was scanned (and ignored) while the message form was active!");
            return true;
        }

        if (currentForm == enterNumberForm) {
            // отсканировали номер карты
            processCardNumberEntered(barcode);
            return true;
        }

        if (currentForm == enterHolderIdForm) {
            // отсканировали ID анкеты клиента
            processHolderIdEntered(barcode);
            return true;
        }

        // какая-то другая, неправильная форма
        //  (диалог "ввести анкетные данные", "крутилка", окно сообщения, и проч)
        // надо "пикнуть" и сказать, что событие обработано
        getController().beepError("Barcode was scanned on an illegal form " + currentForm);
        return true;
    }

    @Override
    public boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        if (currentForm == messageForm) {
            // "сними" окошко, дура, а потом катай карту
            log.error("MSR event was detected (and ignored) while the message form was active!");
            return true;
        }

        if (currentForm == enterNumberForm) {
            // прокатали карту в поле ввода номера карты
            try {
                String cardNumber = Factory.getTechProcessImpl().getCards().parseCardNumber(track1, track2, track3, track4);
                if (cardNumber != null) {
                    processCardNumberEntered(cardNumber);
                }
                return true;
            } catch (Throwable t) {
                log.error("failed to decipher card number from a magnetic strip!", t);
                return false;
            }
        }
        // х.з. на каком окне (без поля ввода!) прокатали что-то с магнитной полосой - пробросим выше
        return false;
    }

    private void showSpinnerForm(String msg) {
        String t = msg == null ? "" : msg.replace("\n", "<br>");
        spinnerForm.setTextMessage("<html><i>" + t + "</i></html>");
        setCurrentForm(spinnerForm);
    }

    private void showMessageForm(String msg, CommonMessageForm.ExitState exitState) {
        messageForm.setMessage(msg);
        messageForm.setExitState(exitState);
        setCurrentForm(messageForm);
    }


}
