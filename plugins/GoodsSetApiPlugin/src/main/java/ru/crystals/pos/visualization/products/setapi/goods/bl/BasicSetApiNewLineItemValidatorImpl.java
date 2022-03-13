package ru.crystals.pos.visualization.products.setapi.goods.bl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.api.commons.BasicSetApiNewLineItemValidator;
import ru.crystals.api.commons.SetApiNewLineItemValidator;
import ru.crystals.pos.visualization.products.setapi.goods.i18n.ResBundleSetApiGoods;

import java.text.MessageFormat;
import java.util.Set;

/**
 * Самая примитивная реализация {@link SetApiNewLineItemValidator}.
 *
 * @author aperevozchikov
 */
public class BasicSetApiNewLineItemValidatorImpl extends BasicSetApiNewLineItemValidator {

    private static final Logger log = LoggerFactory.getLogger(BasicSetApiNewLineItemValidatorImpl.class);

    public BasicSetApiNewLineItemValidatorImpl(Set<Float> taxes) {
        super(taxes);
    }

    @Override
    protected ViolationConverter converter() {
        return (violation, lineItem) -> {
            switch (violation) {
                case NULL_ARGUMENT:
                    return ResBundleSetApiGoods.getString("set.api.goods.line.item.is.null");
                case NO_MARKING_OR_BARCODE:
                    return ResBundleSetApiGoods.getString("set.api.goods.either.barcode.or.marking.should.not.be.empty");
                case INVALID_PRICE:
                    return ResBundleSetApiGoods.getString("set.api.goods.the.price.ot.the.line.item.is.negative");
                case INVALID_QUANTITY:
                    return ResBundleSetApiGoods.getString("set.api.goods.the.qnty.ot.the.line.item.is.nonpositive");
                case INVALID_NDS:
                    return MessageFormat.format(ResBundleSetApiGoods.getString("set.api.goods.vat.rate.is.invalid"), lineItem.getNds());
                case VALIDATION_FAILURE:
                default:
                    return null;
            }
        };
    }

    @Override
    protected Logger log() {
        return log;
    }

}
