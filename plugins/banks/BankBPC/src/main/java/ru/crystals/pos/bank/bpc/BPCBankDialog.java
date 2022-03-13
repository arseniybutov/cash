package ru.crystals.pos.bank.bpc;

import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankDialogType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BPCBankDialog implements BankDialog {
    private String title;
    private BankDialogType dialogType;
    private String message;
    private List<String> buttons;
    private List<String> values;

    public static BPCBankDialog createDialog(String rawDialogParams) {
        HashMap<String, String> screenParams = parseDialogParams(rawDialogParams);
        BPCBankDialog dialog = new BPCBankDialog();
        dialog.setTitle(screenParams.get("pTitle"));
        dialog.setDialogType(BankDialogType.values()[Integer.parseInt(screenParams.get("screenID"))]);
        dialog.setMessage(parseMessage(screenParams));
        dialog.setButtons(parseButtons(screenParams));
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (screenParams.get("pStr" + i) != null) {
                values.add(screenParams.get("pStr" + i));
            }
        }
        dialog.setValues(values);
        return dialog;
    }

    private static List<String> parseButtons(Map<String, String> screenParams) {
        List<String> buttons = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            if (screenParams.get("pButton" + i) != null && !screenParams.get("pButton" + i).equals("NULL")) {
                buttons.add(screenParams.get("pButton" + i));
            }
        }
        return buttons;
    }

    private void setButtons(List<String> buttons) {
        this.buttons = buttons;
    }

    private static String parseMessage(HashMap<String, String> screenParams) {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (screenParams.get("pStr" + i) != null && !screenParams.get("pStr" + i).equals("NULL")) {
                message.append(screenParams.get("pStr" + i)).append(' ');
            }
        }
        return message.toString();
    }

    private void setMessage(String message) {
        this.message = message;
    }

    private static HashMap<String, String> parseDialogParams(String rawDialogParams) {
        HashMap<String, String> screenParams = new LinkedHashMap<>();
        for (String s : rawDialogParams.split(";")) {
            String[] strings = s.split("=");
            String key = strings[0];
            String value = null;
            if (strings.length > 1) {
                value = strings[1];
            }
            if (!"NULL".equals(value)) {
                screenParams.put(key, value);
            }
        }
        return screenParams;
    }

    @Override
    public BankDialogType getDialogType() {
        return dialogType;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<String> getValues() {
        return values;
    }

    @Override
    public List<String> getButtons() {
        return buttons;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setDialogType(BankDialogType dialogType) {
        this.dialogType = dialogType;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
