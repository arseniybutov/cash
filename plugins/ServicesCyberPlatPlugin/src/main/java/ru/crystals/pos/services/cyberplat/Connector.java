package ru.crystals.pos.services.cyberplat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.CyberPlat.IPriv;
import org.CyberPlat.IPrivException;
import org.CyberPlat.IPrivKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.services.exception.ServicesException;

public class Connector {

	private static final Logger LOG = LoggerFactory.getLogger(Connector.class);

	/** Кодовая страница сервера */
	private static final String ENC = "windows-1251";

	private String serviceFolder;

	/** Пароль для закрытого ключа */
	private String password;

	/** Серийный номер открытого банковского ключа */
	private String bankKeySerialNumber;

	/** Закрытый ключ для формирования подписей */
	private IPrivKey sec;

	/** Открытый ключ банка для проверки подписей в ответах */
	private IPrivKey pub;

	public Connector(String serviceFolder, String password, String bankKeySerialNumber) {
		this.serviceFolder = serviceFolder;
		this.password = password;
		this.bankKeySerialNumber = bankKeySerialNumber;
	}

	public void start() throws ServicesException {
		if (bankKeySerialNumber.isEmpty()) {
			throw new ServicesException(ResBundleServicesCyberPlat.getString("BANK_KEY_SERIAL_NUMBER_ABSENT"));
		}

		/*
		 * Обязательно вызывать для указания кодовой страницы изначального
		 * документа
		 */
		IPriv.setCodePage(ENC);

		try {
			/* Загрузка закрытого ключа для формирования подписей */
			sec = IPriv.openSecretKey(serviceFolder + "secret.key", password);

			/* Загрузка открытого ключа банка */
			pub = IPriv.openPublicKey(serviceFolder + "pubkeys.key", Integer.valueOf(bankKeySerialNumber));

		} catch (IPrivException e) {
			LOG.error("", e);
			throw new ServicesException(ResBundleServicesCyberPlat.getString("KEY_ERROR"));
		}
	}

	public void stop() {
		/* Закрытие ключей по окончании работы */
		if (sec != null)
			sec.closeKey();
		if (pub != null)
			pub.closeKey();
	}

	/**
	 * Функция отправки запроса
	 * 
	 * @param url
	 *            URL запроса
	 * @param req
	 *            Запрос
	 * @return Ответ
	 * @throws IPrivException
	 * @throws IOException
	 */
	public String sendRequest(String url, String req) throws IPrivException, IOException {

		long time = System.currentTimeMillis();

		LOG.debug("REQUEST (" + url + "):\n" + req + "\n");

		/* Кодирование запроса */
		req = "inputmessage=" + URLEncoder.encode(sec.signText(req), "UTF-8");

		/* Соединение с сервером */
		URL u = new URL(url);
		URLConnection con = u.openConnection();
		con.setDoOutput(true);
		con.connect();

		/* Отправка запроса */
		con.getOutputStream().write(req.getBytes(ENC));

		/* Чтение ответа */
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), ENC));
		char[] rawResponse = new char[1024];
		int rawResponseLength = in.read(rawResponse);
		StringBuffer buffer = new StringBuffer();
		buffer.append(rawResponse, 0, rawResponseLength);
		String resp = buffer.toString();

		/* Проверка подписи сервера */
		resp = pub.verifyText(resp);

		LOG.debug("RESPONSE (" + url + ", " + (System.currentTimeMillis() - time) + " ms):\n" + resp);

		return resp;
	}

}
