package ru.crystals.pos.fiscalprinter.atol3.highlevel.types;

public class AtolStringDecoder implements ValueDecoder<String> {
    // copied from DataPacket
    private static final char[] charSet = new char[] { 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф',
            'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', ' ', '!', '"', '#', '№', '%', '&', '’', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1',
            '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
            'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', ' ', 'а', 'б', 'в', 'г', 'д', 'е', 'ж', 'з', 'и',
            'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', '$', '€', '—'};

    @Override
    public String decode(byte[] data, int from, int length) {
        StringBuilder result = new StringBuilder();

        int to = from + length;
        for (int i = from; i < to; ++i) {
            byte b = data[i];
            int index = (b < 0) ? (b + 256) : b;

            switch (index) {
                case 250:
                    result.append(charSet[162]);
                    break;
                case 254:
                    result.append("\u0009");
                    break;
                default:
                    // Если символ вне диапазона, то пробел
                    result.append(charSet[(index < charSet.length) ? index : 31]);
                    break;
            }
        }

        return result.toString();
    }
}
