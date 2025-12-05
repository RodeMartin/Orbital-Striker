package com.orbitalstriker.backend.modules.game.dto.football;

import lombok.Data;
import java.util.List;

@Data
public class ApiPlayersResponse {
    private List<ApiPlayerWrapper> response;
}
