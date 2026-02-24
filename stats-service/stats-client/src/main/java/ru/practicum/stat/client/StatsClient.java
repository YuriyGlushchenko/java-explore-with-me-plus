package ru.practicum.stat.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stat.dto.ParamDto;
import ru.practicum.stat.dto.ParamHitDto;
import ru.practicum.stat.dto.StatDto;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Component
public class StatsClient {
    final RestTemplate template;
    final String statUrl;

    public StatsClient(RestTemplate template, @Value("${stats-server.url}") String statUrl) {
        this.template = template;
        this.statUrl = statUrl;
    }

    public void hit(ParamHitDto paramHitDto) {
        HttpEntity<ParamHitDto> requestEntity = new HttpEntity<>(paramHitDto);
        template.exchange(statUrl + "/hit", POST, requestEntity, Object.class);
    }


    public List<StatDto> get(ParamDto paramDto) {
        Map<String, Object> parameters = Map.of(
                "start", paramDto.getStart(),
                "end", paramDto.getEnd(),
                "uris", paramDto.getUris(),
                "unique", paramDto.getUnique()
        );
        String url = statUrl + "/stats?start={start}&end={end}&uris={uris}&unique={unique}";
        ResponseEntity<StatDto[]> response = template.exchange(url, GET, null, StatDto[].class, parameters);
        StatDto[] stats = response.getBody();
        return (stats != null) ? List.of(stats) : List.of();
    }

}
