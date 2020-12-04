package model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SliceDetail {
    private String title;
    private String thumb;
    private String url;
    private String id;
    private Boolean isspoiler;
    private String author;
    private String support;
    private String dev;
    private String game;
}
