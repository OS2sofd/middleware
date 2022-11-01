package dk.digst.digital.post.memolib.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Telephone implements MeMoClass {

  private Integer telephoneNumber;
  private String relatedAgent;
}
