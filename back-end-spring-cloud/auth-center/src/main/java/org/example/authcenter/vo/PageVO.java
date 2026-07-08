package org.example.authcenter.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Lightweight page response used by admin APIs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {

    private List<T> list;
    private Long total;
    private Long page;
    private Long pageSize;
}
