package dk.digst.digital.post.memolib.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdditionalContentData implements MeMoClass {

  private String contentDataType;
  private String contentDataName;
  private String contentDataValue;
}
