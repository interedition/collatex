package de.tud.kom.stringutils.utils;

/**
 * Utility class containing helper methods for string operations.
 * 
 * @author Arno Mittelbach
 *
 */
public class StringUtils {

	/**
	 * Equivalent to PHP's join.
	 * 
	 * @param strings
	 * @param token
	 * @return
	 */
	public static String join(String strings[], String token) {
        StringBuffer sb = new StringBuffer();
       
        for( int i = 0; i < strings.length - 1; i++ ) {
            sb.append( strings[i] )
              .append( token );
        }
        
        /* append final string and return buffer's contents */
        return sb.append( strings[ strings.length - 1 ] ).toString();
    }
	
	/**
	 * 
	 * @param strings
	 * @param token
	 * @return
	 */
	public static String join(String strings[], char token) {
		return join(strings, String.valueOf(token));
	}
}
