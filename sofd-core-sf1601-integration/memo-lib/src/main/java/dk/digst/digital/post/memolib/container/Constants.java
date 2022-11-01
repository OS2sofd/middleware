package dk.digst.digital.post.memolib.container;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.tukaani.xz.LZMA2Options;

@UtilityClass
public class Constants {

  @Getter
  public enum LzmaDictionarySize {
    _4_MB(4),
    _8_MB(8),
    _16_MB(16),
    _32_MB(32),
    _64_MB(64),
    _128_MB(128);

    private final int sizeInBytes;

    private final LZMA2Options lzmaOptions;

    private LzmaDictionarySize(int size) {
      this.sizeInBytes = size * 1024 * 1024;
      this.lzmaOptions = createOptions();
    }

    private LZMA2Options createOptions() {
      try {
        LZMA2Options newLzmaOptions = new LZMA2Options(LZMA_COMPRESSION_LEVEL);
        newLzmaOptions.setDictSize(sizeInBytes);

        return newLzmaOptions;
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
  }

  public static final int BUFFER_SIZE_IN_BYTES = 1024 * 1024;

  public static final LzmaDictionarySize LZMA_DICTIONARY_DEFAULT_SIZE = LzmaDictionarySize._16_MB;

  public static final LzmaDictionarySize LZMA_DICTIONARY_MAX_SIZE = LzmaDictionarySize._128_MB;

  public static final int LZMA_COMPRESSION_LEVEL = 3;

  public static final int TAR_BLOCK_SIZE = TarConstants.DEFAULT_BLKSIZE;

  public static final int TAR_RECORD_SIZE = TarConstants.DEFAULT_RCDSIZE;

  public static final Charset TAR_ENCODING = StandardCharsets.UTF_8;

  public static final boolean TAR_LENIENT = false;

  public static final boolean TAR_ADD_PAX_HEADERS_FOR_NON_ASCII_NAMES = true;

  public static final int TAR_LONG_FILE_MODE = TarArchiveOutputStream.LONGFILE_POSIX;
}
