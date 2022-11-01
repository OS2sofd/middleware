package dk.digst.digital.post.memolib.builder;

import dk.digst.digital.post.memolib.model.Action;
import dk.digst.digital.post.memolib.model.EntryPoint;
import dk.digst.digital.post.memolib.model.Reservation;
import java.time.LocalDateTime;

public class ActionBuilder {

  private String label;
  private String actionCode;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private Reservation reservation;
  private EntryPoint entryPoint;

  public static ActionBuilder newBuilder() {
    return new ActionBuilder();
  }

  public ActionBuilder label(String label) {
    this.label = label;
    return this;
  }

  public ActionBuilder actionCode(String actionCode) {
    this.actionCode = actionCode;
    return this;
  }

  public ActionBuilder startDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
    return this;
  }

  public ActionBuilder endDateTime(LocalDateTime endDateTime) {
    this.endDateTime = endDateTime;
    return this;
  }

  public ActionBuilder reservation(Reservation reservation) {
    this.reservation = reservation;
    return this;
  }

  public ActionBuilder entryPoint(EntryPoint entryPoint) {
    this.entryPoint = entryPoint;
    return this;
  }

  public Action build() {
    return new Action(label, actionCode, startDateTime, endDateTime, reservation, entryPoint);
  }
}
