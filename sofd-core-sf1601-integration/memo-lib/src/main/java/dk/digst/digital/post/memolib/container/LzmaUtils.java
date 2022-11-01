package dk.digst.digital.post.memolib.container;

import dk.digst.digital.post.memolib.container.Constants.LzmaDictionarySize;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAInputStream;

@UtilityClass
class LzmaUtils {

  private static final int SUPPORTED_COMPRESSION_LEVELS_MIN = 1;

  private static final int SUPPORTED_COMPRESSION_LEVELS_MAX = 9;

  private static final Map<Integer, Integer> MEMORY_USAGES;

  static {
    Map<Integer, Integer> memoryUsages = new HashMap<>();

    Arrays.stream(Constants.LzmaDictionarySize.values())
        .map(LzmaDictionarySize::getSizeInBytes)
        .forEach(
            dictionarySize -> {
              for (int compressionLevel = SUPPORTED_COMPRESSION_LEVELS_MIN;
                  compressionLevel <= SUPPORTED_COMPRESSION_LEVELS_MAX;
                  compressionLevel++) {
                try {
                  LZMA2Options options = new LZMA2Options(compressionLevel);
                  options.setDictSize(dictionarySize);
                  int lc = options.getLc();
                  int lp = options.getLp();
                  int dictSize = options.getDictSize();
                  int memoryUsage = LZMAInputStream.getMemoryUsage(dictSize, lc, lp);

                  memoryUsages.compute(
                      dictSize,
                      (s, maxMemory) -> Math.max(maxMemory == null ? 0 : maxMemory, memoryUsage));
                } catch (Exception e) {
                  /*
                   * Ignore
                   */
                }
              }
            });

    MEMORY_USAGES = memoryUsages;
  }

  @Getter
  @AllArgsConstructor
  public static class LzmaStatistics {

    private final byte propsByte;

    private final int dictSize;

    private final long uncompSize;

    private final int memoryNeeded;
  }

  public static LzmaStatistics getStatistics(InputStream inputStream) throws IOException {
    DataInputStream inData = new DataInputStream(inputStream);

    /*
     * Properties byte (lc, lp, and pb)
     */
    byte propsByte = inData.readByte();

    /*
     * Dictionary size is an unsigned 32-bit little endian integer.
     */
    int dictSize = 0;
    for (int i = 0; i < 4; ++i) {
      dictSize |= inData.readUnsignedByte() << (8 * i);
    }

    /*
     * Uncompressed size is an unsigned 64-bit little endian integer.
     * The maximum 64-bit value is a special case (becomes -1 here)
     * which indicates that the end marker is used instead of knowing
     * the uncompressed size beforehand.
     */
    long uncompSize = 0;
    for (int i = 0; i < 8; ++i) {
      uncompSize |= (long) inData.readUnsignedByte() << (8 * i);
    }

    inputStream.reset();

    /*
     * Check the memory usage limit.
     */
    int memoryNeeded = LZMAInputStream.getMemoryUsage(dictSize, propsByte);

    return new LzmaStatistics(propsByte, dictSize, uncompSize, memoryNeeded);
  }

  public static int getMaxMemoryInKb(int dictSize) {
    return MEMORY_USAGES.get(dictSize);
  }
}
