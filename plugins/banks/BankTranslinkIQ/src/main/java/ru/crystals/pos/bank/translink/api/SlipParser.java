package ru.crystals.pos.bank.translink.api;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Текст чека может содержать команды управления печатью.
 * Контрольные символы печати расположены в начале строки и начинаются с символа \.
 * <dl>
 * <dt>{@code \dw}</dt><dd>Текстовая часть строки должна быть напечатана выделенным шрифтом (bold).
 * Если принтер не поддерживает данный режим печати, надо добавлять
 * пробел после каждого символа для имитации печати выделенным шрифтом</dd>
 *
 * <dt>{@code \dh}</dt><dd>Текстовая часть строки должна быть напечатана шрифтом двойной высоты.
 * Если принтер не поддерживает символы двойной высоты, тег игнорируется.</dd>
 *
 * <dt>{@code \bc}</dt><dd>Текстовая часть строки должна быть напечатана в формате штрих-кода.
 * Если печать штрих-кода не поддерживается, строка печатается как обычный текст</dd>
 *
 * <dt>{@code \qr</dt><dd>Текстовая часть строки должна быть напечатана в формате QR-кода.
 * Если печать QR-кода не поддерживается, строка печатается как обычный текст</dd>
 *
 * <dt>{@code \im}</dt><dd>Печать графической информации. Игнорируется, если принтер не поддерживает печать графической информации</dd>
 * <dt>{@code \ct}</dt><dd>Центрирование строки</dd>
 * <dt>{@code \\}</dt><dd>Печать управляющего символа {@code \}</dd>
 * <dt>{@code \sr}</dt><dd>Чек содержит строку подписи, уведомите кассира, чтобы он обратил внимание на эту копию чека.</dd>
 * <dt>{@code \cd}</dt><dd>Текущий контент чека, данные клиента, которые могут быть объединены в основной чек</dd>
 * <dt>{@code \mc}</dt><dd>Текущее содержимое чека является копией для продавца</dd>
 * <dt>{@code \cl}</dt><dd>Управляющая строка, которая определяет тег разделителя чеков, он указывает, что чек должен быть обрезан в этой позиции</dd>
 *
 * </dl>
 */
public class SlipParser {

    public List<List<String>> parse(String receiptText) {
        if (StringUtils.isEmpty(receiptText)) {
            return Collections.emptyList();
        }
        final String[] lines = StringUtils.splitPreserveAllTokens(receiptText, "\n");
        final ArrayList<String> shopSlip = new ArrayList<>();
        final ArrayList<String> customerSlip = new ArrayList<>();

        final List<String> currentSlip = new ArrayList<>();
        boolean forShopSlip = true;
        for (String line : lines) {
            if (!line.startsWith("\\")) {
                currentSlip.add(line);
                continue;
            }
            if (line.startsWith("\\\\")) {
                currentSlip.add(StringUtils.substring(line, 1));
                continue;
            }
            final String code = StringUtils.substring(line, 1, 3);
            switch (code) {
                case "mc":
                    forShopSlip = true;
                    break;
                case "cd":
                    forShopSlip = false;
                    break;
                case "cl":
                    if (forShopSlip) {
                        shopSlip.addAll(currentSlip);
                    } else {
                        customerSlip.addAll(currentSlip);
                    }
                    forShopSlip = !forShopSlip;
                    currentSlip.clear();
                    break;
                case "ct":
                case "dw":
                case "dh":
                case "bc":
                case "qr":
                    currentSlip.add(StringUtils.substring(line, 3));
                    break;
                case "im":
                case "sr":
                default:
                    break;
            }
        }
        if (forShopSlip) {
            shopSlip.addAll(currentSlip);
        } else {
            customerSlip.addAll(currentSlip);
        }
        currentSlip.clear();

        final List<List<String>> result = new ArrayList<>();
        if (!customerSlip.isEmpty()) {
            result.add(customerSlip);
        }
        if (!shopSlip.isEmpty()) {
            result.add(shopSlip);
        }
        return result;
    }
}
