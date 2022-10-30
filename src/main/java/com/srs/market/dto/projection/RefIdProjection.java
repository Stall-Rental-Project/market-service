package com.srs.market.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @author duynt on 10/14/21
 */
@Getter
@Setter
@AllArgsConstructor
public class RefIdProjection {
    private UUID originalId;
    private UUID draftId;
}
