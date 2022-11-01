package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.Reservation;
import java.time.LocalDateTime;
import java.util.UUID;

public class ReservationBuilder {

  private String description;
  private UUID reservationUUID;
  private String reservationAbstract;
  private String location;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String organizerMail;
  private String organizerName;

  public static ReservationBuilder newBuilder() {
    return new ReservationBuilder();
  }

  public ReservationBuilder description(String description) {
    this.description = description;
    return this;
  }

  public ReservationBuilder reservationUUID(UUID reservationUUID) {
    this.reservationUUID = reservationUUID;
    return this;
  }

  public ReservationBuilder reservationAbstract(String reservationAbstract) {
    this.reservationAbstract = reservationAbstract;
    return this;
  }

  public ReservationBuilder location(String location) {
    this.location = location;
    return this;
  }

  public ReservationBuilder startDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
    return this;
  }

  public ReservationBuilder endDateTime(LocalDateTime endDateTime) {
    this.endDateTime = endDateTime;
    return this;
  }

  public ReservationBuilder organizerMail(String organizerMail) {
    this.organizerMail = organizerMail;
    return this;
  }

  public ReservationBuilder organizerName(String organizerName) {
    this.organizerName = organizerName;
    return this;
  }

  public Reservation build() {
    return new Reservation(
        description,
        reservationUUID,
        reservationAbstract,
        location,
        startDateTime,
        endDateTime,
        organizerMail,
        organizerName);
  }
}
