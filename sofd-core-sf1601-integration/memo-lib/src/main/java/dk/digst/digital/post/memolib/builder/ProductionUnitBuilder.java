package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.ProductionUnit;

public class ProductionUnitBuilder {

  private int productionUnitNumber;
  private String productionUnitName;

  public static ProductionUnitBuilder newBuilder() {
    return new ProductionUnitBuilder();
  }

  public ProductionUnitBuilder productionUnitNumber(int productionUnitNumber) {
    this.productionUnitNumber = productionUnitNumber;
    return this;
  }

  public ProductionUnitBuilder productionUnitName(String productionUnitName) {
    this.productionUnitName = productionUnitName;
    return this;
  }

  public ProductionUnit build() {
    return new ProductionUnit(productionUnitNumber, productionUnitName);
  }
}
