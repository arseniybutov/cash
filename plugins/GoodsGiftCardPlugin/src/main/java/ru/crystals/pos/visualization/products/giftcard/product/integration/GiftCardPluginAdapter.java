package ru.crystals.pos.visualization.products.giftcard.product.integration;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.cards.common.CardStatus;
import ru.crystals.cards.presentcards.PresentCardInformationVO;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.cards.PluginCard;
import ru.crystals.pos.cards.PresentCards;
import ru.crystals.pos.catalog.ProductConfig;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductGiftCardEntity;
import ru.crystals.pos.check.CheckStatus;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionGiftCardEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.ShiftEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SimpleServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SlipsContainer;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TransactionData;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.integration.CommonAbstractPluginAdapter;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.products.giftcard.product.controller.GiftCardPluginController;
import ru.crystals.pos.visualization.products.giftcard.product.model.GiftCardPluginModel;
import ru.crystals.pos.visualization.products.giftcard.product.view.GiftCardPluginView;
import ru.crystals.pos.visualization.products.giftcard.ret.ValidateReturnlGiftCardForm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ProductCashPluginComponent(typeName = ProductDiscriminators.PRODUCT_GIFT_CARD_ENTITY, mainEntity = ProductGiftCardEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class GiftCardPluginAdapter extends CommonAbstractPluginAdapter {
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    private final ValidateReturnlGiftCardForm returnlGiftCardForm;
    private final GiftCardPluginModel model;
    private final GiftCardPluginController controller;
    private final GiftCardPluginView view;

    @Autowired
    public GiftCardPluginAdapter(ValidateReturnlGiftCardForm returnlGiftCardForm, GiftCardPluginController controller) {
        this.returnlGiftCardForm = returnlGiftCardForm;
        this.controller = controller;
        model = new GiftCardPluginModel();
        view = new GiftCardPluginView();

        model.setModelListener(view);
        view.setController(controller);
        this.controller.setModel(model);
        this.controller.setAdapter(this);
    }

    @Override
    protected GiftCardPluginModel getModel() {
        return model;
    }

    @Override
    protected GiftCardPluginController getController() {
        return controller;
    }

    @Override
    protected GiftCardPluginView getView() {
        return view;
    }

    @Override
    public void preparePrintCheck(Check check, PositionEntity p) {
        PositionGiftCardEntity pos = (PositionGiftCardEntity) p;
        TransactionData pt = new TransactionData(p.getDateTime());
        pt.getSlips().add(String.format(ResBundleGoodsGiftCard.getString("GIFT_CARD_POSITION_SLIP"),
                pos.getCardNumber(),
                (pos.getAmount() == null ? "" : CurrencyUtil.convertMoney(pos.getAmount()).toString()),
                (pos.getExpirationDate() == null ? "" : sdf.format(pos.getExpirationDate()))
        ));
        SlipsContainer sc = check.getCheckSlipsContainer(ProductDiscriminators.PRODUCT_GIFT_CARD_ENTITY);
        if (sc == null) {
            sc = new SlipsContainer(null);
            check.setCheckSlipsContainer(ProductDiscriminators.PRODUCT_GIFT_CARD_ENTITY, sc);
        }
        sc.add(pt);
    }

    @Override
    public void preparePrintCheck(List<ServiceDocument> serviceDocuments, PurchaseEntity purchase) {
        if (purchase.isSale() && purchase.getCheckStatus() != CheckStatus.Cancelled) {
            List<PositionGiftCardEntity> giftCardPositions = new ArrayList<>();
            for (PositionEntity p : purchase.getPositions()) {
                if (p instanceof PositionGiftCardEntity) {
                    giftCardPositions.add((PositionGiftCardEntity) p);
                }
            }

            if (!giftCardPositions.isEmpty()) {
                SimpleServiceDocument slip = new SimpleServiceDocument();
                slip.setPromo(true);
                List<String> rows = new ArrayList<>();
                rows.add(ResBundleGoodsGiftCard.getString("GIFT_CARD_POSITION_SLIP_HEADER"));
                rows.add("------------------------------------------");
                Date minDate = null;
                for (PositionGiftCardEntity p : giftCardPositions) {
                    if (minDate == null || (p.getExpirationDate() != null && minDate.getTime() > p.getExpirationDate().getTime())) {
                        minDate = p.getExpirationDate();
                    }
                    rows.add(String.format(ResBundleGoodsGiftCard.getString("GIFT_CARD_POSITION_SLIP"), p.getCardNumber(),
                            CurrencyUtil.convertMoney(p.getAmount()).toString()));
                }
                rows.add("------------------------------------------");
                if (minDate != null) {
                    rows.add(String.format(ResBundleGoodsGiftCard.getString("GIFT_CARD_POSITION_SLIP_FOOTER"), sdf.format(minDate)));
                }
                slip.addText(rows);
                serviceDocuments.add(slip);
            }
        }
    }

    @Override
    public boolean canUseAdminCommand(PositionEntity position) {
        return false;
    }

    @Override
    public boolean canRepeatPosition() {
        return false;
    }

    @Override
    public boolean isReturnPossible(PositionEntity position) {
        // произвольный возврат для подарочных карт запрещен
        if (position == null) {
            return false;
        }
        ProductConfig pc = Factory.getTechProcessImpl().getProductConfig(ProductDiscriminators.PRODUCT_GIFT_CARD_ENTITY);
        if (pc.isReturnInSameCash() || pc.isReturnInSameShift()) {
            long cashNumber = Factory.getTechProcessImpl().getProperties().getCashNumber();
            ShiftEntity shift = Factory.getTechProcessImpl().getShift();
            boolean result = (!pc.isReturnInSameCash() || cashNumber == position.getPurchase().getShift().getCashNum())
                    && (!pc.isReturnInSameShift() || shiftEquals(position.getPurchase().getShift(), shift));
            if (result) {
                return checkGiftCardStatus(position);
            } else {
                return false;
            }
        } else {
            return checkGiftCardStatus(position);
        }
    }

    private boolean checkGiftCardStatus(PositionEntity position) {
        try {
            PresentCardInformationVO cardInfo = getCardPluginService().getCardData(((PositionGiftCardEntity) position).getCardNumber());
            return (cardInfo.getStatus() == CardStatus.Active);// все прочие статусы вернуть нельзя
        } catch (Exception ex) {
            LOG.error("", ex);// в случае отсутствия связи возврат не работает
            return false;
        }
    }

    private boolean shiftEquals(ShiftEntity shift1, ShiftEntity shift2) {
        if (shift1 == null || shift2 == null) {
            return false;
        }

        boolean result = shift1.getNumShift().longValue() == shift2.getNumShift().longValue();
        result = result && shift1.getCashNum().longValue() == shift2.getCashNum().longValue();
        result = result && shift1.getShopIndex().longValue() == shift2.getShopIndex().longValue();

        return result;
    }

    private PresentCards getCardPluginService() {
        PresentCards returnValue = null;
        for (PluginCard card : getFactory().getCards()) {
            if (card instanceof PresentCards) {
                returnValue = (PresentCards) card;
                break;
            }
        }

        return returnValue;
    }

    @Override
    public List<PositionEntity> getReturnPositions(PurchaseEntity returnPurchase, List<PositionEntity> checkReturnPositions, boolean fullReturn,
                                                   List<PositionEntity> validatedPositions) {
        if (!Factory.getTechProcessImpl().getProductConfig(ProductDiscriminators.PRODUCT_GIFT_CARD_ENTITY).isReturnGiftCardCheckNeed()) {
            return checkReturnPositions;
        }
        return returnlGiftCardForm.validatePositions(returnPurchase, checkReturnPositions, fullReturn);
    }

    @Override
    public void notifyAddPosition(boolean checkOperationType) {
        // nop
    }
}
