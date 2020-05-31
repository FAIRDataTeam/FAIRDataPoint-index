package solutions.fairdata.fdp.index.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPageImpl<T> {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomPageable {
        private int page;
        private int size;
        private CustomSort sort;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomSort {
        private boolean sorted;
        private boolean unsorted;
        private boolean empty;
    }


    private int totalPages;
    private long totalElements;
    private boolean first;
    private CustomSort sort;
    private CustomPageable pageable;
    private int number;
    private int numberOfElements;
    private boolean last;
    private int size;
    private List<T> content;
    private boolean empty;

}
