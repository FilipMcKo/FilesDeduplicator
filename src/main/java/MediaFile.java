import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MediaFile {
    long lastModified;
    long picSize;
    String picFileName;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MediaFile that = (MediaFile) o;
        return lastModified == that.lastModified && picSize == that.picSize && picFileName.equals(that.picFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastModified, picSize, picFileName);
    }
}
