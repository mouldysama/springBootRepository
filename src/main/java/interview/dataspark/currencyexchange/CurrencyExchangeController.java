package interview.dataspark.currencyexchange;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/***
 * Controller module for the currency exchange application
 * @author Kenny
 *
 */
@RestController
public class CurrencyExchangeController {
	
	@Autowired
	private CurrencyExchangeService exchangeService;
	
	/***
	 * Getter method to main service module used by the currency exchange application
	 * @return reference to ExchangeService instance 
	 */
	public CurrencyExchangeService getExchangeService() {
		return exchangeService;
	}

	/***
	 * Setter method to main service module used by the currency exchange application
	 * @param reference to ExchangeService instance 
	 */
	public void setExchangeService(CurrencyExchangeService exchangeService) {
		this.exchangeService = exchangeService;
	}

	/***
	 * Loads an introductory page detailing location of data repository, along with some instructions on how to access the rest of the APIs.
	 * Maps to GET("/") method.
	 * @return List of strings to be parsed in the main page as json message
	 */
	@RequestMapping("/")
	public List<Object> loadIntroPageInfo() {	
		return exchangeService.loadIntroPageInfo();
	}

	/***
	 * Lists out all files available in the data repository 
	 * Maps to GET("/ByDate") method.
	 * @return List of strings of format "yyyy-MM-dd.txt" to be parsed in the page as json message
	 */
	@RequestMapping(value="/ByDate")
	@Cacheable("fileNamesInRepository")
	public List<Object> getAllDates() {
		return exchangeService.getAllDates();
	}
	
	/***
	 * Returns exchange rates for all currencies for a given date
	 * Maps to GET("/ByDate/{date}") method.
	 * @param date of "yyyy-MM-dd" format
	 * @return If date is valid and data is available, the method returns a list of objects(date, currencyOfInterest, referenceCurrency, exchangeRate) to be parsed as json message list. Otherwise return error message.
	 */
	@RequestMapping("/ByDate/{date}")
	@Cacheable(value="fileName", key="#date")
	public List<Object> getRateByDate(@PathVariable String date) {
		return exchangeService.getRateByDate(date);
	}

	/***
	 * Returns exchange rates for a specified currency for a given date
	 * Maps to GET("/ByDate/{date}/{currencyOfInterest}") method.
	 * @param date of "yyyy-MM-dd" format
	 * @param currencyOfInterest of "XXX" format - case sensitive
	 * @return If date, currencyOfInterest is valid and data is available, the method returns an object(date, currencyOfInterest, referenceCurrency, exchangeRate) to be parsed as json message. Otherwise return error message.
	 */
	@RequestMapping("/ByDate/{date}/{currencyOfInterest}")
	@Cacheable(value="singleEntryByCurrencyByDate", key="#date + #currencyOfInterest")
	public List<Object> getSpecificRateByDate(@PathVariable("date") String date, @PathVariable("currencyOfInterest") String currencyOfInterest) {
		return exchangeService.getSpecificRateByDate(date, currencyOfInterest);
	}
	
	/***
	 * Returns exchange rates for a specified currency for a given date using a specified reference currency (non-USD included).
	 * Maps to GET("/ByDate/{date}/{currencyOfInterest}/{referenceCurrency}") method.
	 * @param date of "yyyy-MM-dd" format
	 * @param currencyOfInterest of "XXX" format - case sensitive
	 * @param referenceCurrency of "XXX" format - case sensitive
	 * @return If date, currencyOfInterest,referenceCurrency is valid and data is available, the method returns an object(date, currencyOfInterest, referenceCurrency, exchangeRate) to be parsed as json message. Otherwise return error message. Error message to be returned if currencyOfInterest==referenceCurrency.
	 */
	@RequestMapping("/ByDate/{date}/{currencyOfInterest}/{referenceCurrency}")
	@Cacheable(value="singleEntryByCurrencyByDateNewRef", key="#date + #currencyOfInterest + #referenceCurrency")
	public List<Object> getSpecificRateByDate(@PathVariable("date") String date, @PathVariable("currencyOfInterest") String currencyOfInterest, @PathVariable("referenceCurrency") String referenceCurrency) {
		return exchangeService.getSpecificRateByDate(date, currencyOfInterest, referenceCurrency);
	}

	/***
	 * Provide static instructions on accessing exchange rates for specified currencies and date ranges.
	 * Maps to GET("/ByCurrency") method.
	 * @return Static instructions on accessing exchange rates for specified currencies and date ranges.
	 */
	@RequestMapping("/ByCurrency")
	@Cacheable(value="listAllCurrencies")
	public List<Object> getAllCurrencies() {
		return exchangeService.getAllCurrencies();
	}
	
	/*** 
	 * Provide static instructions on accessing exchange rates for specified currencies and date ranges.
	 * Maps to GET("/ByCurrency/{currencyOfInterest}") method.
	 * @param currencyOfInterest of "XXX" format - case sensitive. 
	 * @return Static instructions on accessing exchange rates for specified currencies and date ranges. 
	 */
	@RequestMapping("/ByCurrency/{currencyOfInterest}")
	@Cacheable(value="allEntriesByCurrency", key="#currencyOfInterest")
	public List<Object> getRateByCurrency(@PathVariable String currencyOfInterest) {
		return exchangeService.getRateByCurrency(currencyOfInterest);
	}
	
	/***
	 * Returns exchange rates for a specified currency for a given date
	 * Maps to GET("/ByCurrency/{currencyOfInterest}/{date}") method.
	 * @param date of "yyyy-MM-dd" format
	 * @param currencyOfInterest of "XXX" format - case sensitive
	 * @return If date, currencyOfInterest is valid and data is available, the method returns an object(date, currencyOfInterest, referenceCurrency, exchangeRate) to be parsed as json message. Otherwise return error message.
	 */
	@RequestMapping("/ByCurrency/{currencyOfInterest}/{date}")
	@Cacheable(value="singleEntryByCurrencyAndDate", key="#date + #currencyOfInterest")
	public List<Object> getSpecificRateByCurrency(@PathVariable("currencyOfInterest") String currencyOfInterest, @PathVariable("date") String date) {
		return exchangeService.getSpecificRateByDate(date, currencyOfInterest);
	}
	
	/***
	 * Returns exchange rates for a specified currency for a given date range
	 * @param currencyOfInterest of "XXX" format - case sensitive
	 * @param startDate of "yyyy-MM-dd" format
 	 * @param endDate of "yyyy-MM-dd" format
	 * @return If currencyOfInterest, startDate, endDate is valid and data is available, the method returns a list of objects(date, currencyOfInterest, referenceCurrency, exchangeRate) to be parsed as json message list. Otherwise return error message.
	 */
	@RequestMapping("/ByCurrency/{currencyOfInterest}/{startDate}/{endDate}")
	@Cacheable(value="specifiedEntriesByCurrencyAndDateRange", key="#startDate + #endDate + #currencyOfInterest")
	public List<Object> getSpecifiedRatesByCurrency(@PathVariable("currencyOfInterest") String currencyOfInterest, @PathVariable("startDate") String startDate, @PathVariable("endDate") String endDate) {
		return exchangeService.getSpecifiedRatesByCurrency(currencyOfInterest, startDate, endDate);
	}
}
