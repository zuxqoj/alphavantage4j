package org.patriques;

import org.patriques.input.Function;
import org.patriques.input.Symbol;
import org.patriques.input.Symbols;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.quote.BatchStockQuotesResponse;
import org.patriques.output.quote.SingleStockQuotesResponse;

/**
 * The Batch Stock Quotes api provides stock quotes give a list of stock symbols.
 */
public class SingleStockQuote {

  private final ApiConnector apiConnector;

  /**
   * Constructs a Batch Stock Quotes api endpoint with the help of an {@link ApiConnector}
   *
   * @param apiConnector the connection to the api
   */
  public SingleStockQuote(ApiConnector apiConnector) {
    this.apiConnector = apiConnector;
  }

  /**
   * This API returns stock quotes updated realtime.
   *
   * @param symbols the stock symbols to lookup
   * @return {@link BatchStockQuotesResponse} stock quote data
   */
  public SingleStockQuotesResponse quote(String symbol) {
//    if (symbols.length > 100) {
//      throw new AlphaVantageException("Tried to get stock quotes for " + symbols.length + " stocks. The Batch Stock" +
//              " Quotes API will only return quotes for the first 100 symbols.");
//    }
    String json = apiConnector.getRequest(new Symbol(symbol), Function.GLOBAL_QUOTE);
    System.out.println("json:: " + json);
    return SingleStockQuotesResponse.from(json);
  }
}
