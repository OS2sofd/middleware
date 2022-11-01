package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.Address;
import dk.digst.digital.post.memolib.model.AddressPoint;

public class AddressBuilder {

  private String id;
  private String addressLabel;
  private String houseNumber;
  private String door;
  private String floor;
  private String co;
  private String zipCode;
  private String city;
  private String country;
  private AddressPoint addressPoint;

  public static AddressBuilder newBuilder() {
    return new AddressBuilder();
  }

  public AddressBuilder id(String id) {
    this.id = id;
    return this;
  }

  public AddressBuilder addressLabel(String addressLabel) {
    this.addressLabel = addressLabel;
    return this;
  }

  public AddressBuilder houseNumber(String houseNumber) {
    this.houseNumber = houseNumber;
    return this;
  }

  public AddressBuilder door(String door) {
    this.door = door;
    return this;
  }

  public AddressBuilder floor(String floor) {
    this.floor = floor;
    return this;
  }

  public AddressBuilder co(String co) {
    this.co = co;
    return this;
  }

  public AddressBuilder zipCode(String zipCode) {
    this.zipCode = zipCode;
    return this;
  }

  public AddressBuilder city(String city) {
    this.city = city;
    return this;
  }

  public AddressBuilder country(String country) {
    this.country = country;
    return this;
  }

  public AddressBuilder addressPoint(AddressPoint addressPoint) {
    this.addressPoint = addressPoint;
    return this;
  }

  public Address build() {
    return new Address(
        id, addressLabel, houseNumber, door, floor, co, zipCode, city, country, addressPoint);
  }
}
