package ru.crystals.pos.siebel;

import ru.crystals.bundles.BundleId;
import ru.crystals.bundles.ContextBundle;
import ru.crystals.cards.CardBonusBalance;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.cards.siebel.results.SiebelTokenResult;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.siebel.SiebelTokenSelectorGui;

import java.awt.FontMetrics;

// Чтобы аннотация работала, класс должен находиться в пакете ru.crystals.pos.*
@ContextBundle(id = @BundleId(implemented = SiebelTokenSelectorGui.class), lazy = false)
public class SiebelTokenSelectorGuiImpl implements SiebelTokenSelectorGui {

    SiebelTokenSelectorView view = new SiebelTokenSelectorView();
    SiebelTokenSelectorController controller = new SiebelTokenSelectorController(view);

    @Override
    public void start(SiebelTokenResult cardResult) {
        controller.start(cardResult);
    }

    @Override
    public SiebelTokenResult getTokenResult() {
        return controller.getTokenResult();
    }

    @Override
    public void setWaitState(String message) {
        Factory.getInstance().getMainWindow().getCheckContainer().showWaitComponent(message);
    }

    @Override
    public void finish() {
        Factory.getInstance().getMainWindow().getCheckContainer().getPositionsListContainer().updateCheck(
                Factory.getTechProcessImpl().getCheck(), false
        );
    }

    public void refreshVisualCardState(PurchaseEntity purchase) {
        purchase.getCards().stream()
                .filter(c -> SiebelService.PROVIDER_NAME.equals(c.getProcessingName()))
                .findFirst()
                .ifPresent(c -> {
                    // По традиции карты у нас запрятаны в самый глубокий сракотан.
                    if (c.getCardType() != null && !c.getCardType().getCards().isEmpty()) {
                        CardBonusBalance cardBonusBalance = c.getCardType().getCards().get(0).getCardBonusBalance();
                        if (cardBonusBalance != null) {
                            Long chargeOffAmount = cardBonusBalance.getChargeOffAmount() != null ? cardBonusBalance.getChargeOffAmount() : 0L;
                            Factory.getInstance().eventShowBonusLimit(cardBonusBalance.getBalanceElementary() - chargeOffAmount);
                        }
                    }
                });
    }

    @Override
    public void reset() {
        controller.reset();
    }

    @Override
    public int computeStringWidth(String str) {
        FontMetrics fm = view.getFontMetrics(MyriadFont.getItalic(37F));
        return fm.stringWidth(str);
    }
}

