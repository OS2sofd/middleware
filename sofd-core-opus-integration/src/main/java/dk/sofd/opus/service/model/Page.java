package dk.sofd.opus.service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Page
{
    private long size;
    private long totalElements;
    private long totalPages;
    private long number;
}
