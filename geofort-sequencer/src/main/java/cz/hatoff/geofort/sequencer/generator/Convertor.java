package cz.hatoff.geofort.sequencer.generator;



public class Convertor {

    private static final int BASE = 36;
    /**
     * Prevede cislo mezi dvemi soustavami
     * @param baseFrom soustava, ve ktere je cislo uvedene
     * @param baseTo soustava, do ktere cislo prevadime
     * @param number cislo k prevodu 0-9, A-Z
     * @return cislo prevedene do zadane soustavy
     * @throws java.lang.IllegalArgumentException pokud ma kterakoliv ze soustav
     * bazi nizsi nez 2 nebo vyssi nez 36
     */
    public static String convert(int baseFrom, int baseTo, String number) throws IllegalArgumentException{
        return convertFromDecimal(convertToDecimal(number));
    }
    /**
     * Provede prevod ze zadane soustavy do desitkove, zadana soustava je
     * reprezentovana pomoci cisel 0-9, a pismen A-Z, soustavy se zakladem
     * vyssim nez 36 nejsou podporovany
     * @param number cislo k prevedeni
     * @return cislo v desitkove soustave
     * @throws java.lang.IllegalArgumentException v pripade soustavy nizsi 2 a vyssi 36
     */
    public static int convertToDecimal(String number) throws IllegalArgumentException{
        int result = 0; //vysledek
        int position = 0; //kolikate misto odzadu pocitame
        for(int i = number.length() - 1; i >= 0; i--){
            int numeral = convertToInt(number.charAt(i));
            if(numeral >= BASE) throw new IllegalArgumentException("Neplatna soustava");
            result += numeral * Math.pow(BASE, position); //prevod cislice na odpovidajici pozici
            position++;// posun pozice
        }
        return result;
    }

    public static String convertFromDecimal(int number){
        String result = "";
        while(number != 0){
            int remainder = number % BASE; //zjisteni zbytku (hodnoty na odpovidajici pozici)
            number = number / BASE; //posun o pozici dal
            result = convertToNumeral(remainder) + result;
        }
        return result;
    }
    /**
     * Prevede ASCII znak reprezentujici cislici soustavy na integer
     * @param charAt 0-9 A-Z
     * @return cislo v desitkove soustave odpovidajici hodnote cislice
     */
    private static int convertToInt(char charAt) {
        if(charAt >= 48 && charAt <= 57) return charAt - 48;
        else if(charAt >= 65 && charAt <= 90) return charAt - 65 + 10;//0-9 jsou cisla
        throw new IllegalArgumentException("Cislice neni 0-9 A-Z");

    }

    private static char convertToNumeral(int numeral) {
        if(numeral >= 0 && numeral < 10) return (char)(48 + numeral);
        else if(numeral >= 10 && numeral <= 35) return (char)(65 + numeral - 10);
        throw new IllegalArgumentException("Neni cislice 0-35");
    }

}
