package employeeschedulingsystem;

/**
 * add helper method to ensure that empty string converted to null (e.g. for empty string type in availability loader)
 */
public interface CsvEntityLoader {
    default String identityButConvertEmptyStringToNull(String s){
        return "".equals(s)? null: s;
    }
}
