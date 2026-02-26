package ru.practicum.stat.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stat.dto.ParamDto;
import ru.practicum.stat.dto.ParamHitDto;
import ru.practicum.stat.dto.StatDto;

import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Component
@Slf4j
public class StatsClient {
    final RestTemplate template;
    final String statUrl;

    public StatsClient(RestTemplate template, @Value("${stats-server.url}") String statUrl) {
        this.template = template;
        this.statUrl = statUrl;
    }

    public void hit(ParamHitDto paramHitDto) {
        try {
            HttpEntity<ParamHitDto> requestEntity = new HttpEntity<>(paramHitDto);
            template.exchange(statUrl + "/hit", POST, requestEntity, Object.class);
        } catch (RuntimeException e) {
            log.warn("Не удалось сохранить хит: {}", e.getMessage());
        }
    }


    public List<StatDto> get(ParamDto paramDto) {
        String url = UriComponentsBuilder
                .fromUriString(statUrl)
                .path("/stats")
                .queryParam("start", paramDto.getStart())
                .queryParam("end", paramDto.getEnd())
                .queryParam("uris", (Object) paramDto.getUris())
                .queryParam("unique", paramDto.getUnique())
                .encode()
                .toUriString();
        try {
            ResponseEntity<List<StatDto>> response = template.exchange(
                    url,
                    GET,
                    null,
                    new ParameterizedTypeReference<List<StatDto>>() {
                    }
            );
            return response.getBody();
        } catch (RuntimeException e) {
            log.warn("Ошибка при получении статистики: {}.", e.getMessage());
            return List.of(StatDto.builder().hits(-1).build());
        }
    }
}
