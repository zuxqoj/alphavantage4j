package co.alphavantage.output.technicalindicators;

import co.alphavantage.output.AlphaVantageException;
import co.alphavantage.output.JsonParser;
import co.alphavantage.output.technicalindicators.data.IndicatorData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Representation of Kaufman adaptive moving average (KAMA) response from api.
 *
 * @see TechnicalIndicatorResponse
 */
public class KAMA extends TechnicalIndicatorResponse<IndicatorData> {

  private KAMA(final Map<String, String> metaData,
               final List<IndicatorData> indicatorData) {
    super(metaData, indicatorData);
  }

  /**
   * Creates {@code KAMA} instance from json.
   *
   * @param json string to parse
   * @return KAMA instance
   */
  public static KAMA from(String json) {
    Parser parser = new Parser();
    return parser.parseJson(json);
  }

  /**
   * Helper class for parsing json to {@code KAMA}.
   *
   * @see TechnicalIndicatorParser
   * @see JsonParser
   */
  private static class Parser extends TechnicalIndicatorParser<KAMA> {

    @Override
    String getIndicatorKey() {
      return "Technical Analysis: KAMA";
    }

    @Override
    KAMA resolve(Map<String, String> metaData,
                Map<String, Map<String, String>> indicatorData) throws AlphaVantageException {
      List<IndicatorData> indicators = new ArrayList<>();
      indicatorData.forEach((key, values) -> indicators.add(new IndicatorData(
              LocalDateTime.parse(key, DATE_WITH_SIMPLE_TIME_FORMAT),
              Double.parseDouble(values.get("KAMA"))
      )));
      return new KAMA(metaData, indicators);
    }
  }
}