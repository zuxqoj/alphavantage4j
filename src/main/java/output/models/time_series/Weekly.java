package output.models.time_series;

import output.data.ResponseData;
import output.data.time_series.StockData;
import output.models.ResponseModel;

import java.util.Map;

public class Weekly implements ResponseModel<ResponseData<StockData>> {

  @Override
  public String getDataKey() {
    return "Weekly Time Series";
  }

  @Override
  public ResponseData<StockData> resolve(Map<String, String> metaData,
                                         Map<String, Map<String, String>> dataResponse) {
    return new ResponseData<>(metaData, StockDataResolver.sparse(dataResponse));
  }
}