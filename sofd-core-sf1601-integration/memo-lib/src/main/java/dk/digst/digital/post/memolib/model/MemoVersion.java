package dk.digst.digital.post.memolib.model;

import java.math.BigDecimal;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MemoVersion {

    public final BigDecimal MEMO_VERSION = BigDecimal.valueOf(1.1);

    public final String MEMO_SCH_VERSION = "1.1.0";
}
