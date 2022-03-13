package ru.crystals.pos.bank.bpc;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PpServerEmulator {
    private static ServerSocket serverSocket;
    private static OutputStream outputStreamWriter;
    private Socket socket;
    private OutputStream writer;
    private Socket dialogSocket;

    public static void main(String[] args) throws IOException, InterruptedException {
        PpServerEmulator ppServerEmulator = new PpServerEmulator();
        ppServerEmulator.run();
    }

    private void run() throws IOException, InterruptedException {
        serverSocket = new ServerSocket(16500);
        while (!Thread.interrupted()) {
            socket = serverSocket.accept();
            InputStream reader = socket.getInputStream();
            writer = socket.getOutputStream();
            byte[] request = new byte[37];
            reader.read(request);
            Map<Integer, DataByte> requestMap = Parser.parse(request);
            showMessage(requestMap);
            Thread.sleep(2000L);
            showScreen3(requestMap);
//            Thread.sleep(10000L);
//            showScreen4(requestMap);
            completeOperation(requestMap);
        }
    }

    private void completeOperation(Map<Integer, DataByte> requestMap) throws IOException {
        InputStream dialogResponseReader = dialogSocket.getInputStream();
        byte[] cbuf = new byte[300];
        dialogResponseReader.read(cbuf);
        System.out.println(new String(cbuf));
        HashMap<String, String> stringStringHashMap = parseDialogParams(new String(cbuf));
        Request response;
        System.out.println(stringStringHashMap.get("eventKey"));
        if (stringStringHashMap.get("eventKey").equals("32")) {
            response = createResponse(requestMap);
        } else {
            response = createNegativeResponse(requestMap);
        }
        writer.write(response.toBytes());
        dialogSocket.close();
        outputStreamWriter.close();
    }

    private void showScreen4(Map<Integer, DataByte> requestMap) throws IOException {
        dialogSocket = new Socket();
        dialogSocket.connect(new InetSocketAddress("127.0.0.1", 6517));

        outputStreamWriter = dialogSocket.getOutputStream();
        String dialog = null;
        if (requestMap.get(0x01).toString().equals("PUR")) {
            dialog =
                "Status=0;len=92;screenID=4;maxInp=0;minInp=0;format=2;pTitle=ОПЛАТА;pStr=NULL;pInitStr=NULL;pStr1=NULL;pStr2=Введите номер карты;pButton0=НЕТ;pButton1=ДА;CurAlpha=RUR\u0002;nDecPoint=2;eventKey=0;pBuf=NULL";
        } else if (requestMap.get(0x01).toString().equals("VOI")) {
            dialog =
                "Status=0;len=92;screenID=4;maxInp=0;minInp=0;format=2;pTitle=ОТМЕНА;pStr=NULL;pInitStr=NULL;pButton0=НЕТ;pButton1=ДА;CurAlpha=RUR\u0002;nDecPoint=2;eventKey=0;pBuf=NULL";
        } else if (requestMap.get(0x01).toString().equals("SRV")) {
            dialog =
                "Status=0;len=92;screenID=4;maxInp=0;minInp=0;format=2;pTitle=ОТМЕНА;pStr=NULL;pInitStr=NULL;pButton0=НЕТ;pButton1=ДА;CurAlpha=RUR\u0002;nDecPoint=2;eventKey=0;pBuf=NULL";
        }
        outputStreamWriter.write(dialog.getBytes(Charset.forName("cp866")));
        outputStreamWriter.flush();
    }

    private void showScreen3(Map<Integer, DataByte> requestMap) throws IOException {
        dialogSocket = new Socket();
        dialogSocket.connect(new InetSocketAddress("127.0.0.1", 6517));

        outputStreamWriter = dialogSocket.getOutputStream();
        String dialog = null;
        if (requestMap.get(0x01).toString().equals("PUR")) {
            dialog =
                "Status=0;len=92;screenID=5;maxInp=0;minInp=0;format=2;pTitle=ОПЛАТА;pStr1=ТЕСТ1;pStr2=ТЕСТ2;pInitStr=NULL;pButton0=НЕТ;pButton1=ДА;CurAlpha=RUR\u0002;nDecPoint=2;eventKey=0;pBuf=NULL";
        } else if (requestMap.get(0x01).toString().equals("VOI")) {
            dialog =
                "Status=0;len=92;screenID=3;maxInp=0;minInp=0;format=2;pTitle=ОТМЕНА;pStr=NULL;pInitStr=NULL;pButton0=НЕТ;pButton1=ДА;CurAlpha=RUR\u0002;nDecPoint=2;eventKey=0;pBuf=NULL";
        } else if (requestMap.get(0x01).toString().equals("SRV")) {
            dialog =
                "Status=0;len=92;screenID=5;maxInp=0;minInp=0;format=2;pTitle=ОТМЕНА;pStr0=NULL;pStr1=ТЕСТ;pStr2=ТЕСТ2;pInitStr=NULL;pButton0=НЕТ;pButton1=ДА;CurAlpha=RUR\u0002;nDecPoint=2;eventKey=0;pBuf=NULL";
        }
        outputStreamWriter.write(dialog.getBytes(Charset.forName("cp866")));
        outputStreamWriter.flush();
    }

    private void showMessage(Map<Integer, DataByte> requestMap) throws IOException {
        dialogSocket = new Socket();
        dialogSocket.connect(new InetSocketAddress("127.0.0.1", 6517));

        outputStreamWriter = dialogSocket.getOutputStream();
        String dialog = null;
        if (requestMap.get(0x01).toString().equals("PUR")) {
            dialog =
                "Status=0;len=92;screenID=0;maxInp=0;minInp=0;format=2;pTitle=ОПЛАТА;pStr=NULL;pInitStr=NULL;pStr1=NULL;pStr2=Подождите;pButton0=НЕТ;pButton1=ДА;CurAlpha=RUR\u0002;nDecPoint=2;eventKey=0;pBuf=NULL";
        } else if (requestMap.get(0x01).toString().equals("VOI")) {
            dialog =
                "Status=0;len=92;screenID=0;maxInp=0;minInp=0;format=2;pTitle=ОТМЕНА;pStr=NULL;pInitStr=NULL;pButton0=НЕТ;pButton1=ДА;CurAlpha=RUR\u0002;nDecPoint=2;eventKey=0;pBuf=NULL";
        } else if (requestMap.get(0x01).toString().equals("SRV")) {
            dialog =
                "Status=0;len=92;screenID=0;maxInp=0;minInp=0;format=2;pTitle=ОТМЕНА;pStr=NULL;pInitStr=NULL;pButton0=НЕТ;pButton1=ДА;CurAlpha=RUR\u0002;nDecPoint=2;eventKey=0;pBuf=NULL";
        }
        outputStreamWriter.write(dialog.getBytes(Charset.forName("cp866")));
        outputStreamWriter.flush();
    }

    private Request createNegativeResponse(Map<Integer, DataByte> requestMap) {
        Request response = new Request();
        response.addField(Tag.Input.MESSAGE_ID, requestMap.get(0x01).toString());
        response.addField(Tag.Output.RESPONSE_CODE, "TT");
        return response;
    }

    private HashMap<String, String> parseDialogParams(String rawDialogParams) {
        HashMap<String, String> screenParams = new LinkedHashMap<String, String>();
        for (String s : rawDialogParams.trim().split(";")) {
            System.out.println(s);
            String key = s.split("=")[0];
            String value = s.split("=")[1];
            screenParams.put(key, value);
        }
        return screenParams;
    }

    private static Request createResponse(Map<Integer, DataByte> request) {
        Request response = new Request();
        response.addField(Tag.Input.MESSAGE_ID, request.get(0x01).toString());
        response.addField(Tag.Output.RESPONSE_CODE, "00");
        response.addField(0x84, StringUtils.leftPad(String.valueOf(1423), 12, '0'));
        response.addField(0x89, "4731226598873221");
        response.addField(0x8A, "0914");
        response.addField(0x8B, "123456");
        response.addField(0x8C, "123456");
        response.addField(0x8D, "0104");
        response.addField(0x8E, "1259");
        response.addField(0x8F, "VISA");
        response.addField(0x90, "       RETAIL10");
        response.addField(0x91, "010000");
        response.addField(0x92, "022");
        response.addField(0x93, "TT");
        response.addField(0x94, "S");
        response.addField(0x83, "0066558899");
        response.addField(0x98, "665544332211");
        response.addField(0x9D, "12345678");
        response.addField(0x99, "000001");
        response.addField(0x82, "01");
        response.addField(0x9C, " TEXT ");
        response.addField(0xA1, "Y");
        return response;
    }
}
