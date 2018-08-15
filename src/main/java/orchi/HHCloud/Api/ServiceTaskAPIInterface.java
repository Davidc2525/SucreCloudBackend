/**
 * ServiceTaskAPIInterface.java
 */
package orchi.HHCloud.Api;

import orchi.HHCloud.ParseParamsMultiPart2;

/**
 * @author david
 * 14 ago. 2018
 */
public interface ServiceTaskAPIInterface {

	void sendError(String e) throws Exception;

	void sendError(String error, String ex) throws Exception;

	void sendError(String error, Exception e) throws Exception;

	/**permite comprobar la disponibilidad de una api junto con su operacion*/
	void checkAvailability(String apiName,String operationName) throws Exception;

}