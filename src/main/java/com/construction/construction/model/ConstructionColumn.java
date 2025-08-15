package com.construction.construction.model;

import lombok.Data;

@Data
public class ConstructionColumn {
    private Long id;
    private String line;
    private String label;
    private Double km;
    private Double lat;
    private Double lng;
    private Double utmEasting;
    private Double utmNorthing;
    private Boolean installed;
    private String zone;
}
