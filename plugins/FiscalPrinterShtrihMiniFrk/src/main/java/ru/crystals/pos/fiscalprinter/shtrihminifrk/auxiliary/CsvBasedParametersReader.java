package ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Данный {@link ParametersReader читатель настроек} .. считывает настройки из CSV-файла.
 * 
 * @author aperevozchikov
 */
public class CsvBasedParametersReader implements ParametersReader {
    private static final Logger log = LoggerFactory.getLogger(CsvBasedParametersReader.class);
    
    /**
     * Кодировка, в которой записано содержимое CSV-файла
     */
    private static final Charset CSV_ENCODING = Charset.forName("UTF-8");
    
    /**
     * Регулярное выражение, описывающее формат хранения настроек в CSV-файле.
     * <p/>
     * назначение групп:
     * <ol>
     * <li> Номер таблицы;
     * <li> Номер ряда;
     * <li> Номер поля;
     * <li> Размер поля;
     * <li> Тип поля;
     * <li> Мин. значение;
     * <li> Макс. значение;
     * <li> Название;
     * <li> Значение.
     * </ol>
     */
    // @formatter:off
    private static final String REGEXP = "\\s*(\\d+)\\s*," +
        "\\s*(\\d+)\\s*," +
        "\\s*(\\d+)\\s*," +
        "\\s*(\\d+)\\s*," +
        "\\s*(\\d+)\\s*," +
        "\\s*(\\d+)\\s*," +
        "\\s*(\\d+)\\s*," +
        "(.+)," +
        "\\s*\"(.*)\"\\s*";
    // @formatter:on
    
    /**
     * pattern по {@link #REGEXP этому} регулярному выражению
     */
    private static final Pattern PATTERN = Pattern.compile(REGEXP);

    /**
     * Путь к файлу с настройками, откуда надо будет вести чтение
     */
    private String parametersFilePath;

    /**
     * Единственно правильный конструктор.
     * 
     * @param parametersFilePath
     *            Путь к файлу с настройками, откуда надо будет вести чтение
     */
    public CsvBasedParametersReader(String parametersFilePath) {
        this.parametersFilePath = parametersFilePath;
    }

    @Override
    public Collection<ShtrihParameter> readParameters() {
        Collection<ShtrihParameter> result;
        
        log.info("entering readParameters()");
        
        if (StringUtils.isBlank(parametersFilePath)) {
            // видимо, для этой моели оборудования просто не нужна инициализация - норма
            log.info("leaving readParameters(): The \"parametersFilePath\" is EMPTY");
            return Collections.emptyList();
        }
        
        // 1. Сначала распарсим файл:
        result = readParametes(parametersFilePath);
        
        // 2. а потом удалим неликвид:
        for (Iterator<ShtrihParameter> it = result.iterator(); it.hasNext();) {
            ShtrihParameter sp = it.next();
            if (!validate(sp)) {
                // какой-то неликвид обнаружен!
                log.error("readParameters(): INVALID line ({}) was detected in the \"{}\" file!", sp, parametersFilePath);
                it.remove();
            }
        } // for it
        
        log.info("leaving readParameters(). The result size is: {}", result.size());
        
        return result;
    }
    
    /**
     * Считает из указанного файла и вернет настройки.
     * 
     * @param fileName
     *            имя файла, хранящего настройки. что надо считать и вернуть
     * @return не <code>null</code> - в крайнем случае вернет пустую коллекцию; в списке элементов <code>null</code>'ей тоже не будет
     */
    private Collection<ShtrihParameter> readParametes(String fileName) {
        Collection<ShtrihParameter> result = null;
        
        if (StringUtils.isBlank(fileName)) {
            log.error("readParametes(String). The argument is EMPTY!");
            return Collections.emptyList();
        }
        
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), CSV_ENCODING))) {
            result = new LinkedList<>();
            while (reader.ready()) {
                String line = reader.readLine();
                ShtrihParameter element = processLine(line);
                if (element != null) {
                    result.add(element);
                }
            } // while
        } catch (FileNotFoundException e) {
            log.error(String.format("readParametes(String). The file: \"%s\" was NOT FOUND", fileName), e);
            result = null;
        } catch (IOException e) {
            log.error(String.format("readParametes(String). IOE while processing file: \"%s\"", fileName), e);
            result = null;
        } catch (Throwable t) {
            log.error(String.format("readParametes(String). Some other exception while processing file: \"%s\"", fileName), t);
            result = null;
        }
        
        if (result == null) {
            result = Collections.emptyList();
        }
        
        return result;
    }    

    /**
     * Отвалидирует указанный параметр.
     * 
     * @param parameter
     *            параметр. что надо отвалидировать
     * @return <code>false</code>, если аргумент невалиден
     */
    private boolean validate(ShtrihParameter parameter) {
        if (parameter == null) {
            log.warn("validate(ShtrihParameter). The argument is NULL");
            return false;
        }
        
        if (parameter.getFieldType() == null) {
            log.warn("validate(ShtrihParameter). The \"fieldType\" field of the argument is NULL");
            return false;
        }
        
        if (parameter.getValue() == null) {
            log.warn("validate(ShtrihParameter). The \"value\" field of the argument is NULL");
            return false;
        }
        
        if (parameter.getFieldWidth() < 1) {
            log.warn("validate(ShtrihParameter). The \"fieldWidth\" field of the argument is INVALID ({})", parameter.getFieldWidth());
            return false;
        }
        
        if (ShtrihFieldType.NUMBER.equals(parameter.getValue())) {
            try {
                new BigInteger(parameter.getValue());
            } catch (Throwable t) {
                log.warn("validate(ShtrihParameter). The \"value\" field ({}) of the argument is INVALID: expected to be a Number!", parameter.getValue());
                return false;
            }
        }
        
        return true;
    }

    /**
     * распарсит указанную строку и вернет {@link ShtrihParameter настройку}, что в этой строке описана.
     * 
     * @param line
     *            строка, описывающая настройку, объектное представление которой надо вернуть
     * @return <code>null</code>, если не удалось распарсить
     */
    private ShtrihParameter processLine(String line) {
        ShtrihParameter result = null;
        
        if (StringUtils.isEmpty(line)) {
            log.error("processLine(String): the argument is EMPTY!");
            return null;
        }

        Matcher m = PATTERN.matcher(line);
        if (!m.matches()) {
            if (line != null && line.startsWith("//")) {
                // видимо, эта строка - просто комментарий. Не надо об этом орать
                log.trace("processLine(String): the argument [{}] does NOT MATCH the required pattern [{}]", line, REGEXP);
            } else {
                // видимо, файл с настройками похерили
                log.warn("processLine(String): the argument [{}] does NOT MATCH the required pattern [{}]", line, REGEXP);
            }
            return null;
        }
        
        // строка распарсена. Но можем еще обделаться при переводе в объектное представление:
        try {
            ShtrihFieldType fieldType = ShtrihFieldType.getTypeByCode(Integer.valueOf(m.group(5)));
            if (fieldType == null) {
                // какой-то неизвестный тип данных
                log.warn("processLine(String): unknown field type [{}] was detected in the line [{}]", m.group(5), line);
                return null;
            }
            
            // все равно еще при парсинге можем получить ошибку (например, переполнение)
            result = new ShtrihParameter();
            result.setTableNo(Integer.valueOf(m.group(1)));
            result.setRowNo(Integer.valueOf(m.group(2)));
            result.setFieldNo(Integer.valueOf(m.group(3)));
            result.setFieldWidth(Integer.valueOf(m.group(4)));
            result.setFieldType(fieldType);
            result.setMinValue(Long.valueOf(m.group(6)));
            result.setMaxValue(Long.valueOf(m.group(7)));
            result.setName(m.group(8));
            result.setValue(m.group(9));
        } catch (Throwable t) {
            log.error(String.format("processLine(String): failed to convert string [%s] into object", line), t);
            result = null;
        }
        
        return result;
    }    
    
    @Override
    public String toString() {
        return String.format("csv-based-parameters-reader [file: \"%s\"]", parametersFilePath);
    }
    
}