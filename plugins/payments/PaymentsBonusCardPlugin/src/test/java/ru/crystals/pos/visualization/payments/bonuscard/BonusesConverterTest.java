package ru.crystals.pos.visualization.payments.bonuscard;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;
import ru.crystals.pos.payments.BonusesConverter;
import ru.crystals.wsclient.cards.internal.BonusAccountTypeVO;
import ru.crystals.wsclient.cards.internal.BonusAccountVO;
import ru.crystals.wsclient.cards.internal.BonusAccountsType;

public class BonusesConverterTest {

    @Test
    public void testConvertBonuses() {

        BonusAccountTypeVO bonusAccountsTypeVO = getBonusAccountTypeVO(11l, 1l);

        assertEquals(0l, BonusesConverter.convertBonuses(getBonusAccountVO(bonusAccountsTypeVO, "0")).longValue());
        assertEquals(0l, BonusesConverter.convertBonuses(getBonusAccountVO(bonusAccountsTypeVO, "10")).longValue());
        assertEquals(1l, BonusesConverter.convertBonuses(getBonusAccountVO(bonusAccountsTypeVO, "11")).longValue());
        assertEquals(1l, BonusesConverter.convertBonuses(getBonusAccountVO(bonusAccountsTypeVO, "12")).longValue());
    }

    private BonusAccountTypeVO getBonusAccountTypeVO(long bonusCourse, long currencyCourse) {
        BonusAccountTypeVO bonusAccountsTypeVO = new BonusAccountTypeVO();
        bonusAccountsTypeVO.setAccountsType(BonusAccountsType.MONEY);
        bonusAccountsTypeVO.setBonusCourse(bonusCourse);
        bonusAccountsTypeVO.setCurrencyCourse(currencyCourse);
        return bonusAccountsTypeVO;
    }

    private BonusAccountVO getBonusAccountVO(BonusAccountTypeVO bonusAccountsTypeVO, String balance) {
        BonusAccountVO bonusAccount = new BonusAccountVO();
        bonusAccount.setBalance(balance);
        bonusAccount.setBonusAccountsTypeVO(bonusAccountsTypeVO);
        return bonusAccount;
    }
}