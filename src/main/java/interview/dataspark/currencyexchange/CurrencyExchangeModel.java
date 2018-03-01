package interview.dataspark.currencyexchange;

/***
 * Model created to hold each currency exchange record per currencyOfInterest wrt referenceCurrency per date.  
 * All fields are private with its associated getter/setter methods. The methods should be self-explanatory.
 * @author Kenny
 *
 */
public class CurrencyExchangeModel {
	
	private String date;
	private String currencyOfInterest;
	private String referenceCurrency;
	private double exchangeRate;
	
	public CurrencyExchangeModel(String date, String currencyOfInterest, String referenceCurrency, double exchangeRate) {
		super();
		this.date = date;
		this.currencyOfInterest = currencyOfInterest;
		this.referenceCurrency = referenceCurrency;
		this.exchangeRate = exchangeRate;
	}	
	public CurrencyExchangeModel() {
		this.date = "";
		this.currencyOfInterest = "";
		this.referenceCurrency = "";
		this.exchangeRate = 0;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getCurrencyOfInterest() {
		return currencyOfInterest;
	}
	public void setCurrencyOfInterest(String currencyOfInterest) {
		this.currencyOfInterest = currencyOfInterest;
	}
	public String getReferenceCurrency() {
		return referenceCurrency;
	}
	public void setReferenceCurrency(String referenceCurrency) {
		this.referenceCurrency = referenceCurrency;
	}
	public double getExchangeRate() {
		return exchangeRate;
	}
	public void setExchangeRate(double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}
	
}
