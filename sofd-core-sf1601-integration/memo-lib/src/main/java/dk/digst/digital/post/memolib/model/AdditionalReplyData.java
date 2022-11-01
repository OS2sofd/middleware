package dk.digst.digital.post.memolib.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdditionalReplyData implements MeMoClass {

    private String label;
    private String value;
}
